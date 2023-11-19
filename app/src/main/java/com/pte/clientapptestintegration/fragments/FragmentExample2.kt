package com.pte.clientapptestintegration.fragments

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import com.jakewharton.threetenabp.AndroidThreeTen
import com.pte.clientapptestintegration.R
import org.json.JSONArray
import org.json.JSONObject
import org.threeten.bp.LocalDateTime
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FragmentExample2 : Fragment() {

    private val htmlFileName = "sample2.html"
    private val CAMERA_REQUEST = 1888
    private val PERMISSIONS_REQUEST = 123
    private val attachments = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_example_2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI elements using the 'view' parameter
        // For example:
        // val startButton = view.findViewById<View>(R.id.startButton)
        // startButton.setOnClickListener { /* your code here */ }

        AndroidThreeTen.init(requireContext())
        disableStrictMode()

        setLocalizedTexts()

        val startButton: Button = view.findViewById(R.id.startButton)
        startButton.setOnClickListener {
            val htmlObject = readHtmlSample()
            if (htmlObject != "") {
                val jsonObject = getAllJsonValues()
                createIntentWithFiles(htmlObject, jsonObject.toString())
            }
        }

        // Add a click listener for the "Capture Photo" button
        val capturePhotoButton: Button = view.findViewById(R.id.capturePhotoButton)
        capturePhotoButton.setOnClickListener {
            openCamera()
        }
    }

    private fun disableStrictMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }

    private fun createIntentWithFiles(htmlObject: String, jsonObject: String): Intent {
        val uniqueId = LocalDateTime.now().toString() // Generate a unique ID

        // Use requireContext() to get the context of the Fragment
        val externalFilesDir = requireContext().getExternalFilesDir(null)
        val folderPath = File(externalFilesDir, uniqueId)
        if (!folderPath.exists()) {
            folderPath.mkdirs()
        }

        val uriFile = createFile(htmlObject + "PTE_SPLIT_HERE" + jsonObject, folderPath.absolutePath, "workflow_file")

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.component = ComponentName("org.oraclejet.pte", "org.oraclejet.pte.MainActivity")
        intent.type = "*/*"
        intent.putExtra("pte", "pte")
        intent.putExtra("file", uriFile)
        startActivity(Intent.createChooser(intent, "Share files"))

        return intent
    }

    private fun createFile(content: String, folderPath: String, fileName: String) : Uri {
        val filePath = folderPath + File.separator + fileName

        val file = File(filePath)
        file.writeText(content)
        return file.toUri()
        //return FileProvider.getUriForFile(this, "com.pte.clientapptestintegration.fileprovider", file)
    }


    private fun readHtmlSample(): String {
        val assetManager = resources.assets

        try {
            val inputStream = assetManager.open(htmlFileName)
            return inputStream.bufferedReader().use { it.readText() }

        } catch (e: IOException) {
        }
        return ""
    }

    private fun getAllJsonValues(): JsonObject {
        val jsonObject = JsonObject()

        jsonObject.addProperty("formFields", getForms()); //specification in this file
        jsonObject.addProperty("tableRows", getRows()); //specification in this file
        jsonObject.addProperty("eSignatures", getESignatures()); //eSignatures in an object with the key matching the one from the template
        jsonObject.addProperty("checkColumn", "Cost"); //column that has the logic of sum in the table
        jsonObject.addProperty("decimalsInTable", 2); //how many decimals for a dynamic table
        jsonObject.addProperty("margins", getMargins()); //margins of the page, legacy
        jsonObject.addProperty("jsPDFOptions", getJsPDFOptions()); //options for jsPDF
        jsonObject.addProperty("fileName", getFileName()); //options for jsPDF
        jsonObject.addProperty("attachments", getAttachments()); //array of images in base64
        jsonObject.addProperty("styleSheet", ""); //cssFile as string for future implementations

        return jsonObject;
    }

    private fun getFileName(): String {
        val currentDateTime = Date()
        val formatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
        return formatter.format(currentDateTime)
    }

    private fun getAttachments(): String {
        val jsonObject = JSONArray(attachments);
        return jsonObject.toString();
    }

    private fun getJsPDFOptions(): String {
        val jsonData = """
            {
        orientation: 'p',
        unit: 'px',
        format: [1240, 1754],
        //putOnlyUsedFonts: true,
        compress: true,
        floatPrecision: 16
      }
        """.trimIndent()

        val jsonObject = JSONObject(jsonData);
        return jsonObject.toString();
    }

    private fun getMargins(): String {
        val jsonData = """
            {
        top: 40,
        bottom: 200,
        left: 40,
        right: 40,
        userFor: 'page'
      }
        """.trimIndent()

        val jsonObject = JSONObject(jsonData);
        return jsonObject.toString();
    }

    private fun getESignatures(): String {
        val jsonData = """
        [
          {
            id: citizenSignature1,
            data: ""
          },
          {
            id: employeeSignature1,
            data: ""
          }
        ]
    """.trimIndent()

        val citizenName = view?.findViewById<EditText>(R.id.citizenNameInput)?.text.toString()
        val employeeName = view?.findViewById<EditText>(R.id.employeeNameInput)?.text.toString()

        val jsonArray = JSONArray(jsonData);
        jsonArray.getJSONObject(0).put("name", citizenName)
        jsonArray.getJSONObject(1).put("name", employeeName)
        return jsonArray.toString();
    }

    private fun getRows(): String {
        val jsonData = """
            {
            "table1_static": {
              "verification": []
              }
            }
        """.trimIndent()

        val jsonObject = JSONObject(jsonData)
        val table1_static_values = jsonObject.getJSONObject("table1_static").getJSONArray("verification")
        val checkbox1 = view?.findViewById<CheckBox>(R.id.checkbox1)
        val checkbox2 = view?.findViewById<CheckBox>(R.id.checkbox2)
        if (true == checkbox1?.isChecked) {
            table1_static_values.put(checkbox1.tag)
        }
        if (true == checkbox2?.isChecked) {
            table1_static_values.put(checkbox2.tag)
        }

        return jsonObject.toString();
    }

    private fun getForms(): String {
        val jsonData = """
            {
  "AuthorityName": "Власть города Ситивил",
  "Country": "Испания",
  "AddressLine1": "456 Центральная улица",
  "AddressLine2": "Офис 789",
  "CityStateZipCode": "Город Ситивил, CV12345",
  "PhoneNumber": "+1 (555) 123-4567",
  "EmailAddress": "john.doe@example.com",
  "WebsiteURL": "https://cityvilleauthority.example"
}
        """.trimIndent()

        val citizenName = view?.findViewById<EditText>(R.id.citizenNameInput)?.text.toString()
        val employeeName = view?.findViewById<EditText>(R.id.employeeNameInput)?.text.toString()
        val citizenAddress = view?.findViewById<EditText>(R.id.citizenAddressInput)?.text.toString()

        val jsonObject = JSONObject(jsonData)
        jsonObject.put("Date", Date().toString())
        jsonObject.put("FullName", citizenName)
        jsonObject.put("EmployeeFullName", employeeName)
        jsonObject.put("Address", citizenAddress)

        return jsonObject.toString();
    }

    private fun setLocalizedTexts() {
        //val employeeHint = getString(R.string.employee_hint)
        val employeeHint = getString(R.string.employee_hint)
        val citizenNameLabel = getString(R.string.citizen_name_label)
        val acceptText = getString(R.string.accept_radio)
        val declineText = getString(R.string.decline_radio)
        val provideSignaturesText = getString(R.string.provide_signatures)
        val startButtonText = getString(R.string.start_button)

        val employeeInput = view?.findViewById<EditText>(R.id.employeeInput)
        employeeInput?.hint = employeeHint

        val citizenNameLabelView = view?.findViewById<TextView>(R.id.citizenNameLabel)
        citizenNameLabelView?.text = citizenNameLabel

        val acceptRadio = view?.findViewById<RadioButton>(R.id.acceptRadio)
        acceptRadio?.text = acceptText

        val declineRadio = view?.findViewById<RadioButton>(R.id.declineRadio)
        declineRadio?.text = declineText

        val provideSignaturesCheck = view?.findViewById<CheckBox>(R.id.signatureCheck)
        provideSignaturesCheck?.text = provideSignaturesText

        val startButton = view?.findViewById<Button>(R.id.startButton)
        startButton?.text = startButtonText
    }


    private fun openCamera() {
        // Use ActivityCompat.checkSelfPermission with requireActivity() to get the activity context
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Use ActivityCompat.requestPermissions with requireActivity() to request permissions
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), PERMISSIONS_REQUEST)
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap
            val base64Photo = "data:image/jpeg;base64," + encodeToBase64(photo)
            attachments.add(base64Photo)
            // Use the base64Photo as needed
        }
    }

    private fun encodeToBase64(image: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
