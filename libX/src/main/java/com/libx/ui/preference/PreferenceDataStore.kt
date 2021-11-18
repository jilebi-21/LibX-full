package com.libx.ui.preference

import java.lang.UnsupportedOperationException

abstract class PreferenceDataStore {
    open fun putString(key: String, value: String?) {
        throw UnsupportedOperationException("Not implemented on this data store")
    }

    open fun putStringSet(key: String, values: Set<String?>?) {
        throw UnsupportedOperationException("Not implemented on this data store")
    }

    open fun putInt(key: String, value: Int) {
        throw UnsupportedOperationException("Not implemented on this data store")
    }

    open fun putLong(key: String, value: Long) {
        throw UnsupportedOperationException("Not implemented on this data store")
    }

    open fun putFloat(key: String, value: Float) {
        throw UnsupportedOperationException("Not implemented on this data store")
    }

    open fun putBoolean(key: String, value: Boolean) {
        throw UnsupportedOperationException("Not implemented on this data store")
    }

    open fun getString(key: String, defValue: String?): String? {
        return defValue
    }

    open fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        return defValues
    }

    open fun getInt(key: String, defValue: Int): Int {
        return defValue
    }

    open fun getLong(key: String, defValue: Long): Long {
        return defValue
    }

    open fun getFloat(key: String, defValue: Float): Float {
        return defValue
    }

    open fun getBoolean(key: String, defValue: Boolean): Boolean {
        return defValue
    }
}