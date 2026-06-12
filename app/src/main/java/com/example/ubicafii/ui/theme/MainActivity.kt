package com.example.ubicafii

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.ubicafii.data.repository.EspacioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val repository = EspacioRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}