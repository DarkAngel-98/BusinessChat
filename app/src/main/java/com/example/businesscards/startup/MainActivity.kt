package com.example.businesscards.startup

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.businesscards.R
import com.example.businesscards.chat.ChatFragment
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.databinding.ActivityMainBinding
import com.example.businesscards.interfaces.BasicListener
import com.example.businesscards.models.PushNotification
import com.example.businesscards.notification.RetrofitInstance
import com.example.businesscards.notification.MyFirebaseMessagingService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), BasicListener {
    private lateinit var binding: ActivityMainBinding
    private var progressBar: ProgressBar? = null
    private var tmpIntent: String? = null
    private var navController: NavController? = null
    var firebaseUser: FirebaseUser? = null
    private lateinit var auth: FirebaseAuth
    private val TAG = "MAIN_ACTIVITY"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyFirebaseMessagingService.prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnSuccessListener {myNewToken ->
            if(myNewToken != null){
                MyFirebaseMessagingService.token = myNewToken
                updateToken(myNewToken)
            }
        }
        auth = FirebaseAuth.getInstance()
        firebaseUser = auth.currentUser

        tmpIntent = intent.getStringExtra(HeartSingleton.IntentFlag).toString()
        if (!tmpIntent.isNullOrEmpty())
            Log.d("KEY", tmpIntent!!)
        progressBar = binding.homeProgressBar
        hideActionBarTitle()
        FirebaseMessaging.getInstance().subscribeToTopic(HeartSingleton.TOPIC)
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHost.navController
        binding.mainBottomNavigationPanel.setupWithNavController(navController!!)
        setStatus(1)
    }

    fun sendNotifications(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d(TAG, "Response is successful!")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch (er: Exception) {
            Log.e(TAG, er.toString())
        }
    }

    private fun setStatus(status: Int){
        var currentUserId = firebaseUser?.uid!!

        FirebaseDatabase.getInstance()
            .getReference(HeartSingleton.FireUsersDB)
            .child(currentUserId).child(HeartSingleton.FireStatus).setValue(status)
    }

    private fun updateToken(token: String){
        var currentUserId = firebaseUser?.uid!!

        FirebaseDatabase.getInstance()
            .getReference(HeartSingleton.FireUsersDB)
            .child(currentUserId).child(HeartSingleton.FireToken).setValue(token)
    }
    private fun setHomeTitle(title: String?){
        supportActionBar?.title = title
    }
    private fun hideActionBarTitle(){
        supportActionBar?.hide()
    }

    fun showProgress(){
        progressBar?.visibility = View.VISIBLE
    }
    fun hideProgress(){
        progressBar?.visibility = View.GONE
    }
    private fun getCurrentFragment(): Fragment? {
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.primaryNavigationFragment?.let { return navFragment.childFragmentManager.primaryNavigationFragment }
        }
        return null
    }
    override fun onBackPressed() {
        if(getCurrentFragment() is ChatFragment){
            (getCurrentFragment() as ChatFragment).onBackPressed()
        }
        super.onBackPressed()
    }


    fun hideNavigationPanel(){
        binding.mainBottomNavigationPanel.visibility = View.GONE
    }

    fun showNavigationPanel(){
        binding.mainBottomNavigationPanel.visibility = View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        setStatus(0)
    }

    override fun onResume() {
        super.onResume()
        setStatus(1)
    }

    override fun onStarted() {
        progressBar?.visibility = View.VISIBLE
    }

    override fun onStopped() {
        progressBar?.visibility = View.GONE
    }


}