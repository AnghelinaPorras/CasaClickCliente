package com.casaclick.cliente.ui.screens
            import androidx.compose.foundation.*
            import androidx.compose.foundation.layout.*
            import androidx.compose.foundation.lazy.LazyColumn
            import androidx.compose.foundation.lazy.items
            import androidx.compose.foundation.lazy.grid.*
            import androidx.compose.foundation.shape.RoundedCornerShape
            import androidx.compose.material.icons.Icons
            import androidx.compose.material.icons.filled.*
            import androidx.compose.material3.*
            import androidx.compose.runtime.*
            import androidx.compose.runtime.saveable.rememberSaveable
            import androidx.compose.ui.*
            import androidx.compose.ui.graphics.Color
            import androidx.compose.ui.text.font.FontWeight
            import androidx.compose.ui.text.input.*
            import androidx.compose.ui.unit.dp
            import androidx.compose.ui.layout.ContentScale
            import androidx.lifecycle.compose.collectAsStateWithLifecycle
            import com.casaclick.cliente.BuildConfig
            import com.casaclick.cliente.viewmodel.*
            import com.casaclick.cliente.model.*
            import com.casaclick.cliente.ui.components.*

        @Composable fun SplashScreen() {
            Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally) {
                Icon(Icons.Default.Home,contentDescription=null,tint=Color.White,modifier=Modifier.size(90.dp))
                Text("CasaClick",style=MaterialTheme.typography.displaySmall,color=Color.White,fontWeight=FontWeight.Bold)
                Text("PERÚ",color=Color(0xFFFFDF9A),style=MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(28.dp));Text("Un lugar para tu próxima historia",color=Color.White)
            }
        }
