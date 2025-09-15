package com.webstyle.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${app.upload.dir:uploads/products}")
    private String uploadDir;

    public String salvarArquivo(MultipartFile file) throws IOException {
        // Validações básicas
        if (file.isEmpty()) {
            throw new IOException("Arquivo está vazio");
        }

        // Validar tipo de arquivo (apenas imagens)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Arquivo deve ser uma imagem");
        }

        // Criar diretório se não existir
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Gerar nome único para o arquivo
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        
        // Salvar arquivo
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Retornar o caminho relativo
        return uploadDir + "/" + uniqueFileName;
    }

    public void deletarArquivo(String caminhoArquivo) {
        try {
            Path filePath = Paths.get(caminhoArquivo);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log do erro, mas não quebra o fluxo
            System.err.println("Erro ao deletar arquivo: " + e.getMessage());
        }
    }

    public boolean arquivoExiste(String caminhoArquivo) {
        return Files.exists(Paths.get(caminhoArquivo));
    }
}