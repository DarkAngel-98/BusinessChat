package com.example.businesscards.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.businesscards.R
import com.example.businesscards.adapters.MessagesAdapter.ViewHolder
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.models.Chat
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MessagesAdapter(
    private val chats: ArrayList<Chat>,
    private val layoutLeftID: Int,
    private val layoutRightID: Int,
    private val currentUserId: String?,
    private val currentUserImageUrl: String?,
    private val remoteUserImageUrl: String?
) : RecyclerView.Adapter<ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == layoutRightID) {
            var view: View =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_right_item_layout, parent, false)
            ViewHolder(view)
        } else {
            var view: View =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_left_item_layout, parent, false)
            ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var chat: Chat = chats[position]
        holder.messageText?.text = chat.message

//        if (getItemViewType(position) == R.layout.chat_right_item_layout) {
//            if (currentUserImageUrl.isNullOrEmpty() || currentUserImageUrl == HeartSingleton.FireDefault)
//                holder.image?.setImageResource(R.drawable.ic_default_profile_picture)
//            else {
//                Picasso.get().load(currentUserImageUrl).into(holder.image)
//            }
//        }
//        if (getItemViewType(position) == R.layout.chat_left_item_layout) {
//            if (remoteUserImageUrl.isNullOrEmpty() || remoteUserImageUrl == HeartSingleton.FireDefault)
//                holder.image?.setImageResource(R.drawable.ic_default_profile_picture)
//            else {
//                Picasso.get().load(remoteUserImageUrl).into(holder.image)
//            }
//        }
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        var messageText: TextView? = itemView.findViewById(R.id.chat_message)
//        var image: CircleImageView? = itemView.findViewById(R.id.chat_profile_image)


    }

    override fun getItemViewType(position: Int): Int {
        return if (chats[position].senderId.equals(currentUserId)) {
            layoutRightID
        } else {
            layoutLeftID
        }
    }
}