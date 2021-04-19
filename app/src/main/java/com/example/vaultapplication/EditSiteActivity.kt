package com.example.vaultapplication

import android.R
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.vaultapplication.auth.AuthenticationActivity
import com.example.vaultapplication.database.SectorDao
import com.example.vaultapplication.database.SiteDao
import com.example.vaultapplication.database.VaultDatabase
import com.example.vaultapplication.databinding.ActivityEditSiteBinding
import com.example.vaultapplication.encryption.SiteEncryption
import com.example.vaultapplication.model.Sector
import com.example.vaultapplication.model.Site
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditSiteActivity : AppCompatActivity() {

    private lateinit var editSiteBinding : ActivityEditSiteBinding
    private lateinit var sectorDao : SectorDao
    private lateinit var siteDao: SiteDao
    private var siteData : Site? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editSiteBinding = ActivityEditSiteBinding.inflate(layoutInflater)
        setContentView(editSiteBinding.root)

        setSupportActionBar(editSiteBinding.actionBar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        sectorDao = VaultDatabase.getDatabaseInstance(applicationContext).sectorDao()
        siteDao = VaultDatabase.getDatabaseInstance(applicationContext).siteDao()

        setSectorItem()
        getSiteData()
        setUpdateSiteListener()
        checkPasswordStrength()

    }

    private fun checkPasswordStrength() {
        editSiteBinding.siteDetails.password.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                UpdatePasswordStrength.updatePasswordStrengthView(s.toString(),editSiteBinding.siteDetails.passwordStrength)
            }

            override fun afterTextChanged(s: Editable?) {}

        })
    }

    private fun setUpdateSiteListener() {
        editSiteBinding.update.setOnClickListener {
            saveSiteDetails()
        }
    }

    private fun getSiteData() {
        val extras = intent.extras
        if(extras != null){
            siteData = extras.getParcelable(Constants.PARCELABLE_SITE_KEY) as Site?
            siteData?.let { setDataToViews(it) }
        }
    }

    private fun setDataToViews(siteData: Site) {
        editSiteBinding.siteDetails.url.setText(siteData.getUrl())
        editSiteBinding.siteDetails.siteName.setText(siteData.getSiteName())
        editSiteBinding.siteDetails.sector.setText(siteData.getSector())
        editSiteBinding.siteDetails.username.setText(SiteEncryption.decrypt(siteData.getUsername()))
        editSiteBinding.siteDetails.password.setText(SiteEncryption.decrypt(siteData.getPassword()))
        editSiteBinding.siteDetails.notes.setText(siteData.getNotes())

        SiteEncryption.decrypt(siteData.getPassword())?.let {
            UpdatePasswordStrength.updatePasswordStrengthView(
                it,editSiteBinding.siteDetails.passwordStrength)
        }

    }

    private fun saveSiteDetails() {
        val url = editSiteBinding.siteDetails.url.text.toString().trim()
        val siteName = editSiteBinding.siteDetails.siteName.text.toString().trim()
        val sector = editSiteBinding.siteDetails.sector.text.toString().trim()
        val userName = editSiteBinding.siteDetails.username.text.toString().trim()
        val password = editSiteBinding.siteDetails.password.text.toString().trim()
        val notes = editSiteBinding.siteDetails.notes.text.toString().trim()
        val credential = getUserCredentials()
        val siteId = siteData?.getId()
        val deviceIdentifier = siteData?.deviceIdentifier
        if (checkInputs(url, siteName, sector, userName, password)) {
            val encryptedUserName = SiteEncryption.encrypt(userName)
            val encryptedPassword = SiteEncryption.encrypt(password)
            if (encryptedUserName != null && encryptedPassword != null) {
                if (credential != 0L) {
                    if(siteId !=null && deviceIdentifier != null){
                        val site = Site(
                            url,
                            siteName,
                            sector,
                            encryptedUserName,
                            encryptedPassword,
                            notes,
                            credential,
                            deviceIdentifier,
                            siteId
                        )
                        lifecycleScope.launch(Dispatchers.IO) {
                            siteDao.updateSite(site)
                            sectorDao.addSector(Sector(sectorName = sector))
                            startActivity(Intent(this@EditSiteActivity,MainActivity::class.java))
                            showToast("Updated Successfully")
                            finish()
                        }
                    }

                } else {
                    startActivity(Intent(this, AuthenticationActivity::class.java))
                    finish()
                }

            }
        }
    }

    private fun showToast(message: String) {

        lifecycleScope.launch(Dispatchers.Main) {
            val toast = Toast(applicationContext)
            toast.duration = Toast.LENGTH_LONG
            toast.setText(message)
            toast.show()
        }

    }
    private fun getUserCredentials(): Long {
        val sharedPreferences = getSharedPreferences(Constants.USER_KEY, MODE_PRIVATE)
        return sharedPreferences.getLong(Constants.PHONE_NUMBER, 0L)
    }

    private fun checkInputs(
        url: String,
        siteName: String,
        sector: String,
        userName: String,
        password: String
    ): Boolean {
        var isInputValid = false
        when {
            url.isEmpty() -> showInputError(editSiteBinding.siteDetails.url, "URL Cannot be Empty")
            siteName.isEmpty() -> showInputError(
                editSiteBinding.siteDetails.siteName,
                "Site Name Cannot be Empty"
            )
            sector.isEmpty() -> showInputError(
                editSiteBinding.siteDetails.sector,
                "Sector Cannot be Empty"
            )
            userName.isEmpty() -> showInputError(
                editSiteBinding.siteDetails.username,
                "UserName Cannot be Empty"
            )
            password.isEmpty() -> showInputError(
                editSiteBinding.siteDetails.password,
                "Password Cannot be Empty"
            )
            else -> isInputValid = true

        }
        return isInputValid

    }

    private fun showInputError(field: EditText, message: String) {
        field.error = message
        field.requestFocus()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setSectorItem() {

        sectorDao.getSector().observe(this, {
            val sectorList = it.map { it.getSectorName() }
            val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, sectorList)
            editSiteBinding.siteDetails.sector.setAdapter(adapter)
            editSiteBinding.siteDetails.sector.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    editSiteBinding.siteDetails.sector.showDropDown()

                }


            editSiteBinding.siteDetails.sector.setOnTouchListener(View.OnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= (editSiteBinding.siteDetails.sector.right - editSiteBinding.siteDetails.sector.compoundDrawables[2].bounds.width())) {

                        editSiteBinding.siteDetails.sector.showDropDown()
                        return@OnTouchListener true
                    }
                }
                false
            })
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}