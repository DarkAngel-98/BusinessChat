package com.example.businesscards.models

import java.io.Serializable


class UserInfo():Serializable {
    var companyName: String? = ""
    var email: String? = ""
    var firstName: String? = ""
    var homeAddress: String? = ""
    var lastName: String? = ""
    var mobilePhone: String? = ""
    var password: String? = ""
    var id: String? = ""
    var imageURL: String? = ""
    var username: String? = ""
    var token: String? = ""
    var status: Int? = 0
}
// status -> 0-> user offline, 1 -> user online