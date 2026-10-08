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

        @Composable fun LoginScreen(vm: AuthViewModel) {
            val estado by vm.estado.collectAsStateWithLifecycle()
            var correo by rememberSaveable { mutableStateOf("") };var clave by remember { mutableStateOf("") }
            var url by rememberSaveable { mutableStateOf(vm.url) }
            Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).imePadding().padding(28.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                Spacer(Modifier.height(28.dp))
                Icon(Icons.Default.Home,contentDescription=null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(64.dp))
                Text("CasaClick Perú",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold)
                Text(if(BuildConfig.ROL=="cliente") "Encuentra tu próximo hogar" else "Tu oficina inmobiliaria",style=MaterialTheme.typography.titleMedium)
                Text(if(BuildConfig.ROL=="cliente") "Acceso para clientes" else "Acceso para asesores",color=MaterialTheme.colorScheme.primary)
                OutlinedTextField(correo,{correo=it},label={Text("Correo")},singleLine=true,keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=KeyboardType.Email),modifier=Modifier.fillMaxWidth())
                OutlinedTextField(clave,{clave=it},label={Text("Contraseña")},singleLine=true,visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
                Button(onClick={vm.login(correo,clave,url)},enabled=!estado.cargando,modifier=Modifier.fillMaxWidth().height(52.dp)) { if(estado.cargando) CircularProgressIndicator(Modifier.size(22.dp),color=Color.White,strokeWidth=2.dp) else Text("Ingresar") }
                if(estado.mensaje.isNotBlank()) Text(estado.mensaje,color=MaterialTheme.colorScheme.error)
                HorizontalDivider()
                Text("Conexión con CasaClick",style=MaterialTheme.typography.labelLarge)
                OutlinedTextField(url,{url=it},label={Text("URL de la API")},singleLine=true,keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=KeyboardType.Uri),modifier=Modifier.fillMaxWidth())
                Text("El primer ingreso requiere conexión. Después podrás acceder con la misma cuenta y consultar lo que ya descargaste.",style=MaterialTheme.typography.bodySmall)
                Text("Anghelina Porras Torres · Evaluación parcial",style=MaterialTheme.typography.labelSmall)
            }
        }
