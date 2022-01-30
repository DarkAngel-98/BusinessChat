package com.example.businesscards.interfaces

import com.example.businesscards.models.BusinessCardModel

interface BusinessCardListener {

    fun interface OnLongClick {
        fun cardLongPressed(card: BusinessCardModel)
    }
    fun interface OnLinkedInPressed {
        fun linkedInListener(linkedInUrl: String)
    }

}