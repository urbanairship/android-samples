package com.urbanairship.richpush.sample;

import android.content.SharedPreferences;
import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class SharedPreferencesImpl implements SharedPreferences{
    private Map<String, Object> preferences = new ConcurrentHashMap<String, Object>();
    private List<OnSharedPreferenceChangeListener> listeners = new ArrayList<OnSharedPreferenceChangeListener>();

    @Override
    public boolean contains(String key) {
        return preferences.containsKey(key);
    }

    @Override
    public Editor edit() {
        return new UAEditor();
    }

    @Override
    public Map<String, ?> getAll() {
        return new HashMap<String, Object>(preferences);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        Boolean value = (Boolean) preferences.get(key);
        return value == null ? defValue : value;
    }

    @Override
    public float getFloat(String key, float defValue) {
        Float value = (Float) preferences.get(key);
        return value == null ? defValue : value;
    }

    @Override
    public int getInt(String key, int defValue) {
        Integer value = (Integer) preferences.get(key);
        return value == null ? defValue : value;
    }

    @Override
    public long getLong(String key, long defValue) {
        Long value = (Long) preferences.get(key);
        return value == null ? defValue : value;
    }

    @Override
    public String getString(String key, String defValue) {
        String value = (String) preferences.get(key);
        return value == null ? defValue : value;
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValue) {
        @SuppressWarnings("unchecked")
        Set<String> value = (Set<String>) preferences.get(key);
        return value == null ? defValue : value;
    }

    @Override
    public synchronized void registerOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public synchronized void unregisterOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener listener) {
        listeners.remove(listener);
    }

    private synchronized ArrayList<OnSharedPreferenceChangeListener> getListeners() {
        return new ArrayList<OnSharedPreferenceChangeListener>(listeners);
    }

    public final class UAEditor implements Editor {

        private Object removeEdit = new Object();
        private boolean clear = false;
        private Map<String, Object> edits = new HashMap<String, Object>();

        @Override
        public synchronized void apply() {
            Set<String> keys = edits.keySet();
            final List<String> changedKeys = new ArrayList<String>();

            // Clear all of the fields if the clear flag is set
            if (clear) {
                clear = false;
                changedKeys.addAll(preferences.keySet());
                preferences.clear();
            }

            // Commit the edits
            for (String key: keys) {
                Object value = edits.get(key);

                if (value == removeEdit) {
                    preferences.remove(key);
                } else {
                    preferences.put(key, value);
                }

                if (!changedKeys.contains(key)) {
                    changedKeys.add(key);
                }
            }

            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // Notify any changes on keys
                    for (OnSharedPreferenceChangeListener listener : getListeners()) {
                        for (String key : changedKeys) {
                            listener.onSharedPreferenceChanged(SharedPreferencesImpl.this, key);
                        }
                    }
                }
            });


            edits.clear();
        }

        @Override
        public synchronized Editor clear() {
            clear = true;
            return this;
        }

        @Override
        public synchronized boolean commit() {
            apply();
            return true;
        }

        @Override
        public synchronized Editor putBoolean(String key, boolean value) {
            edits.put(key, value);
            return this;
        }

        @Override
        public synchronized Editor putFloat(String key, float value) {
            edits.put(key, value);
            return this;
        }

        @Override
        public synchronized Editor putInt(String key, int value) {
            edits.put(key, value);
            return this;
        }

        @Override
        public synchronized Editor putLong(String key, long value) {
            edits.put(key, value);
            return this;
        }

        @Override
        public synchronized Editor putString(String key, String value) {
            edits.put(key, value);
            return this;
        }

        @Override
        public synchronized Editor putStringSet(String key, Set<String> value) {
            edits.put(key, value);
            return this;
        }

        @Override
        public synchronized Editor remove(String key) {
            edits.put(key, removeEdit);
            return this;
        }

    }

}
