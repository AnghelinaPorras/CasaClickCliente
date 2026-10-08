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

        @Composable fun ListScreen(vm: PropiedadViewModel, detalle: (Long) -> Unit) {
            val lista by vm.propiedades.collectAsStateWithLifecycle();val favoritos by vm.favoritos.collectAsStateWithLifecycle();val f by vm.filtros.collectAsStateWithLifecycle();val count by vm.cantidadPrecio.collectAsStateWithLifecycle()
            var filtrosAbiertos by rememberSaveable { mutableStateOf(false) }
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) { Text(if(f.favoritas) "Tus favoritas" else "Propiedades en Lima",fontWeight=FontWeight.Bold);TextButton(onClick={filtrosAbiertos=!filtrosAbiertos}) { Text("Filtros") } }
                if(filtrosAbiertos) Column(Modifier.padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) { listOf("TODAS","VENTA","ALQUILER").forEach { op -> FilterChip(selected=f.operacion==op,onClick={vm.filtros.value=f.copy(operacion=op)},label={Text(op.lowercase().replaceFirstChar { it.uppercase() })}) } }
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(f.minimo,{vm.filtros.value=f.copy(minimo=it)},label={Text("Precio mín.")},modifier=Modifier.weight(1f),singleLine=true,keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=KeyboardType.Decimal))
                        OutlinedTextField(f.maximo,{vm.filtros.value=f.copy(maximo=it)},label={Text("Precio máx.")},modifier=Modifier.weight(1f),singleLine=true,keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=KeyboardType.Decimal))
                    }
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(f.habitaciones,{vm.filtros.value=f.copy(habitaciones=it)},label={Text("Habitaciones")},modifier=Modifier.weight(1f),singleLine=true,keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=KeyboardType.Number))
                        OutlinedTextField(f.distrito,{vm.filtros.value=f.copy(distrito=it)},label={Text("Distrito")},modifier=Modifier.weight(1f),singleLine=true)
                    }
                    if(BuildConfig.ROL=="cliente") Row(verticalAlignment=Alignment.CenterVertically) { Checkbox(f.favoritas,{vm.filtros.value=f.copy(favoritas=it)});Text("Solo favoritas") }
                    TextButton(onClick={vm.filtros.value=Filtros()}) { Text("Limpiar filtros") }
                }
                Text("${lista.size} resultados · $count propiedades en el rango de precio",Modifier.padding(16.dp),style=MaterialTheme.typography.bodySmall)
                if(lista.isEmpty()) Column(Modifier.padding(24.dp)) { Text("No hay propiedades para mostrar",style=MaterialTheme.typography.titleLarge);Text("Revisa tus filtros o abre Sincronización para descargar el catálogo por primera vez.") }
                LazyVerticalGrid(columns=GridCells.Adaptive(170.dp),contentPadding=PaddingValues(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp),modifier=Modifier.weight(1f)) {
                    items(lista,key={it.id}) { p -> PropiedadCard(p,p.id in favoritos,{detalle(p.id)},{vm.favorito(p.id)}) }
                }
            }
        }
