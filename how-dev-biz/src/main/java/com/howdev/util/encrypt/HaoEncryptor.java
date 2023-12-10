package com.howdev.util.encrypt;

/**
 * HaoEncryptor class
 *
 * @author haozhifeng
 * @date 2023/12/09
 */
public class HaoEncryptor {
    private static final String KEY = "HelloHaoReal_!16";
    public static void main(String[] args) {
        String key = KEY;

        String algorithmName = EncryptAlgorithmEnum.AES.getAlgorithmName();

        String projectPath = System.getProperty("user.dir");
        String dataFileDirectory = projectPath + "/how-dev-biz/test-data/dataFile/";

        String originalFile = dataFileDirectory + "user_bill_info.csv";
        String chipperTextFileToSave = dataFileDirectory + "hdataFile/" + algorithmName + "/user_bill_info-encrypt.csv";
        String plainTextFileToSave = dataFileDirectory + "hdataFile/" + algorithmName + "/user_bill_info-decrypt.csv";

        //SymmetryEncryptor.encryptFile(algorithmName, key, originalFile, chipperTextFileToSave);
        SymmetryEncryptor.decryptFile(algorithmName, key, chipperTextFileToSave, plainTextFileToSave);
    }
}
