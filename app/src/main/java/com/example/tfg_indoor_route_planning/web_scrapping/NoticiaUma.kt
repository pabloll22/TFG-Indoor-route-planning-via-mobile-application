package com.example.tfg_indoor_route_planning.web_scrapping

data class NoticiaUma(
    val titulo: String,
    val descripcion: String,
    val imageUrl: String?,
    val link: String?
)

data class DestacadoUma(
    val titulo: String,
    val descripcion: String,
    val imageUrl: String?,
    val link: String?
)
