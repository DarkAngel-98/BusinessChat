package com.example.businesscards.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import com.example.businesscards.R
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.databinding.FragmentWebViewBinding
import com.example.businesscards.startup.MainActivity

class WebViewFragment : Fragment() {

    private lateinit var binding:FragmentWebViewBinding
    private var webUrl = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            webUrl = arguments?.get(HeartSingleton.WEB_LINKED_IN) as String
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_web_view, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.linedInWebView.settings.javaScriptEnabled = true
        loadWebView()
    }

    private fun loadWebView() {
        Log.i("BASE_URL", "")
        binding.linedInWebView.loadUrl(webUrl)
        binding.linedInWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                view?.loadUrl(url)
                Log.i("REDIRECTED", url)
                return super.shouldOverrideUrlLoading(view, request)
            }
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                activity?.let { (activity as MainActivity).showProgress() }
                super.onPageStarted(view, url, favicon)
                Log.i("STARTED", url.toString())
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                activity?.let { (activity as MainActivity).hideProgress() }
                super.onPageFinished(view, url)
                Log.i("FINISHED", url.toString())
            }
            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                activity?.let { (activity as MainActivity).hideProgress() }
                super.onReceivedError(view, request, error)
            }
        }
    }


}