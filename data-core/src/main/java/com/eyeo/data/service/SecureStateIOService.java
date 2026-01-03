package com.eyeo.data.service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

/**
 * Secure State I/O Service - Processamento AES-256-GCM
 * 
 * Implementa o modelo "Shared-Nothing" com processamento em streaming
 */
@Service
public class SecureStateIOService {
    
    private static final Logger logger = LoggerFactory.getLogger(SecureStateIOService.class);
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256; // bits
    private static final int IV_SIZE = 12;   // bytes (96 bits)
    private static final int TAG_SIZE = 128; // bits
    private static final int CHUNK_SIZE = 64 * 1024; // 64KB
    
    @Value("${storage.path:/protected-storage}")
    private String storagePath;
    
    private SecretKey masterKey;
    private SecureRandom secureRandom;
    
    @PostConstruct
    public void init() {
        // Adiciona Bouncy Castle como provider
        Security.addProvider(new BouncyCastleProvider());
        
        secureRandom = new SecureRandom();
        
        // Em produção, carregar chave de KMS/Vault
        masterKey = loadOrGenerateMasterKey();
        
        // Cria diretório de storage se não existir
        File storageDir = new File(storagePath);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        
        logger.info("✓ SecureStateIO inicializado");
        logger.info("  - Algoritmo: {}", ALGORITHM);
        logger.info("  - Key Size: {} bits", KEY_SIZE);
        logger.info("  - Storage Path: {}", storagePath);
    }
    
    /**
     * Criptografa stream de entrada e grava no storage
     * 
     * @param inputStream Stream de entrada (vídeo bruto)
     * @param storageKey Chave única para identificar o blob
     * @return Informações do arquivo criptografado
     */
    public EncryptionResult encryptStream(InputStream inputStream, String storageKey) 
            throws Exception {
        
        logger.info("🔐 Iniciando criptografia de stream: {}", storageKey);
        
        File outputFile = new File(storagePath, storageKey + ".enc");
        long totalBytesProcessed = 0;
        int chunkCount = 0; // Moved outside try block
        
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            
            // Buffer para leitura
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // Gera IV único para cada chunk (evita padrões)
                byte[] iv = new byte[IV_SIZE];
                secureRandom.nextBytes(iv);
                
                // Criptografa chunk
                byte[] encryptedChunk = encryptChunk(buffer, 0, bytesRead, iv);
                
                // Escreve: [IV_SIZE][IV][ENCRYPTED_DATA]
                bos.write(IV_SIZE); // Tamanho do IV (sempre 12)
                bos.write(iv);      // IV único
                bos.write(encryptedChunk); // Dados criptografados + GCM tag
                
                totalBytesProcessed += bytesRead;
                chunkCount++;
                
                if (chunkCount % 100 == 0) {
                    logger.debug("  Processados {} chunks ({} MB)", 
                               chunkCount, totalBytesProcessed / (1024 * 1024));
                }
            }
            
            bos.flush();
        }
        
        logger.info("✓ Stream criptografado: {} chunks, {} MB, arquivo: {}", 
                   chunkCount, totalBytesProcessed / (1024 * 1024), outputFile.getName());
        
        return new EncryptionResult(
            storageKey,
            outputFile.getAbsolutePath(),
            outputFile.length(),
            totalBytesProcessed
        );
    }
    
    /**
     * Descriptografa stream do storage
     * 
     * @param storageKey Chave do blob
     * @param outputStream Stream de saída para escrever dados descriptografados
     */
    public long decryptStream(String storageKey, OutputStream outputStream) 
            throws Exception {
        
        logger.info("🔓 Iniciando descriptografia de stream: {}", storageKey);
        
        File inputFile = new File(storagePath, storageKey + ".enc");
        
        if (!inputFile.exists()) {
            throw new FileNotFoundException("Blob não encontrado: " + storageKey);
        }
        
        long totalBytesDecrypted = 0;
        
        try (FileInputStream fis = new FileInputStream(inputFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             BufferedOutputStream bos = new BufferedOutputStream(outputStream)) {
            
            while (bis.available() > 0) {
                // Lê tamanho do IV
                int ivSize = bis.read();
                if (ivSize == -1) break; // EOF
                
                // Lê IV
                byte[] iv = new byte[ivSize];
                bis.read(iv);
                
                // Lê chunk criptografado (tamanho fixo + tag)
                byte[] encryptedChunk = new byte[CHUNK_SIZE + (TAG_SIZE / 8)];
                int bytesRead = bis.read(encryptedChunk);
                
                // Descriptografa
                byte[] decryptedChunk = decryptChunk(encryptedChunk, 0, bytesRead, iv);
                
                // Escreve dados descriptografados
                bos.write(decryptedChunk);
                totalBytesDecrypted += decryptedChunk.length;
            }
            
            bos.flush();
        }
        
        logger.info("✓ Stream descriptografado: {} MB", totalBytesDecrypted / (1024 * 1024));
        return totalBytesDecrypted;
    }
    
    /**
     * Criptografa um chunk de dados
     */
    private byte[] encryptChunk(byte[] data, int offset, int length, byte[] iv) 
            throws Exception {
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, gcmSpec);
        
        return cipher.doFinal(data, offset, length);
    }
    
    /**
     * Descriptografa um chunk de dados
     */
    private byte[] decryptChunk(byte[] encryptedData, int offset, int length, byte[] iv) 
            throws Exception {
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.DECRYPT_MODE, masterKey, gcmSpec);
        
        return cipher.doFinal(encryptedData, offset, length);
    }
    
    /**
     * Carrega ou gera chave mestra (em produção: usar KMS/Vault)
     */
    private SecretKey loadOrGenerateMasterKey() {
        // PRODUÇÃO: Carregar de AWS KMS, Azure Key Vault, HashiCorp Vault
        // File keyFile = new File(storagePath, ".masterkey");
        
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_SIZE, secureRandom);
            
            logger.warn("⚠️ Gerando chave mestra TEMPORÁRIA (usar KMS em produção!)");
            return keyGen.generateKey();
            
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar chave mestra", e);
        }
    }
    
    /**
     * Verifica se blob existe no storage
     */
    public boolean blobExists(String storageKey) {
        File file = new File(storagePath, storageKey + ".enc");
        return file.exists();
    }
    
    /**
     * Retorna tamanho do blob criptografado
     */
    public long getBlobSize(String storageKey) {
        File file = new File(storagePath, storageKey + ".enc");
        return file.exists() ? file.length() : -1;
    }
    
    /**
     * Inner class para resultado de criptografia
     */
    public static class EncryptionResult {
        public final String storageKey;
        public final String filePath;
        public final long encryptedSize;
        public final long originalSize;
        
        public EncryptionResult(String storageKey, String filePath, 
                              long encryptedSize, long originalSize) {
            this.storageKey = storageKey;
            this.filePath = filePath;
            this.encryptedSize = encryptedSize;
            this.originalSize = originalSize;
        }
    }
}
