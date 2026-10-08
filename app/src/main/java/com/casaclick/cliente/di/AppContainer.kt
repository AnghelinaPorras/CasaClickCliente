package com.casaclick.cliente.di
import android.content.Context
import androidx.room.Room
import com.casaclick.cliente.data.local.AppDatabase
import com.casaclick.cliente.data.repository.*
import com.casaclick.cliente.util.*
import com.casaclick.cliente.worker.SyncWorker
import kotlinx.coroutines.sync.Mutex
class AppContainer(private val context: Context) {
    private val db=Room.databaseBuilder(context,AppDatabase::class.java,"casaclick.db").build()
    val sesion=SessionStore(context)
    val auth=AuthRepository(db,sesion,Mutex())
    val sync=SyncRepository(db,auth)
    val propiedades=PropiedadRepository(db,auth) { SyncWorker.programar(context) }
    val visitas=VisitaRepository(db,auth) { SyncWorker.programar(context) }
    val conectividad=ConnectivityObserver(context)
    fun programar()=SyncWorker.programar(context)
}
