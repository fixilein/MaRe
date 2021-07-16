package at.fhooe.mc.android.matex.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import at.fhooe.mc.android.matex.BuildConfig
import at.fhooe.mc.android.matex.R
import at.fhooe.mc.android.matex.network.Ads
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AboutActivity : AppCompatActivity() {

    private var mRewardedAd: RewardedAd? = null
    private var adButton: Button? = null
    private var tapCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val webView = findViewById<WebView>(R.id.activity_about_web_view)
        webView.loadUrl("file:///android_asset/licenses.html")

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_ON)
        }

        val versionLabel = findViewById<TextView>(R.id.textViewVersionNumber)
        val versionText = "${getString(R.string.version)}${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        versionLabel.text = versionText

        versionLabel.setOnClickListener {
            tapCount++
            if (tapCount > 9) {
                tapCount = 0
                Ads.toggleTestAd(applicationContext)
            }
        }

        val beerButton = findViewById<Button>(R.id.buttonBuyMeABeer)
        beerButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.DONATE_URL)))
        }

        loadAd()

        adButton = findViewById(R.id.buttonWatchAd)
        adButton?.setOnClickListener {
            showRewardAd()
        }
    }


    private fun loadAd() {
        val adRequest = AdRequest.Builder().build()
        val prefTestAd = Ads.getTestAdPref(applicationContext)

        val id = if (BuildConfig.DEBUG || prefTestAd) Ads.AD_REWARD_TEST_ID else Ads.AD_REWARD_UNIT_ID

        RewardedAd.load(this, id, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.message)
                mRewardedAd = null
                adButton?.isEnabled = false
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.d(Companion.TAG, "Ad was loaded.")
                mRewardedAd = rewardedAd
                adButton?.isEnabled = true
            }
        })
    }

    private fun showRewardAd() {
        if (mRewardedAd != null) {
            mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(Companion.TAG, "Ad was dismissed.")
                    adButton?.isEnabled = false
                    loadAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    Log.d(Companion.TAG, "Ad failed to show.")
                    adButton?.isEnabled = false
                    loadAd()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(Companion.TAG, "Ad showed fullscreen content.")
                    // Called when ad is dismissed.
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mRewardedAd = null
                    // adButton?.isEnabled = false
                    // loadAd()
                }
            }
            mRewardedAd?.show(this) {
                fun onUserEarnedReward(rewardItem: RewardItem) {
                    val rewardAmount = rewardItem.amount
                    val rewardType = rewardItem.type
                    Log.d(TAG, "User earned the reward. Amount: $rewardAmount Type: $rewardType")

                    adButton?.isEnabled = false
                    loadAd()
                }
            }
        } else {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
            adButton?.isEnabled = false
            loadAd()
        }
    }

    companion object {
        private const val TAG = "AboutActivity"
    }
}
