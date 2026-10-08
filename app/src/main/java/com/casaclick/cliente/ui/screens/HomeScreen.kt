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

        @Composable fun HomeScreen(nombre: String, pvm: PropiedadViewModel, vvm: VisitaViewModel, svm: SyncViewModel, navegar: (String) -> Unit) {
            val propiedades by pvm.todas.collectAsStateWithLifecycle();val visitas by vvm.todas.collectAsStateWithLifecycle();val operaciones by svm.operaciones.collectAsStateWithLifecycle()
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
                Text("Hola, ${nombre.substringBefore(' ')}",style=MaterialTheme.typography.titleMedium)
                Text(if(BuildConfig.ROL=="cliente") "Tu próximo hogar\nempieza aquí." else "Conecta personas\ncon su nuevo hogar.",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold)
                Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer),shape=RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                        CasaIlustracion(Modifier.fillMaxWidth().height(145.dp))
                        Text(if(BuildConfig.ROL=="cliente") "Explora Lima a tu ritmo" else "CasaClick · Panel del asesor",style=MaterialTheme.typography.titleLarge)
                        Text("${propiedades.size} propiedades en tu catálogo local")
                        Button(onClick={pvm.filtros.value=Filtros();navegar("lista")}) { Text(if(BuildConfig.ROL=="cliente") "Buscar propiedades" else "Gestionar propiedades") }
                    }
                }
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    Card(Modifier.weight(1f)) { Column(Modifier.padding(16.dp)) { Text("${visitas.size}",style=MaterialTheme.typography.headlineMedium);Text("Visitas") } }
                    Card(Modifier.weight(1f)) { Column(Modifier.padding(16.dp)) { Text("${operaciones.count { it.estado in listOf("PENDIENTE","ENVIANDO","ERROR") }}",style=MaterialTheme.typography.headlineMedium);Text("Por sincronizar") } }
                }
                if(BuildConfig.ROL=="cliente") OutlinedButton(onClick={pvm.filtros.value=Filtros(favoritas=true);navegar("lista")},modifier=Modifier.fillMaxWidth()) { Icon(Icons.Default.FavoriteBorder,null);Spacer(Modifier.width(8.dp));Text("Mis propiedades favoritas") }
                else Button(onClick={navegar("form/0")},modifier=Modifier.fillMaxWidth()) { Text("Registrar una propiedad") }
                OutlinedButton(onClick={navegar("sync")},modifier=Modifier.fillMaxWidth()) { Text("Revisar sincronización") }
                Text("Los datos se consultan desde este celular. Las visitas y los cambios pendientes se confirman cuando el servidor los acepta.",style=MaterialTheme.typography.bodySmall)
            }
        }
