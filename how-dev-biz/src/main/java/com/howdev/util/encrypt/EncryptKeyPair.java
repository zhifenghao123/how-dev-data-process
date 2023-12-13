package com.howdev.util.encrypt;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Objects;


/**
 * EncryptKeyPair class
 *
 * @author haozhifeng
 * @date 2023/12/08
 */
public class EncryptKeyPair {
    /**
     * 私钥对象
     */
    PrivateKey privateKey;
    /**
     * 公钥对象
     */
    PublicKey publicKey;

    /**
     * 获取私钥字符串
     *
     * @param
     * @return:
     * @author: haozhifeng
     */
    public String getPrivateKeyString() {
        if (Objects.isNull(privateKey)) {
            return null;
        }
        byte[] privateKeyEncoded = privateKey.getEncoded();
        String privateEncodeString = Base64.getEncoder().encodeToString(privateKeyEncoded);
        return privateEncodeString;
    }

    /**
     * 获取公钥字符串
     *
     * @param
     * @return:
     * @author: haozhifeng
     */
    public String getPublicKeyString(){
        if (Objects.isNull(publicKey)) {
            return null;
        }
        byte[] publicKeyEncoded = publicKey.getEncoded();
        String publicEncodeString = Base64.getEncoder().encodeToString(publicKeyEncoded);

        return publicEncodeString;
    }


    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }
}
