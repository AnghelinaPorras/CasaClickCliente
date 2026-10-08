package com.casaclick.cliente.ui.theme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val colores=lightColorScheme(primary=Color(0xFF087F72),onPrimary=Color.White,primaryContainer=Color(0xFFD8F2E9),onPrimaryContainer=Color(0xFF173C33),secondary=Color(0xFF9A6508),secondaryContainer=Color(0xFFFFE6BA),onSecondaryContainer=Color(0xFF4C350C),background=Color(0xFFF7FAF9),onBackground=Color(0xFF183630),surface=Color(0xFFF7FAF9),onSurface=Color(0xFF183630),surfaceVariant=Color(0xFFE8F2ED),onSurfaceVariant=Color(0xFF435D54),surfaceTint=Color(0xFF087F72),inverseSurface=Color(0xFF223D35),outline=Color(0xFF71918B))
@Composable fun CasaClickTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme=colores,content=content) }
