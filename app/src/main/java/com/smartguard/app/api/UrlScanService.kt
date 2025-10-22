package com.smartguard.app.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

data class UrlScanSubmitRequest(
    val url: String,
    val visibility: String = "public"
)

data class UrlScanSubmitResponse(
    val message: String?,
    val uuid: String?,
    val result: String?,
    val api: String?
)

data class UrlScanResultResponse(
    val page: PageInfo?,
    val verdicts: Verdicts?,
    val stats: ScanStats?
)

data class PageInfo(
    val url: String?,
    val domain: String?,
    val title: String?
)

data class Verdicts(
    val overall: VerdictInfo?,
    val urlscan: VerdictInfo?,
    val engines: EngineVerdicts?
)

data class VerdictInfo(
    val score: Int?,
    val malicious: Boolean?,
    val hasVerdicts: Boolean?
)

data class EngineVerdicts(
    val malicious: Any?,
    val suspicious: Any?,
    val benign: Any?
)

data class ScanStats(
    val malicious: Int?,
    val secureRequests: Int?,
    val totalLinks: Int?
)

interface UrlScanApi {
    @POST("scan/")
    @Headers("Content-Type: application/json")
    suspend fun submitScan(
        @Header("API-Key") apiKey: String,
        @Body request: UrlScanSubmitRequest
    ): Response<UrlScanSubmitResponse>

    @GET("result/{uuid}/")
    suspend fun getResult(
        @Path("uuid") uuid: String
    ): Response<UrlScanResultResponse>
}

object UrlScanService {
    private const val BASE_URL = "https://urlscan.io/api/v1/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: UrlScanApi = retrofit.create(UrlScanApi::class.java)
}

