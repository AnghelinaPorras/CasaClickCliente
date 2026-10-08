package com.casaclick.cliente.ui.components
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable fun SyncDetailsCard(idLocal: Long, idRemoto: Long?, uuid: String, version: Int, estado: String) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Text("Datos de sincronización",fontWeight=FontWeight.Bold)
            Text("ID local: $idLocal")
            Text("ID remoto: ${idRemoto ?: "Aún no asignado"}")
            Text("UUID del registro:",style=MaterialTheme.typography.labelMedium)
            Text(uuid,fontFamily=FontFamily.Monospace,style=MaterialTheme.typography.bodySmall)
            Text("Versión: $version")
            SyncStatusIndicator(estado)
        }
    }
}
