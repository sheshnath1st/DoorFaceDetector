package com.cleveroad.aropensource.ui.screens.main

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import com.cleveroad.arfacedetector.ui.screens.main.mlkit.FaceDetectListener
import com.cleveroad.arfacedetector.ui.screens.main.mlkit.FaceDetectorFragment
import com.cleveroad.arfacedetector.ui.screens.main.mlkit.common.CameraSource.waitForNextFrame
import com.cleveroad.aropensource.R
import com.cleveroad.aropensource.ui.base.BaseLifecycleActivity
import com.cleveroad.aropensource.ui.screens.dialogs.CDConstants
import com.cleveroad.aropensource.ui.screens.dialogs.CDialog
import com.cleveroad.aropensource.ui.screens.main.chooser.InstrumentsCallback
import com.cleveroad.aropensource.ui.screens.main.chooser.InstrumentsFragment
import com.cleveroad.aropensource.ui.screens.register.ARMainActivity
import com.kairos.Kairos
import com.kairos.KairosListener
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseLifecycleActivity(), InstrumentsCallback, FaceDetectListener {

    companion object {
        fun start(context: Context) = context.run {
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }.let {
                startActivity(it)
            }
        }
    }

    override val containerId = R.id.container

    override val layoutId = R.layout.activity_main

    //Text To Speech
    lateinit var mTTS: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showInstrumentsScreen()
        myKairos = Kairos()
        // set authentication
        myKairos!!.setAuthentication(this, app_id, api_key)
        identlistener = object : KairosListener {
            override fun onSuccess(response: String) {
                runOnUiThread(Runnable {
                    var name = readJSONForName(response)
                    Toast.makeText(this@MainActivity, "Welcome $name", Toast.LENGTH_LONG).show()
                    speakOut("Welcome $name")
                    if (name != null) {
                        showDialog(name,"Android Developer","200","4.5/5")
                    }
                    waitForNextFrame = false
                })
            }

            override fun onFail(response: String) {
                runOnUiThread(Runnable {
                    Toast.makeText(this@MainActivity, "Failed $response", Toast.LENGTH_LONG).show()
                    waitForNextFrame = false
                })
            }
        }

        mTTS = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                //if there is no error then set language
                mTTS.language = Locale.UK
            }
        })
    }

    private fun speakOut(toSpeak: String) {
        if (toSpeak == "" || toSpeak.trim() == "Welcome") {
            //if there is no text in edit text
            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show()
            mTTS.speak("Try again", TextToSpeech.QUEUE_FLUSH, null)
        } else {
            //if there is text in edit text
            Toast.makeText(this, toSpeak, Toast.LENGTH_SHORT).show()
            mTTS.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    private fun showInstrumentsScreen() {
        replaceFragment(InstrumentsFragment.newInstance(), false)
    }

    override fun mlKitlSelected() {
        replaceFragment(FaceDetectorFragment.newInstance(R.drawable.ic_joincleveroad_medium, this))
    }

    override fun arCoreSelected() {
//        replaceFragment(AugmentedFacesFragment.newInstance(R.drawable.ic_joincleveroad_medium))
        startActivity(Intent(this, ARMainActivity::class.java))
    }

    var myKairos: Kairos? = null
    var bitmap:Bitmap?=null
    override fun onSuccess(var1: ArrayList<Bitmap>?) {
        val arrayCopy: ArrayList<Bitmap> = ArrayList(var1)
        runOnUiThread(Runnable {
//            Toast.makeText(this, "Success butmap " + arrayCopy.size, Toast.LENGTH_SHORT).show()
            val selector = "FULL"
            if (arrayCopy.size > 0) {
                bitmap= arrayCopy[arrayCopy.size - 1]
                myKairos!!.recognize(bitmap,
                        galleryId,
                        selector,
                        null,
                        minHeadScale,
                        null,
                        identlistener)

                /*   myKairos!!.detect(arrayCopy.get(arrayCopy.size - 1),
                           selector,
                           minHeadScale,
                           identlistener)*/

            } else {
                waitForNextFrame = false
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

    override fun onPause() {
        if (mTTS.isSpeaking) {
            //if speaking then stop
            mTTS.stop()
            //mTTS.shutdown()
        }
        super.onPause()
    }

    override fun onFail(var1: String?) {
//        TODO("Not yet implemented")
    }

    val app_id = "40da4fe1"
    val api_key = "dbd14fe5b866741bd20e0fe91e41456e"
    val galleryId = "people"
    val minHeadScale = "0.1"
    val multipleFaces = "false"
    var identlistener: KairosListener? = null


    private fun showDialog(name: String,desc: String,reward: String,rating: String) {
        if (name == "" || name.trim() == "Welcome"){
            return
        }
        CDialog(this,bitmap).createFaceAlert(name,desc,reward,rating,bitmap,
                CDConstants.SUCCESS, CDConstants.LARGE)
                .setAnimation(CDConstants.SCALE_FROM_BOTTOM_TO_TOP)
                .setDuration(3000)
//                .setTextSize(CDConstants.LARGE_TEXT_SIZE)
                .show()
    }


}