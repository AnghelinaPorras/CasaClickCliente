package com.casaclick.cliente.ui.components
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

        import coil.compose.SubcomposeAsyncImage
        import androidx.compose.ui.geometry.Offset
        import androidx.compose.ui.geometry.Size
        import java.text.NumberFormat
        import java.util.Locale
        fun soles(precio: Double): String = "S/ " + NumberFormat.getNumberInstance(Locale("es","PE")).apply { maximumFractionDigits=0 }.format(precio)
        @Composable fun CasaIlustracion(modifier: Modifier=Modifier, variante: Int=0) {
            Canvas(modifier.background(Color(0xFFD9ECE6))) {
                val w=size.width;val h=size.height
                drawCircle(Color(0xFFF0B85B),h*.14f,Offset(w*.82f,h*.22f))
                drawRect(Color(0xFF90BCAD),Offset(0f,h*.78f),Size(w,h*.22f))
                val x=w*.22f;val y=h*.25f
                drawRect(Color(0xFFF7FAF9),Offset(x,y),Size(w*.53f,h*.6f))
                drawRect(Color(0xFF087F72),Offset(x-w*.03f,y),Size(w*.59f,h*.055f))
                for(row in 0..2) for(col in 0..2) drawRect(if((row+col+variante)%2==0) Color(0xFF7DA9A1) else Color(0xFFB3D5CB),Offset(x+w*(.07f+col*.14f),y+h*(.1f+row*.14f)),Size(w*.08f,h*.085f))
                drawRect(Color(0xFFB17834),Offset(w*.46f,h*.7f),Size(w*.065f,h*.15f))
                drawCircle(Color(0xFF5A9E85),h*.13f,Offset(w*.12f,h*.67f))
                drawLine(Color(0xFF587B64),Offset(w*.12f,h*.7f),Offset(w*.12f,h*.9f),w*.012f)
            }
        }
        @Composable fun FotoPropiedad(url: String?, modifier: Modifier, variante: Int=0) {
            if(url.isNullOrBlank()) CasaIlustracion(modifier,variante)
            else SubcomposeAsyncImage(model=url,contentDescription="Imagen referencial de la propiedad",modifier=modifier,contentScale=ContentScale.Crop,loading={ CasaIlustracion(Modifier.fillMaxSize(),variante) },error={ CasaIlustracion(Modifier.fillMaxSize(),variante) })
        }
        @Composable fun PropiedadCard(p: Propiedad, favorita: Boolean, abrir: () -> Unit, favorito: () -> Unit) {
            Card(onClick=abrir,shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=Color.White)) {
                Box {
                    FotoPropiedad(p.imagenes.firstOrNull(),Modifier.fillMaxWidth().height(140.dp))
                    Surface(Modifier.align(Alignment.TopStart).padding(8.dp),shape=RoundedCornerShape(8.dp),color=MaterialTheme.colorScheme.primary) { Text(p.tipoOperacion,Modifier.padding(horizontal=8.dp,vertical=4.dp),color=Color.White,style=MaterialTheme.typography.labelSmall) }
                    if(BuildConfig.ROL=="cliente") FilledIconButton(onClick=favorito,modifier=Modifier.align(Alignment.TopEnd).padding(4.dp),colors=IconButtonDefaults.filledIconButtonColors(containerColor=Color.White)) { Icon(if(favorita) Icons.Default.Favorite else Icons.Default.FavoriteBorder,contentDescription="Guardar favorita",tint=MaterialTheme.colorScheme.primary) }
                }
                Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Text(soles(p.precio)+(if(p.tipoOperacion=="ALQUILER") " / mes" else ""),fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium)
                    Text(p.titulo,maxLines=2,style=MaterialTheme.typography.bodyMedium)
                    Text(p.distrito,color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.labelMedium)
                    Text("${p.habitaciones} hab. · ${p.areaM2.toInt()} m²",style=MaterialTheme.typography.bodySmall)
                    SyncStatusIndicator(p.estadoSync)
                }
            }
        }
