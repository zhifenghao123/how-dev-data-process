package com.howdev.util.encrypt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * SymmetryEncryptor class
 *
 * @author haozhifeng
 * @date 2023/12/09
 */
public class SymmetryEncryptor {
    private static final int BUFFER_SIZE = 4096;

    public static String encrypt(String algorithm, String key, String value) {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), algorithm);
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
        byte[] encryptedValue;
        try {
            encryptedValue = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
        return Base64.getEncoder().encodeToString(encryptedValue);
    }

    public static String decrypt(String algorithm, String key,String encryptedValue) {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), algorithm);
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
        byte[] decodedValue = Base64.getDecoder().decode(encryptedValue);
        byte[] decryptedValue;
        try {
            decryptedValue = cipher.doFinal(decodedValue);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
        return new String(decryptedValue, StandardCharsets.UTF_8);
    }

    public static boolean encryptFile(String algorithm, String key, String inputFile, String outputFile) {
        InputStream inputStream;
        OutputStream outputStream;
        try {
            inputStream = new FileInputStream(inputFile);
            outputStream = new FileOutputStream(outputFile);
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
            return false;
        }
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return false;
        }
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), algorithm);
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while (true) {
            try {
                if (!((bytesRead = inputStream.read(buffer)) != -1)) {
                    break;
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
                return false;
            }
            byte[] outputBytes = cipher.update(buffer, 0, bytesRead);
            if (outputBytes != null) {
                try {
                    outputStream.write(outputBytes);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    return false;
                }
            }
        }
        byte[] outputBytes = new byte[0];
        try {
            outputBytes = cipher.doFinal();
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return false;
        }
        if (outputBytes != null) {
            try {
                outputStream.write(outputBytes);
            } catch (IOException ioException) {
                ioException.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static boolean decryptFile(String algorithm, String key, String inputFile, String outputFile) {
        InputStream inputStream;
        OutputStream outputStream;
        try {
            inputStream = new FileInputStream(inputFile);
            outputStream = new FileOutputStream(outputFile);
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
            return false;
        }
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return false;
        }
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), algorithm);
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while (true) {
            try {
                if (!((bytesRead = inputStream.read(buffer)) != -1)) {
                    break;
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
                return false;
            }
            byte[] outputBytes = cipher.update(buffer, 0, bytesRead);
            if (outputBytes != null) {
                try {
                    outputStream.write(outputBytes);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    return false;
                }
            }
        }
        byte[] outputBytes;
        try {
            outputBytes = cipher.doFinal();
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return false;
        }
        if (outputBytes != null) {
            try {
                outputStream.write(outputBytes);
            } catch (IOException ioException) {
                ioException.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        String testPlainText = "呵呵，我是郝志锋。🐱";
        String key = "HelloHaoReal_!16";


        String algorithmName = EncryptAlgorithmEnum.AES.getAlgorithmName();
        String encrypt = encrypt(algorithmName, key, testPlainText);
        System.out.println("encrypt result:");
        System.out.println(encrypt);

        String decrypt = decrypt(algorithmName, key, encrypt);
        System.out.println("decrypt result:");
        System.out.println(decrypt);


        String projectPath = System.getProperty("user.dir");
        String dataFileDirectory = projectPath + "/how-dev-biz/test-data/encrypt/";

        String originalFile = dataFileDirectory + "dataFile/test_data.csv";
        String chipperTextFileToSave = dataFileDirectory + "symmetry/" + algorithmName + "/test_data-encrypt.csv";
        String plainTextFileToSave = dataFileDirectory + "symmetry/" + algorithmName + "/test_data-decrypt.csv";

        encryptFile(algorithmName, key, originalFile, chipperTextFileToSave);
        decryptFile(algorithmName, key, chipperTextFileToSave, plainTextFileToSave);

    }
}
