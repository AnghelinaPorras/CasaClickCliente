package com.casaclick.cliente
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.casaclick.cliente.ui.theme.CasaClickTheme
import com.casaclick.cliente.ui.navigation.AppNavigation
import com.casaclick.cliente.viewmodel.CasaClickFactory
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);enableEdgeToEdge()
        val factory=CasaClickFactory((application as CasaClickApplication).container)
        setContent { CasaClickTheme { AppNavigation(factory) } }
    }
}
