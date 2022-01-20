package com.example.businesscards.notification

import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.models.TokenModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseIdService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        var firebaseUser = FirebaseAuth.getInstance().currentUser
        var refreshToken = FirebaseMessaging.getInstance().token

        if(firebaseUser != null){
            updateToken(refreshToken.toString())
        }
    }

    private fun updateToken(refreshToken: String) {
        var firebaseUser = FirebaseAuth.getInstance().currentUser
        var databaseReference = FirebaseDatabase.getInstance().getReference(HeartSingleton.FireTokenDB)
        var token: TokenModel = TokenModel(refreshToken)
        databaseReference.child(firebaseUser!!.uid).setValue(token)
    }
}