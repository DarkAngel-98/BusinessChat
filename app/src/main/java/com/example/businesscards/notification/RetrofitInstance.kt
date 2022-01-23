package com.example.businesscards.notification

import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.interfaces.APIService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {

    companion object{
        private val retrofit by lazy {
            Retrofit
                .Builder()
                .baseUrl(HeartSingleton.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val api by lazy {
            retrofit.create(APIService::class.java)
        }
    }
}