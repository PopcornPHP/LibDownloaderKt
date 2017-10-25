package natus.diit.com.libhelper

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class RegisterActivity : AppCompatActivity() {

    lateinit var registerIntent: Intent

    private var registerButton: Button? = null

    private var etName: EditText? = null
    private var etSurname: EditText? = null
    private var etCardNumber: EditText? = null
    private var etPassword: EditText? = null
    private var etPasswordConfirmation: EditText? = null
    private var etEmail: EditText? = null

    private var email: String? = null
    private var name: String? = null
    private var surname: String? = null
    private var password: String? = null
    private var passwordConfirmation: String? = null
    private var cardNumber: String? = null

    private var receivedCookie: String? = null

    private var preferences: Preferences? = null
    private var domain: String? = null


    internal var LOG = "MyLog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etName = findViewById(R.id.nameText) as EditText
        etSurname = findViewById(R.id.surnameText) as EditText
        etCardNumber = findViewById(R.id.libCardText) as EditText
        etPassword = findViewById(R.id.passwordText) as EditText
        etPasswordConfirmation = findViewById(R.id.passwordConfirmationText) as EditText
        etEmail = findViewById(R.id.emailText) as EditText

        preferences = this.application as Preferences
        domain = preferences!!.domain
        receivedCookie = preferences!!.savedReceivedCookie

        registerButton = findViewById(R.id.registerFinishedButton) as Button

        registerButton!!.setOnClickListener {
            Log.i(LOG, "In onclick() registerButton")

            registerIntent = Intent()

            email = etEmail!!.text.toString()
            name = etName!!.text.toString()
            surname = etSurname!!.text.toString()
            password = etPassword!!.text.toString()
            passwordConfirmation = etPasswordConfirmation!!.text.toString()
            cardNumber = etCardNumber!!.text.toString()

            RegistrationChecker().execute()
        }

    }

    private inner class RegistrationChecker : AsyncTask<Void, Void, String>() {

        internal var urlConnection: HttpURLConnection? = null
        internal var reader: BufferedReader? = null
        internal var resultJson = ""


        override fun doInBackground(vararg params: Void): String {
            // получаем данные с внешнего ресурса
            try {
                val regName = URLEncoder.encode(name, "UTF-8")
                val regSurname = URLEncoder.encode(surname, "UTF-8")
                val regLogin = URLEncoder.encode(cardNumber, "UTF-8")
                val regPassword = URLEncoder.encode(password, "UTF-8")
                val regPasswordConfirmation = URLEncoder.encode(passwordConfirmation, "UTF-8")
                val regEmail = URLEncoder.encode(email, "UTF-8")
                val url = URL(domain + "/api/user/signup?name=" + regName +
                        "&surname=" + regSurname +
                        "&login=" + regLogin +
                        "&password=" + regPassword +
                        "&password_confirmation=" + regPasswordConfirmation +
                        "&email=" + regEmail)

                resultJson = preferences!!.getJSONFromServer(url, receivedCookie)

                Log.i(LOG, "" + resultJson)

            } catch (e: Exception) {
                Log.i(LOG, "Error " + e.toString())
                e.printStackTrace()
            }

            return resultJson
        }

        override fun onPostExecute(strJson: String) {
            super.onPostExecute(strJson)
            val dataJsonObj: JSONObject
            try {
                dataJsonObj = JSONObject(strJson)
                Log.i(LOG, "" + strJson)
                if (strJson.startsWith("{\"response\"")) {
                    Log.i(LOG, "" + strJson)
                    Toast.makeText(this@RegisterActivity, "Реєстрація пройшла успішно", Toast.LENGTH_LONG).show()
                    finish()
                } else {

                    val k = dataJsonObj.getString("error")
                    Log.i(LOG, "" + k)
                    Toast.makeText(this@RegisterActivity,
                            "Помилка при реєстрації, первірте правильність введених даних",
                            Toast.LENGTH_LONG).show()
                }

            } catch (e: JSONException) {
                Toast.makeText(this@RegisterActivity, "Error" + e.message, Toast.LENGTH_LONG).show()
                finish()
            }

        }
    }


}
