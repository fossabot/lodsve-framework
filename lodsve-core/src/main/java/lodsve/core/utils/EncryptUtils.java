/*
 * Copyright (C) 2019 Sun.Hao(https://www.crazy-coder.cn/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lodsve.core.utils;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 加密工具类.
 *
 * @author <a href="mailto:sunhao.java@gmail.com">sunhao(sunhao.java@gmail.com)</a>
 * @date 2015-1-6 20:57
 */
public class EncryptUtils {
    private static final Logger logger = LoggerFactory.getLogger(EncryptUtils.class);
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 用MD5算法进行加密
     *
     * @param plainText 明文，需要加密的字符串
     * @return MD5加密后的结果
     */
    public static String encodeMD5(String plainText) {
        return encode(plainText, EncryptType.MD5);
    }

    /**
     * 用SHA算法进行加密
     *
     * @param plainText 明文，需要加密的字符串
     * @return SHA加密后的结果
     */
    public static String encodeSHA(String plainText) {
        return encode(plainText, EncryptType.SHA);
    }

    /**
     * 用base64算法进行加密
     *
     * @param plainText 明文，需要加密的字符串
     * @return base64加密后的结果
     */
    public static String encodeBase64(String plainText) {
        Assert.hasText(plainText);

        try {
            return new String(Base64.encodeBase64(plainText.getBytes("utf-8")), "utf-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }

        return StringUtils.EMPTY;
    }

    /**
     * 用base64算法进行解密
     *
     * @param cipherText 密文，需要解密的字符串
     * @return base64解密后的结果
     */
    public static String decodeBase64(String cipherText) {
        Assert.hasText(cipherText);

        try {
            return new String(Base64.decodeBase64(cipherText.getBytes("utf-8")), "utf-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }

        return StringUtils.EMPTY;
    }

    /**
     * 根据类型加密
     *
     * @param plainText   明文
     * @param encryptType 加密类型
     * @return md5
     */
    private static String encode(String plainText, EncryptType encryptType) {
        String dstr = null;
        try {
            MessageDigest md = MessageDigest.getInstance(encryptType.name());
            md.update(plainText.getBytes());
            dstr = new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return dstr;
    }

    /**
     * 加密类型
     */
    private enum EncryptType {
        /**
         * md5
         */
        MD5, SHA
    }

    /**
     * 获取文件MD5值
     *
     * @param file 文件
     * @return 文件MD5值
     */
    public static String getFileMD5(File file) {
        try {
            return getFileMD5(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return "";
        }
    }

    /**
     * 获取文件MD5值
     *
     * @param in 文件流
     * @return 文件流MD5值
     */
    public static String getFileMD5(FileInputStream in) {
        try {
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");

            FileChannel ch = in.getChannel();
            MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, in.available());
            messagedigest.update(byteBuffer);

            return bufferToHex(messagedigest.digest());
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 获取文件MD5值
     *
     * @param fileBytes 文件流
     * @return 文件流MD5值
     */
    public static String getFileMD5(byte[] fileBytes) {
        try {
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");

            messagedigest.update(fileBytes);
            return bufferToHex(messagedigest.digest());
        } catch (Exception e) {
            return "";
        }
    }

    private static String bufferToHex(byte[] bytes) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte[] bytes, int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = HEX_DIGITS[(bt & 0xf0) >> 4];
        char c1 = HEX_DIGITS[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }
}
