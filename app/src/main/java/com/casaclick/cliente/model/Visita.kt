package com.casaclick.cliente.model
data class Visita(val id: Long, val propiedadId: Long, val fecha: String, val hora: String, val estado: String, val estadoSync: String, val clienteNombre: String, val idRemoto: Long? = null, val uuid: String = "", val version: Int = 0)
