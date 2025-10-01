package com.github.kr328.clash.common.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.github.kr328.clash.common.log.Logger
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * @Author : Pony
 * @Time : created on 2024/4/24 11:06
 * @Description :DataStore数据操作管理类，创建本管理类对象时传入文件名
 */
class DatastoreManager(fileName: String) {
    private val Context.dataStore by preferencesDataStore(name = fileName)
    private lateinit var dataStore: DataStore<Preferences>

    fun init(context: Context) {
        dataStore = context.dataStore
    }

    fun clearAll() {
        runBlocking {
            dataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }

    fun <T> putValue(key: String, value: T) {
        when (value) {
            is Long -> putLong(key, value)
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Float -> putFloat(key, value)
            else -> {
                try {
                    val str = GsonBuilder().create().toJson(value)
                    putString(key, str)
                    Log.d("TAG_DATASTORE", "DataStoreManager-putValue-else:$str")
                } catch (_: Exception) {
                }
            }
        }
    }

    fun <T> getValue(key: String, default: T): Flow<*> {
        Logger.i("the default type is:$default")
        val data = when (default) {
            is Long.Companion -> getLong(key)
            is Int.Companion -> getInt(key)
            is Boolean.Companion -> getBoolean(key)
            is Float.Companion -> getFloat(key)
            else -> getString(key)
        }
        Log.d("TAG_DATASTORE", "DataStoreManager-getValue:$data")
        return data
    }

    fun putString(key: String, value: String) {
        val preferencesKey = stringPreferencesKey(key)
        editDS(preferencesKey, value)
    }

    fun getString(key: String, defaultValue: String = ""): Flow<String> {
        val preferencesKey = stringPreferencesKey(key)
        return getDSData(preferencesKey, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        val preferencesKey = booleanPreferencesKey(key)
        editDS(preferencesKey, value)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Flow<Boolean> {
        val preferencesKey = booleanPreferencesKey(key)
        return getDSData(preferencesKey, defaultValue)
    }

    fun putLong(key: String, value: Long) {
        val preferencesKey = longPreferencesKey(key)
        editDS(preferencesKey, value)
    }

    fun getLong(key: String, defaultValue: Long = 0L): Flow<Long> {
        val preferencesKey = longPreferencesKey(key)
        return getDSData(preferencesKey, defaultValue)
    }

    fun putFloat(key: String, value: Float) {
        val preferencesKey = floatPreferencesKey(key)
        editDS(preferencesKey, value)
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Flow<Float> {
        val preferencesKey = floatPreferencesKey(key)
        return getDSData(preferencesKey, defaultValue)
    }

    fun putInt(key: String, value: Int) {
        val preferencesKey = intPreferencesKey(key)
        editDS(preferencesKey, value)
    }

    fun getInt(key: String, defaultValue: Int = 0): Flow<Int> {
        val preferencesKey = intPreferencesKey(key)
        return getDSData(preferencesKey, defaultValue)
    }

    private inline fun <reified T> editDS(key: Preferences.Key<T>, value: T) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        }
    }

    private inline fun <reified T> getDSData(key: Preferences.Key<T>, defValue: T): Flow<T> {
        return dataStore.data.map { preferences -> preferences[key] ?: defValue }
    }
}