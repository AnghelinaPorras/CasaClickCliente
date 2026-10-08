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

        import java.text.SimpleDateFormat
        import java.util.Date
        import java.util.Locale
        @Composable fun SyncScreen(vm: SyncViewModel) {
            val operaciones by vm.operaciones.collectAsStateWithLifecycle();val metadata by vm.metadata.collectAsStateWithLifecycle();val ocupado by vm.trabajando.collectAsStateWithLifecycle()
            var descartar by remember { mutableStateOf<Long?>(null) }
            val errores=operaciones.count { it.estado=="ERROR" || (it.estado=="PENDIENTE" && it.mensajeError!=null) }
            val ultima=metadata.filter { it.recurso in listOf("propiedades","visitas") }.maxOfOrNull { it.ultimaSincronizacionExitosa } ?: 0
            LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=24.dp)) {
                item {
                    Text("Tus datos, siempre contigo",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
                    Text("Pendientes: ${operaciones.count { it.estado in listOf("PENDIENTE","ENVIANDO") }} · Errores: ${operaciones.count { it.estado=="ERROR" }}")
                    Text("Última descarga exitosa: "+if(ultima==0L) "Nunca" else SimpleDateFormat("dd/MM/yyyy HH:mm",Locale("es","PE")).format(Date(ultima)),style=MaterialTheme.typography.bodySmall)
                    metadata.find { it.recurso=="estado" }?.let { Text(it.mensaje,Modifier.padding(vertical=8.dp)) }
                    Button(onClick=vm::sincronizar,enabled=!ocupado,modifier=Modifier.fillMaxWidth()) { Text(if(ocupado) "Sincronizando…" else "Sincronizar ahora") }
                    if(BuildConfig.ROL=="administrador") Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick={vm.falla(true)},enabled=!ocupado) { Text("Simular 503") }
                        OutlinedButton(onClick={vm.falla(false)},enabled=!ocupado) { Text("Recuperar API") }
                    }
                    if(errores>0) Text("Errores y conflictos: $errores",color=MaterialTheme.colorScheme.error,modifier=Modifier.padding(top=12.dp))
                    Text("Operaciones e historial",Modifier.padding(top=16.dp),style=MaterialTheme.typography.titleLarge)
                }
                if(operaciones.isEmpty()) item { Text("No hay operaciones pendientes ni historial. Los datos descargados ya están disponibles en Room.") }
                items(operaciones,key={it.id}) { op -> Card(colors=CardDefaults.cardColors(containerColor=Color.White)) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Text("${op.entidad.replaceFirstChar { it.uppercase() }} · ${op.tipoOperacion} · #${op.idEntidadLocal}",fontWeight=FontWeight.Bold)
                    SyncStatusIndicator(op.estado)
                    Text("UUID: ${op.uuidOperacion}",style=MaterialTheme.typography.labelSmall)
                    Text("Intentos: ${op.intentos}",style=MaterialTheme.typography.labelSmall)
                    op.codigoHttp?.let { Text("Respuesta HTTP: $it",style=MaterialTheme.typography.labelMedium) }
                    op.mensajeError?.let { Text(it,color=if(op.estado=="ERROR") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface) }
                    if(op.codigoHttp==409) Text("Corrige fecha/hora desde Visitas, o descarta el cambio para recuperar la versión remota.",style=MaterialTheme.typography.bodySmall)
                    if(op.estado=="ERROR" || (op.estado=="PENDIENTE" && op.mensajeError!=null)) TextButton(onClick={vm.reintentar(op.id)},enabled=!ocupado) { Text("Reintentar") }
                    if(op.estado in listOf("ERROR","PENDIENTE")) TextButton(onClick={descartar=op.id},enabled=!ocupado) { Text("Descartar mi cambio") }
                    if(op.estado=="SINCRONIZADO") TextButton(onClick={vm.reenviar(op.id)},enabled=!ocupado) { Text("Reenviar (prueba de duplicado)") }
                } } }
            }
            descartar?.let { id -> AlertDialog(onDismissRequest={descartar=null},title={Text("Descartar mi cambio")},text={Text("Se consultará la versión remota antes de descartar un envío. Una creación que nunca se envió se elimina solo de este celular.")},confirmButton={TextButton(onClick={descartar=null;vm.descartar(id)}) { Text("Descartar") }},dismissButton={TextButton(onClick={descartar=null}) { Text("Volver") }}) }
        }
