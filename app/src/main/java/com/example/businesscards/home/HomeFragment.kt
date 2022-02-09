package com.example.businesscards.home

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.businesscards.R
import com.example.businesscards.SplashActivity
import com.example.businesscards.adapters.AccountInfoAdapter
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.constants.PreferenceClass
import com.example.businesscards.databinding.FragmentHomeBinding
import com.example.businesscards.interfaces.BasicListener
import com.example.businesscards.models.AccountInfoModels
import com.example.businesscards.models.UserInfo
import com.example.businesscards.startup.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso

class HomeFragment : Fragment(), BasicListener {
    private var prefs: PreferenceClass? = null
    private lateinit var binding: FragmentHomeBinding
    var accountInfoAdapter: AccountInfoAdapter? = null
    var tmpBundleValue: String? = null
    var userInfo: UserInfo? = null
    private val TAG = "HOME_FRAGMENT"

    var fName: String? = null
    var lName: String? = null
    var usersName: String? = null
    var email: String? = null
    var mobPhone: String? = null
    var compName: String? = null
    var homeAddress: String? = null
    var imageURL: String? = null

    var firebaseUser: FirebaseUser? = null

    var databaseReference: DatabaseReference? = null

    var storageReference: StorageReference? = null
    private val IMAGE_REQUEST = 1
    private var newImageUrl: Uri? = null
    private var uploadTask: StorageTask<UploadTask.TaskSnapshot>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tmpBundleValue = arguments?.get(HeartSingleton.IntentFlag) as String
        }
        //Log.d("BUNDLE", tmpBundleValue!!)

        prefs = PreferenceClass(requireActivity())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_home, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onStarted()
        binding.rvAccountInfo.adapter = accountInfoAdapter
        binding.rvAccountInfo.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false

        )

        if (tmpBundleValue.equals(HeartSingleton.IntentRegister)) {
            fName = prefs?.getFirstName()
            lName = prefs?.getLastName()
            usersName = prefs?.getUsername()
            email = prefs?.getUserEmail()
            mobPhone = prefs?.getMobilePhone()
            compName = prefs?.getCompanyName()
            homeAddress = prefs?.getHomeAddress()
            imageURL = prefs?.getImageUrl()
            binding.homeProfileUsername.text = usersName
            binding.homeProfileEmail.text = email
            if (!imageURL.isNullOrEmpty() && imageURL.equals(HeartSingleton.FireDefault))
                binding.homeProfileImage.setImageResource(R.drawable.ic_add_a_photo)
            else {
                val bitmapImage = Picasso.get().load(imageURL).get()
                val newBitmapImage =
                    Bitmap.createScaledBitmap(bitmapImage, 50, 50, false)
                binding.homeProfileImage.setImageBitmap(newBitmapImage)
            }

            var accountInfoTitleList = listOf(
                AccountInfoModels("First Name", fName),
                AccountInfoModels("Last Name", lName),
                AccountInfoModels("Mobile Phone", mobPhone),
                AccountInfoModels("Company Name", compName),
                AccountInfoModels("Home Address", homeAddress)
            )
            binding.rvAccountInfo.apply {
                adapter =
                    AccountInfoAdapter(accountInfoTitleList, R.layout.account_info_row)
            }
        } else {
            firebaseUser = FirebaseAuth.getInstance().currentUser
            databaseReference = FirebaseDatabase
                .getInstance()
                .getReference(HeartSingleton.FireUsersDB)
                .child(firebaseUser!!.uid)

            databaseReference?.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    userInfo = snapshot.getValue(UserInfo::class.java) as UserInfo
                    fName = userInfo?.firstName
                    lName = userInfo?.lastName
                    usersName = userInfo?.username
                    email = userInfo?.email
                    mobPhone = userInfo?.mobilePhone
                    compName = userInfo?.companyName
                    homeAddress = userInfo?.homeAddress
                    imageURL = userInfo?.imageURL
                    binding.homeProfileUsername.text = usersName
                    binding.homeProfileEmail.text = email
                    if (imageURL.isNullOrEmpty() || imageURL.equals(HeartSingleton.FireDefault))
                        binding.homeProfileImage.setImageResource(R.color.green_blue)
                    else
                        Picasso.get().load(imageURL).into(binding.homeProfileImage)

                    var accountInfoTitleList = listOf(
                        AccountInfoModels("First Name", fName),
                        AccountInfoModels("Last Name", lName),
                        AccountInfoModels("Mobile Phone", mobPhone),
                        AccountInfoModels("Company Name", compName),
                        AccountInfoModels("Home Address", homeAddress)
                    )
                    binding.rvAccountInfo.apply {
                        adapter =
                            AccountInfoAdapter(accountInfoTitleList, R.layout.account_info_row)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
            storageReference =
                FirebaseStorage.getInstance().getReference(HeartSingleton.FireUploadsDB)
        }
        accountInfoAdapter?.notifyDataSetChanged()
        onStopped()
        setupListeners()
    }

    private fun setupListeners() {
        binding.logoutButton.setOnClickListener {
            showAlertDialog()
        }
        binding.addPictureButton.setOnClickListener {
            openImages()
        }
        binding.favoriteCards.setOnClickListener {
            FavoriteBusinessCardsBottomSheetFragment.showFavoriteBusinessCards(requireActivity())
        }
    }

    private fun openImages() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, IMAGE_REQUEST)
    }

    private fun getFileExtension(uri: Uri): String {
        var contentResolver: ContentResolver = requireContext().contentResolver
        var mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))!!
    }

    private fun uploadImage() {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage(HeartSingleton.AlertDialogUploading)
        progressDialog.show()

        if (newImageUrl != null) {
            val fileReference: StorageReference = storageReference!!.child(
                System.currentTimeMillis().toString() + "." + getFileExtension(newImageUrl!!)
            )
            uploadTask = fileReference.putFile(newImageUrl!!)

                uploadTask!!.continueWithTask { task ->
                    if(!task.isSuccessful){
                        task.exception?.let {
                            throw it
                        }
                    }
                    fileReference.downloadUrl
                }.addOnCompleteListener{task ->
                    if(task.isSuccessful){
                        val downloadUri = task.result
                        val downloadUriString = downloadUri.toString()
                        prefs?.saveImageUrl(downloadUriString)
                        databaseReference = FirebaseDatabase
                            .getInstance()
                            .getReference(HeartSingleton.FireUsersDB)
                            .child(firebaseUser!!.uid)

                        var hashMap: HashMap<String, Any> = HashMap()
                        hashMap[HeartSingleton.FireImageUrl] = downloadUriString
                        databaseReference?.updateChildren(hashMap)

                        progressDialog.dismiss()
                    }
                    else{
                        progressDialog.dismiss()
                    }
                }.addOnFailureListener{task ->
                    Log.e(TAG, task.message.toString())
                    progressDialog.dismiss()
                }
        } else {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            newImageUrl = data.data
            if (uploadTask != null && uploadTask!!.isInProgress) {
                Toast.makeText(requireContext(), "Upload in progress", Toast.LENGTH_SHORT).show()
            } else
                uploadImage()
        }
    }

    private fun showAlertDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle(HeartSingleton.AlertDialogLogOut)
        alertDialog.setPositiveButton("YES") { _, _ ->
            prefs?.setUserLoggedIn(false)
            setStatus(0)
            val intent = Intent(requireContext(), SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            Handler(Looper.getMainLooper()).postDelayed({startActivity(intent)},500)
        }
        alertDialog.setNegativeButton("NO", DialogInterface.OnClickListener { dialogInterface, _ ->
            dialogInterface.cancel()
        })
        alertDialog.create()
        alertDialog.show()
    }

    override fun onStarted() {
        activity?.let { (activity as MainActivity).showProgress() }
    }

    override fun onStopped() {
        activity?.let { (activity as MainActivity).hideProgress() }
    }

    private fun setStatus(status: Int){
        var currentUserId = firebaseUser?.uid!!

        FirebaseDatabase.getInstance()
            .getReference(HeartSingleton.FireUsersDB)
            .child(currentUserId).child(HeartSingleton.FireStatus).setValue(status)
    }

}
