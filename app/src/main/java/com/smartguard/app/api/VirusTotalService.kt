package com.smartguard.app.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

data class UrlScanRequest(
    val url: String
)

data class UrlAnalysisResponse(
    val data: AnalysisData?
)

data class AnalysisData(
    val id: String,
    val type: String
)

data class UrlReportResponse(
    val data: ReportData?
)

data class ReportData(
    val attributes: ReportAttributes?
)

data class ReportAttributes(
    val last_analysis_stats: AnalysisStats?,
    val last_final_url: String?,
    val title: String?,
    val reputation: Int?
)

data class AnalysisStats(
    val harmless: Int,
    val malicious: Int,
    val suspicious: Int,
    val undetected: Int,
    val timeout: Int
)

interface VirusTotalApi {
    @POST("urls")
    @FormUrlEncoded
    suspend fun scanUrl(
        @Header("x-apikey") apiKey: String,
        @Field("url") url: String
    ): Response<UrlAnalysisResponse>

    @GET("urls/{id}")
    suspend fun getUrlReport(
        @Header("x-apikey") apiKey: String,
        @Path("id") urlId: String
    ): Response<UrlReportResponse>
    
    @GET("urls/{urlId}")
    suspend fun checkUrlReport(
        @Header("x-apikey") apiKey: String,
        @Path("urlId") urlId: String
    ): Response<UrlReportResponse>
}

object VirusTotalService {
    private const val BASE_URL = "https://www.virustotal.com/api/v3/"
    
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

    val api: VirusTotalApi = retrofit.create(VirusTotalApi::class.java)
}

