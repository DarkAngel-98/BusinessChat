package com.example.businesscards.constants

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class PreferenceClass(context: Context) {
    private val appContext = context.applicationContext

    private val prefs: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(appContext)

    fun setUserLoggedIn(logged: Boolean) {
        prefs.edit().putBoolean(HeartSingleton.PrefLogged, logged).apply()
    }

    fun getUserLoggedIn(): Boolean {
        return prefs.getBoolean(HeartSingleton.PrefLogged, false)
    }


    fun saveUserId(Id: String) {
        prefs.edit().putString(HeartSingleton.PrefId, Id).apply()
    }

    fun getUserId(): String{
        return prefs.getString(HeartSingleton.PrefId, "")!!
    }

    fun saveFirstName(firstName: String) {
        prefs.edit().putString(HeartSingleton.PrefFirstName, firstName).apply()
    }
    fun getFirstName(): String {
        return prefs.getString(HeartSingleton.PrefFirstName, "")!!
    }

    fun saveLastName(lastName: String) {
        prefs.edit().putString(HeartSingleton.PrefLastName, lastName).apply()
    }
    fun getLastName(): String {
        return prefs.getString(HeartSingleton.PrefLastName, "")!!
    }

    fun saveUsername(username: String) {
        prefs.edit().putString(HeartSingleton.PrefUsername, username).apply()
    }
    fun getUsername(): String {
        return prefs.getString(HeartSingleton.PrefUsername, "")!!
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString(HeartSingleton.PrefEmail, email).apply()
    }
    fun getUserEmail(): String{
        return prefs.getString(HeartSingleton.PrefEmail, "")!!
    }

    fun savePassword(password: String) {
        prefs.edit().putString(HeartSingleton.PrefPassword, password).apply()
    }

    fun getPassword(): String {
        return prefs.getString(HeartSingleton.PrefPassword, "")!!
    }
    fun saveMobilePhone(mobilePhone: String) {
        prefs.edit().putString(HeartSingleton.PrefMobilePhone, mobilePhone).apply()
    }
    fun getMobilePhone(): String {
        return prefs.getString(HeartSingleton.PrefMobilePhone, "")!!
    }
    fun saveCompanyName(companyName: String) {
        prefs.edit().putString(HeartSingleton.PrefCompanyName, companyName).apply()
    }
    fun getCompanyName(): String {
        return prefs.getString(HeartSingleton.PrefCompanyName, "")!!
    }

    fun saveJobPosition(jobPosition: String) {
        prefs.edit().putString(HeartSingleton.PrefJobPosition, jobPosition).apply()
    }
    fun getJobPosition(): String {
        return prefs.getString(HeartSingleton.PrefJobPosition, "")!!
    }

    fun saveYearsOfExperience(experience: Int) {
        prefs.edit().putInt(HeartSingleton.PrefYearsOfExperience, experience).apply()
    }
    fun getYearsOfExperience(): Int {
        return prefs.getInt(HeartSingleton.PrefYearsOfExperience, -1)
    }

    fun saveInterests(interests: String) {
        prefs.edit().putString(HeartSingleton.PrefInterests, interests).apply()
    }
    fun getInterests(): String {
        return prefs.getString(HeartSingleton.PrefInterests, "")!!
    }

    fun saveLinkedinProfile(linkedinProfile: String) {
        prefs.edit().putString(HeartSingleton.PrefLinkedinProfile, linkedinProfile).apply()
    }
    fun getLinkedinProfile(): String {
        return prefs.getString(HeartSingleton.PrefLinkedinProfile, "")!!
    }

    fun saveHomeAddress(homeAddress: String) {
        prefs.edit().putString(HeartSingleton.PrefHomeAddress, homeAddress).apply()
    }
    fun getHomeAddress(): String {
        return prefs.getString(HeartSingleton.PrefHomeAddress, "")!!
    }

    fun saveImageUrl(imageUrl: String) {
        prefs.edit().putString(HeartSingleton.PrefImageURL, imageUrl).apply()
    }

    fun getImageUrl(): String {
        return prefs.getString(HeartSingleton.PrefImageURL, "")!!
    }

    fun deleteUsername() {
        prefs.edit().remove(HeartSingleton.PrefUsername).apply()
    }

    fun deletePassword() {
        prefs.edit().remove(HeartSingleton.PrefPassword).apply()
    }
    fun deleteUserEmail(){
        prefs.edit().remove(HeartSingleton.PrefEmail).apply()
    }

}
