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

        import com.casaclick.cliente.data.local.entities.PropiedadEntity
        import com.google.gson.Gson
        @Composable fun FormScreen(id: Long, vm: PropiedadViewModel, volver: () -> Unit) {
            LaunchedEffect(id) { vm.cargarFormulario(id) }
            val p by vm.formulario.collectAsStateWithLifecycle()
            p?.let { actual -> PropiedadForm(actual,vm,volver) } ?: Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) { CircularProgressIndicator() }
        }
        @Composable private fun PropiedadForm(p: PropiedadEntity, vm: PropiedadViewModel, volver: () -> Unit) {
            val errorRepo by vm.errorFormulario.collectAsStateWithLifecycle()
            var titulo by rememberSaveable(p.uuid) { mutableStateOf(p.titulo) };var distrito by rememberSaveable(p.uuid) { mutableStateOf(p.distrito) }
            var precio by rememberSaveable(p.uuid) { mutableStateOf(if(p.precio==0.0) "" else p.precio.toString()) };var area by rememberSaveable(p.uuid) { mutableStateOf(if(p.areaM2==0.0) "" else p.areaM2.toString()) }
            var hab by rememberSaveable(p.uuid) { mutableStateOf(p.habitaciones.toString()) };var tipo by rememberSaveable(p.uuid) { mutableStateOf(p.tipoOperacion) }
            var descripcion by rememberSaveable(p.uuid) { mutableStateOf(p.descripcion) }
            var imagenes by rememberSaveable(p.uuid) { mutableStateOf(Gson().fromJson(p.imagenesJson,Array<String>::class.java).joinToString("\n")) }
            var error by remember { mutableStateOf("") }
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text(if(p.id==0L) "Nueva propiedad" else "Editar propiedad",style=MaterialTheme.typography.headlineSmall)
                OutlinedTextField(titulo,{titulo=it},label={Text("Título")},modifier=Modifier.fillMaxWidth())
                OutlinedTextField(distrito,{distrito=it},label={Text("Distrito")},modifier=Modifier.fillMaxWidth())
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("VENTA","ALQUILER").forEach { op -> FilterChip(tipo==op,{tipo=op},label={Text(op)}) } }
                OutlinedTextField(precio,{precio=it},label={Text("Precio en soles · mayor a 0")},keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=KeyboardType.Decimal),modifier=Modifier.fillMaxWidth())
                OutlinedTextField(area,{area=it},label={Text("Área en m²")},keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=KeyboardType.Decimal),modifier=Modifier.fillMaxWidth())
                OutlinedTextField(hab,{hab=it},label={Text("Habitaciones")},keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.fillMaxWidth())
                OutlinedTextField(descripcion,{descripcion=it},label={Text("Descripción")},modifier=Modifier.fillMaxWidth(),minLines=3)
                OutlinedTextField(imagenes,{imagenes=it},label={Text("Imágenes HTTPS · una URL por línea")},modifier=Modifier.fillMaxWidth(),minLines=3)
                if(error.isNotBlank()) Text(error,color=MaterialTheme.colorScheme.error)
                if(errorRepo.isNotBlank()) Text(errorRepo,color=MaterialTheme.colorScheme.error)
                Text("El cambio se guarda primero en este celular. Si hay una versión nueva en el servidor, verás el conflicto en Sincronización.",style=MaterialTheme.typography.bodySmall)
                Button(onClick={
                    val urls=imagenes.lines().map { it.trim() }.filter { it.isNotEmpty() }
                    if(urls.size>8 || urls.any { !it.startsWith("https://") }) error="Usa hasta 8 URLs HTTPS."
                    else if(precio.toDoubleOrNull()==null || area.toDoubleOrNull()==null || hab.toIntOrNull()==null) error="Ingresa números válidos."
                    else vm.guardar(p.copy(titulo=titulo,distrito=distrito,tipoOperacion=tipo,precio=precio.toDouble(),areaM2=area.toDouble(),habitaciones=hab.toInt(),descripcion=descripcion,imagenesJson=Gson().toJson(urls)),volver)
                },modifier=Modifier.fillMaxWidth()) { Text("Guardar propiedad") }
            }
        }
