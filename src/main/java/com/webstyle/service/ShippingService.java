package com.webstyle.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para cálculo de frete com integração ViaCEP
 * Calcula distância aproximada de São Paulo (Centro de Distribuição)
 */
@Service
public class ShippingService {
    
    // Coordenadas do centro de São Paulo (Centro de Distribuição)
    private static final double SP_LATITUDE = -23.5505;
    private static final double SP_LONGITUDE = -46.6333;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public ShippingService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Consulta CEP na API ViaCEP
     */
    public CepInfo consultarCep(String cep) throws IOException {
        // Remove formatação do CEP
        String cepLimpo = cep.replaceAll("[^0-9]", "");
        
        if (cepLimpo.length() != 8) {
            throw new IllegalArgumentException("CEP inválido. Deve conter 8 dígitos.");
        }
        
        try {
            String url = "https://viacep.com.br/ws/" + cepLimpo + "/json/";
            String response = restTemplate.getForObject(url, String.class);
            
            JsonNode jsonNode = objectMapper.readTree(response);
            
            // Verifica se o CEP existe
            if (jsonNode.has("erro") && jsonNode.get("erro").asBoolean()) {
                throw new IllegalArgumentException("CEP não encontrado.");
            }
            
            CepInfo cepInfo = new CepInfo();
            cepInfo.setCep(jsonNode.get("cep").asText());
            cepInfo.setLogradouro(jsonNode.get("logradouro").asText());
            cepInfo.setBairro(jsonNode.get("bairro").asText());
            cepInfo.setLocalidade(jsonNode.get("localidade").asText());
            cepInfo.setUf(jsonNode.get("uf").asText());
            cepInfo.setIbge(jsonNode.has("ibge") ? jsonNode.get("ibge").asText() : "");
            
            return cepInfo;
            
        } catch (Exception e) {
            throw new IOException("Erro ao consultar CEP: " + e.getMessage());
        }
    }
    
    /**
     * Calcula opções de frete baseado no CEP de destino e valor do carrinho
     */
    public List<ShippingOption> calcularFrete(String cep, BigDecimal valorCarrinho) throws IOException {
        CepInfo cepInfo = consultarCep(cep);
        
        // Calcula distância aproximada baseada na localização
        double distanciaKm = calcularDistanciaAproximada(cepInfo);
        
        List<ShippingOption> opcoes = new ArrayList<>();
        
        // OPÇÃO 1: PAC (Econômico)
        ShippingOption pac = new ShippingOption();
        pac.setNome("PAC - Correios");
        pac.setDescricao("Entrega econômica");
        pac.setTipo("PAC");
        pac.setValor(calcularValorPac(distanciaKm, valorCarrinho));
        pac.setPrazoMinimo(calcularPrazoPac(distanciaKm));
        pac.setPrazoMaximo(pac.getPrazoMinimo() + 2);
        opcoes.add(pac);
        
        // OPÇÃO 2: SEDEX (Rápido)
        ShippingOption sedex = new ShippingOption();
        sedex.setNome("SEDEX - Correios");
        sedex.setDescricao("Entrega rápida");
        sedex.setTipo("SEDEX");
        sedex.setValor(calcularValorSedex(distanciaKm, valorCarrinho));
        sedex.setPrazoMinimo(calcularPrazoSedex(distanciaKm));
        sedex.setPrazoMaximo(sedex.getPrazoMinimo() + 1);
        opcoes.add(sedex);
        
        // OPÇÃO 3: Transportadora (para longas distâncias) ou Entrega Expressa (curtas)
        if (distanciaKm > 300) {
            // Transportadora para longas distâncias
            ShippingOption transportadora = new ShippingOption();
            transportadora.setNome("Transportadora");
            transportadora.setDescricao("Entrega com transportadora parceira");
            transportadora.setTipo("TRANSPORTADORA");
            transportadora.setValor(calcularValorTransportadora(distanciaKm, valorCarrinho));
            transportadora.setPrazoMinimo(calcularPrazoTransportadora(distanciaKm));
            transportadora.setPrazoMaximo(transportadora.getPrazoMinimo() + 3);
            opcoes.add(transportadora);
        } else {
            // Entrega Expressa para curtas distâncias
            ShippingOption expressa = new ShippingOption();
            expressa.setNome("Entrega Expressa");
            expressa.setDescricao("Entrega no mesmo dia ou em até 24h");
            expressa.setTipo("EXPRESSA");
            expressa.setValor(calcularValorExpressa(distanciaKm, valorCarrinho));
            expressa.setPrazoMinimo(distanciaKm < 50 ? 0 : 1);
            expressa.setPrazoMaximo(1);
            opcoes.add(expressa);
        }
        
        // Verifica frete grátis (acima de R$ 300)
        if (valorCarrinho.compareTo(new BigDecimal("300.00")) >= 0) {
            // Frete grátis no PAC
            pac.setValor(BigDecimal.ZERO);
            pac.setDescricao("Entrega econômica - FRETE GRÁTIS!");
            pac.setFreteGratis(true);
        }
        
        return opcoes;
    }
    
    /**
     * Calcula distância aproximada baseada na UF e município
     */
    private double calcularDistanciaAproximada(CepInfo cepInfo) {
        String uf = cepInfo.getUf();
        String cidade = cepInfo.getLocalidade().toUpperCase();
        
        // Distâncias aproximadas de São Paulo capital
        // Mesma cidade de São Paulo
        if (uf.equals("SP") && cidade.contains("SÃO PAULO")) {
            return 15.0; // Dentro da cidade
        }
        
        // Estado de São Paulo
        if (uf.equals("SP")) {
            // Cidades próximas da Grande SP
            if (cidade.contains("GUARULHOS") || cidade.contains("OSASCO") || 
                cidade.contains("SANTO ANDRÉ") || cidade.contains("SÃO BERNARDO") ||
                cidade.contains("DIADEMA") || cidade.contains("MAUÁ") || 
                cidade.contains("CARAPICUÍBA") || cidade.contains("BARUERI")) {
                return 25.0;
            }
            // Interior próximo (até 100km)
            if (cidade.contains("CAMPINAS") || cidade.contains("JUNDIAÍ") || 
                cidade.contains("SOROCABA") || cidade.contains("SÃO JOSÉ DOS CAMPOS")) {
                return 80.0;
            }
            // Interior médio (100-300km)
            if (cidade.contains("RIBEIRÃO PRETO") || cidade.contains("SANTOS") ||
                cidade.contains("SÃO JOSÉ DO RIO PRETO") || cidade.contains("PIRACICABA")) {
                return 250.0;
            }
            // Interior longe
            return 450.0;
        }
        
        // Estados vizinhos
        switch (uf) {
            case "RJ": return 450.0;  // Rio de Janeiro
            case "MG": return 600.0;  // Minas Gerais
            case "PR": return 400.0;  // Paraná
            case "SC": return 700.0;  // Santa Catarina
            case "RS": return 1100.0; // Rio Grande do Sul
            case "ES": return 900.0;  // Espírito Santo
            case "MS": return 1000.0; // Mato Grosso do Sul
            case "MT": return 1700.0; // Mato Grosso
            case "GO": return 900.0;  // Goiás
            case "DF": return 1000.0; // Distrito Federal
            case "BA": return 1900.0; // Bahia
            case "SE": return 2100.0; // Sergipe
            case "AL": return 2300.0; // Alagoas
            case "PE": return 2700.0; // Pernambuco
            case "PB": return 2900.0; // Paraíba
            case "RN": return 3000.0; // Rio Grande do Norte
            case "CE": return 3100.0; // Ceará
            case "PI": return 2800.0; // Piauí
            case "MA": return 3000.0; // Maranhão
            case "TO": return 1700.0; // Tocantins
            case "PA": return 2800.0; // Pará
            case "AP": return 3400.0; // Amapá
            case "RR": return 4000.0; // Roraima
            case "AM": return 3800.0; // Amazonas
            case "AC": return 3500.0; // Acre
            case "RO": return 2900.0; // Rondônia
            default: return 1500.0;   // Padrão
        }
    }
    
    /**
     * Calcula valor do PAC
     */
    private BigDecimal calcularValorPac(double distanciaKm, BigDecimal valorCarrinho) {
        // Fórmula: Base de 12 reais + 0.02 por km + 1% do valor
        BigDecimal valorBase = new BigDecimal("12.00");
        BigDecimal valorDistancia = BigDecimal.valueOf(distanciaKm * 0.02);
        BigDecimal valorPercentual = valorCarrinho.multiply(new BigDecimal("0.01"));
        
        BigDecimal total = valorBase.add(valorDistancia).add(valorPercentual);
        
        // Valor mínimo de 8 reais e máximo de 80 reais
        if (total.compareTo(new BigDecimal("8.00")) < 0) {
            total = new BigDecimal("8.00");
        }
        if (total.compareTo(new BigDecimal("80.00")) > 0) {
            total = new BigDecimal("80.00");
        }
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calcula prazo do PAC (em dias úteis)
     */
    private int calcularPrazoPac(double distanciaKm) {
        if (distanciaKm < 50) return 2;
        if (distanciaKm < 200) return 3;
        if (distanciaKm < 500) return 5;
        if (distanciaKm < 1000) return 7;
        if (distanciaKm < 2000) return 10;
        return 15;
    }
    
    /**
     * Calcula valor do SEDEX
     */
    private BigDecimal calcularValorSedex(double distanciaKm, BigDecimal valorCarrinho) {
        // SEDEX é aproximadamente 1.8x mais caro que PAC
        BigDecimal valorPac = calcularValorPac(distanciaKm, valorCarrinho);
        BigDecimal total = valorPac.multiply(new BigDecimal("1.8"));
        
        // Valor mínimo de 15 reais e máximo de 150 reais
        if (total.compareTo(new BigDecimal("15.00")) < 0) {
            total = new BigDecimal("15.00");
        }
        if (total.compareTo(new BigDecimal("150.00")) > 0) {
            total = new BigDecimal("150.00");
        }
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calcula prazo do SEDEX (em dias úteis)
     */
    private int calcularPrazoSedex(double distanciaKm) {
        if (distanciaKm < 50) return 1;
        if (distanciaKm < 200) return 1;
        if (distanciaKm < 500) return 2;
        if (distanciaKm < 1000) return 3;
        if (distanciaKm < 2000) return 5;
        return 7;
    }
    
    /**
     * Calcula valor da Transportadora
     */
    private BigDecimal calcularValorTransportadora(double distanciaKm, BigDecimal valorCarrinho) {
        // Transportadora: mais barato que SEDEX mas mais caro que PAC para longas distâncias
        BigDecimal valorBase = new BigDecimal("25.00");
        BigDecimal valorDistancia = BigDecimal.valueOf(distanciaKm * 0.015);
        BigDecimal valorPercentual = valorCarrinho.multiply(new BigDecimal("0.008"));
        
        BigDecimal total = valorBase.add(valorDistancia).add(valorPercentual);
        
        // Valor mínimo de 20 reais e máximo de 100 reais
        if (total.compareTo(new BigDecimal("20.00")) < 0) {
            total = new BigDecimal("20.00");
        }
        if (total.compareTo(new BigDecimal("100.00")) > 0) {
            total = new BigDecimal("100.00");
        }
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calcula prazo da Transportadora (em dias úteis)
     */
    private int calcularPrazoTransportadora(double distanciaKm) {
        if (distanciaKm < 500) return 4;
        if (distanciaKm < 1000) return 6;
        if (distanciaKm < 2000) return 8;
        return 12;
    }
    
    /**
     * Calcula valor da Entrega Expressa
     */
    private BigDecimal calcularValorExpressa(double distanciaKm, BigDecimal valorCarrinho) {
        // Expressa: mais cara, para entregas rápidas em curtas distâncias
        BigDecimal valorBase = new BigDecimal("35.00");
        BigDecimal valorDistancia = BigDecimal.valueOf(distanciaKm * 0.5);
        
        BigDecimal total = valorBase.add(valorDistancia);
        
        // Valor mínimo de 30 reais e máximo de 120 reais
        if (total.compareTo(new BigDecimal("30.00")) < 0) {
            total = new BigDecimal("30.00");
        }
        if (total.compareTo(new BigDecimal("120.00")) > 0) {
            total = new BigDecimal("120.00");
        }
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Classe interna para informações do CEP
     */
    public static class CepInfo {
        private String cep;
        private String logradouro;
        private String bairro;
        private String localidade;
        private String uf;
        private String ibge;
        
        // Getters e Setters
        public String getCep() { return cep; }
        public void setCep(String cep) { this.cep = cep; }
        
        public String getLogradouro() { return logradouro; }
        public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
        
        public String getBairro() { return bairro; }
        public void setBairro(String bairro) { this.bairro = bairro; }
        
        public String getLocalidade() { return localidade; }
        public void setLocalidade(String localidade) { this.localidade = localidade; }
        
        public String getUf() { return uf; }
        public void setUf(String uf) { this.uf = uf; }
        
        public String getIbge() { return ibge; }
        public void setIbge(String ibge) { this.ibge = ibge; }
    }
    
    /**
     * Classe interna para opções de frete
     */
    public static class ShippingOption {
        private String nome;
        private String descricao;
        private String tipo;
        private BigDecimal valor;
        private int prazoMinimo;
        private int prazoMaximo;
        private boolean freteGratis;
        
        // Getters e Setters
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        
        public BigDecimal getValor() { return valor; }
        public void setValor(BigDecimal valor) { this.valor = valor; }
        
        public int getPrazoMinimo() { return prazoMinimo; }
        public void setPrazoMinimo(int prazoMinimo) { this.prazoMinimo = prazoMinimo; }
        
        public int getPrazoMaximo() { return prazoMaximo; }
        public void setPrazoMaximo(int prazoMaximo) { this.prazoMaximo = prazoMaximo; }
        
        public boolean isFreteGratis() { return freteGratis; }
        public void setFreteGratis(boolean freteGratis) { this.freteGratis = freteGratis; }
        
        public String getPrazoFormatado() {
            if (prazoMinimo == 0) {
                return "Hoje mesmo";
            } else if (prazoMinimo == prazoMaximo) {
                return prazoMinimo + " dia" + (prazoMinimo > 1 ? "s" : "") + " útil" + (prazoMinimo > 1 ? "is" : "");
            } else {
                return prazoMinimo + " a " + prazoMaximo + " dias úteis";
            }
        }
    }
}