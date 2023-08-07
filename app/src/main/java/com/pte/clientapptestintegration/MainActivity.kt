package com.pte.clientapptestintegration

import android.content.ComponentName
import java.util.Locale
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.google.gson.JsonObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val htmlFileName = "sample.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setLocalizedTexts()

        val startButton = findViewById<View>(R.id.startButton)
        startButton.setOnClickListener {
            val htmlObject = readHtmlSample()
            if (htmlObject != "") {
                val jsonObject = getAllJsonValues()

                val intent = Intent()
                intent.component = ComponentName("org.oraclejet.multilingualInvoiceGenerator", "org.oraclejet.multilingualInvoiceGenerator.MainActivity")
                intent.putExtra("html_object", htmlObject)
                intent.putExtra("json_object", jsonObject.toString())

                startActivity(intent)
            }
        }
    }

    private fun readHtmlSample(): String {
        val assetManager = resources.assets

        try {
            val inputStream = assetManager.open(htmlFileName)
            val htmlContent = inputStream.bufferedReader().use { it.readText() }
            return htmlContent

        } catch (e: IOException) {
        }
        return ""
    }

    private fun getAllJsonValues(): String {
        val jsonObject = JsonObject()
        jsonObject.addProperty("", "")
        return jsonObject.toString()
    }

    private fun setLocalizedTexts() {
        val locale = Locale.getDefault()

        val configuration = resources.configuration
        configuration.setLocale(locale)

        val context = createConfigurationContext(configuration)

        // Get localized strings using the context with the updated locale
        val employeeHint = context.getString(R.string.employee_hint)
        val citizenNameLabel = context.getString(R.string.citizen_name_label)
        val acceptText = context.getString(R.string.accept_radio)
        val declineText = context.getString(R.string.decline_radio)
        val provideSignaturesText = context.getString(R.string.provide_signatures)
        val startButtonText = context.getString(R.string.start_button)

        val employeeInput = findViewById<EditText>(R.id.employeeInput)
        employeeInput.hint = employeeHint

        val citizenNameLabelView = findViewById<TextView>(R.id.citizenNameLabel)
        citizenNameLabelView.text = citizenNameLabel

        val acceptRadio = findViewById<RadioButton>(R.id.acceptRadio)
        acceptRadio.text = acceptText

        val declineRadio = findViewById<RadioButton>(R.id.declineRadio)
        declineRadio.text = declineText

        val provideSignaturesCheck = findViewById<CheckBox>(R.id.signatureCheck)
        provideSignaturesCheck.text = provideSignaturesText

        val startButton = findViewById<Button>(R.id.startButton)
        startButton.text = startButtonText
    }
}
