package com.libx.ui.preference.helpers

import android.content.ContentResolver
import android.provider.Settings
import com.libx.ui.preference.PreferenceDataStore

class SecureSettingsStore(
    private val contentResolver: ContentResolver
) : PreferenceDataStore() {

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return getInt(key, if (defValue) 1 else 0) != 0
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return Settings.Secure.getFloat(contentResolver, key, defValue)
    }

    override fun getInt(key: String, defValue: Int): Int {
        return Settings.Secure.getInt(contentResolver, key, defValue)
    }

    override fun getLong(key: String, defValue: Long): Long {
        return Settings.Secure.getLong(contentResolver, key, defValue)
    }

    override fun getString(key: String, defValue: String?): String? {
        val result = Settings.Secure.getString(contentResolver, key)
        return result ?: defValue
    }

    override fun putBoolean(key: String, value: Boolean) {
        putInt(key, if (value) 1 else 0)
    }

    override fun putFloat(key: String, value: Float) {
        Settings.Secure.putFloat(contentResolver, key, value)
    }

    override fun putInt(key: String, value: Int) {
        Settings.Secure.putInt(contentResolver, key, value)
    }

    override fun putLong(key: String, value: Long) {
        Settings.Secure.putLong(contentResolver, key, value)
    }

    override fun putString(key: String, value: String?) {
        Settings.Secure.putString(contentResolver, key, value)
    }
}