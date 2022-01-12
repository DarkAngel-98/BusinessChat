package com.example.businesscards.startup

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.constants.PreferenceClass
import com.example.businesscards.databinding.ActivityLoginBinding
import com.example.businesscards.interfaces.BasicListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class LoginActivity : AppCompatActivity(), BasicListener {
    private lateinit var binding: ActivityLoginBinding
    private var prefs: PreferenceClass? = null
    private lateinit var auth: FirebaseAuth
    lateinit var firebaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideActionBarTitle()
        auth = FirebaseAuth.getInstance()
        prefs = PreferenceClass(this)
        setupListeners()
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            var mail = binding.loginEmail.text.toString()
            var password = binding.loginPassword.text.toString()

            if (mail.isEmpty() || password.isEmpty())
                showAlertDialog()
            else {
                onStarted()
                auth.signInWithEmailAndPassword(mail, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        prefs?.setUserLoggedIn(true)
                        prefs?.saveUserEmail(mail)
                        prefs?.savePassword(password)

                        Toast.makeText(this, "User exists", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra(HeartSingleton.IntentFlag, HeartSingleton.IntentLogin)
                        onStopped()
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Failure", Toast.LENGTH_SHORT).show()
                        onStopped()
                    }
                }
            }

        }
    }

    private fun hideActionBarTitle() {
        supportActionBar?.hide()
    }

    private fun showAlertDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(HeartSingleton.AlertDialogTitle)
        alertDialog.setNegativeButton(
            "OK",
            DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.cancel()
            })
        alertDialog.create()
        alertDialog.show()
    }

    override fun onStarted() {
        this.binding.loginProgressBar.visibility = View.VISIBLE
    }

    override fun onStopped() {
        this.binding.loginProgressBar.visibility = View.GONE
    }
}