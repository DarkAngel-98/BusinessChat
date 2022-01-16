package com.example.businesscards.chat

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.businesscards.R
import com.example.businesscards.StartActivity
import com.example.businesscards.adapters.UsersAdapter
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.constants.PreferenceClass
import com.example.businesscards.databinding.FragmentUsersBinding
import com.example.businesscards.interfaces.BasicListener
import com.example.businesscards.interfaces.UserListener
import com.example.businesscards.models.UserInfo
import com.example.businesscards.startup.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class UsersFragment : Fragment(), BasicListener, UserListener {
    private lateinit var binding: FragmentUsersBinding
    private var communicationFragment: CommunicationFragment = CommunicationFragment()
    private var businessCardFragment: MyBusinessCardBottomSheetFragment = MyBusinessCardBottomSheetFragment()
    private var usersAdapter: UsersAdapter? = null
    private var prefs: PreferenceClass? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {

        }
        prefs = PreferenceClass(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_users, container, false)
        binding.lifecycleOwner = this
        onStarted()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvAllUsers.layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
        getAllUsers()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getAllUsers(){
        var usersList: ArrayList<UserInfo> = ArrayList()
        usersAdapter = UsersAdapter(usersList, R.layout.user_row_layout, this)
        var firebaseUser = FirebaseAuth.getInstance().currentUser
        var databaseReference =
            FirebaseDatabase
                .getInstance()
                .getReference(HeartSingleton.FireUsersDB)
        databaseReference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                usersList.clear()
                for(dataRow in snapshot.children){
                    var chatInfo: UserInfo = dataRow.getValue(UserInfo::class.java) as UserInfo

                    if(!chatInfo.id.equals(firebaseUser?.uid)){
                        usersList.add(chatInfo)
                    }
                }
                binding.rvAllUsers.apply {

                    adapter = usersAdapter
                    usersAdapter?.notifyDataSetChanged()
                }
                onStopped()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun onStarted() {
        activity?.let { (activity as MainActivity).showProgress() }
    }

    override fun onStopped() {
        activity?.let { (activity as MainActivity).hideProgress() }
    }
    private fun openCommunicationFragment(user: UserInfo?){
        val bundle = bundleOf(HeartSingleton.BundleChatChat to user)
        communicationFragment.arguments = bundle
        activity?.let { (activity as MainActivity)
            .setNavigationPanelSelectedTab(R.id.navigate_chat)
        }
        activity?.let { (activity as MainActivity).supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_frame_layout, communicationFragment)
            .commit()
        }
    }

    private fun openBusinessCardFragment(user: UserInfo){
        val bundle = bundleOf(HeartSingleton.BundleBusinessCard to user)
        businessCardFragment.arguments = bundle
        activity?.let { (activity as MainActivity)
            .setNavigationPanelSelectedTab(R.id.navigate_business_cards)
        }
        activity?.let { (activity as MainActivity).supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_frame_layout, businessCardFragment)
            .commit()
        }
    }

    private fun openMyCard(){
        var currentUser = UserInfo()

        currentUser.firstName = prefs?.getFirstName()
        currentUser.lastName = prefs?.getLastName()
        currentUser.imageURL = prefs?.getImageUrl()
        currentUser.companyName = prefs?.getCompanyName()
        currentUser.email = prefs?.getUserEmail()
        currentUser.mobilePhone = prefs?.getMobilePhone()

        val bundle = bundleOf(HeartSingleton.BundleBusinessCard to currentUser)
        businessCardFragment.arguments = bundle
        MyBusinessCardBottomSheetFragment.showReportComment(currentUser, requireActivity())
    }

    private fun showAlertDialog(title: String, user: UserInfo) {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle(title)
        alertDialog.setPositiveButton("Card") { _, _ ->
            Handler(Looper.getMainLooper()).postDelayed({openMyCard()},500)
        }

        alertDialog.setNegativeButton("Chat") { _, _ ->
            Handler(Looper.getMainLooper()).postDelayed({openCommunicationFragment(user)},500)
        }

        alertDialog.create()
        alertDialog.show()
    }
    override fun onUserClicked(user: UserInfo) {
        showAlertDialog(HeartSingleton.AlertDialogCardOrChat, user)
    }
}