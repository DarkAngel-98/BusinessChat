package com.example.businesscards.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.businesscards.R
import com.example.businesscards.adapters.BusinessCardAdapter
import com.example.businesscards.adapters.MessagesAdapter
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.constants.PreferenceClass
import com.example.businesscards.startup.MainActivity
import com.example.businesscards.databinding.FragmentBusinessBinding
import com.example.businesscards.interfaces.BasicListener
import com.example.businesscards.models.BusinessCardModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class BusinessFragment : Fragment(), BasicListener {

    private lateinit var binding: FragmentBusinessBinding
    var businessAdapter: BusinessCardAdapter? = null
    var cardList: ArrayList<BusinessCardModel>? = null
    var prefs: PreferenceClass? = null
    var firebaseUser: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
        prefs = PreferenceClass(requireActivity())
        firebaseUser = FirebaseAuth.getInstance().currentUser
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_business, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { (activity as MainActivity).hideProgress()}

        binding.rvAllBusinessCards.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        showBusinessCards()

    }
    private fun showBusinessCards() {
        cardList = ArrayList()

        var databaseReference =
            FirebaseDatabase.getInstance().getReference(HeartSingleton.FireCardsDB)
        databaseReference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                cardList?.clear()
                for (cardData in snapshot.children) {
                    var businessCard = cardData.getValue(BusinessCardModel::class.java) as BusinessCardModel
                        if (businessCard.receiverId.equals(firebaseUser?.uid)){
                            cardList?.add(businessCard)
                        }
                }
                businessAdapter =
                    BusinessCardAdapter(cardList!!, R.layout.business_card_row_layout)
                businessAdapter?.notifyItemRangeChanged(0, cardList!!.size-1)
                binding.rvAllBusinessCards.adapter = businessAdapter
                binding.rvAllBusinessCards.scrollToPosition(binding.rvAllBusinessCards.adapter!!.itemCount - 1)
                onStopped()
            }

            override fun onCancelled(error: DatabaseError) {
                // not needed here
            }

        })
    }

    override fun onStarted() {
        activity?.let { (activity as MainActivity).showProgress() }
    }

    override fun onStopped() {
        activity?.let { (activity as MainActivity).hideProgress() }
    }
}