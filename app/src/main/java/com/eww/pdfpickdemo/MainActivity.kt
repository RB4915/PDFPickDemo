package com.eww.pdfpickdemo

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    val REQUEST_CODE_INT_PICK = 112
    val REQUEST_CODE_INT_PERMISSION = 113
    val REQUEST_CODE = "request_code"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        val btn_pick: Button = findViewById(R.id.btn_pick)

        btn_pick.setOnClickListener {
            if (!checkPermission()) {
                requestPermission()
            }else{
                pickPdfRequest()
            }


        }
    }

    private fun checkPermission(): Boolean {
        return if (SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val result =
                ContextCompat.checkSelfPermission(this@MainActivity, READ_EXTERNAL_STORAGE)
            val result1 =
                ContextCompat.checkSelfPermission(this@MainActivity, WRITE_EXTERNAL_STORAGE)
            result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
        }
    }




    private fun requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data =
                    Uri.parse(String.format("package:%s", applicationContext.packageName))
                intent.putExtra(REQUEST_CODE, REQUEST_CODE_INT_PERMISSION)
                resultPermissionRequest.launch(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                intent.putExtra(REQUEST_CODE, REQUEST_CODE_INT_PERMISSION)
                resultPermissionRequest.launch(intent)
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_INT_PERMISSION
            )
        }
    }

    var resultPickRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val uri = data?.data


                val takeFlags: Int? = (data?.flags?.and(
                    (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                ))
                // Check for the freshest data.
                contentResolver.takePersistableUriPermission(uri!!, takeFlags!!)


                data.data?.also { documentUri ->

                    /**
                     * Upon getting a document uri returned, we can use
                     * [ContentResolver.takePersistableUriPermission] in order to persist the
                     * permission across restarts.
                     *
                     * This may not be necessary for your app. If the permission is not
                     * persisted, access to the uri is granted until the receiving Activity is
                     * finished. You can extend the lifetime of the permission grant by passing
                     * it along to another Android component. This is done by including the uri
                     * in the data field or the ClipData object of the Intent used to launch that
                     * component. Additionally, you need to add FLAG_GRANT_READ_URI_PERMISSION
                     * and/or FLAG_GRANT_WRITE_URI_PERMISSION to the Intent.
                     *
                     * This app takes the persistable URI permission grant to demonstrate how, and
                     * to allow us to reopen the last opened document when the app starts.
                     */
                    contentResolver.takePersistableUriPermission(
                        documentUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    Log.e("Hellodata", "URIII    " + documentUri)
                    Log.e("Hellodata", "URIII    " + MediaUtility.getPath(this, documentUri))

                }
            }

        }


    var resultPermissionRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                if (SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        // perform action when allow permission success
                        pickPdfRequest()
                    } else {
                        Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

        }


    private fun pickPdfRequest(){
        if (Build.VERSION.SDK_INT < 19) {
            val intent = Intent()
            intent.type = "application/pdf"
            intent.action = Intent.ACTION_GET_CONTENT
            intent.putExtra(REQUEST_CODE, REQUEST_CODE_INT_PICK)
            resultPickRequest.launch(intent)

        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/pdf"
            intent.putExtra(REQUEST_CODE, REQUEST_CODE_INT_PICK)
            resultPickRequest.launch(intent)
        }
    }
}