package com.example.vaultapplication.auth

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.example.vaultapplication.R
import com.example.vaultapplication.database.UserDao
import com.example.vaultapplication.database.VaultDatabase
import com.example.vaultapplication.databinding.FragmentSignUpBinding
import com.example.vaultapplication.encryption.user.UserEncryption
import com.example.vaultapplication.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SignUpFragment : Fragment() {

    private  var _signUpBinding: FragmentSignUpBinding? = null
    private val signUpBinding   get() = _signUpBinding!!
    private lateinit var userDao : UserDao
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _signUpBinding  = FragmentSignUpBinding.inflate(inflater, container, false)
        userDao = VaultDatabase.getDatabaseInstance(requireContext()).userDao()
        setSignUpClickListener()

        return signUpBinding.root
    }

    private fun showToast(message: String){

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
//        toast.duration = Toast.LENGTH_LONG
//        toast.setGravity(Gravity.FILL_HORIZONTAL ,0,0)
//        toast.view =  layout
//        toast.show()
    }

    private fun setSignUpClickListener() {
        signUpBinding.signUp.setOnClickListener {
            signUpUser()
        }
    }

    private fun signUpUser() {
        val phoneNumber = signUpBinding.phoneNumber.text.toString()
        var mPin = signUpBinding.mPin.text.toString()
        val reMPin = signUpBinding.reEnterMPin.text.toString()
        if(checkInputs(phoneNumber, mPin, reMPin)){

            val encryptedMPin = encryptMPin(mPin)
            if(encryptedMPin != null)
                saveUserToDatabase(phoneNumber.toLong(), encryptedMPin)

        }
    }

    private fun saveUserToDatabase(phoneNumber: Long, mPin: String) {
        lifecycleScope.launch(Dispatchers.IO){
            userDao.addUser(User(phoneNumber, mPin))
            checkUserLoginStatus(phoneNumber)
            withContext(Dispatchers.Main){
                showToast(
                    "Congrtats!!! Success\n" +
                            "Signin to access the vault"
                )
                activity?.findViewById<ViewPager>(R.id.auth_view_pager)?.currentItem = 0
            }

        }
    }

    private fun checkUserLoginStatus(phoneNumber: Long) {
        val sharedPreferences = requireContext().getSharedPreferences("user_login", MODE_PRIVATE)
        val sharedEditor = sharedPreferences.edit()
        sharedEditor.putLong("phone_number", phoneNumber).apply()

    }

    private fun encryptMPin(mPin: String): String? {


       return UserEncryption.encrypt(mPin)

    }

    private fun checkInputs(phoneNumber: String, mPin: String, reMPin: String): Boolean {
        var isValidInput = false
        when {

            phoneNumber.length!=10 -> {
                signUpBinding.phoneNumber.error = "Enter valid phone number"
                signUpBinding.phoneNumber.requestFocus()
            }
            mPin.length!=4 -> {
                signUpBinding.mPin.error = "Enter 4 Digits MPin"
                signUpBinding.mPin.requestFocus()
            }
            mPin!=reMPin -> {
                signUpBinding.reEnterMPin.error = "MPin doesn't match"
                signUpBinding.reEnterMPin.requestFocus()
            }
            else -> isValidInput = true
        }
        return isValidInput
    }

}