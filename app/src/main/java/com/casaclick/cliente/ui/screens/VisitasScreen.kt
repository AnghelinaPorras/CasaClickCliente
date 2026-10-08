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

        @Composable fun VisitasScreen(vm: VisitaViewModel, pvm: PropiedadViewModel, navegar: (String) -> Unit) {
            val visitas by vm.visitas.collectAsStateWithLifecycle();val propiedades by pvm.todas.collectAsStateWithLifecycle();val fecha by vm.fechaFiltro.collectAsStateWithLifecycle()
            var confirmar by remember { mutableStateOf<Long?>(null) }
            var detalleSync by remember { mutableStateOf<Visita?>(null) }
            Column(Modifier.fillMaxSize().padding(horizontal=16.dp)) {
                OutlinedTextField(fecha,{vm.fechaFiltro.value=it},label={Text("Filtrar por fecha · AAAA-MM-DD")},modifier=Modifier.fillMaxWidth(),singleLine=true,trailingIcon={if(fecha.isNotEmpty()) IconButton(onClick={vm.fechaFiltro.value=""}) { Icon(Icons.Default.Close,"Limpiar fecha") }})
                Text("${visitas.size} visitas",Modifier.padding(vertical=12.dp),style=MaterialTheme.typography.titleMedium)
                if(visitas.isEmpty()) Text("No hay visitas guardadas para esta fecha. Sincroniza o agenda una desde el detalle de una propiedad.")
                LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=16.dp)) {
                    items(visitas,key={it.id}) { v -> Card(colors=CardDefaults.cardColors(containerColor=Color.White)) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text(propiedades.find { it.id==v.propiedadId }?.titulo ?: "Propiedad #${v.propiedadId}",fontWeight=FontWeight.Bold)
                        Text("${v.fecha} · ${v.hora} · Lima")
                        if(BuildConfig.ROL=="administrador") Text("Cliente: ${v.clienteNombre}")
                        Text(v.estado,color=if(v.estado=="PROVISIONAL") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary);SyncStatusIndicator(v.estadoSync)
                        if(v.estadoSync!="SINCRONIZADO") Text("El servidor aún no ha aceptado este cambio.",style=MaterialTheme.typography.bodySmall)
                        TextButton(onClick={detalleSync=v}) { Text("Ver datos de sincronización") }
                        if(BuildConfig.ROL=="cliente") {
                            if(v.estadoSync in listOf("ERROR","SINCRONIZADO") && v.estado !in listOf("REALIZADA","CANCELADA")) TextButton(onClick={navegar("agenda/${v.propiedadId}/${v.id}")}) { Text(if(v.estadoSync=="ERROR") "Corregir fecha/hora" else "Reprogramar") }
                            if(v.estadoSync=="SINCRONIZADO" && v.estado !in listOf("REALIZADA","CANCELADA")) TextButton(onClick={confirmar=v.id}) { Text("Cancelar visita") }
                        } else if(v.estadoSync=="SINCRONIZADO") {
                            Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("CONFIRMADA","REALIZADA","CANCELADA").filter { it!=v.estado }.forEach { estado -> OutlinedButton(onClick={vm.estado(v.id,estado)}) { Text(estado.lowercase().replaceFirstChar { it.uppercase() }) } } }
                        }
                    } } }
                }
            }
            detalleSync?.let { v -> AlertDialog(onDismissRequest={detalleSync=null},title={Text("Sincronización de la visita")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)) { Text("${v.fecha} · ${v.hora} · ${v.estado}");SyncDetailsCard(v.id,v.idRemoto,v.uuid,v.version,v.estadoSync) }},confirmButton={TextButton(onClick={detalleSync=null}) { Text("Volver") }}) }
            confirmar?.let { id -> AlertDialog(onDismissRequest={confirmar=null},title={Text("Cancelar visita")},text={Text("La cancelación se confirmará al sincronizar.")},confirmButton={TextButton(onClick={confirmar=null;vm.estado(id,"CANCELADA")}) { Text("Cancelar visita") }},dismissButton={TextButton(onClick={confirmar=null}) { Text("Volver") }}) }
        }
