package at.fhooe.mc.android.matex.network

import android.content.Context
import android.widget.Toast
import at.fhooe.mc.android.matex.R

class Ads {
    companion object {
        const val AD_REWARD_UNIT_ID = "ca-app-pub-8038269995942724/8574510232"
        const val AD_REWARD_TEST_ID = "ca-app-pub-3940256099942544/5224354917"

        const val BANNER_TEST_ID = "ca-app-pub-3940256099942544/6300978111"
        const val BANNER_UNIT_ID = "ca-app-pub-8038269995942724/7469543635"

        fun toggleTestAd(context: Context) {
            val sharedPref = getSharedPrefs(context)

            var testAd =
                sharedPref.getBoolean(context.getString(R.string.preference_test_ad), false)

            with(sharedPref.edit()) {
                putBoolean(
                    context.getString(at.fhooe.mc.android.matex.R.string.preference_test_ad),
                    !testAd
                )
                apply()
            }

            testAd = sharedPref.getBoolean(context.getString(R.string.preference_test_ad), false)

            val message = if (testAd) "Now using test ads." else "Using real ads."
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        @JvmStatic
        fun getTestAdPref(context: Context): Boolean = getSharedPrefs(context).getBoolean(
            context.getString(R.string.preference_test_ad),
            false
        )

        fun getHasRemoveAds(context: Context): Boolean = getSharedPrefs(context).getBoolean(
            context.getString(R.string.preference_remove_ads),
            false
        )

        fun setHasRemoveAds(context: Context, value: Boolean) {
            val sharedPref = getSharedPrefs(context)

            with(sharedPref.edit()) {
                putBoolean(
                    context.getString(R.string.preference_remove_ads),
                    value
                )
                apply()
            }
        }

        private fun getSharedPrefs(context: Context) = context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
    }
}