package com.example.mynotes.model


import java.io.Serializable

data class NoteF(val title: String? = null,
                 val date: String? = null,
                 val description: String? = null):Serializable
