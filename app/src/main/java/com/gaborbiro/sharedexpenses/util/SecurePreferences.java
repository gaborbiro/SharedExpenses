package com.gaborbiro.sharedexpenses.util;


/*
 Copyright (C) 2012 Sveinung Kval Bakken, sveinung.bakken@gmail.com

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


final class SecurePreferences
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String KEY_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String SECRET_KEY_HASH_TRANSFORMATION = "SHA-256";
    private static final String CHARSET = "UTF-8";
    private final boolean encryptKeys;
    private final Cipher writer;
    private final Cipher reader;
    private final Cipher keyWriter;
    private final SharedPreferences preferences;

    private Map<String, SharedPreferences.OnSharedPreferenceChangeListener> listeners;


    /**
     * This will initialize an instance of the SecurePreferences class
     *
     * @param context        your current context.
     * @param preferenceName name of preferences file (preferenceName.xml)
     * @param secureKey      the key used for encryption, finding a good key scheme is
     *                       hard. Hardcoding your key in the application
     *                       is bad, but better than plaintext preferences. Having the
     *                       user enter the key upon application launch
     *                       is a safe(r) alternative, but annoying to the user.
     * @param encryptKeys    settings this to false will only encrypt the values, true
     *                       will encrypt both values and keys. Keys can
     *                       contain a lot of information about the plaintext value of
     *                       the value which can be used to decipher the
     *                       value.
     * @throws SecurePreferencesException
     */
    public SecurePreferences(Context context,
                             @SuppressWarnings("SameParameterValue") String preferenceName,
                             String secureKey, @SuppressWarnings("SameParameterValue") boolean encryptKeys)
            throws SecurePreferencesException {
        try {
            this.writer = Cipher.getInstance(TRANSFORMATION);
            this.reader = Cipher.getInstance(TRANSFORMATION);
            this.keyWriter = Cipher.getInstance(KEY_TRANSFORMATION);

            initCiphers(secureKey);

            this.preferences =
                    context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
            this.preferences.registerOnSharedPreferenceChangeListener(this);
            this.encryptKeys = encryptKeys;
            this.listeners = new HashMap<>();
        } catch (GeneralSecurityException e) {
            throw new SecurePreferencesException(e);
        } catch (UnsupportedEncodingException e) {
            throw new SecurePreferencesException(e);
        }
    }


    private static byte[] convert(Cipher cipher, byte[] bs)
            throws SecurePreferencesException {
        try {
            return cipher.doFinal(bs);
        } catch (Exception e) {
            throw new SecurePreferencesException(e);
        }
    }


    private void initCiphers(String secureKey)
            throws UnsupportedEncodingException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidAlgorithmParameterException {
        IvParameterSpec ivSpec = getIv();
        SecretKeySpec secretKey = getSecretKey(secureKey);

        writer.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        reader.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        keyWriter.init(Cipher.ENCRYPT_MODE, secretKey);
    }


    private IvParameterSpec getIv() {
        byte[] iv = new byte[writer.getBlockSize()];
        System.arraycopy("fldsjfodasjifudslfjdsaofshaufihadsf".getBytes(), 0, iv, 0,
                writer.getBlockSize());
        return new IvParameterSpec(iv);
    }


    private SecretKeySpec getSecretKey(String key)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] keyBytes = createKeyBytes(key);
        return new SecretKeySpec(keyBytes, TRANSFORMATION);
    }


    private byte[] createKeyBytes(String key)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(SECRET_KEY_HASH_TRANSFORMATION);
        md.reset();
        return md.digest(key.getBytes(CHARSET));
    }


    private String toKey(String key) {
        if (encryptKeys) {
            return encrypt(key, keyWriter);
        } else {
            return key;
        }
    }


    private void putValue(String key, String value) throws SecurePreferencesException {
        String secureValueEncoded = encrypt(value, writer);
        preferences.edit()
                .putString(key, secureValueEncoded)
                .apply();
    }


    private String encrypt(String value, Cipher writer)
            throws SecurePreferencesException {
        byte[] secureValue;
        try {
            secureValue = convert(writer, value.getBytes(CHARSET));
        } catch (UnsupportedEncodingException e) {
            throw new SecurePreferencesException(e);
        }
        return Base64.encodeToString(secureValue, Base64.NO_WRAP);
    }


    private String decrypt(String securedEncodedValue) {
        byte[] securedValue = Base64.decode(securedEncodedValue, Base64.NO_WRAP);
        byte[] value = convert(reader, securedValue);
        try {
            return new String(value, CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new SecurePreferencesException(e);
        }
    }


    public void put(String key, String value) {
        if (value == null) {
            preferences.edit()
                    .remove(toKey(key))
                    .apply();
        } else {
            putValue(toKey(key), value);
        }
    }


    @SuppressWarnings("unused")
    public boolean containsKey(String key) {
        return preferences.contains(toKey(key));
    }


    public void removeValue(String key) {
        preferences.edit()
                .remove(toKey(key))
                .apply();
    }


    public String getString(String key) throws SecurePreferencesException {
        if (preferences.contains(toKey(key))) {
            String securedEncodedValue = preferences.getString(toKey(key), "");
            return decrypt(securedEncodedValue);
        }
        return null;
    }


    @SuppressWarnings("unused")
    public void clear() {
        preferences.edit()
                .clear()
                .apply();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (listeners.containsKey(key)) {
            listeners.get(key).onSharedPreferenceChanged(sharedPreferences, key);
        }
    }


    /**
     * Registers a callback to be invoked when a change happens to the specified
     * preference.
     *
     * @param key      Preference key for which the specified callback should be
     *                 registered to
     * @param listener The callback that will run.
     * @see #unregisterOnSharedPreferenceChangeListener
     */
    void registerOnSharedPreferenceChangeListener(String key,
                                                  SharedPreferences.OnSharedPreferenceChangeListener listener) {
        listeners.put(toKey(key), listener);
    }


    /**
     * Unregisters a previous callback.
     *
     * @param key PReference key, the callback of which that should be unregistered.
     * @see #registerOnSharedPreferenceChangeListener
     */
    void unregisterOnSharedPreferenceChangeListener(String key) {
        listeners.remove(toKey(key));
    }

    public static class SecurePreferencesException extends RuntimeException {

        public SecurePreferencesException(Throwable e) {
            super(e);
        }
    }

    boolean export(OutputStream out) {
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(out);
            output.writeObject(preferences.getAll());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    boolean import_(InputStream in) {
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(in);
            SharedPreferences.Editor prefEdit = preferences.edit();
            prefEdit.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();
//                if (v instanceof Boolean)
//                    prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
//                else if (v instanceof Float)
//                    prefEdit.putFloat(key, ((Float) v).floatValue());
//                else if (v instanceof Integer)
//                    prefEdit.putInt(key, ((Integer) v).intValue());
//                else if (v instanceof Long)
//                    prefEdit.putLong(key, ((Long) v).longValue());
//                else if (v instanceof String)
                    prefEdit.putString(key, ((String) v));
            }
            prefEdit.commit();
            return true;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
}
