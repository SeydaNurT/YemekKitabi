package com.example.yemektarifim.model

import androidx.room.Entity

@Entity
data class Tarif (
    var isim : String ,
    var malzeme : String ,
    var gorsel : ByteArray
)