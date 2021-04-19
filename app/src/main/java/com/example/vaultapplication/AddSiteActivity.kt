package com.example.vaultapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vaultapplication.auth.AuthenticationActivity
import com.example.vaultapplication.database.SectorDao
import com.example.vaultapplication.database.SiteDao
import com.example.vaultapplication.database.VaultDatabase
import com.example.vaultapplication.databinding.ActivityAddSiteBinding
import com.example.vaultapplication.encryption.SiteEncryption
import com.example.vaultapplication.model.Sector
import com.example.vaultapplication.model.Site
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class AddSiteActivity : AppCompatActivity() {
    private lateinit var addSiteBinding: ActivityAddSiteBinding
    private lateinit var sectorDao: SectorDao
    private lateinit var siteDao: SiteDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addSiteBinding = ActivityAddSiteBinding.inflate(layoutInflater)
        setContentView(addSiteBinding.root)

        sectorDao = VaultDatabase.getDatabaseInstance(applicationContext).sectorDao()
        siteDao = VaultDatabase.getDatabaseInstance(applicationContext).siteDao()
        setSupportActionBar(addSiteBinding.actionBar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        setSectorItem()

        setAddSiteListener()
        setResetValueListener()
        checkPasswordStrength()


    }

    private fun checkPasswordStrength() {
        addSiteBinding.siteDetails.password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                UpdatePasswordStrength.updatePasswordStrengthView(
                    s.toString(),
                    addSiteBinding.siteDetails.passwordStrength
                )
            }

            override fun afterTextChanged(s: Editable?) {}

        })
    }

    private fun setResetValueListener() {
        addSiteBinding.reset.setOnClickListener {
            clearForm(addSiteBinding.root)
        }
    }

    private fun clearForm(group: ViewGroup) {
        var i = 0
        val count = group.childCount
        while (i < count) {
            val view: View = group.getChildAt(i)
            if (view is EditText) {
                view.text = null
            }
            if (view is ViewGroup && view.childCount > 0) clearForm(view)
            ++i
        }
    }


    private fun setAddSiteListener() {
        addSiteBinding.save.setOnClickListener {
            saveSiteDetails()
        }
    }

    private fun saveSiteDetails() {
        val url = addSiteBinding.siteDetails.url.text.toString().trim()
        val siteName = addSiteBinding.siteDetails.siteName.text.toString().trim()
        val sector = addSiteBinding.siteDetails.sector.text.toString().trim()
        val userName = addSiteBinding.siteDetails.username.text.toString().trim()
        val password = addSiteBinding.siteDetails.password.text.toString().trim()
        val notes = addSiteBinding.siteDetails.notes.text.toString().trim()
        val credential = getUserCredentials()
        val deviceIdentifier = UUID.randomUUID().toString()
        if (checkInputs(url, siteName, sector, userName, password)) {
            val encryptedUserName = SiteEncryption.encrypt(userName)
            val encryptedPassword = SiteEncryption.encrypt(password)
            if (encryptedUserName != null && encryptedPassword != null) {
                if (credential != 0L) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        var id :Int = siteDao.getId()
                        val site = Site(
                            url,
                            siteName,
                            sector,
                            encryptedUserName,
                            encryptedPassword,
                            notes,
                            credential,
                            deviceIdentifier,
                            ++id
                        )
                        siteDao.addSite(site)
                        sectorDao.addSector(Sector(sectorName = sector))
                        showToast("Saved Successfully")
                        finish()
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
            url.isEmpty() -> showInputError(addSiteBinding.siteDetails.url, "URL Cannot be Empty")
            siteName.isEmpty() -> showInputError(
                addSiteBinding.siteDetails.siteName,
                "Site Name Cannot be Empty"
            )
            sector.isEmpty() -> showInputError(
                addSiteBinding.siteDetails.sector,
                "Sector Cannot be Empty"
            )
            userName.isEmpty() -> showInputError(
                addSiteBinding.siteDetails.username,
                "UserName Cannot be Empty"
            )
            password.isEmpty() -> showInputError(
                addSiteBinding.siteDetails.password,
                "Password Cannot be Empty"
            )
            else -> isInputValid = true

        }
        return isInputValid

    }

    fun showInputError(field: EditText, message: String) {
        field.error = message
        field.requestFocus()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setSectorItem() {

        sectorDao.getSector().observe(this, {
            val sectorList = it.map { it.getSectorName() }
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, sectorList)
            addSiteBinding.siteDetails.sector.setAdapter(adapter)
            addSiteBinding.siteDetails.sector.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    addSiteBinding.siteDetails.sector.showDropDown()

                }


            addSiteBinding.siteDetails.sector.setOnTouchListener(OnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= (addSiteBinding.siteDetails.sector.right - addSiteBinding.siteDetails.sector.compoundDrawables[2].bounds.width())) {

                        addSiteBinding.siteDetails.sector.showDropDown()
                        return@OnTouchListener true
                    }
                }
                false
            })
        })


    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> finish()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
    fun test(){}
}
