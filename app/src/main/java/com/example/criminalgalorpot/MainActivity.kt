package com.example.criminalgalorpot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.criminalgalorpot.ui.CriminalIntentApp
import com.example.criminalgalorpot.ui.theme.CriminalGalorpotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CriminalGalorpotTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CriminalIntentApp()
                }
            }
        }
    }
}