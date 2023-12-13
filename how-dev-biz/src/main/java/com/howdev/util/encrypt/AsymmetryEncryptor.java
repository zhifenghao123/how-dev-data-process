package com.howdev.util.encrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.io.FileUtils;

/**
 * AsymmetryEncryptor class
 *
 * @author haozhifeng
 * @date 2023/12/08
 */
public class AsymmetryEncryptor {

    private static final int BLOCK_SIZE = 117; // For JDK 7 and above, the maximum data size for RSA is the key length - 11 bytes.
    // For JDK 6 and below, the maximum data size for RSA is the key length.

    private static final int BUFFER_SIZE = 4096;

    /**
     * 生成公钥和私钥并保存到文件中
     *
     * @param algorithm   algorithm
     * @param privatePath privatePath
     * @param publicPath  publicPath
     *
     * @return:
     * @author: haozhifeng
     */
    private static boolean generateKeyPairAndSaveFile(String algorithm, String privatePath, String publicPath) {
        EncryptKeyPair encryptKeyPair;
        encryptKeyPair = generateKeyPair(algorithm);
        String privateKeyString = encryptKeyPair.getPrivateKeyString();
        String publicKeyString = encryptKeyPair.getPublicKeyString();
        //需导入commons-io
        try {
            FileUtils
                    .writeStringToFile(new File(privatePath), privateKeyString, String.valueOf(StandardCharsets.UTF_8));
            FileUtils.writeStringToFile(new File(publicPath), publicKeyString, String.valueOf(StandardCharsets.UTF_8));
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 生成公钥和私钥
     *
     * @param algorithm 算法
     */
    private static EncryptKeyPair generateKeyPair(String algorithm) {
        //返回生成指定算法的 public/private 密钥对的 KeyPairGenerator 对象
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        //生成一个密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        //私钥
        PrivateKey privateKey = keyPair.getPrivate();
        //公钥
        PublicKey publicKey = keyPair.getPublic();

        EncryptKeyPair encryptKeyPair = new EncryptKeyPair();
        encryptKeyPair.setPrivateKey(privateKey);
        encryptKeyPair.setPublicKey(publicKey);
        return encryptKeyPair;
    }

    /**
     * getPublicKey
     *
     * @param algorithm         algorithm
     * @param publicKeyFilePath publicKeyFilePath
     *
     * @return:
     * @author: haozhifeng
     */
    private static PublicKey getPublicKey(String algorithm, String publicKeyFilePath) {
        String publicEncodeString;
        try {
            publicEncodeString = FileUtils.readFileToString(new File(publicKeyFilePath),
                    String.valueOf(StandardCharsets.UTF_8));
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return null;
        }
        //返回转换指定算法的 public/private 关键字的 KeyFactory 对象。
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        //此类表示根据 ASN.1 类型 SubjectPublicKeyInfo 进行编码的公用密钥的 ASN.1 编码
        X509EncodedKeySpec x509EncodedKeySpec;
        x509EncodedKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicEncodeString));
        try {
            return keyFactory.generatePublic(x509EncodedKeySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * getPrivateKey
     *
     * @param algorithm      algorithm
     * @param privateKeyPath privateKeyPath
     *
     * @return:
     * @author: haozhifeng
     */
    private static PrivateKey getPrivateKey(String algorithm, String privateKeyPath) {
        String privateEncodeString;
        try {
            privateEncodeString = FileUtils.readFileToString(new File(privateKeyPath),
                    String.valueOf(StandardCharsets.UTF_8));
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return null;
        }
        //返回转换指定算法的 public/private 关键字的 KeyFactory 对象。
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        //创建私钥key的规则  此类表示按照 ASN.1 类型 PrivateKeyInfo 进行编码的专用密钥的 ASN.1 编码
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = null;
        pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateEncodeString));
        //私钥对象
        try {
            return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * encrypt
     *
     * @param algorithm algorithm
     * @param key       key
     * @param plainText plainText
     *
     * @return:
     * @author: haozhifeng
     */
    private static String encrypt(String algorithm, Key key, String plainText) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
        byte[] bytes;
        try {
            bytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * decrypt
     *
     * @param algorithm  algorithm
     * @param key        key
     * @param cipherText cipherText
     *
     * @return:
     * @author: haozhifeng
     */
    private static String decrypt(String algorithm, Key key, String cipherText) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
        byte[] bytes1;
        try {
            bytes1 = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
        return new String(bytes1);
    }

    public static boolean encryptFile(String algorithm, PublicKey publicKey, String inputFile, String outputFile) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return false;
        }
        InputStream inputStream;
        OutputStream outputStream;
        try {
            inputStream = new FileInputStream(inputFile);
            outputStream = new FileOutputStream(outputFile);
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
            return false;
        }

        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] outputBytes = cipher.update(buffer, 0, bytesRead);
                if (outputBytes != null) {
                    try {
                        outputStream.write(outputBytes);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] outputBytes = new byte[0];
        try {
            outputBytes = cipher.doFinal();
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        if (outputBytes != null) {
            try {
                outputStream.write(outputBytes);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        return true;
    }

    public static boolean encryptFileNew(String algorithm, PublicKey publicKey, String inputFile, String outputFile) {
        byte[] inputData;
        try {
            inputData = FileUtils.readFileToByteArray(new File(inputFile));
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return false;
        }

        Cipher cipher;
        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return false;
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }

        byte[] outputData = new byte[1024*1024*5];
        int inputLength = inputData.length;
        int blockCount = (inputLength + BLOCK_SIZE - 1) / BLOCK_SIZE;
        for (int i = 0; i < blockCount; i++) {
            int start = i * BLOCK_SIZE;
            int end = Math.min(start + BLOCK_SIZE, inputLength);
            byte[] block = new byte[end - start];
            System.arraycopy(inputData, start, block, 0, block.length);

            byte[] encryptedBlock = new byte[0];
            try {
                encryptedBlock = cipher.doFinal(block);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
                return false;
            }
            System.arraycopy(encryptedBlock, 0, outputData, i * BLOCK_SIZE, encryptedBlock.length);
        }

        try {
            FileUtils.writeByteArrayToFile(new File(outputFile), outputData);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean decryptFile(String algorithm, PrivateKey privateKey, String inputFile, String outputFile) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return false;
        }

        InputStream inputStream;
        OutputStream outputStream;
        try {
            inputStream = new FileInputStream(inputFile);
            outputStream = new FileOutputStream(outputFile);
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
            return false;
        }

        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] outputBytes = cipher.update(buffer, 0, bytesRead);
                if (outputBytes != null) {
                    try {
                        outputStream.write(outputBytes);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] outputBytes = new byte[0];
        try {
            outputBytes = cipher.doFinal();
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        if (outputBytes != null) {
            try {
                outputStream.write(outputBytes);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        return true;
    }

    public static boolean decryptFileNew(String algorithm, PrivateKey privateKey, String inputFile, String outputFile) {
        byte[] inputData;
        try {
            inputData = FileUtils.readFileToByteArray(new File(inputFile));
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return false;
        }

        Cipher cipher;
        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return false;
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }

        byte[] outputData = new byte[inputData.length];
        int inputLength = inputData.length;
        int blockCount = (inputLength + BLOCK_SIZE - 1) / BLOCK_SIZE;
        for (int i = 0; i < blockCount; i++) {
            int start = i * BLOCK_SIZE;
            int end = Math.min(start + BLOCK_SIZE, inputLength);
            byte[] block = new byte[end - start];
            System.arraycopy(inputData, start, block, 0, block.length);

            byte[] decryptedBlock;
            try {
                decryptedBlock = cipher.doFinal(block);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
                return false;
            }
            System.arraycopy(decryptedBlock, 0, outputData, i * BLOCK_SIZE, decryptedBlock.length);
        }

        try {
            FileUtils.writeByteArrayToFile(new File(outputFile), outputData);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return false;
        }
        return true;
    }

    public static void main(String[] args) {

        String projectPath = System.getProperty("user.dir");
        String rsaKeyFileDirectory = projectPath + "/how-dev-biz/test-data/encrypt/encryptKey/";
        String dataFileDirectory = projectPath + "/how-dev-biz/test-data/encrypt/dataFile/";

        String privateKeyFileName = "hPrivateKey.txt";
        String publicKeyFileName = "hPublicKey.txt";

        String privateKeyFile = rsaKeyFileDirectory + privateKeyFileName;
        String publicKeyFile = rsaKeyFileDirectory + publicKeyFileName;


        EncryptAlgorithmEnum encryptAlgorithm = EncryptAlgorithmEnum.RSA;

        String testPlainText = "呵呵，我是郝志锋。🐱";

        // testGenerateKeyPair(encryptAlgorithm);
        testGenerateKeyPairAndSaveFile(encryptAlgorithm, privateKeyFile, publicKeyFile);
        //String decryptPlainText = testEncrypt(encryptAlgorithm, publicKeyFile, testPlainText);

        //testDecrypt(encryptAlgorithm, privateKeyFile, decryptPlainText);



        String originalFile = dataFileDirectory + "test_data.csv";
        String chipperTextFileToSave = dataFileDirectory + "asymmetry/test_data-encrypt.csv";
        String plainTextFileToSave = dataFileDirectory + "asymmetry/test_data-decrypt.csv";

        //testEncryptFile(encryptAlgorithm, publicKeyFile, originalFile, chipperTextFileToSave);
        //testDecryptFile(encryptAlgorithm, privateKeyFile, chipperTextFileToSave, plainTextFileToSave);

        testEncryptFileNew(encryptAlgorithm, publicKeyFile, originalFile, chipperTextFileToSave);
        testDecryptFileNew(encryptAlgorithm, privateKeyFile, chipperTextFileToSave, plainTextFileToSave);
    }

    public static void testGenerateKeyPair(EncryptAlgorithmEnum encryptAlgorithm) {
        EncryptKeyPair encryptKeyPair = generateKeyPair(encryptAlgorithm.getAlgorithmName());
        System.out.println(encryptKeyPair.getPrivateKeyString());
        System.out.println(encryptKeyPair.getPublicKeyString());
    }

    public static void testGenerateKeyPairAndSaveFile(EncryptAlgorithmEnum encryptAlgorithm, String privateKeyFile,
                                                      String publicKeyFile) {
        generateKeyPairAndSaveFile(encryptAlgorithm.getAlgorithmName(), privateKeyFile, publicKeyFile);
    }

    public static String testEncrypt(EncryptAlgorithmEnum encryptAlgorithm, String publicKeyFile, String plainText) {
        String algorithmName = encryptAlgorithm.getAlgorithmName();
        PublicKey publicKey = getPublicKey(algorithmName, publicKeyFile);
        String encryptCipherText = encrypt(algorithmName, publicKey, plainText);
        System.out.println("encrypt result:");
        System.out.println(encryptCipherText);
        return encryptCipherText;
    }

    public static String testDecrypt(EncryptAlgorithmEnum encryptAlgorithm, String privateKeyFile, String cipherText) {
        String algorithmName = encryptAlgorithm.getAlgorithmName();
        PrivateKey privateKey = getPrivateKey(algorithmName, privateKeyFile);
        String decryptPlainText = decrypt(algorithmName, privateKey, cipherText);
        System.out.println("decrypt result:");
        System.out.println(decryptPlainText);
        return decryptPlainText;
    }

    public static void testEncryptFile(EncryptAlgorithmEnum encryptAlgorithm,  String publicKeyFile,
                                       String originalFile, String chipperTextFileToSave) {
        String algorithmName = encryptAlgorithm.getAlgorithmName();
        PublicKey publicKey = getPublicKey(algorithmName, publicKeyFile);

        boolean encryptFileResult = encryptFile(algorithmName, publicKey, originalFile, publicKeyFile);
        System.out.println("encryptFileResult:" + encryptFileResult);
    }

    public static void testEncryptFileNew(EncryptAlgorithmEnum encryptAlgorithm,  String publicKeyFile,
                                       String originalFile, String chipperTextFileToSave) {
        String algorithmName = encryptAlgorithm.getAlgorithmName();
        PublicKey publicKey = getPublicKey(algorithmName, publicKeyFile);

        boolean encryptFileResult = encryptFileNew(algorithmName, publicKey, originalFile, chipperTextFileToSave);
        System.out.println("encryptFileResult:" + encryptFileResult);
    }

    public static void testDecryptFile(EncryptAlgorithmEnum encryptAlgorithm,  String privateKeyFile,
                                       String chipperTextFile, String plainTextFileToSave) {
        String algorithmName = encryptAlgorithm.getAlgorithmName();
        PrivateKey privateKey = getPrivateKey(algorithmName, privateKeyFile);

        decryptFile(algorithmName, privateKey, chipperTextFile, plainTextFileToSave);
    }

    public static void testDecryptFileNew(EncryptAlgorithmEnum encryptAlgorithm,  String privateKeyFile,
                                       String chipperTextFile, String plainTextFileToSave) {
        String algorithmName = encryptAlgorithm.getAlgorithmName();
        PrivateKey privateKey = getPrivateKey(algorithmName, privateKeyFile);

        decryptFileNew(algorithmName, privateKey, chipperTextFile, plainTextFileToSave);
    }
}
