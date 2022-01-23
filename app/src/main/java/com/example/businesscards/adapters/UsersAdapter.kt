package com.example.businesscards.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.businesscards.R
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.databinding.UserRowLayoutBinding
import com.example.businesscards.interfaces.UserListener
import com.example.businesscards.models.UserInfo
import com.squareup.picasso.Picasso

class UsersAdapter(
    private val users: ArrayList<UserInfo>,
    private val layoutID:Int,
    private val userListener: UserListener
): RecyclerView.Adapter<UsersAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: UserRowLayoutBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: UserInfo){
            binding.userItem = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val userItemBinding = UserRowLayoutBinding.inflate(inflater, parent, false)
        return MyViewHolder(userItemBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = users[position]
        user.let {
            holder.bind(it)
            holder.binding.usersUsername.text = users[position].username!!
            holder.binding.usersFirstName.text = users[position].firstName!!
            holder.binding.usersLastName.text = users[position].lastName!!
            Log.d("CHAT_USER_IMAGE_URL", user.imageURL.toString())
            if(!users[position].imageURL.isNullOrEmpty() && !(users[position].imageURL.equals(HeartSingleton.FireDefault)))
                Picasso.get().load(users[position].imageURL).into(holder.binding.usersProfilePic)
            else if(users[position].imageURL.equals(HeartSingleton.FireDefault))
                holder.binding.usersProfilePic.setImageResource(R.drawable.ic_default_profile_picture)

            if(users[position].status == 0)
                holder.binding.usersStatus.setImageResource(R.color.gray_status_offline)
            else if(users[position].status == 1)
                holder.binding.usersStatus.setImageResource(R.color.green_status_online)

            holder.binding.userRowRootLayout.setOnClickListener {
                userListener.onUserClicked(user)
            }
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}