package com.example.businesscards.interfaces

import com.example.businesscards.models.UserInfo

interface UserListener {
    fun onUserClicked(user: UserInfo)

    fun interface OnUserLongClick {
        fun userLongPressed(user: UserInfo)
    }
}