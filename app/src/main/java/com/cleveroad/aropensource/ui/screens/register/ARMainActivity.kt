package com.cleveroad.aropensource.ui.screens.register;


import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cleveroad.aropensource.R
import com.kairos.Kairos
import com.kairos.KairosListener
import org.json.JSONException
import java.io.UnsupportedEncodingException

public class ARMainActivity : AppCompatActivity() {


    val app_id = "40da4fe1"
    val api_key = "dbd14fe5b866741bd20e0fe91e41456e"
    //IMPORTANT ------------------------------------------------------------------------------

    //IMPORTANT ------------------------------------------------------------------------------
    var detlistener: KairosListener? = null
    var identlistener: KairosListener? = null

    lateinit var enroll: Button
    var identify: Button? = null
    var imageView: ImageView? = null
    var image: Bitmap? = null
    var myKairos: Kairos? = null
    val galleryId = "people"

    var info: TextView? = null
    var name: EditText? = null
    val minHeadScale = "0.1"
    val multipleFaces = "false"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_ar)
        // instantiate a new kairos instance
        myKairos = Kairos()
        this@ARMainActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        imageView = findViewById(R.id.imageview)
        enroll = findViewById(R.id.enroll)
        identify = findViewById(R.id.identify)
        info = findViewById(R.id.info)

        // set authentication
        myKairos!!.setAuthentication(this, app_id, api_key)
        name = findViewById(R.id.name)

        // Create an instance of the KairosListener
        detlistener = object : KairosListener {
            override fun onSuccess(response: String) {
                makeToast("Success! Registered the face")
                info!!.text = "SUCCESS!"
            }

            override fun onFail(response: String) {
                makeToast("Fail: $response")
                info!!.text = "Fail: $response"
            }
        }
        identlistener = object : KairosListener {
            override fun onSuccess(response: String) {
                makeToast("Success! You are: " + readJSONForName(response))
                info!!.setText(readJSONForName(response))
            }

            override fun onFail(response: String) {
                makeToast("Fail: $response")
                info!!.text = "Fail: $response"
            }
        }
        enroll!!.setOnClickListener {
            try {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        !== PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@ARMainActivity, arrayOf(Manifest.permission.CAMERA), 100)
                } else {
                    if (name!!.text.toString().isEmpty() || name!!.text.toString() == null) {
                        longToast("Please enter a name before enrolling the face.")
                    } else {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(cameraIntent, 2)
                    }
                }
            } catch (e: Exception) {
                makeToast("Error Message: " + e.message + ". Cause: " + e.cause)
            }
        }
        identify!!.setOnClickListener(View.OnClickListener {
            try {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        !== PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@ARMainActivity, arrayOf(Manifest.permission.CAMERA), 100)
                } else {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, 1)
                }
                //       identifyImage();
            } catch (e: Exception) {
                makeToast("Error Message: " + e.message + ". Cause: " + e.cause)
                info!!.text = "Error Message: " + e.message + ". Cause: " + e.cause
            }
        })
    }

    open fun readJSONForName(response: String): String? {
        var match = ""
        val location = response.indexOf("subject_id")
        match = response.substring(location + 13)
        var name: String? = ""
        for (i in 0 until match.length) {
            name += if (match[i] == '"') {
                break
            } else {
                Character.toString(match[i])
            }
        }
        return name
    }

    @Throws(UnsupportedEncodingException::class, JSONException::class)
    open fun identifyImage() {
        val selector = "FULL"
        myKairos!!.recognize(image,
                galleryId,
                selector,
                null,
                minHeadScale,
                null,
                identlistener)
/*
        myKairos!!.detect(image,
                selector,
                minHeadScale,
                identlistener)*/
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = getMenuInflater()
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.info) {
            AlertDialog.Builder(this@ARMainActivity)
                    .setTitle("About This App")
                    .setMessage("This app makes use of the Kairos SDK for Android to implement facial recognition. First, a user must take a picture of a person. Once they have, they can enter a name and click the enroll button to register that person's face. Finally, they can take another picture and use the identify button for recognizing the face.")
                    .setCancelable(true)
                    .setNeutralButton("OK") { dialog, which -> dialog.cancel() }.show()
        }
        return super.onOptionsItemSelected(item)
    }

    @Throws(UnsupportedEncodingException::class, JSONException::class)
    open fun enrollImage() {
        if (name!!.text.toString() == null) {
            makeToast("No name entered")
        } else {
            val subjectId = name!!.text.toString()
            val selector = "FULL"
            myKairos!!.enroll(image,
                    subjectId,
                    galleryId,
                    selector,
                    multipleFaces,
                    minHeadScale,
                    detlistener)
        }
    }

    /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
     }*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            image = data.extras["data"] as Bitmap
            imageView!!.setImageBitmap(image)
            try {
                enrollImage()
            } catch (e: Exception) {
                longToast("ERROR: $e")
            }
        } else if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            image = data.extras["data"] as Bitmap
            imageView!!.setImageBitmap(image)
            try {
                identifyImage()
            } catch (e: Exception) {
                longToast("ERROR: $e")
            }
        }
    }

    open fun makeToast(s: String) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show()
    }

    open fun longToast(s: String) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show()
    }
}
