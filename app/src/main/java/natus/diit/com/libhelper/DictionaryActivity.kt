package natus.diit.com.libhelper

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner

class DictionaryActivity : AppCompatActivity() {

    private var etDictionarySearch: EditText? = null
    private var btnDictionaryTranslate: Button? = null

    private var preferences: Preferences? = null
    private var domain: String? = null

    private var translateDirection: String? = null
    private var search: String? = null

    lateinit var translateDirectionSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictionary)
        setToolbar(this, R.string.title_activity_dictionary)


        preferences = Preferences(this)

        domain = preferences!!.domain

        translateDirectionSpinner = findViewById(R.id.translate_direction_spinner) as Spinner

        val adapter = ArrayAdapter.createFromResource(this,
                R.array.dictionary_translate_direction, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        translateDirectionSpinner.adapter = adapter

        etDictionarySearch = findViewById(R.id.dictionarySearchField) as EditText

        btnDictionaryTranslate = findViewById(R.id.dictionarySearchButton) as Button

        btnDictionaryTranslate!!.setOnClickListener {
            search = etDictionarySearch!!.text.toString()
            etDictionarySearch!!.setText("")
            translateDirection = translateDirectionSpinner.selectedItem.toString()

            preferences!!.savedDictionarySearch = search
            preferences!!.savedTranslateDirection = translateDirection

            val intent = Intent(this@DictionaryActivity, TranslationsListActivity::class.java)
            startActivity(intent)
        }

        btnDictionaryTranslate!!.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val animation = AnimationUtils.loadAnimation(this@DictionaryActivity,
                            R.anim.my_anim)
                    btnDictionaryTranslate!!.startAnimation(animation)
                }
            }
            false
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                if (NavUtils.getParentActivityName(this) != null) {
                    NavUtils.navigateUpFromSameTask(this)
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}
