package com.lvsmsmch.deckbuilder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lvsmsmch.deckbuilder.presentation.DeckBuilderRoot

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DeckBuilderRoot(onExitApp = { finish() })
        }
    }
}
