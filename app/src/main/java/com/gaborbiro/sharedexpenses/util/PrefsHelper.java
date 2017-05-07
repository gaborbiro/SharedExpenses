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

import javax.inject.Inject;

@SuppressWarnings({"SameParameterValue", "unused"})
public class PrefsHelper {

    private static final String PREFS_NAME = "settings";
    private static final String SEPARATOR = "dfg,hsdfk__jg34n95t";

    private SecurePreferences securePreferences;

    @Inject
    PrefsHelper(Context context) {
        securePreferences = new SecurePreferences(context, PREFS_NAME,
                generateUDID(context), true);
    }

    public void put(String key, Parcelable[] values) {
        Parcel parcel = Parcel.obtain();
        parcel.writeParcelableArray(values, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        put(key, Base64.encodeToString(bytes, 0));
    }


    public Parcelable[] get(String key, ClassLoader classLoader) {
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


    public void put(String key, Parcelable parcelable) {
        Parcel parcel = Parcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        put(key, Base64.encodeToString(bytes, 0));
    }


    public <T> T get(String key, Parcelable.Creator<T> creator) {
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


    public Map get(String key, Map defaultValues) {
        String data = get(key, (String) null);

        if (data == null) {
            return defaultValues;
        }
        try {
            byte[] bytes = Base64.decode(data, 0);
            ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(bytes, 0, bytes.length));
            return (Map) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void put(String key, Map map) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos;
            oos = new ObjectOutputStream(baos);
            oos.writeObject(map);
            oos.flush();
            put(key, Base64.encodeToString(baos.toByteArray(), 0));
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String[] get(String key, String[] defaultValues) {
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


    public void put(String key, String[] values) {
        if (values == null) {
            put(key, (String) null);
        } else {
            put(key, TextUtils.join(SEPARATOR, values));
        }
    }


    public void put(String key, boolean value) {
        securePreferences.put(key, Boolean.toString(value));
    }


    public boolean get(String key, boolean defaultValue) {
        String value = securePreferences.getString(key);
        return TextUtils.isEmpty(value) ? defaultValue : Boolean.valueOf(value);
    }


    public void put(String key, String value) {
        if (value == null) {
            securePreferences.removeValue(key);
        } else {
            securePreferences.put(key, value);
        }
    }


    public String get(String key, String defaultValue) {
        String value = securePreferences.getString(key);
        return TextUtils.isEmpty(value) ? defaultValue : value;
    }


    public void put(String key, int value) {
        securePreferences.put(key, Integer.toString(value));
    }


    public int get(String key, int defaultValue) {
        String value = securePreferences.getString(key);
        return TextUtils.isEmpty(value) ? defaultValue : Integer.valueOf(value);
    }


    public void put(String key, long value) {
        securePreferences.put(key, Long.toString(value));
    }


    public long get(String key, long defaultValue) {
        String value = securePreferences.getString(key);
        return TextUtils.isEmpty(value) ? defaultValue : Long.valueOf(value);
    }


    public void put(String key, float value) {
        securePreferences.put(key, Float.toString(value));
    }


    public float get(String key, float defaultValue) {
        String value = securePreferences.getString(key);
        return TextUtils.isEmpty(value) ? defaultValue : Float.valueOf(value);
    }

    public void remove(String key) {
        securePreferences.removeValue(key);
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
    public void registerOnSharedPreferenceChangeListener(String key,
                                                         SharedPreferences.OnSharedPreferenceChangeListener listener) {
        securePreferences.registerOnSharedPreferenceChangeListener(key, listener);
    }


    /**
     * Unregisters a previous callback.
     *
     * @param key PReference key, the callback of which that should be unregistered.
     * @see #registerOnSharedPreferenceChangeListener
     */
    public void unregisterOnSharedPreferenceChangeListener(String key) {
        securePreferences.unregisterOnSharedPreferenceChangeListener(key);
    }

    public boolean export(OutputStream out) {
        return securePreferences.export(out);
    }

    public boolean import_(InputStream in) {
        return securePreferences.import_(in);
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
                applicationContext.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

        try {
            if (!"9774d56d682e549c".equals(androidId)) {
                deviceUuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
            } else {
                // On some 2.2 devices androidId is always 9774d56d682e549c,
                // which is unsafe
                TelephonyManager tm = (TelephonyManager) applicationContext.getSystemService(
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
