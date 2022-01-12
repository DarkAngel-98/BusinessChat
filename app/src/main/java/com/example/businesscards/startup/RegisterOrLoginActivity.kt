package com.example.businesscards.startup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.businesscards.R
import com.example.businesscards.databinding.ActivityRegisterOrLoginBinding

class RegisterOrLoginActivity : AppCompatActivity() {
    private lateinit var binding:ActivityRegisterOrLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterOrLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideActionBar()
        setupListeners()

    }
    private fun hideActionBar(){
        supportActionBar?.hide()
    }
    private fun setupListeners(){
        binding.registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        binding.loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

}