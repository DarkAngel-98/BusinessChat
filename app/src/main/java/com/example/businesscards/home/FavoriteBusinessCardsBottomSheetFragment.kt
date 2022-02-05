package com.example.businesscards.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.example.businesscards.R
import com.example.businesscards.adapters.BusinessCardAdapter
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.constants.PreferenceClass
import com.example.businesscards.databinding.FragmentFavoriteBusinessCardsBottomSheetBinding
import com.example.businesscards.interfaces.BasicListener
import com.example.businesscards.models.BusinessCardModel
import com.example.businesscards.startup.MainActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class FavoriteBusinessCardsBottomSheetFragment : BottomSheetDialogFragment(), BasicListener {

    private lateinit var binding: FragmentFavoriteBusinessCardsBottomSheetBinding
    var prefs: PreferenceClass? = null
    var businessCardAdapter: BusinessCardAdapter? = null
    var favoriteCards: ArrayList<BusinessCardModel>? = ArrayList()
    var snapHelper: SnapHelper = PagerSnapHelper()

    companion object{
        private const val TAG = "businessCardBottomSheetFragmentTag"

        fun showFavoriteBusinessCards(requireActivity: FragmentActivity) =
            FavoriteBusinessCardsBottomSheetFragment()
                .apply {

                }.show(requireActivity.supportFragmentManager, TAG)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceClass(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_favorite_business_cards_bottom_sheet, container, false)
        binding.lifecycleOwner = this

        arguments?.let {
        }

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.behavior?.apply {
            isFitToContents = false
            state = BottomSheetBehavior.STATE_EXPANDED
        }
        initUI()
    }

    private fun initUI(){
        var layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvFavoriteCards.layoutManager = layoutManager
        snapHelper.attachToRecyclerView(binding.rvFavoriteCards)
        favoriteCards = retrieveBusinessCards()
        if(favoriteCards?.size == 0 || favoriteCards.isNullOrEmpty()){
            binding.noFavoriteCards.visibility = View.VISIBLE
        }else{binding.noFavoriteCards.visibility = View.GONE}
        businessCardAdapter = favoriteCards?.let { BusinessCardAdapter(it, R.layout.business_card_row_layout) }
        binding.rvFavoriteCards.adapter = businessCardAdapter
        businessCardAdapter?.notifyItemRangeChanged(0, favoriteCards!!.size-1)

        setupListeners()
    }

    private fun retrieveBusinessCards(): ArrayList<BusinessCardModel>?{
        val retrievedBusinessCardList: String? = prefs?.getImportantCards()
        var retrievedCardList: ArrayList<BusinessCardModel>? = ArrayList()
        val gson = Gson()
        val type = object: TypeToken<ArrayList<BusinessCardModel>>() {}.type
        retrievedCardList = gson.fromJson(retrievedBusinessCardList, type)

        return retrievedCardList
    }

    private fun setupListeners(){
        binding.backBtn.setOnClickListener {
            dialog?.dismiss()
        }

        businessCardAdapter?.setLongPressListener{
            longPressDialog(HeartSingleton.AlertDialogOK, it)
        }
        businessCardAdapter?.setLinkedInListener{
            //openVebView(it)
            navigateToWebView(it)
            Handler(Looper.getMainLooper()).postDelayed({this.dismiss()},200)
        }
    }

    private fun navigateToWebView(webUrl: String){
        val bundle = bundleOf(HeartSingleton.WEB_LINKED_IN to webUrl)
        findNavController().navigate(R.id.webViewFragment, bundle,null)
    }

    private fun openVebView(url: String){

        var webPage: Uri = Uri.parse(url)

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            webPage = Uri.parse("http://$url")
        }
        val intent = Intent(Intent.ACTION_VIEW, webPage)
        if(intent.resolveActivity(requireActivity().packageManager) != null)
            startActivity(intent)
    }

    private fun longPressDialog(negativeButton: String, card: BusinessCardModel) {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle(card.cardFullName)

        alertDialog.setNegativeButton(negativeButton) { _, _ -> }

        alertDialog.create()
        alertDialog.show()
    }

    override fun onStarted() {
        activity?.let { (activity as MainActivity).showProgress() }
    }

    override fun onStopped() {
        activity?.let { (activity as MainActivity).hideProgress() }
    }

}