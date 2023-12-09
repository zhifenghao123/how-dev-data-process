package com.howdev.util.encrypt;

/**
 * EncryptAlgorithmEnum class
 *
 * @author haozhifeng
 * @date 2023/12/08
 */
public enum EncryptAlgorithmEnum {
    /**
     * 凯撒加密
     */
    CAESAR("Caesar", EncryptTypeEnum.CAESAR),

    /**
     * Base64
     */
    Base64("Base64", EncryptTypeEnum.Base64),

    /**
     * MD5
     */
    MD5("MD5", EncryptTypeEnum.MDA),

    /**
     * SHA-1
     */
    SHA_1("SHA-1", EncryptTypeEnum.MDA),

    /**
     * SHA-256
     */
    SHA_256("SHA-256", EncryptTypeEnum.MDA),

    /**
     * DES
     */
    DES("DES", EncryptTypeEnum.SYMMETRY),

    /**
     * 3DES
     */
    TRIPLE_DES("3DES", EncryptTypeEnum.SYMMETRY),

    /**
     * AES
     */
    AES("AES", EncryptTypeEnum.SYMMETRY),

    /**
     * RSA
     */
    RSA("RSA", EncryptTypeEnum.ASYMMETRY),

    /**
     * DSA
     */
    DSA("DSA", EncryptTypeEnum.ASYMMETRY),

    /**
     * Diffie-Hellman (D-H) 密钥交换协议中的公钥加密算法
     */
    DIFFIE_HELLMAN("DIFFIEHELLMAN", EncryptTypeEnum.ASYMMETRY),

    /**
     * Elliptic Curve Cryptography（ECC,椭圆曲线加密算法）
     */
    ECC("ECC", EncryptTypeEnum.ASYMMETRY);


    /**
     * 算法名称
     */
    private final String algorithmName;
    /**
     * 加密类型
     */
    private final EncryptTypeEnum encryptType;

    EncryptAlgorithmEnum(String algorithmName, EncryptTypeEnum encryptType) {
        this.algorithmName = algorithmName;
        this.encryptType = encryptType;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public EncryptTypeEnum getEncryptType() {
        return encryptType;
    }

    enum EncryptTypeEnum {
        /**
         * 凯撒加密
         */
        CAESAR(1, "凯撒加密"),
        /**
         * Base64
         */
        Base64(2, "Base64"),
        /**
         * 信息摘要加密算法
         */
        MDA(3,"信息摘要加密算法"),
        /**
         * 对称加密
         */
        SYMMETRY(4, "对称加密"),
        /**
         * 非对称加密
         */
        ASYMMETRY(5, "非对称加密");

        /**
         * 类型
         */
        private final Integer type;
        /**
         * 描述
         */
        private final String desc;

        EncryptTypeEnum(Integer type, String desc) {
            this.type = type;
            this.desc = desc;
        }
    }
}
