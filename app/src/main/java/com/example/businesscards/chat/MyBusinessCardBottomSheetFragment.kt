package com.example.businesscards.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import com.example.businesscards.R
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.constants.PreferenceClass
import com.example.businesscards.databinding.FragmentMyBusinessCardBottomSheetBinding
import com.example.businesscards.interfaces.BasicListener
import com.example.businesscards.models.BusinessCardModel
import com.example.businesscards.models.NotificationData
import com.example.businesscards.models.PushNotification
import com.example.businesscards.models.UserInfo
import com.example.businesscards.notification.MyFirebaseMessagingService
import com.example.businesscards.startup.MainActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import yuku.ambilwarna.AmbilWarnaDialog
import java.lang.reflect.Type


class MyBusinessCardBottomSheetFragment : BottomSheetDialogFragment(), BasicListener {

    private lateinit var binding: FragmentMyBusinessCardBottomSheetBinding
    private var user: UserInfo? = null
    private var card: BusinessCardModel = BusinessCardModel()
    private var prefs: PreferenceClass? = null
    private lateinit var auth: FirebaseAuth
    var firebaseUser: FirebaseUser? = null
    lateinit var databaseReference: DatabaseReference

    companion object{
        private const val TAG = "businessCardBottomSheetFragmentTag"
        var defaultColor = R.color.orange

        fun showBusinessCard(user: UserInfo, requireActivity: FragmentActivity) =
            MyBusinessCardBottomSheetFragment().apply {
            arguments = bundleOf(HeartSingleton.BundleBusinessCard to user)
            }.show(requireActivity.supportFragmentManager, TAG)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PreferenceClass(requireActivity())
        auth = FirebaseAuth.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_my_business_card_bottom_sheet, container, false)
        binding.lifecycleOwner = this

        arguments?.let {
            user = arguments?.get(HeartSingleton.BundleBusinessCard) as UserInfo
        }
        firebaseUser = auth.currentUser

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.behavior?.apply {
            isFitToContents = false
            state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }

        binding.myBusinessCard.setBackgroundColor(defaultColor)
        binding.myCardSendBtn.setBackgroundColor(defaultColor)

        card.cardImageUrl = prefs?.getImageUrl()
        card.cardFullName = prefs?.getFirstName() + " " + prefs?.getLastName()
        card.cardCompanyName = prefs?.getCompanyName()
        card.cardEmail = prefs?.getUserEmail()
        card.mobilePhone = prefs?.getMobilePhone()

        card.jobPosition = prefs?.getJobPosition()
        card.yearsOfExperience = prefs?.getYearsOfExperience()
        card.interests = prefs?.getInterests()
        card.linkedInProfile = prefs?.getLinkedinProfile()

        binding.myCardFullName.text = prefs?.getFirstName() + "\n" + prefs?.getLastName()
        binding.myCardCompanyName.setText(prefs?.getCompanyName())
        binding.myCardEmail.setText(prefs?.getUserEmail())
        binding.myCardMobilePhone.setText(prefs?.getMobilePhone())

        binding.myCardJobPosition.setText(prefs?.getJobPosition())
        binding.myCardYearsOfExperience.setText(prefs?.getYearsOfExperience().toString())
        binding.myCardInterests.setText(prefs?.getInterests())
        binding.myCardLinkedInProfile.setText(prefs?.getLinkedinProfile())

        if(prefs?.getImageUrl() != null)
            Picasso.get().load(prefs?.getImageUrl()).into(binding.myCardProfilePicture)

        setupListeners()

    }

    private fun setupListeners(){
        binding.myCardEditDoneBtn.setOnClickListener {
            if(binding.myCardEditDoneBtnText.text.equals("edit")) {
                binding.myCardEditDoneBtnText.text = "done"
                editBasicInfoEnabled()
                binding.myCardSendBtn.visibility = View.GONE
                binding.additionalInfo.visibility = View.VISIBLE
            }
            else if(binding.myCardEditDoneBtnText.text.equals("done")){
                var cName = binding.myCardCompanyName.text.toString()
                var cMail = binding.myCardEmail.text.toString()
                var cPhone = binding.myCardMobilePhone.text.toString()

                var cJobPosition = binding.myCardJobPosition.text.toString()
                var cYearsOfExperience = binding.myCardYearsOfExperience.text.toString()
                var cInterests = binding.myCardInterests.text.toString()
                var cLinkedin = binding.myCardLinkedInProfile.text.toString()

                card.jobPosition = cJobPosition
                card.yearsOfExperience = cYearsOfExperience.toInt()
                card.interests = cInterests
                card.linkedInProfile = cLinkedin

                if(cName.isNotEmpty() && cMail.isNotEmpty() && cPhone.isNotEmpty()){
                    binding.myCardEditDoneBtnText.text = "edit"
                    editBasicInfoDisabled()
                    binding.myCardSendBtn.visibility = View.VISIBLE
                    binding.additionalInfo.visibility = View.GONE

                    prefs?.saveCompanyName(cName)
                    prefs?.saveUserEmail(cMail)
                    prefs?.saveMobilePhone(cPhone)
                    prefs?.saveJobPosition(cJobPosition)
                    prefs?.saveYearsOfExperience(cYearsOfExperience.toInt())
                    prefs?.saveInterests(cInterests)
                    prefs?.saveLinkedinProfile(cLinkedin)

                    updateUser(cName, cMail, cPhone)

                }
                else{
                    showAlertDialog(HeartSingleton.AlertDialogTitle)
                }
            }
        }
        binding.myCardSendBtn.setOnClickListener {
            var senderId = prefs?.getUserId()!!
            var receiverId = user?.id!!
            var toWhomISentCard: ArrayList<String>? = retrieveReceiversIds()

            if (toWhomISentCard != null) {
                for(everySingleReceiverId in toWhomISentCard)
                    if(everySingleReceiverId == receiverId){
                        showDuplicateCardsDialog(HeartSingleton.AlertDialogAlreadySentCard, senderId, receiverId, card)
                        break
                    }
            }
            else{
                sendBusinessCard(senderId, receiverId, card)
            }
        }
        binding.colorPickerBtn.setOnClickListener {
            openColorPicker()
        }
    }

    private fun showDuplicateCardsDialog(title: String, senderId: String, receiverId: String, card: BusinessCardModel){
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle(title)
        alertDialog.setPositiveButton("Send Again") { _, _ ->
            sendBusinessCard(senderId, receiverId, card)
        }
        alertDialog.setNegativeButton("Dismiss") { dialogInterface, _ ->
            dialogInterface.cancel()
        }
        alertDialog.create()
        alertDialog.show()
    }

    private fun editBasicInfoEnabled(){

        binding.myCardCompanyName.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_edit, 0)
        binding.myCardEmail.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_edit, 0)
        binding.myCardMobilePhone.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_edit, 0)
        binding.myCardJobPosition.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_edit, 0)
        binding.myCardYearsOfExperience.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_edit, 0)
        binding.myCardInterests.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_edit, 0)
        binding.myCardLinkedInProfile.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_edit, 0)

        binding.myCardCompanyName.isFocusableInTouchMode = true
        binding.myCardEmail.isFocusableInTouchMode = true
        binding.myCardMobilePhone.isFocusableInTouchMode = true
        binding.myCardJobPosition.isFocusableInTouchMode = true
        binding.myCardYearsOfExperience.isFocusableInTouchMode = true
        binding.myCardInterests.isFocusableInTouchMode = true
        binding.myCardLinkedInProfile.isFocusableInTouchMode = true

        binding.myCardCompanyName.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.myCardEmail.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.myCardMobilePhone.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.myCardJobPosition.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.myCardYearsOfExperience.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.myCardInterests.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.myCardLinkedInProfile.imeOptions = EditorInfo.IME_ACTION_DONE
    }

    private fun editBasicInfoDisabled(){
        binding.myCardCompanyName.setCompoundDrawablesWithIntrinsicBounds(0,0,0, 0)
        binding.myCardEmail.setCompoundDrawablesWithIntrinsicBounds(0,0,0, 0)
        binding.myCardMobilePhone.setCompoundDrawablesWithIntrinsicBounds(0,0,0, 0)
        binding.myCardJobPosition.setCompoundDrawablesWithIntrinsicBounds(0,0,0, 0)
        binding.myCardYearsOfExperience.setCompoundDrawablesWithIntrinsicBounds(0,0,0, 0)
        binding.myCardInterests.setCompoundDrawablesWithIntrinsicBounds(0,0,0, 0)
        binding.myCardLinkedInProfile.setCompoundDrawablesWithIntrinsicBounds(0,0,0, 0)

        binding.myCardCompanyName.isFocusableInTouchMode = false
        binding.myCardEmail.isFocusableInTouchMode = false
        binding.myCardMobilePhone.isFocusableInTouchMode = false
        binding.myCardJobPosition.isFocusableInTouchMode = false
        binding.myCardYearsOfExperience.isFocusableInTouchMode = false
        binding.myCardInterests.isFocusableInTouchMode = false
        binding.myCardLinkedInProfile.isFocusableInTouchMode = false

        binding.myCardCompanyName.clearFocus()
        binding.myCardEmail.clearFocus()
        binding.myCardMobilePhone.clearFocus()
        binding.myCardJobPosition.clearFocus()
        binding.myCardYearsOfExperience.clearFocus()
        binding.myCardInterests.clearFocus()
        binding.myCardLinkedInProfile.clearFocus()
    }

    private fun updateUser(compName: String, mail: String, phone: String){
        var currentUserId = firebaseUser?.uid!!

            FirebaseDatabase.getInstance()
                .getReference(HeartSingleton.FireUsersDB)
                .child(currentUserId).child(HeartSingleton.FireCompanyName).setValue(compName)

        FirebaseDatabase.getInstance()
            .getReference(HeartSingleton.FireUsersDB)
            .child(currentUserId).child(HeartSingleton.FireEmail).setValue(mail)

        FirebaseDatabase.getInstance()
            .getReference(HeartSingleton.FireUsersDB)
            .child(currentUserId).child(HeartSingleton.FireMobilePhone).setValue(phone)
    }

    private fun showAlertDialog(title: String){
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle(title)
        alertDialog.setPositiveButton("Ok") { dialogInterface, _ ->
            dialogInterface.cancel()
        }
        alertDialog.create()
        alertDialog.show()
    }

    private fun sendBusinessCard(senderId: String, receiverId: String, card: BusinessCardModel) {
        databaseReference = FirebaseDatabase.getInstance().reference

        var hashMap: HashMap<String, Any> = HashMap()

        hashMap[HeartSingleton.FireSenderId] = senderId
        hashMap[HeartSingleton.FireReceiverId] = receiverId

        hashMap[HeartSingleton.FireCardImageUrl] = card.cardImageUrl!!
        hashMap[HeartSingleton.FireCardFullName] = card.cardFullName!!
        hashMap[HeartSingleton.FireCardCompanyName] = card.cardCompanyName!!
        hashMap[HeartSingleton.FireCardEmail] = card.cardEmail!!
        hashMap[HeartSingleton.FireCardMobilePhone] = card.mobilePhone!!

        hashMap[HeartSingleton.FireCardJobPosition] = card.jobPosition!!
        hashMap[HeartSingleton.FireCardYearsOfExperience] = card.yearsOfExperience!!
        hashMap[HeartSingleton.FireCardInterests] = card.interests!!
        hashMap[HeartSingleton.FireCardLinkedProfile] = card.linkedInProfile!!
        hashMap[HeartSingleton.FireCardColor] = defaultColor

        saveReceiverIdForCard(receiverId)

        databaseReference.child(HeartSingleton.FireCardsDB).push().setValue(hashMap)

        var cardReference: DatabaseReference =
            FirebaseDatabase
                .getInstance()
                .getReference(HeartSingleton.FireCardIdDB)
                .child(firebaseUser?.uid!!)
                .child(user?.id!!)

        var title = "New Business Card"
        PushNotification(
            NotificationData(
                prefs?.getUsername(), "", title
            ), user?.token!!
        ).also {
            //MyFirebaseMessagingService().whereToNavigate = HeartSingleton.IntentToBusinessCard
            (activity as MainActivity).sendNotifications(it)
        }

        cardReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()){
                    cardReference.child(HeartSingleton.FireId).setValue(user?.id)
                }
                onStopped()
            }

            override fun onCancelled(error: DatabaseError) {
                onStopped()
            }

        })
        dialog?.dismiss()
    }

    private fun saveReceiverIdForCard(receiverId: String){
        val gson = Gson()
        var retrievedCardIds: ArrayList<String>? = retrieveReceiversIds()
        if(retrievedCardIds.isNullOrEmpty()){
            retrievedCardIds = ArrayList()
            retrievedCardIds.add(receiverId)
        }
        else {
            retrievedCardIds.add(receiverId)
        }
        val cardsIntoString = gson.toJson(retrievedCardIds)
        prefs?.saveToWhomYouSentBusinessCard(cardsIntoString)
        onStopped()
    }

    private fun retrieveReceiversIds(): ArrayList<String>?{
        val retrievedBusinessCardIds = prefs?.getListToWhomYouSentBusinessCard()
        var retrievedCardIds: ArrayList<String>? = ArrayList()
        val gson = Gson()
        val type: Type = object: TypeToken<ArrayList<String>>() {}.type
        retrievedCardIds = Gson().fromJson(retrievedBusinessCardIds, type)

        return retrievedCardIds
    }

    private fun openColorPicker(){

        AmbilWarnaDialog(requireContext(), defaultColor, object: AmbilWarnaDialog.OnAmbilWarnaListener{
            override fun onCancel(dialog: AmbilWarnaDialog?) {
                // Not needed
            }
            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                defaultColor = color
                binding.myBusinessCard.setBackgroundColor(color)
                binding.myCardSendBtn.setBackgroundColor(color)
            }
        }).show()
    }

    override fun onStarted() {
        activity?.let { (activity as MainActivity).showProgress() }
    }

    override fun onStopped() {
        activity?.let { (activity as MainActivity).hideProgress() }
    }

}