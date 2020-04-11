package com.example.healthwareapplication.activity.self_assessment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.frats.android.models.response.ResponseModel
import com.example.healthwareapplication.R
import com.example.healthwareapplication.activity.question.QuestionActivity
import com.example.healthwareapplication.adapter.self_assessment.SearchSymptomAdapter
import com.example.healthwareapplication.adapter.self_assessment.SelectedSymptomAdapter
import com.example.healthwareapplication.adapter.self_assessment.SymptomAdapter
import com.example.healthwareapplication.api.ApiClient
import com.example.healthwareapplication.api.ApiInterface
import com.example.healthwareapplication.app_utils.*
import com.example.healthwareapplication.constants.AppConstants
import com.example.healthwareapplication.constants.IntentConstants
import com.example.healthwareapplication.model.self_assessment.SymptomDataModel
import com.example.healthwareapplication.model.self_assessment.SymptomJsonModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_dashboard.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class SADetailActivity : AppCompatActivity() {
//    private lateinit var adapter: SearchSymptomAdapter
    private lateinit var gson: Gson

    //    private lateinit var searchAdapter: SearchSymptomAdapter
    private lateinit var timeLayout: LinearLayout
    private lateinit var symptomList: RecyclerView
    private lateinit var symptom: RecyclerView
    private lateinit var nextBtn: Button
    private lateinit var searchTxt: AutoCompleteTextView
    val symptmJsonAry: JSONArray = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_s_a_detail)

        initComponents()
    }

    private fun initComponents() {
        AppHelper.transparentStatusBar(this)
        timeLayout = findViewById(R.id.timeLayout)
        symptomList = findViewById(R.id.symptomList)
        symptom = findViewById(R.id.symptom)
        nextBtn = findViewById(R.id.nextBtn)
        searchTxt = findViewById(R.id.searchTxt)

        gson = Gson()
        dataBind()

    }

    fun addSymptom(view: View) {
        val intent = Intent(this, AddSymptomActivity::class.java)
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            if (data != null) {
                val modelObj = data!!.getStringExtra(IntentConstants.kSYMPTOM_SELECTED)
                symptmJsonAry.put(JSONObject(modelObj!!))
                symptomList.adapter!!.notifyDataSetChanged()
                showBottom()
            }
        }
    }

    private fun dataBind() {
        symptomList.layoutManager = LinearLayoutManager(this)
        val addAdapter = SelectedSymptomAdapter(symptmJsonAry!!)
        symptomList.adapter = addAdapter
    }

    fun searchClick(view: View) {
        searchTxt.visibility = View.VISIBLE
        symptom.visibility = View.VISIBLE

        searchTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                fetchSymptomBySearch(s.toString().trim())
            }
        })
    }

    private fun fetchSymptomBySearch(search: String?) {
        val apiService: ApiInterface =
            ApiClient.getRetrofitClient(this)!!.create(ApiInterface::class.java)

        val param = JsonObject()
        param.addProperty("search", search)
        AppHelper.printParam("SEARCH PAram:", param)

        val call: Call<JsonObject> = apiService.getSearchSymptomsByName(param)
        DialogUtility.showProgressDialog(this)
        call.enqueue(object : Callback<JsonObject?> {

            override fun onResponse(call: Call<JsonObject?>?, response: Response<JsonObject?>) {
                AppHelper.printUrl("SEARCH URL:", response)

                if (response.isSuccessful) {
                    AppHelper.printResponse("SEARCH REs:", response)

                    DialogUtility.hideProgressDialog()
                    val json = JSONObject(response.body().toString())
                    val responseModel = ResponseModel(json)
                    if (responseModel.isCode()) {
                        val symptomListAry = responseModel.getDataArray()!!
                        symptom.layoutManager = LinearLayoutManager(this@SADetailActivity)
                        val adapter = SymptomAdapter(symptomListAry!!,
                            RecyclerItemClickListener.OnItemClickListener { view, position ->
                                val modelObj = JSONObject(symptomListAry.getJSONObject(position).toString())
                                symptmJsonAry.put(modelObj)
                                AppHelper.showToast(this@SADetailActivity,SymptomJsonModel(modelObj).getId()+"")
                                searchTxt.setText("")
                                symptom.visibility = View.GONE
                                searchTxt.visibility = View.GONE

                                showBottom()
                            })
                        symptom.adapter = adapter

                    } else {
                        AppHelper.showToast(
                            this@SADetailActivity,
                            responseModel.getMessage().toString()
                        )
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject?>?, t: Throwable) {
                if (t is NoConnectivityException) {
                    DialogUtility.hideProgressDialog()
                    AppHelper.showNetNotAvailable(this@SADetailActivity)
                }
            }
        })
    }

    private fun showBottom() {
        Log.e("Show bottom: ", " " + symptmJsonAry.length())
        nextBtn.visibility = View.VISIBLE
        timeLayout.visibility = View.VISIBLE
    }


    fun clickNext(view: View) {
        Log.e("next: ", " " + symptmJsonAry.length())
        val intent = Intent(this, QuestionActivity::class.java)
        startActivity(intent)
    }
}
