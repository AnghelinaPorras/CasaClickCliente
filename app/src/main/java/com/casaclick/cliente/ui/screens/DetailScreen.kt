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

        @Composable fun DetailScreen(id: Long, vm: PropiedadViewModel, navegar: (String) -> Unit, volver: () -> Unit) {
            val lista by vm.todas.collectAsStateWithLifecycle();val favoritos by vm.favoritos.collectAsStateWithLifecycle()
            val p=lista.find { it.id==id };var eliminar by remember { mutableStateOf(false) }
            if(p==null) { Text("Propiedad no disponible en tu catálogo local.",Modifier.padding(24.dp));return }
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
                Text(p.titulo,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
                Text("${p.distrito} · ${p.tipoOperacion}",color=MaterialTheme.colorScheme.primary)
                var imagen by rememberSaveable { mutableIntStateOf(0) }
                FotoPropiedad(p.imagenes.getOrNull(imagen),Modifier.fillMaxWidth().height(235.dp),imagen)
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { (0 until maxOf(3,p.imagenes.size)).forEach { i -> FilterChip(selected=imagen==i,onClick={imagen=i},label={Text("Foto ${i+1}")}) } }
                Text(soles(p.precio)+(if(p.tipoOperacion=="ALQUILER") " / mes" else ""),style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
                Text("${p.areaM2.toInt()} m² · ${p.habitaciones} habitaciones",style=MaterialTheme.typography.titleMedium)
                Text(p.descripcion);SyncStatusIndicator(p.estadoSync)
                SyncDetailsCard(p.id,p.idRemoto,p.uuid,p.version,p.estadoSync)
                Text("Las fotografías son referenciales. Si no se cargan, se muestra una ilustración local.",style=MaterialTheme.typography.bodySmall)
                if(BuildConfig.ROL=="cliente") {
                    Button(onClick={navegar("agenda/${p.id}/0")},modifier=Modifier.fillMaxWidth()) { Text("Agendar visita") }
                    OutlinedButton(onClick={vm.favorito(p.id)},modifier=Modifier.fillMaxWidth()) { Text(if(p.id in favoritos) "Quitar de favoritas" else "Guardar como favorita") }
                } else {
                    Button(onClick={navegar("form/${p.id}")},modifier=Modifier.fillMaxWidth()) { Text("Editar propiedad") }
                    OutlinedButton(onClick={eliminar=true},modifier=Modifier.fillMaxWidth()) { Text("Eliminar propiedad") }
                }
            }
            if(eliminar) AlertDialog(onDismissRequest={eliminar=false},title={Text("Eliminar propiedad")},text={Text("La eliminación quedará pendiente hasta ser aceptada. El servidor la rechazará si existen visitas activas.")},confirmButton={TextButton(onClick={eliminar=false;vm.eliminar(id,volver)}) { Text("Eliminar") }},dismissButton={TextButton(onClick={eliminar=false}) { Text("Volver") }})
        }
