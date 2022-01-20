package com.example.businesscards.startup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.businesscards.R
import com.example.businesscards.chat.BusinessFragment
import com.example.businesscards.chat.ChatFragment
import com.example.businesscards.chat.UsersFragment
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.databinding.ActivityMainBinding
import com.example.businesscards.home.HomeFragment
import com.example.businesscards.interfaces.BasicListener

class MainActivity : AppCompatActivity(), BasicListener {
    private lateinit var binding: ActivityMainBinding
    private var progressBar: ProgressBar? = null
    private var tmpIntent: String? = null
//    private var chatFragment: ChatFragment = ChatFragment()
//    private var usersFragment: UsersFragment = UsersFragment()
//    private var homeFragment: HomeFragment = HomeFragment()
//    private var businessFragment: BusinessFragment = BusinessFragment()
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
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHost.navController
        binding.mainBottomNavigationPanel.setupWithNavController(navController!!)
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

    fun setNavigationPanelSelectedTab(id: Int){
        binding.mainBottomNavigationPanel.selectedItemId = id
    }

    fun hideNavigationPanel(){
        binding.mainBottomNavigationPanel.visibility = View.GONE
    }

    fun showNavigationPanel(){
        binding.mainBottomNavigationPanel.visibility = View.VISIBLE
    }


    override fun onStarted() {
        progressBar?.visibility = View.VISIBLE
    }

    override fun onStopped() {
        progressBar?.visibility = View.GONE
    }


}