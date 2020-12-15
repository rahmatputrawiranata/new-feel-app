package com.feel.feel

import com.feel.feel.data.Data
import com.feel.feel.data.Video
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.orhanobut.hawk.Hawk

const val KEY_TOKEN = "KEY_TOKEN"
const val KEY_REFRESH_TOKEN = "KEY_REFRESH_TOKEN"
const val KEY_API_CLIENT = "KEY_API_CLIENT"
const val KEY_USER_ID = "KEY_USER_ID"
const val KEY_USER_DATA = "KEY_USER_DATA"
const val KEY_PLAYBACK = "KEY_PLAYBACK"
const val KEY_BALANCE = "KEY_BALANCE"

object Util {
    fun setToken(token: String) {
        Hawk.put(KEY_TOKEN, token)
    }

    fun getToken(): String? {
        return Hawk.get(KEY_TOKEN)
    }

    fun setRefreshToken(refreshToken: String) {
        Hawk.put(KEY_REFRESH_TOKEN, refreshToken)
    }

    fun getRefreshToken(): String {
        return Hawk.get(KEY_REFRESH_TOKEN)
    }

    fun hasLogin(): Boolean {
        return Hawk.contains(KEY_TOKEN)
    }

    fun deleteToken() {
        Hawk.delete(KEY_TOKEN)
        Hawk.delete(KEY_REFRESH_TOKEN)
    }

    fun getApiClient(): API {
        return API()
    }

    fun setuserData(userData: Data) {
        Hawk.put(KEY_USER_DATA, userData)
    }

    fun getUserData(): Data {
        return Hawk.get(KEY_USER_DATA)
    }

    fun setPlaybackData(data: String) {
        Hawk.put(KEY_PLAYBACK, data)
    }

    fun getPlaybackData(): String {
        return Hawk.get(KEY_PLAYBACK)
    }

    fun setBalance(balance: Int) {
        Hawk.put(KEY_BALANCE, balance)
    }

    fun getBalance() : Int{
        return Hawk.get(KEY_BALANCE)
    }
}
