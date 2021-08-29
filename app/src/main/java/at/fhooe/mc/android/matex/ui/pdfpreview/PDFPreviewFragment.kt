package at.fhooe.mc.android.matex.ui.pdfpreview

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import at.fhooe.mc.android.matex.BuildConfig
import at.fhooe.mc.android.matex.R
import at.fhooe.mc.android.matex.activities.EditorActivity
import at.fhooe.mc.android.matex.activities.EditorActivity.Companion.document
import at.fhooe.mc.android.matex.network.Ads
import at.fhooe.mc.android.matex.network.Ads.Companion.getTestAdPref
import at.fhooe.mc.android.matex.network.RetrofitGetPdfTask
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import java.io.File
import java.util.*

class PDFPreviewFragment : Fragment() {

    lateinit var pdfView: PDFView
    private lateinit var rootView: View
    private var adView: AdView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_pdf_preview, container, false)
        pdfView = root.findViewById(R.id.pdfView)
        rootView = root

        RetrofitGetPdfTask(context, this, document).execute()

        root.findViewById<Button>(R.id.btn_remove_ads).apply {
            setOnClickListener { onRemoveAdsClicked() }
        }

        setLoading(true)
        return root
    }

    override fun onDestroy() {
        adView?.destroy()
        super.onDestroy()
    }

    private fun setLoading(loading: Boolean) {
        val llLoading = rootView.findViewById<LinearLayout>(R.id.fragment_pdf_loading)
        val llAd = rootView.findViewById<LinearLayout>(R.id.pdf_preview_banner_ad)
        val pdfView = rootView.findViewById<PDFView>(R.id.pdfView)
        when {
            loading -> {
                llLoading.isVisible = true
                llAd.isVisible = true

                createAdView()
                adView?.let { llAd.addView(it) }
            }
            else -> {
                llLoading.isVisible = false
                llAd.isVisible = false
                pdfView.isVisible = true
                if (adView != null) {
                    adView!!.destroy()
                    adView = null
                }
            }
        }
    }

    /**
     * Creates and loads the banner ad if the user has not purchased ad-free.
     */
    private fun createAdView() {
        // Don't load the ad when the user hs purchased ad-free.
        if (Ads.getHasRemoveAds(requireContext())) return

        val llAdText = rootView.findViewById<LinearLayout>(R.id.adText)

        adView = AdView(Objects.requireNonNull(context)).apply {
            adSize = AdSize.LARGE_BANNER
            val isTestAd = BuildConfig.DEBUG || getTestAdPref(Objects.requireNonNull(context))
            adUnitId = if (isTestAd) Ads.BANNER_TEST_ID else Ads.BANNER_UNIT_ID
            val adRequest = AdRequest.Builder().build()
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    if ((activity as EditorActivity?)!!.isBillingInitialized)
                        llAdText.visibility = View.VISIBLE
                }
            }
            loadAd(adRequest)
        }
    }

    fun setError(error: String?) {
        activity!!.runOnUiThread {
            AlertDialog.Builder(this@PDFPreviewFragment.activity)
                .setTitle(getString(R.string.error_generating_pdf))
                .setMessage(error)
                .setPositiveButton(android.R.string.ok, null)
                .create().show()
        }
    }

    fun loadPdf(pdf: File?) {
        setLoading(false)
        pdfView.fromFile(pdf).spacing(1).load()
    }

    private fun onRemoveAdsClicked() = (activity as EditorActivity?)!!.purchaseRemoveAds()
}
