package com.example.businesscards.interfaces

import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.models.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIService {

    @Headers("Authorization: key=${HeartSingleton.SERVER_KEY}",
    "Content-Type: ${HeartSingleton.CONTENT_TYPE}")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}