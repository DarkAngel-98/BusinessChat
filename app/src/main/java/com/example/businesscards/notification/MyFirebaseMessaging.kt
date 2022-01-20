package com.example.businesscards.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.example.businesscards.chat.ChatFragment
import com.example.businesscards.chat.UsersFragment
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.startup.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessaging: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        var sentEd = remoteMessage.data[HeartSingleton.FireSentEd]
        var firebaseUser = FirebaseAuth.getInstance().currentUser

        if(firebaseUser != null && sentEd.equals(firebaseUser.uid)){
            sendNotifications(remoteMessage)
        }

    }

    private fun sendNotifications(remoteMessage: RemoteMessage) {
        var user = remoteMessage.data[HeartSingleton.NotificationUser]
        var icon = remoteMessage.data[HeartSingleton.NotificationIcon]
        var title = remoteMessage.data[HeartSingleton.NotificationTitle]
        var body = remoteMessage.data[HeartSingleton.NotificationBody]

        var notification = remoteMessage.notification
        var tmp: Int  = Integer.parseInt(user?.replace("[\\D]",""))
        val intent = Intent(this, ChatFragment::class.java)
        val bundle = bundleOf(HeartSingleton.BundleUserId to user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        var pendingIntent: PendingIntent = PendingIntent.getActivity(this, tmp, intent, PendingIntent.FLAG_ONE_SHOT)

        var defaultSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        var builder = NotificationCompat.Builder(this)
            .setSmallIcon(Integer.parseInt(icon))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)

        var notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var tmpCounter = 0

        if(tmp > 0) {
            tmpCounter = tmp
        }
        notificationManager.notify(tmpCounter, builder.build())
    }
}