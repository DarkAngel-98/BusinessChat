package com.example.businesscards.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.businesscards.R
import com.example.businesscards.databinding.AccountInfoRowBinding
import com.example.businesscards.models.AccountInfoModels

class AccountInfoAdapter(
    private val titles: List<AccountInfoModels>,
    private val layoutID:Int
): RecyclerView.Adapter<AccountInfoAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: AccountInfoRowBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: AccountInfoModels){
            binding.accountItem = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val accountItemBinding = AccountInfoRowBinding.inflate(inflater, parent, false)
        return MyViewHolder(accountItemBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(titles[position])
        if(titles[position].basicInfoValues.toString().isEmpty()){
            holder.binding.accountInfoImage.setBackgroundResource(R.drawable.ic_close)
        }
        else if(titles[position].basicInfoValues.toString().isNotEmpty())
            holder.binding.accountInfoImage.setBackgroundResource(R.drawable.ic_check)
    }

    override fun getItemCount(): Int {
        return titles.size
    }
}