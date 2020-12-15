 package com.feel.feel

import android.annotation.SuppressLint
import android.util.Log
import com.feel.feel.data.*
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.*
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*


const val BASE_URL = "https://ec2-3-16-51-248.us-east-2.compute.amazonaws.com/api/"
const val AUTHORIZATION_HEADER = "Authorization"
const val TOKEN_TYPE = "Bearer"

interface API {
    @POST("auth/google-login")
    suspend fun googleLogin(
        @Body data: GoogleLoginRequestBody
    ): GooglLoginResponse

    @GET("video")
    suspend fun getVideos(
        @Query("price") price: Int,
        @Query("skip") skip: Int,
        @Query("take") take: Int
    ): List<Video>

    @GET("video/{videoId}")
    suspend fun getVideoDetail(
        @Path("videoId") videoId: String
    ): VideoDetail

    @PUT("user/buy")
    suspend fun buyVideo(
        @Body buyVideoRequestBody: BuyVideoRequestBody
    ): BuyVideoResponse

    @Multipart
    @POST("file/upload")
    suspend fun uploadFile(
        @Part body: MultipartBody.Part
    ): FileUploadResponse

    @POST("video/upload")
    suspend fun uploadVideo(
        @Body uploadVideoRequestBody: UploadVideoRequestBody
    ): Video

    @GET("video/user/{userId}")
    suspend fun getVideoByUser(
        @Path("userId") userId: String
    ): List<Video>

    @GET("user/{userId}")
    suspend fun getUserInfo(
        @Path("userId") userId: String
    ): UserInfoResponse

    @GET("user/owned-video/{userId}")
    suspend fun getOwnedVideoByUser(
        @Path("userId") userId: String
    ): OwnedVideoResponse

    @GET("user/has-video/{userId}/{videoId}")
    suspend fun userDidBuy(
        @Path("userId") userId: String,
        @Path("videoId") videoId: String
    ): String

    @POST("transaction/charge")
    suspend fun charge(
        @Body chargeRequestBody: ChargeRequestBody
    ): ChargeResponse

    @POST("auth/refresh")
    suspend fun refresh(
        @Body refreshTokenRequestBody: RefreshTokenRequestBody
    ): GooglLoginResponse

    companion object {
        operator fun invoke(): API {
            val interceptor = Interceptor { chain ->
                val url = chain
                    .request()
                    .url
                    .newBuilder()
                    .build()
                val token = Util.getToken()
                val requestBuilder = chain.request()
                    .newBuilder()
                    .url(url)
                token?.let {
                    val headerValue = TOKEN_TYPE + " " + it
                    requestBuilder.addHeader(AUTHORIZATION_HEADER, headerValue)
                }

                val request = requestBuilder.build()

                return@Interceptor chain.proceed(request)
            }

            val trustManager = arrayOf<TrustManager>(object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            })
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustManager, java.security.SecureRandom())

            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(loggingInterceptor)
                .authenticator(RefreshAuthenticator())
                .sslSocketFactory(sslContext.socketFactory, trustManager[0] as X509TrustManager)
                .hostnameVerifier(HostnameVerifier { _, _ -> true })
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .client(client)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(API::class.java)
        }

    }
}

class RefreshAuthenticator: Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        response.priorResponse?.let {

        }
        val userId = Util.getUserData().id
        val refreshToken = Util.getUserData().refreshToken
        val refreshTokenRequestBody = RefreshTokenRequestBody(refreshToken, userId)
        val api = Util.getApiClient()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val refreshTokenResponse = api.refresh(refreshTokenRequestBody)
                val token = refreshTokenResponse.data.accessToken
                val newRefreshToken = refreshTokenResponse.data.refreshToken
                Util.setToken(token)
                Util.setRefreshToken(newRefreshToken)
            } catch (e: HttpException) {
            }

        }

        val token = TOKEN_TYPE + " " + Util.getToken()

        return response
            .request
            .newBuilder()
            .header(AUTHORIZATION_HEADER, token)
            .build()
    }
}