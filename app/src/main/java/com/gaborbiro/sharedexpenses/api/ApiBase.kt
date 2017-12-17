package com.gaborbiro.sharedexpenses.api

import com.gaborbiro.sharedexpenses.Constants
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets

abstract class ApiBase(credential: GoogleAccountCredential) {

    public val sheetsApi: Sheets

    init {
        this.sheetsApi = createSheetsApi(credential)
    }

    private fun createSheetsApi(credential: GoogleAccountCredential): Sheets {
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        return Sheets.Builder(
                transport, jsonFactory, credential)
                .setApplicationName(Constants.GOOGLE_APP_NAME)
                .build()
    }
}
