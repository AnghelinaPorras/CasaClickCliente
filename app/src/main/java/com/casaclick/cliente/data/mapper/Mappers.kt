package com.casaclick.cliente.data.mapper
import com.google.gson.Gson
import com.casaclick.cliente.data.local.entities.*
import com.casaclick.cliente.data.remote.dto.*
import com.casaclick.cliente.model.*
private val gson = Gson()
fun PropiedadEntity.dto() = PropiedadDto(idRemoto ?: 0, uuid, titulo, distrito, tipoOperacion, precio, habitaciones, areaM2, descripcion, gson.fromJson(imagenesJson, Array<String>::class.java).toList(), version, eliminado)
fun PropiedadDto.entidad(local: Long = 0) = PropiedadEntity(local, id, uuid, version, "SINCRONIZADO", titulo, distrito, tipoOperacion, precio, habitaciones, areaM2, descripcion, gson.toJson(imagenes), eliminado)
fun PropiedadEntity.modelo(): Propiedad { val d = dto(); return Propiedad(id, titulo, distrito, tipoOperacion, precio, habitaciones, areaM2, descripcion, d.imagenes, estadoSync, idRemoto, uuid, version) }
fun VisitaEntity.dto() = VisitaDto(idRemoto ?: 0, uuid, propiedadIdRemoto, usuarioId, fecha, hora, if (estado == "PROVISIONAL") "SOLICITADA" else estado, version, clienteNombre)
fun VisitaDto.entidad(local: Long = 0, propiedadLocal: Long) = VisitaEntity(local, id, uuid, version, "SINCRONIZADO", propiedadLocal, propiedadId, usuarioId, fecha, hora, estado, clienteNombre)
fun VisitaEntity.modelo() = Visita(id, propiedadId, fecha, hora, estado, estadoSync, clienteNombre, idRemoto, uuid, version)
