package com.casaclick.cliente
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.room.Room
import androidx.room.withTransaction
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.MockResponse
import com.google.gson.Gson
import com.casaclick.cliente.data.local.AppDatabase
import com.casaclick.cliente.data.local.entities.*
import com.casaclick.cliente.data.remote.dto.*
import com.casaclick.cliente.data.repository.*
import com.casaclick.cliente.data.mapper.*
import com.casaclick.cliente.util.SessionStore
@RunWith(AndroidJUnit4::class)
class PersistenciaSincronizacionTest {
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var db: AppDatabase
    private lateinit var server: MockWebServer
    private lateinit var auth: AuthRepository
    private lateinit var sync: SyncRepository
    private val gson=Gson()
    private val dto=PropiedadDto(1,"00000000-0000-4000-8000-000000000999","Casa de prueba","Surco","VENTA",300000.0,3,120.0,"Prueba",emptyList(),1)
    @Before fun preparar() {
        context.deleteDatabase("prueba_casaclick.db")
        db=Room.databaseBuilder(context,AppDatabase::class.java,"prueba_casaclick.db").build()
        server=MockWebServer();server.start()
        val sesion=SessionStore(context,"prueba_sesion")
        sesion.guardar(UsuarioDto(1,"test@casaclick.pe","Prueba",BuildConfig.ROL),"token-prueba","ClaveSegura",server.url("/").toString())
        auth=AuthRepository(db,sesion,Mutex());sync=SyncRepository(db,auth)
    }
    @After fun cerrar() { db.close();server.shutdown();context.deleteDatabase("prueba_casaclick.db") }
    @Test fun transaccionYReaperturaConservanDatosYCola()=runBlocking {
        db.withTransaction {
            val id=db.propiedades().insertar(dto.entidad().copy(estadoSync="PENDIENTE"))
            db.operaciones().insertar(OperacionPendienteEntity(entidad="propiedades",idEntidadLocal=id,tipoOperacion="PUT",payload=gson.toJson(dto)))
        }
        db.close();db=Room.databaseBuilder(context,AppDatabase::class.java,"prueba_casaclick.db").build()
        assertEquals(1,db.propiedades().observar().first().size);assertEquals(1,db.operaciones().pendientes().size)
        assertEquals("PENDIENTE",db.propiedades().observar().first().single().estadoSync)
    }
    @Test fun transaccionFallidaNoDejaDatosSinOperacion()=runBlocking {
        try { db.withTransaction { db.propiedades().insertar(dto.entidad());error("Falla provocada antes de registrar cola") } } catch(_: IllegalStateException) {}
        assertTrue(db.propiedades().observar().first().isEmpty());assertTrue(db.operaciones().pendientes().isEmpty())
    }
    @Test fun conflictoConservaCambioLocalYNoLoSobrescribe()=runBlocking {
        val id=db.propiedades().insertar(dto.entidad().copy(titulo="Mi cambio local",estadoSync="PENDIENTE"))
        db.operaciones().insertar(OperacionPendienteEntity(entidad="propiedades",idEntidadLocal=id,tipoOperacion="PUT",payload=gson.toJson(dto.copy(titulo="Mi cambio local"))))
        server.enqueue(MockResponse().setResponseCode(409).setBody("{\"mensaje\":\"Versión antigua\",\"versionActual\":2}"))
        server.enqueue(MockResponse().setBody(gson.toJson(listOf(dto.copy(titulo="Versión remota",version=2)))))
        server.enqueue(MockResponse().setBody("[]"))
        assertTrue(sync.sincronizar())
        val local=db.propiedades().porId(id)!!
        assertEquals("Mi cambio local",local.titulo);assertEquals("ERROR",local.estadoSync)
        val op=db.operaciones().observar().first().single();assertEquals(409,op.codigoHttp);assertEquals("Versión antigua",op.mensajeError)
    }
    @Test fun error503YReintentoConservanUuidYRegistro()=runBlocking {
        val id=db.propiedades().insertar(dto.entidad().copy(estadoSync="PENDIENTE"))
        val op=OperacionPendienteEntity(entidad="propiedades",idEntidadLocal=id,tipoOperacion="PUT",payload=gson.toJson(dto))
        db.operaciones().insertar(op)
        server.enqueue(MockResponse().setResponseCode(503).setBody("{\"mensaje\":\"Servidor caído\"}"))
        assertFalse(sync.sincronizar());assertEquals("PENDIENTE",db.propiedades().porId(id)!!.estadoSync)
        server.enqueue(MockResponse().setBody(gson.toJson(dto.copy(version=2))))
        server.enqueue(MockResponse().setBody(gson.toJson(listOf(dto.copy(version=2)))))
        server.enqueue(MockResponse().setBody("[]"))
        assertTrue(sync.sincronizar());assertEquals("SINCRONIZADO",db.propiedades().porId(id)!!.estadoSync)
        assertEquals(op.uuidOperacion,server.takeRequest().getHeader("X-Operacion-UUID"))
        assertEquals(op.uuidOperacion,server.takeRequest().getHeader("X-Operacion-UUID"))
        assertEquals(1,db.propiedades().observar().first().size)
    }
    @Test fun respuesta201YReenvioConfirmanIdempotencia()=runBlocking {
        val id=db.propiedades().insertar(dto.entidad().copy(estadoSync="PENDIENTE"))
        val op=OperacionPendienteEntity(entidad="propiedades",idEntidadLocal=id,tipoOperacion="POST",payload=gson.toJson(dto))
        db.operaciones().insertar(op)
        server.enqueue(MockResponse().setResponseCode(201).setBody(gson.toJson(dto)))
        server.enqueue(MockResponse().setBody(gson.toJson(listOf(dto))))
        server.enqueue(MockResponse().setBody("[]"))
        assertTrue(sync.sincronizar())
        assertEquals(201,db.operaciones().observar().first().single().codigoHttp)
        server.enqueue(MockResponse().setResponseCode(201).setHeader("X-Operacion-Repetida","true").setBody(gson.toJson(dto)))
        assertTrue(sync.reenviar(db.operaciones().observar().first().single().id).contains("Duplicada: sí"))
        assertEquals(1,db.propiedades().observar().first().size)
    }
    @Test fun offlineSoloAceptaLaCuentaYClavePreviamenteAutenticadas() {
        val sesion=auth.sesion;val url=sesion.baseUrl
        assertTrue(sesion.verificarOffline("test@casaclick.pe","ClaveSegura",url))
        assertFalse(sesion.verificarOffline("otra@casaclick.pe","ClaveSegura",url))
        assertFalse(sesion.verificarOffline("test@casaclick.pe","incorrecta",url))
        assertFalse(sesion.verificarOffline("test@casaclick.pe","ClaveSegura","http://otro-servidor/"))
    }
}
