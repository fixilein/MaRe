package at.fhooe.mc.android.matex.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import at.fhooe.mc.android.matex.BuildConfig
import at.fhooe.mc.android.matex.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AboutActivity : AppCompatActivity() {

    private var mRewardedAd: RewardedAd? = null
    private var adButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val webView = findViewById<WebView>(R.id.activity_about_web_view)
        webView.loadUrl("file:///android_asset/licenses.html")

        val versionLabel = findViewById<TextView>(R.id.textViewVersionNumber)
        val versionText = "${getString(R.string.version)}${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        versionLabel.text = versionText

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

        val id = if (BuildConfig.DEBUG) AD_TEST_ID else AD_REWARD_UNIT_ID

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
                    Log.d(Companion.TAG, "User earned the reward. Amount: $rewardAmount Type: $rewardType")

                    adButton?.isEnabled = false
                    loadAd()
                }
            }
        } else {
            Log.d(Companion.TAG, "The rewarded ad wasn't ready yet.")
            adButton?.isEnabled = false
            loadAd()
        }
    }

    companion object {
        private const val TAG = "AboutActivity"

        private const val AD_REWARD_UNIT_ID = "ca-app-pub-8038269995942724/8574510232"
        private const val AD_TEST_ID = "ca-app-pub-3940256099942544/5224354917"
    }
}
