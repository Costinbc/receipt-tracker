package com.example.receipttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.receipttracker.ui.AppNavigation
import com.example.receipttracker.ui.theme.ReceiptTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReceiptTrackerTheme {
                AppNavigation()
            }
        }
    }
}
