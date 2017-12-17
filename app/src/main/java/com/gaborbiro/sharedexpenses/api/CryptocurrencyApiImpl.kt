package com.gaborbiro.sharedexpenses.api

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptocurrencyApiImpl @Inject constructor(credential: GoogleAccountCredential) : ApiBase(credential), CryptocurrencyApi {

    override fun getGain(): String {
        return ""
    }
}