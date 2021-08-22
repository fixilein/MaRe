package at.fhooe.mc.android.matex.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import at.fhooe.mc.android.matex.MyFileProvider
import at.fhooe.mc.android.matex.R
import at.fhooe.mc.android.matex.document.Document
import at.fhooe.mc.android.matex.network.BillingSecrets
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import com.anjlab.android.iab.v3.Constants as BillingConstants


class EditorActivity : AppCompatActivity(), BillingProcessor.IBillingHandler {

    private lateinit var billingProcessor: BillingProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        val navView = findViewById<BottomNavigationView>(R.id.nav_view)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration.Builder(
            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
        ).build()

        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        NavigationUI.setupWithNavController(navView, navController)

        documentTitle = intent.getStringExtra("DocumentTitle")
        file = Document.getFileFromName(applicationContext, documentTitle)
        directory = Document.getDirectoryFromName(applicationContext, documentTitle)
        document = Document(applicationContext, documentTitle)

        billingProcessor = BillingProcessor(this, BillingSecrets.LICENSE_KEY, this)
        billingProcessor.initialize()
    }

    override fun onCreateOptionsMenu(_menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_editor, _menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_editor_delete -> {
                showDeleteFileDialog()
                true
            }
            R.id.menu_editor_share_md -> {
                val f = document!!.file
                shareFile(f, MyFileProvider.MIMETYPE_MD)
                true
            }
            R.id.menu_editor_share_pdf -> {
                val pdf = document!!.getPDFFile(
                    applicationContext
                )
                if (!pdf.exists()) {
                    showPdfDoesNotExistAlert()
                } else {
                    shareFile(pdf, MyFileProvider.MIMETYPE_PDF)
                }
                true
            }
            R.id.menu_editor_share_zip -> {
                val f = document!!.getZipFile(
                    applicationContext
                )
                shareFile(f, MyFileProvider.MIMETYPE_ZIP)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onDestroy() {
        billingProcessor.release()
        super.onDestroy()
    }

    private fun showDeleteFileDialog() {
        AlertDialog.Builder(this@EditorActivity)
            .setTitle(String.format(applicationContext.getString(R.string.delete_question), title))
            .setMessage(applicationContext.getString(R.string.delete_question_long))
            .setPositiveButton(android.R.string.ok) { dialog: DialogInterface?, which: Int ->
                document!!.deleteFiles(
                    applicationContext
                )
                finish() // close editor
            }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }

    private fun showPdfDoesNotExistAlert() {
        AlertDialog.Builder(this@EditorActivity)
            .setTitle(getString(R.string.dialog_create_pdf_first))
            .setMessage(getString(R.string.dialog_create_pdf_first_message))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun shareFile(file: File, mimeType: String) {
        val fileProvider = MyFileProvider(this, document)
        fileProvider.shareFile(file, mimeType)
    }

    override fun onBillingInitialized() {
        /*
        * Called when BillingProcessor was initialized and it's ready to purchase
        */
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        Log.e(BILLING_TAG, "ErrorCode: $errorCode")
        Log.e(BILLING_TAG, error.toString())

        when (errorCode) {
            BillingConstants.BILLING_RESPONSE_RESULT_USER_CANCELED -> {
                Toast.makeText(
                    this,
                    getString(R.string.purchase_cancelled_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        /*
        * Called when some error occurred. See Constants class for more details
        *
        * Note - this includes handling the case where the user canceled the buy dialog:
        * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
        */
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        when (productId) {
            BillingSecrets.REMOVE_ADS_PRODUCT_ID -> onRemoveAdsPurchaseSuccessful()
            else -> Log.e(BILLING_TAG, "Unknown purchase successful: $productId")
        }
    }

    override fun onPurchaseHistoryRestored() {
        /*
        * Called when purchase history was restored and the list of all owned PRODUCT ID's
        * was loaded from Google Play
        */
    }

    fun purchaseRemoveAds() {
        billingProcessor.purchase(this, BillingSecrets.REMOVE_ADS_PRODUCT_ID)
    }

    private fun onRemoveAdsPurchaseSuccessful() {
        Toast.makeText(
            this,
            getString(R.string.purchase_successful_message),
            Toast.LENGTH_SHORT
        ).show()

        val sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )

        with(sharedPref.edit()) {
            putBoolean(getString(R.string.preference_test_ad), true)
            apply()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val BILLING_TAG = "MATEX_BILLING"

        // TODO: remove these from companion; this is so ugly
        var documentTitle: String? = null
        var document: Document? = null
        var file: File? = null
        var directory: File? = null
    }
}
