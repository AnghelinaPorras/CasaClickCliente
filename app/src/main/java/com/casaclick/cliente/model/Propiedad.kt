package com.casaclick.cliente.model
data class Propiedad(val id: Long, val titulo: String, val distrito: String, val tipoOperacion: String, val precio: Double, val habitaciones: Int, val areaM2: Double, val descripcion: String, val imagenes: List<String>, val estadoSync: String, val idRemoto: Long? = null, val uuid: String = "", val version: Int = 0)
