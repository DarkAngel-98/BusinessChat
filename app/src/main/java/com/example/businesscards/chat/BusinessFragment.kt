package com.example.businesscards.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.example.businesscards.R
import com.example.businesscards.adapters.BusinessCardAdapter
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.constants.PreferenceClass
import com.example.businesscards.startup.MainActivity
import com.example.businesscards.databinding.FragmentBusinessBinding
import com.example.businesscards.interfaces.BasicListener
import com.example.businesscards.models.BusinessCardModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class BusinessFragment : Fragment(), BasicListener {

    private lateinit var binding: FragmentBusinessBinding
    var businessAdapter: BusinessCardAdapter? = null
    var cardList: ArrayList<BusinessCardModel>? = null
    var prefs: PreferenceClass? = null
    var firebaseUser: FirebaseUser? = null
    var snapHelpers: SnapHelper = PagerSnapHelper()
    private val TAG = "BusinessFragment"

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
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        snapHelpers.attachToRecyclerView(binding.rvAllBusinessCards)

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
                    // check again here on how to delete specific card.
                    cardData.key
                    Log.d(TAG, cardData.key.toString())
                    var businessCard = cardData.getValue(BusinessCardModel::class.java) as BusinessCardModel
                        if (businessCard.receiverId.equals(firebaseUser?.uid)){
                            cardList?.add(businessCard)
                        }
                }
                businessAdapter =
                    BusinessCardAdapter(cardList!!, R.layout.business_card_row_layout)
                businessAdapter?.notifyItemRangeChanged(0, cardList!!.size-1)
                binding.rvAllBusinessCards.adapter = businessAdapter

                onStopped()
                initListeners()
            }

            override fun onCancelled(error: DatabaseError) {
                // not needed here
            }

        })
    }

    private fun initListeners(){
        businessAdapter?.setLongPressListener{
            showAlertDialog(HeartSingleton.AlertDialogSave,HeartSingleton.AlertDialogDelete,it)
        }
        businessAdapter?.setLinkedInListener{
            Toast.makeText(requireContext(), "LinkedIn", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAlertDialog(positiveButton: String, negativeButton: String, card: BusinessCardModel) {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle(card.cardFullName)
        alertDialog.setPositiveButton(positiveButton) { _, _ ->
            onStarted()
            saveBusinessCard(card)
        }

        alertDialog.setNegativeButton(negativeButton) { _, _ ->

        }

        alertDialog.create()
        alertDialog.show()
    }

    private fun saveBusinessCard(card: BusinessCardModel){
        val gson = Gson()
        var retrievedCards: ArrayList<BusinessCardModel>? = retrieveBusinessCards()
        if(retrievedCards.isNullOrEmpty()){
            retrievedCards = ArrayList()
            retrievedCards.add(card)
        }
        else {
            retrievedCards.add(card)
        }
        val cardsIntoString = gson.toJson(retrievedCards)
        prefs?.saveImportantCards(cardsIntoString)
        onStopped()
    }

    private fun retrieveBusinessCards(): ArrayList<BusinessCardModel>?{
        val retrievedBusinessCardList = prefs?.getImportantCards()
        var retrievedCardList: ArrayList<BusinessCardModel>? = ArrayList()
        val gson = Gson()
        val type: Type = object: TypeToken<ArrayList<BusinessCardModel>>() {}.type
        retrievedCardList = Gson().fromJson(retrievedBusinessCardList, type)

        return retrievedCardList
    }

    override fun onStarted() {
        activity?.let { (activity as MainActivity).showProgress() }
    }

    override fun onStopped() {
        activity?.let { (activity as MainActivity).hideProgress() }
    }
}