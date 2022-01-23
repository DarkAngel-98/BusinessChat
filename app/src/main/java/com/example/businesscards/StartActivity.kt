package com.example.businesscards

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import com.example.businesscards.constants.PreferenceClass
import com.example.businesscards.databinding.ActivityStartBinding
import com.example.businesscards.startup.MainActivity
import com.example.businesscards.startup.RegisterOrLoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class StartActivity() : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityStartBinding
    private var prefs: PreferenceClass? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showSplashLogo()
        hideActionBarTitle()
        auth = Firebase.auth
        prefs = PreferenceClass(this)

        Handler(Looper.getMainLooper()).postDelayed({
            showProgress()
            var tag = prefs?.getUserLoggedIn()
            if(tag == true){
                hideProgress()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this, RegisterOrLoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        },1500)

    }
    private fun showProgress(){
        binding.startActivityProgressBar.visibility = View.VISIBLE
    }

    private fun hideProgress(){
        binding.startActivityProgressBar.visibility = View.GONE
    }

    private fun showSplashLogo(){
        binding.splashLogoWrapper.visibility = View.VISIBLE
    }
    private fun hideActionBarTitle(){
        supportActionBar?.hide()
    }

}