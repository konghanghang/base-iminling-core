package com.iminling.common.crypto

import org.apache.commons.codec.binary.Base64
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.AlgorithmParameters
import java.security.Key
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * @author  yslao@outlook.com
 * @since  2021/11/22
 */
class AESUtils {

    companion object {
        init {
            Security.addProvider(BouncyCastleProvider())
        }

        /**
         * 默认ECB key
         */
        const val ECB_KEY = "Hi9MuIAhR49SiWCQViUjHg=="

        // 加密模式
        const val CBC_PKCS7 = "AES/CBC/PKCS7Padding"
        const val CBC_NO_PADDING = "AES/CBC/NoPadding"
        const val ECB_PKCS5 = "AES/ECB/PKCS5Padding"
        private const val AES_NAME = "AES"

        /**
         * 获取AES密钥
         * @param length 密钥长度,可选值：128、192、256，默认128
         */
        @JvmOverloads
        fun generateECBKey(length: Int = 128): String {
            val keyGenerator = KeyGenerator.getInstance(AES_NAME)
            keyGenerator.init(length)
            val key: Key = keyGenerator.generateKey()
            val keyBytes = key.encoded
            return Base64.encodeBase64String(keyBytes)
        }

        /**
         * 加密 ECB模式
         * @param data 需要加密的内容
         * @param key  加密key
         * @param type 算法类型
         * @return 加密后的字符串
         */
        @JvmOverloads
        fun encryptECB(data: String, key: String = ECB_KEY, type: String = ECB_PKCS5): String {
            var keyBytes = Base64.decodeBase64(key)

            val cipher = Cipher.getInstance(type)
            val secretKeySpec = SecretKeySpec(keyBytes, AES_NAME)
            val byteContent: ByteArray = data.toByteArray()
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            val result = cipher.doFinal(byteContent)
            return Base64.encodeBase64String(result)
        }

        /**
         * 解密 ECB模式
         * @param data 需要解密的内容
         * @param key  解密key
         * @param type 算法类型
         * @return 解密后的字符串
         */
        @JvmOverloads
        fun decryptECB(data: String, key: String = ECB_KEY, type: String = ECB_PKCS5): String {
            val secretKeySpec = SecretKeySpec(Base64.decodeBase64(key), AES_NAME)
            val cipher = Cipher.getInstance(type)
            val byteContent: ByteArray = Base64.decodeBase64(data)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
            val result = cipher.doFinal(byteContent)
            return String(result)
        }

        /**
         * 加密 CBC模式
         * @param data 数据
         * @param key key
         * @param iv iv
         * @param type 算法类型
         * @return 加密后的字符串
         */
        @JvmOverloads
        fun encryptCBC(data: String, key: String, iv: String, type: String = CBC_PKCS7): String {
            val dataByte = Base64.decodeBase64(data)
            val keyByte = Base64.decodeBase64(key)
            val ivByte = Base64.decodeBase64(iv)
            return encryptCBC(dataByte, keyByte, ivByte, type)
        }

        /**
         * 加密 CBC模式
         * @param data 数据
         * @param key key
         * @param iv iv
         * @param type 算法类型
         * @return 加密后的字符串
         */
        @JvmOverloads
        fun encryptCBC(data: ByteArray, key: ByteArray, iv: ByteArray, type: String = CBC_PKCS7): String {
            //指定算法，模式，填充方式，创建一个Cipher
            val cipher = Cipher.getInstance(type)
            //生成Key对象
            val sKeySpec: Key = SecretKeySpec(key, AES_NAME)
            //把向量初始化到算法参数
            val params = AlgorithmParameters.getInstance(AES_NAME)
            params.init(IvParameterSpec(iv))
            //指定用途，密钥，参数 初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, params)
            //指定加密
            val result = cipher.doFinal(data)
            //对结果进行Base64编码，否则会得到一串乱码，不便于后续操作
            return Base64.encodeBase64String(result)
        }

        /**
         * 解密 CBC模式
         * @param data 加密后的字符串
         * @param key key
         * @param iv iv
         * @param type 算法类型
         * @return 解密后的字符串
         */
        @JvmOverloads
        fun decryptCBC(data: String, key: String, iv: String, type: String = CBC_PKCS7): String {
            //解密之前先把Base64格式的数据转成原始格式
            val dataByte = Base64.decodeBase64(data)
            val keyByte = Base64.decodeBase64(key)
            val ivByte = Base64.decodeBase64(iv)
            return decryptCBC(dataByte, keyByte, ivByte, type)
        }

        /**
         * 解密 CBC模式
         * @param data 加密后的字符串
         * @param key key
         * @param iv iv
         * @param type 算法类型
         * @return 解密后的字符串
         */
        @JvmOverloads
        fun decryptCBC(data: ByteArray, key: ByteArray, iv: ByteArray, type: String = CBC_PKCS7): String {
            //指定算法，模式，填充方法 创建一个Cipher实例
            val cipher = Cipher.getInstance(type)
            //生成Key对象
            val sKeySpec: Key = SecretKeySpec(key, AES_NAME)
            //把向量初始化到算法参数
            val params = AlgorithmParameters.getInstance(AES_NAME)
            params.init(IvParameterSpec(iv))
            //指定用途，密钥，参数 初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, params)
            //执行解密
            val result = cipher.doFinal(data)
            //解密后转成字符串
            return String(result)
        }
    }

}