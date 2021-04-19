package com.example.vaultapplication

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
import androidx.appcompat.app.AppCompatActivity
import com.example.vaultapplication.databinding.ActivitySiteDetailsBinding
import com.example.vaultapplication.encryption.SiteEncryption
import com.example.vaultapplication.model.Site

class SiteDetailsActivity : AppCompatActivity() {

    private lateinit var siteDetailsBinding : ActivitySiteDetailsBinding
    private var siteData : Site? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        siteDetailsBinding = ActivitySiteDetailsBinding.inflate(layoutInflater)
        setContentView(siteDetailsBinding.root)

        setSupportActionBar(siteDetailsBinding.actionBar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        siteDetailsBinding.root.descendantFocusability = FOCUS_BLOCK_DESCENDANTS
        //siteDetailsBinding.siteDetails.password.setTransformationMethod(AsteriskPasswordTransformationMethod())

        val extras = intent.extras
        if(extras != null){
            siteData = extras.getParcelable(Constants.PARCELABLE_SITE_KEY) as Site?
            siteData?.let { setDataToViews(it) }
        }

    }

    private fun setDataToViews(siteData: Site) {
        siteDetailsBinding.siteDetails.url.setText(siteData.getUrl())
        siteDetailsBinding.siteDetails.siteName.setText(siteData.getSiteName())
        siteDetailsBinding.siteDetails.sector.setText(siteData.getSector())
        siteDetailsBinding.siteDetails.username.setText(SiteEncryption.decrypt(siteData.getUsername()))
        siteDetailsBinding.siteDetails.password.setText(SiteEncryption.decrypt(siteData.getPassword()))
        siteDetailsBinding.siteDetails.notes.setText(siteData.getNotes())

        SiteEncryption.decrypt(siteData.getPassword())?.let {
            UpdatePasswordStrength.updatePasswordStrengthView(
                it,siteDetailsBinding.siteDetails.passwordStrength)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_site_details,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {

            R.id.edit -> {

                val editActivityIntent = Intent(this, EditSiteActivity::class.java)
                val bundle = Bundle()
                bundle.putParcelable(Constants.PARCELABLE_SITE_KEY, siteData)
                editActivityIntent.putExtras(bundle)
                startActivity(editActivityIntent)
            }
            else -> finish()

        }
        return true
    }
}