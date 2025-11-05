package com.pixelcrater.Diaro.utils;

import android.util.Base64;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * http://www.imcore.net/
 */
public class AES256Cipher {
    public static byte[] ivBytes =
            {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    public static Cipher getCipher(int mode, String key) throws Exception {
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(mode, newKey, ivSpec);

        return cipher;
    }

    private static byte[] encodeBytes(byte[] plainTextBytes, String key) throws Exception {
        if (plainTextBytes == null) {
            return null;
        }

        return getCipher(Cipher.ENCRYPT_MODE, key).doFinal(plainTextBytes);
    }

    private static byte[] decodeBytes(byte[] encodedTextBytes, String key) throws Exception {
        if (encodedTextBytes == null) {
            return null;
        }

        return getCipher(Cipher.DECRYPT_MODE, key).doFinal(encodedTextBytes);
    }

    public static String encodeString(String plainText, String key) throws Exception {
        if (plainText == null) {
            return null;
        }

        byte[] plainTextBytes = plainText.getBytes("UTF-8");
        byte[] encryptedBytes = encodeBytes(plainTextBytes, key);

        return Base64.encodeToString(encryptedBytes, 0).trim();
    }

    public static String decodeString(String encodedText, String key) throws Exception {
        if (encodedText == null) {
            return null;
        }

        byte[] encodedTextBytes = Base64.decode(encodedText, 0);
        byte[] decodedBytes = decodeBytes(encodedTextBytes, key);

        return new String(decodedBytes, "UTF-8");
    }

    public static void encodeFile(File fileToEncode, File encodedFile, String key) throws Exception {
        AppLog.d("fileToEncode: " + fileToEncode.getPath() + ", encodedFile: " + encodedFile.getPath());

        // Read fileToEncode
        InputStream fis = new FileInputStream(fileToEncode);

        InputStream stream = new ByteArrayInputStream(Base64.encode(getCipher(Cipher.ENCRYPT_MODE, key).doFinal(IOUtils.toByteArray(fis)), 0));

        // Write encodedBytes to the new encodedFile
        FileOutputStream fos = new FileOutputStream(encodedFile);

        int bSize = 3 * 512;
        // Buffer
        byte[] buf = new byte[bSize];
        // Actual size of buffer
        int len;

        while ((len = stream.read(buf)) != -1) {
            fos.write(buf, 0, len);
        }

        stream.close();
        fis.close();
        fos.close();
    }

    public static void decodeFile(File encodedFile, File decodedFile, String key) throws Exception {
        AppLog.d("encodedFile: " + encodedFile.getPath() + ", decodedFile: " + decodedFile.getPath());

        // Read encodedFile
        InputStream fis = new FileInputStream(encodedFile);
        InputStream stream = new ByteArrayInputStream(getCipher(Cipher.DECRYPT_MODE, key).doFinal(Base64.decode(IOUtils.toByteArray(fis), 0)));
        // Write decodedBytes to the new decodedFile
        OutputStream fos = new FileOutputStream(decodedFile);

        int bSize = 3 * 512;
        // Buffer
        byte[] buf = new byte[bSize];
        // Actual size of buffer
        int len;

        while ((len = stream.read(buf)) != -1) {
//            AppLog.d("len: " + len);
//            AppLog.d("chunk: " + new String(buf));
            fos.write(buf, 0, len);
        }

        stream.close();
        fos.close();
        fis.close();
    }
}
