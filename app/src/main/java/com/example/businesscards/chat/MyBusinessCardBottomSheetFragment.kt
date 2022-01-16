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
import com.example.businesscards.models.BusinessCardModel
import com.example.businesscards.models.UserInfo
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso


class MyBusinessCardBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentMyBusinessCardBottomSheetBinding
    private var user: UserInfo? = null
    private var prefs: PreferenceClass? = null
    private lateinit var auth: FirebaseAuth
    var firebaseUser: FirebaseUser? = null
    lateinit var databaseReference: DatabaseReference

    companion object{
        private const val TAG = "businessCardBottomSheetFragmentTag"

        fun showReportComment(user: UserInfo, requireActivity: FragmentActivity) =
            MyBusinessCardBottomSheetFragment().apply {
            arguments = bundleOf(HeartSingleton.BundleBusinessCard to user)
            }.show(requireActivity.supportFragmentManager, TAG)


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            user = arguments?.get(HeartSingleton.BundleBusinessCard) as UserInfo
//        }
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
        binding.myCardFullName.text = user?.firstName + "\n" + user?.lastName
        binding.myCardCompanyName.setText(user?.companyName)
        binding.myCardEmail.setText(user?.email)
        binding.myCardMobilePhone.setText(user?.mobilePhone)

        if(user?.imageURL != null )
            Picasso.get().load(user?.imageURL).into(binding.myCardProfilePicture)

        setupListeners()

    }

    private fun setupListeners(){
        binding.myCardEditDoneBtn.setOnClickListener {
            if(binding.myCardEditDoneBtnText.text.equals("Edit")) {
                binding.myCardEditDoneBtnText.text = "Done"
                editBasicInfoEnabled()
                binding.myCardSendBtn.visibility = View.GONE
            }
            else if(binding.myCardEditDoneBtnText.text.equals("Done")){
                var cName = binding.myCardCompanyName.text.toString()
                var cMail = binding.myCardEmail.text.toString()
                var cPhone = binding.myCardMobilePhone.text.toString()
                if(cName.isNotEmpty() && cMail.isNotEmpty() && cPhone.isNotEmpty()){
                    binding.myCardEditDoneBtnText.text = "Edit"
                    editBasicInfoDisabled()
                    binding.myCardSendBtn.visibility = View.VISIBLE
                    updateDatabase(cName, cMail, cPhone)

                    prefs?.saveCompanyName(cName)
                    prefs?.saveUserEmail(cMail)
                    prefs?.saveMobilePhone(cPhone)
                }
                else{
                    showAlertDialog(HeartSingleton.AlertDialogTitle)
                }
            }
        }
        binding.myCardSendBtn.setOnClickListener {
            sendBusinessCard("", "", BusinessCardModel())
        }
    }

    private fun editBasicInfoEnabled(){
        binding.myCardCompanyName.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_edit, 0)
        binding.myCardEmail.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_edit, 0)
        binding.myCardMobilePhone.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_edit, 0)

        binding.myCardCompanyName.isFocusableInTouchMode = true
        binding.myCardEmail.isFocusableInTouchMode = true
        binding.myCardMobilePhone.isFocusableInTouchMode = true

        binding.myCardCompanyName.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.myCardEmail.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.myCardMobilePhone.imeOptions = EditorInfo.IME_ACTION_DONE
    }

    private fun editBasicInfoDisabled(){
        binding.myCardCompanyName.setCompoundDrawablesWithIntrinsicBounds(0,0,0, 0)
        binding.myCardEmail.setCompoundDrawablesWithIntrinsicBounds(0,0,0, 0)
        binding.myCardMobilePhone.setCompoundDrawablesWithIntrinsicBounds(0,0,0, 0)

        binding.myCardCompanyName.isFocusableInTouchMode = false
        binding.myCardEmail.isFocusableInTouchMode = false
        binding.myCardMobilePhone.isFocusableInTouchMode = false

        binding.myCardCompanyName.clearFocus()
        binding.myCardEmail.clearFocus()
        binding.myCardMobilePhone.clearFocus()
    }

    private fun updateDatabase(compName: String, mail: String, phone: String){
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
        hashMap[HeartSingleton.FireCardImageUrl] = card.imageUrl!!
        hashMap[HeartSingleton.FireCardFullName] = card.fullName!!
        hashMap[HeartSingleton.FireCardCompanyName] = card.companyName!!
        hashMap[HeartSingleton.FireCardEmail] = card.email!!
        hashMap[HeartSingleton.FireCardMobilePhone] = card.mobilePhone!!

        databaseReference.child(HeartSingleton.FireCardsDB).push().setValue(hashMap)

        var cardReference: DatabaseReference =
            FirebaseDatabase
                .getInstance()
                .getReference(HeartSingleton.FireChatIdDB)
                .child(firebaseUser?.uid!!)
                .child(user?.id!!)

        cardReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()){
                    cardReference.child(HeartSingleton.FireId).setValue(user?.id)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}