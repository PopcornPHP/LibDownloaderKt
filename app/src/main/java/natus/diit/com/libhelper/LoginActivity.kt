package natus.diit.com.libhelper

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import org.json.JSONException
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder


class LoginActivity : AppCompatActivity() {

    private var signInButton: Button? = null
    private val registerButton: Button? = null
    private var loginCheckBox: CheckBox? = null

    private var etLibCardNumberText: EditText? = null
    private var etPassword: EditText? = null

    private var isAuthorized: Boolean = false

    private var cardNumber: String? = null
    private var password: String? = null
    private var isRemembered: Boolean = false

    private var authorizationError: String? = null

    private var preferences: Preferences? = null
    private var domain: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbar()
        setContentView(R.layout.activity_login)

        preferences = Preferences(this)
        domain = preferences!!.domain

        signInButton = findViewById(R.id.sign_in_button) as Button
        //registerButton = (Button) findViewById(R.id.registerButton);

        etLibCardNumberText = findViewById(R.id.libCardText) as EditText
        etPassword = findViewById(R.id.passwordText) as EditText
        loginCheckBox = findViewById(R.id.checkboxID) as CheckBox

        isRemembered = preferences!!.savedIsRemembered
        if (isRemembered) {
            etLibCardNumberText!!.setText(preferences!!.savedLogin)
            etPassword!!.setText(preferences!!.savedPassword)
            loginCheckBox!!.isChecked = isRemembered
        }

        signInButton!!.setOnClickListener {
            cardNumber = etLibCardNumberText!!.text.toString()
            password = etPassword!!.text.toString()
            isRemembered = loginCheckBox!!.isChecked

            Log.i(LOG, "ccc $cardNumber $password")

            if (isRemembered) {
                preferences!!.savedLogin = cardNumber
                preferences!!.savedPassword = password
                preferences!!.savedIsRemembered = isRemembered
            } else {
                preferences!!.savedLogin = ""
                preferences!!.savedPassword = ""
                preferences!!.savedIsRemembered = false
            }
            LoginChecker().execute()
        }

        signInButton!!.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val animation = AnimationUtils.loadAnimation(this@LoginActivity,
                            R.anim.my_anim)
                    signInButton!!.startAnimation(animation)
                }
            }
            false
        }

        //        registerButton.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View view) {
        //                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        //                startActivity(intent);
        //            }
        //        });
    }

    private inner class LoginChecker : AsyncTask<Void, Void, String>() {
        private var resultJson = ""

        override fun doInBackground(vararg params: Void): String {
            try {
                val regCardNumber = URLEncoder.encode(cardNumber, "UTF-8")
                val regPassword = URLEncoder.encode(password, "UTF-8")
                val url = URL(domain + "/api/user/signin?login=" + regCardNumber +
                        "&password=" + regPassword +
                        "&remember=" + isRemembered)

                resultJson = preferences!!.getJSONFromServer(url)
                Log.i(LOG, "resultJson = " + resultJson)

            } catch (e: Exception) {
                Log.i(LOG, "Request Error " + e.toString())
                showSnackBar("Перевірте інтернет з'єднання", findViewById(R.id.passw_login_form))
                e.printStackTrace()
            }

            return resultJson
        }

        override fun onPostExecute(strJson: String) {
            super.onPostExecute(strJson)
            val dataJsonObj: JSONObject
            try {
                dataJsonObj = JSONObject(strJson)
                if (strJson.startsWith("{\"response\"")) {
                    isAuthorized = true
                } else {
                    if (password == "")
                        authorizationError = "Ви не ввели пароль"
                    else if (cardNumber == "")
                        authorizationError = "Ви не ввели читацький номер"
                    else
                        authorizationError = "Ви вказали невірні дані"
                    isAuthorized = false
                    Log.i(LOG, "authorizationError = " + authorizationError!!)
                }

                preferences?.savedIsAuthorized = isAuthorized

                if (isAuthorized) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val mySnackbar = Snackbar.make(findViewById(R.id.passw_login_form),
                            authorizationError!!, Snackbar.LENGTH_LONG)

                    mySnackbar.show()
                }

            } catch (e: JSONException) {
                Log.i(LOG, "Json Error ${e.message}")
            }

        }
    }

    private fun setToolbar(){
        val myToolbar = findViewById(R.id.my_toolbar) as Toolbar?
        myToolbar?.title = getString(R.string.title_activity_authorization)
        setSupportActionBar(myToolbar)
    }

}