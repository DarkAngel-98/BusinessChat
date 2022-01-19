package com.example.businesscards.startup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.businesscards.R
import com.example.businesscards.chat.BusinessFragment
import com.example.businesscards.chat.CommunicationFragment
import com.example.businesscards.chat.UsersFragment
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.databinding.ActivityMainBinding
import com.example.businesscards.home.HomeFragment
import com.example.businesscards.interfaces.BasicListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), BasicListener {
    private lateinit var binding: ActivityMainBinding
    private var progressBar: ProgressBar? = null
    private var tmpIntent: String? = null
    private var communicationFragment: CommunicationFragment = CommunicationFragment()
    private var usersFragment: UsersFragment = UsersFragment()
    private var homeFragment: HomeFragment = HomeFragment()
    private var businessFragment: BusinessFragment = BusinessFragment()
    private var navController: NavController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tmpIntent = intent.getStringExtra(HeartSingleton.IntentFlag).toString()
        if (!tmpIntent.isNullOrEmpty())
            Log.d("KEY", tmpIntent!!)
        progressBar = binding.homeProgressBar
        hideActionBarTitle()
//        changeViews()
//        bottomNavigationPanel()
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHost.navController
        binding.mainBottomNavigationPanel.setupWithNavController(navController!!)
//        bottomView = binding.mainBottomNavigationPanel
//        NavigationUI.setupWithNavController(bottomView,navHost)
//        bottomView!!.setupWithNavController(navHost!!)
//        var navHost = findViewById<View>(R.id.nav_host_fragment)
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

//    private fun changeViews(){
//        val bundle = Bundle()
//        bundle.putString(HeartSingleton.IntentFlag, tmpIntent)
//        homeFragment.arguments = bundle
//        this.binding.mainBottomNavigationPanel.getChildAt(0)
//        supportFragmentManager
//            .beginTransaction()
//            .replace(R.id.main_frame_layout, homeFragment)
//            .commit()
//    }

    private fun openHomeFragment(){
//        supportFragmentManager
//            .beginTransaction()
//            .replace(R.id.main_frame_layout, homeFragment)
//            .commit()

    }
//    private fun openChatFragment(){
//        supportFragmentManager
//            .beginTransaction()
//            .replace(R.id.main_frame_layout, communicationFragment)
////            .addToBackStack(null)
//            .commit()
//    }

    private fun openUsersFragment(){
//        supportFragmentManager
//            .beginTransaction()
//            .replace(R.id.main_frame_layout, usersFragment)
////            .addToBackStack(null)
//            .commit()
    }

    private fun openCardsFragment(){
//        supportFragmentManager
//            .beginTransaction()
//            .replace(R.id.main_frame_layout, businessFragment)
////            .addToBackStack(null)
//            .commit()
    }

    fun setNavigationPanelSelectedTab(id: Int){
        binding.mainBottomNavigationPanel.selectedItemId = id
    }

    fun hideNavigationPanel(){
        binding.mainBottomNavigationPanel.visibility = View.GONE
    }

    fun showNavigationPanel(){
        binding.mainBottomNavigationPanel.visibility = View.VISIBLE
    }

//    private fun bottomNavigationPanel(){
//
//        binding.mainBottomNavigationPanel.setOnItemSelectedListener { item ->
//
//            when(item.itemId){
//                R.id.homeFragment ->{
//                    openHomeFragment()
//                }
//                R.id.usersFragment ->{
//                    openUsersFragment()
//                }
//                R.id.businessFragment ->{
//                    openCardsFragment()
//                }
//            }
//            true
//        }
//    }

    override fun onStarted() {
        progressBar?.visibility = View.VISIBLE
    }

    override fun onStopped() {
        progressBar?.visibility = View.GONE
    }


}