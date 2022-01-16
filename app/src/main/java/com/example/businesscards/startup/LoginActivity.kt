package com.example.businesscards.startup

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.businesscards.R
import com.example.businesscards.adapters.AccountInfoAdapter
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.constants.PreferenceClass
import com.example.businesscards.databinding.ActivityLoginBinding
import com.example.businesscards.interfaces.BasicListener
import com.example.businesscards.models.AccountInfoModels
import com.example.businesscards.models.UserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class LoginActivity : AppCompatActivity(), BasicListener {
    private lateinit var binding: ActivityLoginBinding
    private var prefs: PreferenceClass? = null
    private lateinit var auth: FirebaseAuth
    lateinit var firebaseReference: DatabaseReference
    var firebaseUser: FirebaseUser? = null
    var userInfo: UserInfo? = null

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
                showAlertDialog(HeartSingleton.AlertDialogTitle)
            else {
                onStarted()
                auth.signInWithEmailAndPassword(mail, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        prefs?.setUserLoggedIn(true)
                        saveInfoToPrefs()

                        Toast.makeText(this, R.string.welcome_back, Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra(HeartSingleton.IntentFlag, HeartSingleton.IntentLogin)
                        onStopped()
                        startActivity(intent)
                        finish()
                    } else {
                        showAlertDialog(task.exception.toString())
                        onStopped()
                    }
                }
            }

        }
    }

    fun saveInfoToPrefs(){
        firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseReference = FirebaseDatabase
            .getInstance()
            .getReference(HeartSingleton.FireUsersDB)
            .child(firebaseUser!!.uid)

        firebaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                userInfo = snapshot.getValue(UserInfo::class.java) as UserInfo
                prefs?.saveUserId(userInfo?.id!!)
                prefs?.saveFirstName(userInfo?.firstName!!)
                prefs?.saveLastName(userInfo?.lastName!!)
                prefs?.saveUsername(userInfo?.username!!)
                prefs?.saveUserEmail(userInfo?.email!!)
                prefs?.saveMobilePhone(userInfo?.mobilePhone!!)
                prefs?.saveCompanyName(userInfo?.companyName!!)
                prefs?.saveHomeAddress(userInfo?.homeAddress!!)
                prefs?.saveImageUrl(userInfo?.imageURL!!)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    private fun hideActionBarTitle() {
        supportActionBar?.hide()
    }

    private fun showAlertDialog(title: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(title)
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