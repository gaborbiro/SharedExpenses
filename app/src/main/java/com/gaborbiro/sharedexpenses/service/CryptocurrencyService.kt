package com.gaborbiro.sharedexpenses.service

import com.gaborbiro.sharedexpenses.api.CryptocurrencyApi
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptocurrencyService @Inject constructor() {

    @Inject lateinit var cryptoApi: CryptocurrencyApi

    fun getCryptoGain(): Single<String> {
        return Single.create { emitter ->
            try {
                emitter.onSuccess(cryptoApi.getGain())
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }
}