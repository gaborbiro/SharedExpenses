package com.gaborbiro.sharedexpenses.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.UUID;

import static com.gaborbiro.sharedexpenses.App.getAppContext;

@SuppressWarnings({"SameParameterValue", "unused"})
public class PrefsUtil {

    private static final String PREFS_NAME = "settings";
    private static final String SEPARATOR = "dfg,hsdfk__jg34n95t";

    private static SecurePreferences securePreferences;

    public static void put(String key, Parcelable[] values) {
        Parcel parcel = Parcel.obtain();
        parcel.writeParcelableArray(values, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        put(key, Base64.encodeToString(bytes, 0));
    }


    public static Parcelable[] get(String key, ClassLoader classLoader) {
        String data = get(key, (String) null);

        if (data == null) {
            return null;
        }
        byte[] bytes = Base64.decode(data, 0);
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        return parcel.readParcelableArray(classLoader);
    }


    public static void put(String key, Parcelable parcelable) {
        Parcel parcel = Parcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        put(key, Base64.encodeToString(bytes, 0));
    }


    public static <T> T get(String key, Parcelable.Creator<T> creator) {
        String data = get(key, (String) null);

        if (data == null) {
            return null;
        }
        byte[] bytes = Base64.decode(data, 0);
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        return creator.createFromParcel(parcel);
    }


    public static Map get(String key, Map defaultValues) {
        String data = get(key, (String) null);

        if (data == null) {
            return defaultValues;
        }
        try {
            byte[] bytes = Base64.decode(data, 0);
            ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(bytes, 0, bytes.length));
            return (Map) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void put(String key, Map map) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos;
            oos = new ObjectOutputStream(baos);
            oos.writeObject(map);
            oos.flush();
            put(key, Base64.encodeToString(baos.toByteArray(), 0));
            if (oos != null) {
                oos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String[] get(String key, String[] defaultValues) {
        String defaultValuesStr;

        if (defaultValues == null) {
            defaultValuesStr = null;
        } else {
            defaultValuesStr = TextUtils.join(SEPARATOR, defaultValues);
        }
        String text = get(key, defaultValuesStr);

        if (!TextUtils.isEmpty(text)) {
            return text.split(SEPARATOR);
        } else {
            return new String[0];
        }
    }


    public static void put(String key, String[] values) {
        if (values == null) {
            put(key, (String) null);
        } else {
            put(key, TextUtils.join(SEPARATOR, values));
        }
    }


    public static void put(String key, boolean value) {
        getSecurePreferences().put(key, Boolean.toString(value));
    }


    public static boolean get(String key, boolean defaultValue) {
        String value = getSecurePreferences().getString(key);
        return TextUtils.isEmpty(value) ? defaultValue : Boolean.valueOf(value)
                .booleanValue();
    }


    public static void put(String key, String value) {
        if (value == null) {
            getSecurePreferences().removeValue(key);
        } else {
            getSecurePreferences().put(key, value);
        }
    }


    public static String get(String key, String defaultValue) {
        String value = getSecurePreferences().getString(key);
        return TextUtils.isEmpty(value) ? defaultValue : value;
    }


    public static void put(String key, int value) {
        getSecurePreferences().put(key, Integer.toString(value));
    }


    public static int get(String key, int defaultValue) {
        String value = getSecurePreferences().getString(key);
        return TextUtils.isEmpty(value) ? defaultValue : Integer.valueOf(value);
    }


    public static void put(String key, long value) {
        getSecurePreferences().put(key, Long.toString(value));
    }


    public static long get(String key, long defaultValue) {
        String value = getSecurePreferences().getString(key);
        return TextUtils.isEmpty(value) ? defaultValue : Long.valueOf(value);
    }


    public static void put(String key, float value) {
        getSecurePreferences().put(key, Float.toString(value));
    }


    public static float get(String key, float defaultValue) {
        String value = getSecurePreferences().getString(key);
        return TextUtils.isEmpty(value) ? defaultValue : Float.valueOf(value);
    }

    public static void remove(String key) {
        getSecurePreferences().removeValue(key);
    }


    private static SecurePreferences getSecurePreferences() {
        if (securePreferences == null) {
            securePreferences = new SecurePreferences(getAppContext(), PREFS_NAME,
                    generateUDID(getAppContext()), true);
        }
        return securePreferences;
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
    public static void registerOnSharedPreferenceChangeListener(String key,
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getSecurePreferences().registerOnSharedPreferenceChangeListener(key, listener);
    }


    /**
     * Unregisters a previous callback.
     *
     * @param key PReference key, the callback of which that should be unregistered.
     * @see #registerOnSharedPreferenceChangeListener
     */
    public static void unregisterOnSharedPreferenceChangeListener(String key) {
        getSecurePreferences().unregisterOnSharedPreferenceChangeListener(key);
    }

    public static boolean export(OutputStream out) {
        return getSecurePreferences().export(out);
    }

    public static boolean import_(InputStream in) {
        return getSecurePreferences().import_(in);
    }

    /**
     * Generate a unique id for the device. Changes with every factory reset. If the
     * device doesn't have a proper
     * android_id and deviceId, it falls back to a randomly generated id, that is
     * persisted in SharedPreferences.
     */
    private static String generateUDID(Context applicationContext) {
        String deviceId = null;
        String androidId;
        UUID deviceUuid = null;

        // androidId changes with every factory reset (which is useful in our case)
        androidId = "" + android.provider.Settings.Secure.getString(
                getAppContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

        try {
            if (!"9774d56d682e549c".equals(androidId)) {
                deviceUuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
            } else {
                // On some 2.2 devices androidId is always 9774d56d682e549c,
                // which is unsafe
                TelephonyManager tm = (TelephonyManager) getAppContext().getSystemService(
                        Context.TELEPHONY_SERVICE);

                if (tm != null) {
                    // Tablets may not have imei and/or imsi.
                    // Does not change on factory reset.
                    deviceId = tm.getDeviceId();
                }

                if (TextUtils.isEmpty(deviceId)) {
                    // worst case scenario as this id is lost when the
                    // application stops
                    deviceUuid = UUID.randomUUID();
                } else {
                    deviceUuid = UUID.nameUUIDFromBytes(deviceId.getBytes("utf8"));
                }
            }
        } catch (UnsupportedEncodingException e) {
            // Change it back to "utf8" right now!!
        }
        return deviceUuid.toString();
    }
}
