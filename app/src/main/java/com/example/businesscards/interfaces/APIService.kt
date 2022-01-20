package com.example.businesscards.interfaces

import com.example.businesscards.models.NotificationMyResponse
import com.example.businesscards.models.NotificationSender
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAqYpG56s:APA91bHIU4rlvdym9FcPsESDT_q01iEsICId0yEFdlp8Q8trpiQhUSTH2H-u2ImYxPieGODwWe8wsrBYnzEFUC0_Nh_ug7dV2simE-94k17BXfm6Z01aOatvs-bSJwKW0bS5fzIy5guD"

    )
    @POST("fcm/send")
    fun sendNotification(
        @Body sender:NotificationSender
    ): Response<NotificationMyResponse>

}