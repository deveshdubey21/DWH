package com.example.healthwareapplication.activity.account

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.text.bold
import app.frats.android.models.response.ResponseModel
import com.example.healthwareapplication.R
import com.example.healthwareapplication.R.layout.activity_otp
import com.example.healthwareapplication.activity.account.login.LoginActivity
import com.example.healthwareapplication.activity.dashboard.DashboardActivity
import com.example.healthwareapplication.api.ApiClient
import com.example.healthwareapplication.api.ApiInterface
import com.example.healthwareapplication.app_utils.AppHelper
import com.example.healthwareapplication.app_utils.AppSessions
import com.example.healthwareapplication.app_utils.NoConnectivityException
import com.example.healthwareapplication.constants.IntentConstants
import com.example.healthwareapplication.model.user.UserDetailModel
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_otp.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OtpActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var value1: String
    private lateinit var value2: String
    private lateinit var value3: String
    private lateinit var value4: String
    private var otpString: String? = null
    private var otpMailString: String? = null
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_otp)

        initComponents()
        defaultConfiguration()
    }

    private fun initComponents() {
        AppHelper.transparentStatusBar(this)
        val userDetailModel =
            intent.getSerializableExtra(IntentConstants.kUSER_DATA) as UserDetailModel
        otpMailString = intent.getStringExtra(IntentConstants.kOTP)
        email = userDetailModel.email
    }

    private fun defaultConfiguration() {

        otpEditText1.addTextChangedListener(OTPTextWatcher(otpEditText1))
        otpEditText2.addTextChangedListener(OTPTextWatcher(otpEditText2))
        otpEditText3.addTextChangedListener(OTPTextWatcher(otpEditText3))
        otpEditText4.addTextChangedListener(OTPTextWatcher(otpEditText4))

        otpLayout.setOnClickListener(this)
    }

    inner class OTPTextWatcher(private val view: View) : TextWatcher {

        override fun afterTextChanged(editable: Editable) {
            val text = editable.toString()
            when (view.id) {
                R.id.otpEditText1 -> if (text.length == 1) otpEditText2.requestFocus()
                R.id.otpEditText2 -> if (text.length == 1) otpEditText3.requestFocus() else if (text.isEmpty()) otpEditText1.requestFocus()
                R.id.otpEditText3 -> if (text.length == 1) otpEditText4.requestFocus() else if (text.isEmpty()) otpEditText2.requestFocus()
                R.id.otpEditText4 -> if (text.isEmpty()) otpEditText3.requestFocus()
            }
        }

        override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
        }

        override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
        }

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.otpLayout -> {
                checkValidation()
            }
        }
    }

    private fun checkValidation() {
        value1 = otpEditText1.text.toString()
        value2 = otpEditText2.text.toString()
        value3 = otpEditText3.text.toString()
        value4 = otpEditText4.text.toString()

        var isFlag = true
        if (value1.trim().isEmpty() or value2.trim().isEmpty() or value3.trim()
                .isEmpty() or value4.trim().isEmpty()
        ) {
            AppHelper.showToast(this, getString(R.string.please_enter_otp))
            isFlag = false
        }
        if (isFlag) {
            fetchSubmit()
        }
    }

    private fun fetchSubmit() {
        value1 = otpEditText1.text.toString()
        value2 = otpEditText2.text.toString()
        value3 = otpEditText3.text.toString()
        value4 = otpEditText4.text.toString()

        otpString = value1 + value2 + value3 + value4

        if (otpMailString == otpString) {
            Log.e("OtpValue: ", ": $otpString")
            verifyAccount(otpString!!)
        } else {
            AppHelper.showToast(this, getString(R.string.invalid_otp))
        }
    }

    private fun verifyAccount(otpString: String) {
        val apiService: ApiInterface =
            ApiClient.getRetrofitClient(this)!!.create(ApiInterface::class.java)

        val param = JsonObject()
        param.addProperty("username", email)
        param.addProperty("otp", otpString)

        AppHelper.printParam("verifyParam: ", param)

        val call: Call<JsonObject> = apiService.verifyUser(param)
        call.enqueue(object : Callback<JsonObject?> {

            override fun onResponse(call: Call<JsonObject?>?, response: Response<JsonObject?>) {
                if (response.isSuccessful) {
                    val json = JSONObject(response.body().toString())
                    val responseModel = ResponseModel(json)
                    if (responseModel.isCode()) {
                        showDashboard()
                    } else {
                        AppHelper.showToast(this@OtpActivity, responseModel.getMessage().toString())
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject?>?, t: Throwable) {
                if (t is NoConnectivityException) {
                    AppHelper.showNetNotAvailable(this@OtpActivity)
                }
            }
        })
    }

    private fun showDashboard() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }


}