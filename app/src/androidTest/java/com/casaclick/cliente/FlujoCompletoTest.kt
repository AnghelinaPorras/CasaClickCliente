package com.casaclick.cliente
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.io.File
import androidx.test.runner.screenshot.Screenshot
@RunWith(AndroidJUnit4::class)
class FlujoCompletoTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    private fun esperar(texto: String, timeout: Long=30000) { ui.waitUntil(timeout) { ui.onAllNodesWithText(texto,substring=true).fetchSemanticsNodes().isNotEmpty() } }
    private fun captura(nombre: String) {
        val dir=File(ui.activity.getExternalFilesDir(null),"evidencias");dir.mkdirs()
        File(dir,nombre+".png").outputStream().use { Screenshot.capture().bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) }
    }
    @Test fun loginCatalogoYOperacionConApiReal() {
        val url=InstrumentationRegistry.getArguments().getString("apiUrl")
        Assume.assumeTrue("Esta prueba requiere -e apiUrl URL",url!=null)
        val c=(ui.activity.application as CasaClickApplication).container
        runBlocking { c.auth.salir() }
        esperar("Ingresar");captura("01_login")
        ui.onNodeWithText("Correo").performTextInput(if(BuildConfig.ROL=="cliente") "cliente1@casaclick.pe" else "asesor@casaclick.pe")
        ui.onNodeWithText("Contraseña").performTextInput("CasaClick2026!")
        ui.onNodeWithText("URL de la API").performTextReplacement(url!!)
        ui.onNodeWithText("Ingresar").performClick()
        esperar("Hola,")
        ui.onNodeWithText("Sync").performClick();esperar("Sincronizar ahora")
        ui.onNodeWithText("Sincronizar ahora").performClick()
        esperar("Datos actualizados.")
        ui.onNodeWithText("Inicio").performClick()
        val total=runBlocking { c.propiedades.propiedades.first().size };assertTrue("Catálogo remoto mínimo de 30",total>=30)
        esperar("$total propiedades en tu catálogo local");captura("02_inicio")
        ui.onNodeWithText("Explorar").performClick();esperar("$total resultados");captura("03_catalogo")
        ui.onNodeWithText("Filtros").performClick();ui.onNodeWithText("Venta",useUnmergedTree=true).performClick()
        val ventas=runBlocking { c.propiedades.propiedades.first().count { it.tipoOperacion=="VENTA" } }
        esperar("$ventas resultados");captura("04_filtros")
        ui.onNodeWithText("Filtros").performClick()
        if(BuildConfig.ROL=="cliente") {
            val propiedad=runBlocking { c.propiedades.propiedades.first().first { it.tipoOperacion=="VENTA" } }
            ui.onAllNodesWithText(propiedad.titulo).onFirst().performClick();esperar("Agendar visita");captura("05_detalle")
            val eraFavorita=runBlocking { c.propiedades.favoritos.first().contains(propiedad.id) }
            val accionFavorita=if(eraFavorita) "Quitar de favoritas" else "Guardar como favorita"
            ui.onNodeWithText(accionFavorita).performScrollTo()
            ui.onNodeWithText(accionFavorita).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
            esperar(if(eraFavorita) "Guardar como favorita" else "Quitar de favoritas")
            runBlocking { assertEquals(!eraFavorita,c.propiedades.favoritos.first().contains(propiedad.id)) }
            ui.onNodeWithText("Agendar visita").performScrollTo().performClick();esperar("Guarda",3000)
            val fecha=LocalDate.now().plusDays(20).toString()
            val ocupadas=runBlocking { c.visitas.visitas.first().filter { it.propiedadId==propiedad.id && it.fecha==fecha }.map { it.hora }.toSet() }
            val hora=(0..83).map { java.time.LocalTime.of(8,0).plusMinutes(it*10L).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) }.first { it !in ocupadas }
            ui.onNodeWithText("Fecha · AAAA-MM-DD").performTextReplacement(fecha)
            ui.onNodeWithText("Hora de Lima · HH:MM").performTextReplacement(hora)
            captura("06_agenda")
            ui.onNodeWithText("Guardar solicitud").performScrollTo().performClick()
            esperar("Agendar visita")
            runBlocking { assertTrue(c.sync.sincronizar());assertTrue(c.visitas.visitas.first().any { it.fecha==fecha && it.hora==hora && it.estado=="SOLICITADA" && it.estadoSync=="SINCRONIZADO" }) }
            // Duplicado real y conservación del ID remoto.
            val op=runBlocking { c.sync.operaciones.first().first { it.entidad=="visitas" && it.estado=="SINCRONIZADO" } }
            runBlocking { assertTrue(c.sync.reenviar(op.id).contains("sin duplicar")) }
        } else {
            ui.onNodeWithText("Inicio").performClick();ui.onNodeWithText("Registrar una propiedad").performScrollTo().performClick();esperar("Nueva propiedad")
            ui.onNodeWithText("Título").performTextInput("Propiedad prueba emulador")
            ui.onNodeWithText("Distrito").performTextInput("Surco")
            ui.onNodeWithText("Precio en soles · mayor a 0").performTextReplacement("325000")
            ui.onNodeWithText("Área en m²").performTextReplacement("110")
            ui.onNodeWithText("Área en m²").assertTextContains("110")
            androidx.test.espresso.Espresso.closeSoftKeyboard()
            ui.waitForIdle()
            captura("05_formulario")
            ui.onNodeWithText("Guardar propiedad").performScrollTo()
            ui.waitForIdle()
            ui.onNodeWithText("Guardar propiedad").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
            ui.waitForIdle();captura("06_tras_guardar")
            try { esperar("Hola,") } catch(e: Throwable) { captura("error_validacion");throw e }
            runBlocking { assertTrue(c.sync.sincronizar());assertTrue(c.propiedades.propiedades.first().any { it.titulo=="Propiedad prueba emulador" && it.estadoSync=="SINCRONIZADO" }) }
        }
        if(ui.onAllNodesWithContentDescription("Volver").fetchSemanticsNodes().isNotEmpty()) ui.onNodeWithContentDescription("Volver").performClick()
        ui.onNodeWithText("Sync").performClick();esperar("Operaciones e historial");captura("07_sync")
    }
}
