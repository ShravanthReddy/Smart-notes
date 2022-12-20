package com.shravanth.smartnotes.encryption;

import android.util.Base64;

import androidx.room.TypeConverter;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptedConverter {

    @TypeConverter
    public static byte[] fromEncryptedString(String encryptedString) {
        try {
            // Load the key from the Android KeyStore
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey("my_key", null);

            // Convert the SecretKey object to a byte array
            byte[] aesKeyBytes = secretKey.getEncoded();

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKeyBytes, "AES"));
            return cipher.doFinal(Base64.decode(encryptedString, Base64.DEFAULT));
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

    @TypeConverter
    public static String toEncryptedString(byte[] data) {
        try {
            // Load the key from the Android KeyStore
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey("my_key", null);

            // Convert the SecretKey object to a byte array
            byte[] aesKeyBytes = secretKey.getEncoded();

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKeyBytes, "AES"));
            return Base64.encodeToString(cipher.doFinal(data), Base64.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

