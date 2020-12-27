package com.lazykernel.korurureader

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.lazykernel.korurureader.util.DateUtil
import com.lazykernel.korurureader.util.FileUtil
import com.lazykernel.korurureader.util.NotificationUtil

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var context: Context
    }

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        init()

        // https://medium.com/swlh/intro-to-androidx-activity-result-apis-taking-a-picture-6013c3852c0b
        val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {

            }
            else {
                Toast.makeText(this@MainActivity, "Permission for taking a picture denied", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            val file = FileUtil.instance.createTempFile("tmpimg_${DateUtil.instance.stringTimestamp}_", ".jpg")
            imageUri = file.toUri()
            takePicture.launch(imageUri)
        }
    }

    private fun init() {
        context = this
        // NotificationUtil.instance.createNotificationChannel()
        // TODO: create screenshot intent for notification
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