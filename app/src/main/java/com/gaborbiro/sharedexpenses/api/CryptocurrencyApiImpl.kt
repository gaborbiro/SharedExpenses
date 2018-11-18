package com.gaborbiro.sharedexpenses.api

import com.gaborbiro.sharedexpenses.Constants
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest
import com.google.api.services.sheets.v4.model.DimensionRange
import com.google.api.services.sheets.v4.model.InsertDimensionRequest
import com.google.api.services.sheets.v4.model.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptocurrencyApiImpl @Inject constructor(credential: GoogleAccountCredential) : ApiBase(credential), CryptocurrencyApi {

    override fun getGain(): String {
        val insertRowRequest = Request()
        insertRowRequest.insertDimension = InsertDimensionRequest()
        insertRowRequest.insertDimension.range = DimensionRange().setStartIndex(0).setEndIndex(1).setDimension("ROWS")
        var batchUpdate = BatchUpdateSpreadsheetRequest().setRequests(listOf(insertRowRequest))
        sheetsApi.spreadsheets().batchUpdate(Constants.CRYPTO_SPREADSHEET_ID, batchUpdate).execute()

        val deleteRowRequest = Request()
        deleteRowRequest.deleteDimension = DeleteDimensionRequest()
        deleteRowRequest.deleteDimension.range = DimensionRange().setStartIndex(0).setEndIndex(1).setDimension("ROWS")
        batchUpdate = BatchUpdateSpreadsheetRequest().setRequests(listOf(deleteRowRequest))
        sheetsApi.spreadsheets().batchUpdate(Constants.CRYPTO_SPREADSHEET_ID, batchUpdate).execute()

        val response = sheetsApi.spreadsheets().values()
                .get(Constants.CRYPTO_SPREADSHEET_ID, Constants.CRYPTO_TABLE_RANGE)
                .execute()
        val values = response.getValues()
        if (values != null && values.size == 4) {
            return String.format("Gain: %1\$s (%2\$s)", values[2][0], values[3][0])
        } else {
            return "Error reading gains"
        }
    }
}