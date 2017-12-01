package natus.diit.com.libhelper

import android.content.Intent
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
import natus.diit.com.libhelper.model.user.CheckUserLogIn
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {

    private var signInButton: Button? = null
    private val registerButton: Button? = null
    private var loginCheckBox: CheckBox? = null

    private var etLibCardNumberText: EditText? = null
    private var etPassword: EditText? = null

    private var isAuthorized: Boolean = false

    private var cardNumber: String = ""
    private var password: String = ""
    private var isRemembered: Boolean = false

    private var authorizationError: String = ""

    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbar()
        setContentView(R.layout.activity_login)

        preferences = Preferences(this)

        signInButton = findViewById(R.id.sign_in_button) as Button
        //registerButton = (Button) findViewById(R.id.registerButton);

        etLibCardNumberText = findViewById(R.id.libCardText) as EditText
        etPassword = findViewById(R.id.passwordText) as EditText
        loginCheckBox = findViewById(R.id.checkboxID) as CheckBox

        isRemembered = preferences.savedIsRemembered
        if (isRemembered) {
            etLibCardNumberText?.setText(preferences.savedLogin)
            etPassword?.setText(preferences.savedPassword)
            loginCheckBox?.isChecked = isRemembered
        }

        signInButton?.setOnClickListener {
            cardNumber = etLibCardNumberText!!.text.toString()
            password = etPassword!!.text.toString()
            isRemembered = loginCheckBox!!.isChecked

            if (isRemembered) {
                preferences.savedLogin = cardNumber
                preferences.savedPassword = password
                preferences.savedIsRemembered = isRemembered
            } else {
                preferences.savedLogin = ""
                preferences.savedPassword = ""
                preferences.savedIsRemembered = false
            }

            logIn()
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

    private fun logIn() {
        val call = libBookApi?.signIn(cardNumber, password, isRemembered)
        call?.enqueue(object : Callback<CheckUserLogIn> {
            override fun onResponse(call: Call<CheckUserLogIn>,
                                    response: Response<CheckUserLogIn>) {
                val headers = response.headers()
                preferences.savedReceivedCookie = headers["Set-Cookie"]

                val user = response.body().user

                if (user != null) {
                    isAuthorized = true
                } else {
                    authorizationError = when {
                        password == "" -> "Ви не ввели пароль"
                        cardNumber == "" -> "Ви не ввели читацький номер"
                        else -> "Ви вказали невірні дані"
                    }
                    isAuthorized = false
                }

                preferences.savedIsAuthorized = isAuthorized

                if (isAuthorized) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val mySnackbar = Snackbar.make(findViewById(R.id.passw_login_form),
                            authorizationError, Snackbar.LENGTH_LONG)

                    mySnackbar.show()
                }
            }

            override fun onFailure(call: Call<CheckUserLogIn>, t: Throwable) {
                Log.e(LOG, "LoginActivity Error + " + t.message)
                showSnackBar(findViewById(R.id.passw_login_form)).show()
            }
        })
    }

    private fun setToolbar() {
        val myToolbar = findViewById(R.id.my_toolbar) as Toolbar?
        myToolbar?.title = getString(R.string.title_activity_authorization)
        setSupportActionBar(myToolbar)
    }

}