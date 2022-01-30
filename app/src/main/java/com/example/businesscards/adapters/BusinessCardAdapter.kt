package com.example.businesscards.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.businesscards.databinding.BusinessCardRowLayoutBinding
import com.example.businesscards.interfaces.BusinessCardListener
import com.example.businesscards.models.BusinessCardModel
import com.squareup.picasso.Picasso

class BusinessCardAdapter(
    private val cards: ArrayList<BusinessCardModel>,
    private val layoutID: Int,
) : RecyclerView.Adapter<BusinessCardAdapter.MyViewHolder>() {

    private val TAG = "BusinessCardAdapter"
    private lateinit var businessListener: BusinessCardListener.OnLongClick
    private lateinit var linkedInListener: BusinessCardListener.OnLinkedInPressed

    inner class MyViewHolder(val binding: BusinessCardRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BusinessCardModel) {
            binding.cardItem = item
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val userItemBinding = BusinessCardRowLayoutBinding.inflate(inflater, parent, false)
        return MyViewHolder(userItemBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val card = cards[position]
        card.let {
            holder.bind(it)

            holder.binding.root.setOnLongClickListener {
                businessListener.cardLongPressed(card)
                return@setOnLongClickListener true
            }

            holder.binding.businessFullName.text = card.cardFullName
            holder.binding.businessCompanyName.text = card.cardCompanyName
            holder.binding.businessEmail.text = card.cardEmail
            holder.binding.businessPhone.text = card.mobilePhone

            holder.binding.businessJobPosition.text = card.jobPosition
            holder.binding.businessYearsOfExperience.text = card.yearsOfExperience.toString()
            holder.binding.businessInterests.text = card.interests
            holder.binding.businessLinkedInProfile.text = card.linkedInProfile

            if(holder.binding.businessLinkedInProfile.text.isNotEmpty()){

                holder.binding.businessLinkedInProfile.setOnClickListener {
                    linkedInListener.linkedInListener(holder.binding.businessLinkedInProfile.text.toString())

                }
            }

            if (card.cardImageUrl != null && card.cardImageUrl!!.isNotEmpty())
                Picasso.get().load(card.cardImageUrl).into(holder.binding.businessImage)
        }
    }

    fun setLongPressListener(longPressListener: BusinessCardListener.OnLongClick){
        businessListener = longPressListener
    }

    fun setLinkedInListener(linked: BusinessCardListener.OnLinkedInPressed){
        linkedInListener = linked
    }

    override fun getItemCount(): Int {
        return cards.size
    }
}