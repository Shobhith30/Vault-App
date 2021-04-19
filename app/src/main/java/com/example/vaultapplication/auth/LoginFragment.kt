package com.example.vaultapplication.auth

import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.vaultapplication.Constants
import com.example.vaultapplication.MainActivity
import com.example.vaultapplication.R
import com.example.vaultapplication.database.VaultDatabase
import com.example.vaultapplication.database.UserDao
import com.example.vaultapplication.databinding.FragmentLoginBinding
import com.example.vaultapplication.encryption.user.UserEncryption
import com.example.vaultapplication.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LoginFragment : Fragment() {

    private var _loginBinding: FragmentLoginBinding? = null
    private val loginBinding get() = _loginBinding!!
    private lateinit var biometricManager: BiometricManager
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var userDao: UserDao
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _loginBinding = FragmentLoginBinding.inflate(inflater, container, false)

        userDao = VaultDatabase.getDatabaseInstance(requireContext()).userDao()
        setLoginClickListener()
        if (getUserCredentials() != 0L) {
            showBiometricPrompt()
        }
        loginBinding.fingerprintLogo.setOnClickListener {
            showBiometricPrompt()
        }


        return loginBinding.root
    }

    private fun getUserCredentials(): Long {
        val sharedPreferences = requireContext().getSharedPreferences(
            Constants.USER_KEY,
            AppCompatActivity.MODE_PRIVATE
        )
        return sharedPreferences.getLong(Constants.PHONE_NUMBER, 0L)
    }

    private fun showBiometricPrompt() {
        biometricManager = BiometricManager.from(requireContext())
        //checkBiometricStatus(biometricManager)
        val executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    //showToast(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    showToast("Success")
                    openHomePage()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    //showToast("Failed")
                }
            }
        )

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Use Fingerprint, MPin to Login").setDescription("use your fingerprint to login").setNegativeButtonText("Use MPin")
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    private fun openHomePage() {
        startActivity(Intent(context,MainActivity::class.java))
        activity?.finish()
    }

    private fun showToast(message: String) {

        lifecycleScope.launch(Dispatchers.Main) {
            val toast = Toast(activity?.applicationContext)
            toast.duration = Toast.LENGTH_LONG
            toast.setText(message)
            toast.show()
        }
//        val inflater = layoutInflater
//        val layout: View = inflater.inflate(
//            R.layout.custom_toast, null
//        )
//        val text : TextView = layout.findViewById(R.id.message) as TextView
//        text.text = message
//        val toast = Toast(activity?.applicationContext)
//        toast.setGravity(Gravity.FILL_HORIZONTAL or Gravity.BOTTOM,0,0)
//        toast.duration = Toast.LENGTH_LONG
//        toast.view =  layout
//        toast.show()

    }


    private fun setLoginClickListener() {
        loginBinding.signIn.setOnClickListener {
            userSignIn()
        }
    }

    private fun userSignIn() {
        val mobileNumber = loginBinding.mobileNumber.text.toString()
        val mPin = loginBinding.mPin.text.toString()
        if (checkUserInputs(mobileNumber, mPin)) {

            isUserRegistered(mobileNumber, mPin)

        } else {
            Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isUserRegistered(mobileNumber: String, mPin: String) {
        lifecycleScope.launch(Dispatchers.IO){
            val user : User? = userDao.getUser(mobileNumber.toLong())
            if (user != null) {
                if (mPin == decryptMPin(user.getMPin())) {
                    checkUserLoginStatus(mobileNumber.toLong())
                }else{
                    showToast("Invalid Credentials")
                }
            }else{

                UserEncryption.encrypt(mPin)?.let {
                    userDao.addUser(User(mobileNumber.toLong(),it))
                    checkUserLoginStatus(mobileNumber.toLong())
                }

            }
        }

    }

    private fun checkUserLoginStatus(phoneNumber: Long) {
        val sharedPreferences = requireContext().getSharedPreferences(
            Constants.USER_KEY,
            Context.MODE_PRIVATE
        )
        val sharedEditor = sharedPreferences.edit()
        sharedEditor.putLong(Constants.PHONE_NUMBER, phoneNumber).apply()
        startActivity(Intent(this.context, MainActivity::class.java))
        activity?.finish()

    }

    private fun decryptMPin(mPin: String): String? {

        return UserEncryption.decrypt(mPin)
    }

    private fun checkUserInputs(mobileNumber: String, mPin: String): Boolean {
        var isValidInput = false
        when {
            mobileNumber.length != 10 -> {
                loginBinding.mobileNumber.error = "Enter 10 Digits Mobile Number"
                loginBinding.mobileNumber.requestFocus()
            }
            mPin.length != 4 -> {
                loginBinding.mPin.error = "Enter 4 Digits MPin"
                loginBinding.mPin.requestFocus()
            }
            else -> isValidInput = true
        }

        return isValidInput

    }

}