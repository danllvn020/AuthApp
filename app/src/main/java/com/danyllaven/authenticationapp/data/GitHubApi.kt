package com.danyllaven.authenticationapp.data

import com.google.gson.GsonBuilder
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface GitHubApi {
    @POST("login/oauth/access_token")
    @FormUrlEncoded
    suspend fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @retrofit2.http.Header("Accept") accept: String = "application/json"
    ): Response<AccessTokenResponse>
}

data class AccessTokenResponse(val access_token: String, val token_type: String, val scope: String)

val gson = GsonBuilder()
    .setLenient()
    .create()

object RetrofitClient {
    val instance: GitHubApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://github.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(GitHubApi::class.java)
    }
}