package com.example.businesscards.startup.register

import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.constants.PreferenceClass
import com.example.businesscards.databinding.ActivityRegisterBinding
import com.example.businesscards.interfaces.BasicListener
import com.example.businesscards.startup.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask

class RegisterActivity : AppCompatActivity(), BasicListener {
    private lateinit var binding: ActivityRegisterBinding
    private var prefs: PreferenceClass? = null
    private lateinit var auth: FirebaseAuth
    lateinit var firebaseReference: DatabaseReference
    private val IMAGE_REQUEST = 1
    private var newImageUrl: Uri? = null
    private var uploadTask: StorageTask<UploadTask.TaskSnapshot>? = null

    var firebaseUser: FirebaseUser? = null
    var databaseReference: DatabaseReference? = null
    var storageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)
        hideActionBarTitle()
        auth = FirebaseAuth.getInstance()
        setupListeners()
        prefs = PreferenceClass(this)
        storageReference =
            FirebaseStorage.getInstance().getReference(HeartSingleton.FireUploadsDB)
    }
    private fun setupListeners(){

        binding.registerButton.setOnClickListener {
            onStarted()
            var fName: String = binding.registerFirstName.text.toString()
            var lName: String = binding.registerLastName.text.toString()
            var mobPhone: String = binding.registerMobilePhone.text.toString()
            var compName: String = binding.registerCompanyName.text.toString()
            var homeAddress: String? = binding.registerHomeAddress.text.toString()
            var user: String = binding.registerUsername.text.toString()
            var mail: String = binding.registerEmail.text.toString()
            var pass: String = binding.registerPassword.text.toString()

            if(fName.isEmpty() || lName.isEmpty() || user.isEmpty() || mail.isEmpty() || pass.isEmpty() || mobPhone.isEmpty() || compName.isEmpty()){
                showAlertDialog(HeartSingleton.AlertDialogTitle)
            }
            else {
                registerUser(fName, lName, user, mail, pass,mobPhone, compName, homeAddress)
            }
        }
    }


    private fun showAlertDialog(title: String){
        val alertDialog= AlertDialog.Builder(this)
        alertDialog.setTitle(title)
        alertDialog.setNegativeButton("OK", DialogInterface.OnClickListener { dialogInterface, _ ->
            dialogInterface.cancel()
            onStopped()
        })
        alertDialog.create()
        alertDialog.show()
    }

    private fun registerUser(firstName: String, lastName: String,
                             username: String, email: String, password: String,
                             mobilePhone: String, companyName: String,
                             homeAddress: String?){

        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                var firebaseUser: FirebaseUser? = auth.currentUser
                var userId: String = firebaseUser?.uid!!
                firebaseReference =
                    FirebaseDatabase.getInstance()
                        .getReference(HeartSingleton.FireUsersDB)
                        .child(userId)
                Log.d("TAG", userId)
                var hashMap: HashMap<String, String> = HashMap()
                hashMap[HeartSingleton.FireId] = userId
                hashMap[HeartSingleton.FireFirstName] = firstName
                hashMap[HeartSingleton.FireLastName] = lastName
                hashMap[HeartSingleton.FireEmail] = email
                hashMap[HeartSingleton.FireUsername] = username
                hashMap[HeartSingleton.FirePassword] = password
                hashMap[HeartSingleton.FireMobilePhone] = mobilePhone
                hashMap[HeartSingleton.FireCompanyName] = companyName
                if(homeAddress?.isNotEmpty()!!)
                    hashMap[HeartSingleton.FireHomeAddress] = homeAddress
                hashMap[HeartSingleton.FireImageUrl] = HeartSingleton.FireDefault

                firebaseReference.setValue(hashMap).addOnCompleteListener { task1 ->
                    if(task1.isSuccessful){
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra(HeartSingleton.IntentFlag, HeartSingleton.IntentRegister)

                        // save the important data into sharedPrefs
                        prefs?.setUserLoggedIn(true)
                        prefs?.saveUserId(userId)
                        prefs?.saveFirstName(firstName)
                        prefs?.saveLastName(lastName)
                        prefs?.saveUsername(username)
                        prefs?.saveUserEmail(email)
                        prefs?.savePassword(password)
                        prefs?.saveMobilePhone(mobilePhone)
                        prefs?.saveCompanyName(companyName)
                        if(!homeAddress.isNullOrEmpty())
                            prefs?.saveHomeAddress(homeAddress)

                        onStopped()
                        startActivity(intent)
                        finish()
                    }
                }
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "Failure", Toast.LENGTH_SHORT).show()
                onStopped()
            }
        }
    }
    private fun hideActionBarTitle(){
        supportActionBar?.hide()
    }

    override fun onStarted() {
        this.binding.registerProgressBar.visibility = View.VISIBLE
    }

    override fun onStopped() {
        this.binding.registerProgressBar.visibility = View.GONE
    }

}