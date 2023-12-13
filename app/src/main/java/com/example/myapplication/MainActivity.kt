package com.example.myapplication

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import retrofit2.Call
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import android.Manifest



class MainActivity : AppCompatActivity() {
    private lateinit var questionEditText: EditText
    private val optionEditTexts = ArrayList<EditText>()
    private val correctCheckBoxes = ArrayList<CheckBox>()
    private lateinit var saveButton: Button
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var selectedFileUri : Uri
    private val READ_STORAGE_PERMISSION_CODE = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        questionEditText = findViewById(R.id.questionEditText)

        for (i in 1..4) {
            optionEditTexts.add(findViewById(resources.getIdentifier(
                "optionEditText$i", "id", packageName)))
            correctCheckBoxes.add(findViewById(resources.getIdentifier(
                "correctCheckBox$i", "id", packageName)))
        }

        saveButton = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            saveQuestions()
        }

        val uploadImageButton: Button = findViewById(R.id.uploadButton)
        uploadImageButton.setOnClickListener {
            openFilePicker()
        }
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_STORAGE_PERMISSION_CODE
            )
        } else {


        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with file access

            } else {
                // Permission denied, handle accordingly (e.g., show a message or request again)
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openFilePicker() {
        requestStoragePermission()
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedFileUri = data.data!!
        }
    }

    private fun getRealPathFromURI(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri!!, projection, null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        val path = cursor?.getString(columnIndex!!)
        cursor?.close()
        return path
    }

    private val BASE_URL = "https://applock.jsimple.com/api/"
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: ApiService = retrofit.create(ApiService::class.java)

    private fun saveQuestions() {

        val userId = RequestBody.create(MultipartBody.FORM, "130")
        val levelId = RequestBody.create(MultipartBody.FORM, "1")
        val que = RequestBody.create(MultipartBody.FORM, questionEditText.text.toString())
        print("question--> ${questionEditText.text.toString()}")
        val options = ArrayList<String>()
        val correctAnswers = ArrayList<Int>()
        for (i in 0 until 4) {
            options.add(optionEditTexts[i].text.toString())
            correctAnswers.add(if (correctCheckBoxes[i].isChecked) 1 else 0)
        }
        val choice = RequestBody.create(MultipartBody.FORM, "${options[0]}#${correctAnswers[0]},${options[1]}#${correctAnswers[1]},${options[2]}#${correctAnswers[2]},${options[3]}#${correctAnswers[3]}")
        val selectedFilePath: String? = getRealPathFromURI(selectedFileUri)
        val file = File(selectedFilePath)
        val requestFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
        val imagePart = MultipartBody.Part.createFormData("filename", file.name, requestFile)

        val call: Call<Void> = apiService.uploadFile(userId, levelId, que, choice,  imagePart)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    println("response --> $response")
                    Toast.makeText(this@MainActivity, "Question Added Successfully", Toast.LENGTH_SHORT).show()
                } else {

                    Toast.makeText(this@MainActivity, "Error adding question", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error adding questionsssssss", Toast.LENGTH_SHORT).show()
            }
        })
    }
}


