package natus.diit.com.libhelper

import android.content.res.Resources
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.net.URL
import java.net.URLEncoder


class TranslationsListActivity : AppCompatActivity() {

    internal var LOG = "MyLog"
    private var receivedCookie: String? = null

    private var lwDictionaryList: ListView? = null
    private var search: String? = null
    private var translateDirection: String? = null
    private var tvDictionaryResult: TextView? = null

    private var preferences: Preferences? = null
    private var domain: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dict_list)

        preferences = Preferences(this.applicationContext)

        receivedCookie = preferences!!.savedReceivedCookie
        search = preferences!!.savedDictionarySearch
        translateDirection = preferences!!.savedTranslateDirection
        lwDictionaryList = findViewById(R.id.dictionaryListView) as ListView
        tvDictionaryResult = findViewById(R.id.tv_translation_results) as TextView

        domain = preferences!!.domain

        DictionarySearchTask().execute()
    }


    private inner class DictionarySearchTask : AsyncTask<Void, Void, String>() {
        internal var resultJson = ""

        override fun doInBackground(vararg params: Void): String {
            try {
                val url = URL(domain + "/api/dictionary/search?search=" +
                        URLEncoder.encode(search, "UTF-8") +
                        "&direction=" + translateDirection + "&take=100")

                resultJson = preferences!!.getJSONFromServer(url, receivedCookie)
                Log.i(LOG, "JSON " + resultJson)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return resultJson
        }

        override fun onPostExecute(strJson: String) {
            super.onPostExecute(strJson)
            val dataJsonObj: JSONObject
            try {
                dataJsonObj = JSONObject(strJson)
                val responseObj = dataJsonObj.getJSONObject("response")
                val total = responseObj.getInt("total")

                //if translation was found
                if (total > 0) {
                    val responseData = responseObj.getJSONObject("data")
                    val iterator = responseData.keys()
                    val names = arrayOfNulls<String>(responseData.names().length())

                    run {
                        var i = 0
                        while (iterator.hasNext()) {
                            names[i] = iterator.next()
                            i++
                        }
                    }

                    val adapter = ArrayAdapter<String>(this@TranslationsListActivity,
                            android.R.layout.simple_list_item_1,
                            names)
                    lwDictionaryList!!.adapter = adapter

                    lwDictionaryList!!.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, pos, id ->
                        try {
                            val tmpJSON = responseData.getJSONArray(names[pos])
                            val translationResult = StringBuilder()

                            for (i in 0 until tmpJSON.length()) {
                                translationResult.append(tmpJSON.getString(i))
                                translationResult.append("\n")
                            }

                            tvDictionaryResult!!.text = translationResult.toString()

                        } catch (e: JSONException) {
                            Toast.makeText(this@TranslationsListActivity, "JSON error", Toast.LENGTH_LONG)
                                    .show()
                        }
                    }

                    //Translation was not found
                } else {
                    val res = resources
                    val noneResult = String.format(res.getString(R.string.dictionary_none_result),
                            search)
                    tvDictionaryResult!!.setText(noneResult)
                }

            } catch (e: JSONException) {
                Toast.makeText(this@TranslationsListActivity,
                        "Перевірте інтернет з'єднання",
                        Toast.LENGTH_LONG)
                        .show()
                finish()
            }

        }

    }
}
