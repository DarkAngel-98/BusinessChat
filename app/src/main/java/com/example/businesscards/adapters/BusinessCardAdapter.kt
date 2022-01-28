package com.example.businesscards.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.businesscards.R
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.databinding.BusinessCardRowLayoutBinding
import com.example.businesscards.databinding.UserRowLayoutBinding
import com.example.businesscards.interfaces.UserListener
import com.example.businesscards.models.BusinessCardModel
import com.example.businesscards.models.UserInfo
import com.squareup.picasso.Picasso

class BusinessCardAdapter(
    private val cards: ArrayList<BusinessCardModel>,
    private val layoutID:Int
): RecyclerView.Adapter<BusinessCardAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: BusinessCardRowLayoutBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: BusinessCardModel){
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

            holder.binding.businessFullName.text = card.cardFullName
            holder.binding.businessCompanyName.text = card.cardCompanyName
            holder.binding.businessEmail.text = card.cardEmail
            holder.binding.businessPhone.text = card.mobilePhone

            holder.binding.businessJobPosition.text = card.jobPosition
            holder.binding.businessYearsOfExperience.text = card.yearsOfExperience.toString()
            holder.binding.businessInterests.text = card.interests
            holder.binding.businessLinkedInProfile.text = card.linkedInProfile

            if(card.cardImageUrl != null && card.cardImageUrl!!.isNotEmpty())
                Picasso.get().load(card.cardImageUrl).into(holder.binding.businessImage)
        }
    }

    override fun getItemCount(): Int {
        return cards.size
    }
}