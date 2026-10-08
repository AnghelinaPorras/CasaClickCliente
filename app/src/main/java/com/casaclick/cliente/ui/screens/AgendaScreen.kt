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

        import android.app.DatePickerDialog
        import android.app.TimePickerDialog
        import androidx.compose.ui.platform.LocalContext
        import java.time.LocalDate
        import java.time.LocalTime
        @Composable fun AgendaScreen(propiedad: Long, visita: Long, vm: VisitaViewModel, volver: () -> Unit) {
            val lista by vm.todas.collectAsStateWithLifecycle();val anterior=lista.find { it.id==visita };val context=LocalContext.current
            var fecha by rememberSaveable(visita) { mutableStateOf(anterior?.fecha ?: LocalDate.now().plusDays(1).toString()) }
            var hora by rememberSaveable(visita) { mutableStateOf(anterior?.hora ?: "10:00") }
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
                Icon(Icons.Default.CalendarMonth,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(52.dp))
                Text(if(visita==0L) "Agenda tu visita" else "Corregir fecha y hora",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
                Text("Elige cuándo te gustaría conocer esta propiedad.")
                OutlinedTextField(fecha,{fecha=it},label={Text("Fecha · AAAA-MM-DD")},modifier=Modifier.fillMaxWidth(),singleLine=true)
                OutlinedButton(onClick={val d=runCatching { LocalDate.parse(fecha) }.getOrDefault(LocalDate.now());DatePickerDialog(context,{_,y,m,dia -> fecha=LocalDate.of(y,m+1,dia).toString()},d.year,d.monthValue-1,d.dayOfMonth).show()}) { Text("Elegir fecha") }
                OutlinedTextField(hora,{hora=it},label={Text("Hora de Lima · HH:MM")},modifier=Modifier.fillMaxWidth(),singleLine=true)
                OutlinedButton(onClick={val t=runCatching { LocalTime.parse(hora) }.getOrDefault(LocalTime.of(10,0));TimePickerDialog(context,{_,h,m -> hora="%02d:%02d".format(h,m)},t.hour,t.minute,true).show()}) { Text("Elegir hora") }
                Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer)) { Text("Tu visita será PROVISIONAL hasta que el servidor acepte el horario. Si ya está ocupado, podrás corregirla sin perder tu solicitud.",Modifier.padding(16.dp)) }
                Button(onClick={vm.agendar(propiedad,fecha,hora,visita,volver)},modifier=Modifier.fillMaxWidth()) { Text("Guardar solicitud") }
            }
        }
