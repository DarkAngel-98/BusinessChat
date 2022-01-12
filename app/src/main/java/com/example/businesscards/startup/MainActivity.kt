package com.example.businesscards.startup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.example.businesscards.R
import com.example.businesscards.chat.BusinessFragment
import com.example.businesscards.chat.CommunicationFragment
import com.example.businesscards.chat.UsersFragment
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.databinding.ActivityMainBinding
import com.example.businesscards.home.HomeFragment
import com.example.businesscards.interfaces.BasicListener

class MainActivity : AppCompatActivity(), BasicListener {
    private lateinit var binding: ActivityMainBinding
    private var progressBar: ProgressBar? = null
    private var tmpIntent: String? = null
    private var communicationFragment: CommunicationFragment = CommunicationFragment()
    private var usersFragment: UsersFragment = UsersFragment()
    private var homeFragment: HomeFragment = HomeFragment()
    private var businessFragment: BusinessFragment = BusinessFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tmpIntent = intent.getStringExtra(HeartSingleton.IntentFlag).toString()
        if (!tmpIntent.isNullOrEmpty())
            Log.d("KEY", tmpIntent!!)
        progressBar = binding.homeProgressBar
        hideActionBarTitle()
        changeViews()
        bottomNavigationPanel()
    }

    private fun setHomeTitle(title: String?){
        supportActionBar?.title = title
    }
    private fun hideActionBarTitle(){
        supportActionBar?.hide()
    }

    fun showProgress(){
        progressBar?.visibility = View.VISIBLE
    }
    fun hideProgress(){
        progressBar?.visibility = View.GONE
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun changeViews(){
        val bundle = Bundle()
        bundle.putString(HeartSingleton.IntentFlag, tmpIntent)
        homeFragment.arguments = bundle
        this.binding.mainBottomNavigationPanel.getChildAt(0)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_frame_layout, homeFragment)
            .commit()
    }

    private fun openHomeFragment(){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_frame_layout, homeFragment)
            .commit()
    }
    private fun openChatFragment(){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_frame_layout, communicationFragment)
//            .addToBackStack(null)
            .commit()
    }

    private fun openUsersFragment(){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_frame_layout, usersFragment)
//            .addToBackStack(null)
            .commit()
    }

    private fun openCardsFragment(){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_frame_layout, businessFragment)
//            .addToBackStack(null)
            .commit()
    }

    fun setNavigationPanelSelectedTab(id: Int){
        binding.mainBottomNavigationPanel.selectedItemId = id
    }

    fun hideNavigationPanel(){
        binding.mainBottomNavigationPanel.visibility = View.GONE
    }

    private fun bottomNavigationPanel(){

        binding.mainBottomNavigationPanel.setOnItemSelectedListener { item ->

            when(item.itemId){
                R.id.navigate_home ->{
                    openHomeFragment()
                }
                R.id.navigate_users ->{
                    openUsersFragment()
                }
                R.id.navigate_chat ->{
                    openChatFragment()
                }
                R.id.navigate_business_cards ->{
                    openCardsFragment()
                }
            }
            true
        }
    }

    override fun onStarted() {
        progressBar?.visibility = View.VISIBLE
    }

    override fun onStopped() {
        progressBar?.visibility = View.GONE
    }


}