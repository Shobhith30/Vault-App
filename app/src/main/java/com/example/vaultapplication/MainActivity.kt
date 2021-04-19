package com.example.vaultapplication

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vaultapplication.Constants.RC_SIGN_IN
import com.example.vaultapplication.auth.AuthenticationActivity
import com.example.vaultapplication.database.SectorDao
import com.example.vaultapplication.database.SiteDao
import com.example.vaultapplication.database.VaultDatabase
import com.example.vaultapplication.databinding.ActivityMainBinding
import com.example.vaultapplication.model.Sector
import com.example.vaultapplication.model.Site
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var siteDao: SiteDao
    private lateinit var sectorDao: SectorDao
    private lateinit var siteDetailsAdapter: SiteDetailsAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var db: FirebaseFirestore
    private lateinit var dbRef: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)


        siteDao = VaultDatabase.getDatabaseInstance(applicationContext).siteDao()
        sectorDao = VaultDatabase.getDatabaseInstance(applicationContext).sectorDao()
        //Toast.makeText(applicationContext, "${UUID.randomUUID().toString()}", Toast.LENGTH_SHORT).show()
        addNewSite()
        initializeActionBar()
        initializeRecyclerView()
        setSearchButtonClickListener()
        hideKeyboardOnFocusChange()
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        dbRef = FirebaseDatabase.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id))
            .requestEmail()
            .build()

        //Then we will get the GoogleSignInClient object from GoogleSignIn class
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        siteDao.getSectorCount().observe(this, {
            mainBinding.sector.adapter = SectorSpinnerAdapter(applicationContext, it)
        })

        mainBinding.sector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                val sector =
                    (mainBinding.sector.getItemAtPosition(position) as SectorCount).getSectorName()
                siteDao.getSiteBySector(sector).observe(this@MainActivity, { it ->
                    if (it != null) {
                        siteDetailsAdapter.setSiteData(it)
                    }
                })

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

    }

    private fun hideKeyboardOnFocusChange() {
        mainBinding.search.setOnFocusChangeListener { v, hasFocus ->
            if (v.id == mainBinding.search.id && !hasFocus)
                hideKeyBoard()
        }
    }

    private fun initializeRecyclerView() {
        mainBinding.siteRecycler.layoutManager = LinearLayoutManager(this)
        siteDetailsAdapter = SiteDetailsAdapter()
        mainBinding.siteRecycler.adapter = siteDetailsAdapter
    }

    private fun initializeActionBar() {

        setSupportActionBar(mainBinding.actionBar.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setLogo(R.drawable.pass_manager_text)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.burger_menu)
    }


    private fun addNewSite() {
        mainBinding.addNewSite.setOnClickListener {
            val newSiteIntent = Intent(this, AddSiteActivity::class.java)
            startActivity(newSiteIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                val searchVisibility = mainBinding.search.visibility
                mainBinding.search.visibility = if (searchVisibility == View.VISIBLE) {
                    siteDetailsAdapter.filter.filter("")
                    View.INVISIBLE
                } else View.VISIBLE
            }
            R.id.profile -> {
                deleteSavedUser()
            }
            R.id.sync -> {
                signIn()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun signIn() {

        if (mAuth.currentUser != null) {
            backupSiteData()
            backupSectorData()
            return
        }
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun backupSectorData() {

        dbRef.getReference("sector").child(mAuth.currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    lifecycleScope.launch(Dispatchers.IO) {
                        val sectorDb = sectorDao.getSectorBackup()
                        Log.e("sector",sectorDb.toString())
                        for (snapshot in snapshot.children) {
                            val sector = snapshot.getValue(Sector::class.java) as Sector
                            var isEqual = false
                            for (sectorDb in sectorDb) {
                                if (sector.getSectorName()
                                        .equals(sectorDb.getSectorName(), ignoreCase = true)
                                ) {
                                    isEqual = true
                                    break
                                }
                            }
                            if (!isEqual)
                                sectorDao.addSector(sector)
                        }

                        for (sectorDb in sectorDb) {
                            var isEqual = false
                            for(snapshot in snapshot.children){
                                val sector = snapshot.getValue(Sector::class.java) as Sector
                                if (sector.getSectorName()
                                        .equals(sectorDb.getSectorName(), ignoreCase = true)
                                ) {
                                    isEqual = true
                                    break
                                }
                            }
                            if (!isEqual)

                                addValueToFirebase("sector",sectorDb)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }

    private fun deleteSavedUser() {
        val sharedPreferences = getSharedPreferences(
            Constants.USER_KEY,
            Context.MODE_PRIVATE
        )
        val sharedEditor = sharedPreferences.edit()
        sharedEditor.clear().apply()
        startActivity(Intent(this, AuthenticationActivity::class.java))
        finish()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setSearchButtonClickListener() {
        mainBinding.search.setOnTouchListener(View.OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX + Constants.EXTRA_TOUCH_SPACE >= (mainBinding.search.right - mainBinding.search.compoundDrawables[2].bounds.width())) {


                    siteDetailsAdapter.filter.filter(mainBinding.search.text.toString())
                    hideKeyBoard()
                    return@OnTouchListener true
                }
            }
            false
        })

    }

    private fun hideKeyBoard() {

        val view = this.currentFocus
        if (view != null) {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {

                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)

            } catch (e: ApiException) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {

        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    backupSiteData()
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.google_login_fail),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }

    private fun backupSiteData() {
        val dialog = Dialog(this)
        showDataSyncDialog(dialog)
        val userId = mAuth.currentUser.uid
        lifecycleScope.launch(Dispatchers.IO) {
            val dbSiteValue = siteDao.getSite()
            dbRef.reference.child("site").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (snapshot.childrenCount == 0L) {
                                for (dbSite in dbSiteValue) {
                                    addValueToFirebase("site",dbSite)
                                }
                                dialog.dismiss()
                                return@launch
                            }
                            if (dbSiteValue.isEmpty()) {
                                for (snapshot in snapshot.children) {
                                    val site = snapshot.getValue(Site::class.java) as Site

                                    siteDao.addSite(site)


                                }
                                dialog.dismiss()
                                return@launch
                            }

                            for (snapshot in snapshot.children) {
                                val site = snapshot.getValue(Site::class.java) as Site
                                var isEqual = false
                                for (dbSite in dbSiteValue) {
                                    if (site.deviceIdentifier == dbSite.deviceIdentifier && site.getId() == dbSite.getId()) {
                                        if (dbSite != site) {
                                            if (dbSite.getTimestamp() >= site.getTimestamp())
                                                snapshot.key?.let {
                                                    dbRef.reference.child("site").child(userId)
                                                        .child(it)
                                                        .setValue(dbSite)
                                                }
                                            else
                                                siteDao.updateSite(site)

                                        }
                                        isEqual = true
                                        break
                                    }
                                }
                                if (!isEqual) {

                                    siteDao.addSite(site)
                                }
                            }

                            for (dbSite in dbSiteValue) {
                                var isEqual = false
                                for (snapshotValue in snapshot.children) {
                                    val site = snapshotValue.getValue(Site::class.java) as Site
                                    if (site.deviceIdentifier == dbSite.deviceIdentifier && site.getId() == dbSite.getId()) {

                                        isEqual = true
                                        break
                                    }
                                }
                                if (!isEqual) {
                                    addValueToFirebase("site",dbSite)
                                }
                            }

                            dialog.dismiss()
                        }
                    }


                    override fun onCancelled(error: DatabaseError) {

                    }


                })

        }


    }

    private fun addValueToFirebase(reference : String, value: Any) {
        val userId = mAuth.currentUser.uid
        dbRef.getReference(reference).push().key?.let { key ->
            dbRef.getReference(reference).child(userId).child(key)
                .setValue(value)
        }


    }

    private fun showDataSyncDialog(dialog: Dialog) {

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = layoutInflater.inflate(R.layout.data_sync_message, null)
        val refresh = view.findViewById<ImageView>(R.id.refresh)
        val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        rotation.fillAfter = true
        rotation.repeatCount = Animation.INFINITE
        refresh.startAnimation(rotation)
        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.show()
    }
}