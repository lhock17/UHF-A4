package com.example.uhf_background

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.uhf_background.CheckAppRunning  // Add this import statement
import com.example.uhf_background.ui.theme.UhfbackgroundTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start Background Service
        Toast.makeText(
            applicationContext, "Starting background checking",
            Toast.LENGTH_SHORT
        ).show()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    kotlin.arrayOf<kotlin.String?>(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    0
                )
            }
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    kotlin.arrayOf<kotlin.String?>(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
            }
        }

        val context = applicationContext
        var intent: Intent? = Intent(context, CheckAppRunning::class.java)
        CheckAppRunning.enqueueWork(context, intent)

        setContent {
            UhfbackgroundTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Checking if UHF scanner running",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UhfbackgroundTheme {
        Greeting("Android")
    }
}
