package com.lazykernel.korurureader

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import com.lazykernel.korurureader.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var context: Context
    }

    private var imageUri: Uri? = null
    private var isFABOpen: Boolean = false
    private val viewModel: ImageViewModel by viewModels()

    private var baseFAB: FloatingActionButton? = null
    private var cameraFAB: FloatingActionButton? = null
    private var selectFAB: FloatingActionButton? = null

    private var takePicture: ActivityResultLauncher<Uri>? = null
    private var pickPicture: ActivityResultLauncher<Array<out String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        init()
        setupFABs()
        registerActivityResults()

        val existingUri = savedInstanceState?.getParcelable<Uri>("imageUri")
        if (existingUri != null) {
            runOCRAndDisplay(existingUri)
        }
    }

    override fun onDestroy() {
        FileUtil.instance.deleteTempFiles()
        super.onDestroy()
    }

    private fun init() {
        context = this

        NotificationUtil.instance.createNotificationChannel()
        NotificationUtil.instance.buildScreenshotNotification(Intent(this, TakeScreenshotActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        NotificationUtil.instance.showScreenshotNotification()
    }

    private fun setupFABs() {
        cameraFAB = findViewById<FloatingActionButton>(R.id.fab_camera).apply {
            setOnClickListener { _ ->
                toggleFABMenu()
                val file = FileUtil.instance.createTempFile("tmpimg_${DateUtil.instance.stringTimestamp}_", ".jpg")
                imageUri = FileProvider.getUriForFile(this@MainActivity, context.applicationContext.packageName + ".provider", file)
                takePicture?.launch(imageUri)
            }
        }
        selectFAB = findViewById<FloatingActionButton>(R.id.fab_select).apply {
            setOnClickListener { _ ->
                toggleFABMenu()
                pickPicture?.launch(Array(1) { "image/*" })
            }
        }
        baseFAB = findViewById<FloatingActionButton>(R.id.fab_base).apply {
            setOnClickListener { _ -> toggleFABMenu() }
        }
    }

    private fun registerActivityResults() {
        takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                runOCRAndDisplay(imageUri)
            }
            else {
                Toast.makeText(this@MainActivity, "No picture taken", Toast.LENGTH_SHORT).show()
            }
        }

        pickPicture = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri == null) {
                Toast.makeText(this@MainActivity, "No picture selected", Toast.LENGTH_SHORT).show()
            }
            else {
                runOCRAndDisplay(uri)
            }
        }
    }

    private fun runOCRAndDisplay(uri: Uri?) {
        val text = uri?.let {
            val tesseractUtil = TesseractUtil()
            viewModel.setImage(it)
            tesseractUtil.setImage(it)
            viewModel.setTextRegions(tesseractUtil.getTextBlockRegions())
            val strings = tesseractUtil.extractTextFromImage()
            tesseractUtil.destroy()
            strings
        }

        println(text)
        text?.let { viewModel.setParsedText(it.fold("") { acc, s -> "$acc\n\n$s" }) }
    }

    private fun toggleFABMenu() {
        if (isFABOpen) {
            isFABOpen = false
            baseFAB?.animate()?.rotation(0f)
            cameraFAB?.animate()?.translationY(0f)
            selectFAB?.animate()?.translationY(0f)
        }
        else {
            isFABOpen = true
            baseFAB?.animate()?.rotationBy(180f)
            cameraFAB?.animate()?.translationY(-resources.getDimension(R.dimen.standard_55))
            selectFAB?.animate()?.translationY(-resources.getDimension(R.dimen.standard_105))
        }
    }

    override fun onBackPressed() {
        if (isFABOpen) {
            toggleFABMenu()
        }
        else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}