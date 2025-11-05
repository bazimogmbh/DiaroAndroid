package com.pixelcrater.Diaro.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DeprecatedCrypto {

    private static final String iv = "9876543210abcdef";

    private static byte[] encryptBytes(byte[] messageBytes, String secretkey, boolean use_salt) throws Exception {
        byte[] keyBytes = (use_salt) ? secretkey.getBytes("UTF-8") : getRawKey(secretkey.getBytes("UTF-8"));

        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivs = new IvParameterSpec(iv.getBytes("UTF-8"));
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivs);
        byte[] encryptedBytes = cipher.doFinal(messageBytes);

        return encryptedBytes;
    }

    private static byte[] decryptBytes(byte[] encryptedBytes, String secretkey, boolean use_salt, boolean decryptPref) throws Exception {
        byte[] keyBytes = (use_salt) ? secretkey.getBytes("UTF-8") : getRawKey(secretkey.getBytes("UTF-8"));

        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        if (decryptPref) cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivs = new IvParameterSpec(iv.getBytes("UTF-8"));
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivs);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return decryptedBytes;
    }

    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        kgen.init(128, sr); // 192 and 256 bits may not be available
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        return raw;
    }

    private static String bytesToHex(byte[] data) {
        if (data == null) {
            return null;
        }

        int len = data.length;
        String str = "";
        for (int i = 0; i < len; i++) {
            if ((data[i] & 0xFF) < 16)
                str = str + "0" + java.lang.Integer.toHexString(data[i] & 0xFF);
            else str = str + java.lang.Integer.toHexString(data[i] & 0xFF);
        }
        return str;
    }

    private static byte[] hexToBytes(String str) {
        if (str == null) {
            return null;
        } else if (str.length() < 2) {
            return null;
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
            }
            return buffer;
        }
    }

    public static String encryptString(String clearText, String salt) throws Exception {
        byte[] encryptedBytes = DeprecatedCrypto.encryptBytes(clearText.getBytes(), salt, true);
        return DeprecatedCrypto.bytesToHex(encryptedBytes);
    }

    public static String decryptString(String encryptedText, String salt) throws Exception {
        byte[] decryptedBytes = DeprecatedCrypto.decryptBytes(DeprecatedCrypto.hexToBytes(encryptedText), salt, true, true);
        return new String(decryptedBytes);
    }

    public static void encryptFile(File fileToEncrypt, File encryptedFile, String key) throws Exception {
        AppLog.d("fileToEncrypt: " + fileToEncrypt.getPath() + ", encryptedFile: " + encryptedFile.getPath());

        // Read fileToEncrypt bytes
        InputStream fis = new FileInputStream(fileToEncrypt);
        byte[] fileToEncryptBytes = DeprecatedIOUtil.toByteArray(fis);
        //		Static.log("fileToEncryptBytes: " + fileToEncryptBytes);

        fis.close();

        // Encrypt bytes
        byte[] encryptedBytes = DeprecatedCrypto.encryptBytes(fileToEncryptBytes, key, true);
        //		Static.log("encryptedBytes: " + encryptedBytes);

        // Write encrypted HEX to the new encryptedFile
        FileOutputStream fos = new FileOutputStream(encryptedFile);
        fos.write(encryptedBytes);
        fos.close();
    }

    public static void decryptFile(InputStream encryptedFis, File decryptedFile, String key) throws Exception {
        AppLog.d("encryptedFis: " + encryptedFis + ", decryptedFile: " + decryptedFile.getPath());

        // Read encryptedFis bytes
        byte[] encryptedFileBytes = DeprecatedIOUtil.toByteArray(encryptedFis);
        encryptedFis.close();

        // Decrypt bytes
        byte[] decryptedBytes = DeprecatedCrypto.decryptBytes(encryptedFileBytes, key, true, false);
        //		Static.log("decryptedBytes: " + decryptedBytes);

        // Write decrypted data to the new decryptedFile
        FileOutputStream fos = new FileOutputStream(decryptedFile);
        fos.write(decryptedBytes);
        fos.close();
    }
}
