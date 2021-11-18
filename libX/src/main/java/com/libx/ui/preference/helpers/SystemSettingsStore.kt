package com.libx.ui.preference.helpers

import android.content.ContentResolver
import android.provider.Settings
import com.libx.ui.preference.PreferenceDataStore

class SystemSettingsStore(
    private val contentResolver: ContentResolver
) : PreferenceDataStore() {

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return getInt(key, if (defValue) 1 else 0) != 0
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return Settings.System.getFloat(contentResolver, key, defValue)
    }

    override fun getInt(key: String, defValue: Int): Int {
        return Settings.System.getInt(contentResolver, key, defValue)
    }

    override fun getLong(key: String, defValue: Long): Long {
        return Settings.System.getLong(contentResolver, key, defValue)
    }

    override fun getString(key: String, defValue: String?): String? {
        val result = Settings.System.getString(contentResolver, key)
        return result ?: defValue
    }

    override fun putBoolean(key: String, value: Boolean) {
        putInt(key, if (value) 1 else 0)
    }

    override fun putFloat(key: String, value: Float) {
        Settings.System.putFloat(contentResolver, key, value)
    }

    override fun putInt(key: String, value: Int) {
        Settings.System.putInt(contentResolver, key, value)
    }

    override fun putLong(key: String, value: Long) {
        Settings.System.putLong(contentResolver, key, value)
    }

    override fun putString(key: String, value: String?) {
        Settings.System.putString(contentResolver, key, value)
    }
}