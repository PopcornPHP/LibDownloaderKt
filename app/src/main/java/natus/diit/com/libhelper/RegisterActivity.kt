package natus.diit.com.libhelper

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar

/**
 * Class which works with registration
 * Registration representet as simple WebView
 */
class RegisterActivity : AppCompatActivity() {

    private var mRegisterWebView: WebView? = null

    private val REGISTER_URL = "https://library.diit.edu.ua/uk/registration"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val progressBar = findViewById(R.id.progressBar) as ProgressBar
        progressBar.max = 100

        mRegisterWebView = findViewById(R.id.webView) as WebView

        mRegisterWebView!!.settings.javaScriptEnabled = true

        mRegisterWebView!!.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return false
            }
        })

        mRegisterWebView!!.setWebChromeClient(object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                if (newProgress == 100) {
                    progressBar.visibility = View.INVISIBLE
                } else {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                }
            }
        })

        mRegisterWebView!!.loadUrl(REGISTER_URL)
    }

}