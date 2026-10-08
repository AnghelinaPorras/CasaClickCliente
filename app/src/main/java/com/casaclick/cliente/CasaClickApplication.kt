package com.casaclick.cliente
import android.app.Application
import com.casaclick.cliente.di.AppContainer
import com.casaclick.cliente.worker.SyncWorker
class CasaClickApplication : Application() {
    lateinit var container: AppContainer
        private set
    override fun onCreate() { super.onCreate();container=AppContainer(this);SyncWorker.periodico(this); if(container.sesion.activa) container.programar() }
}
