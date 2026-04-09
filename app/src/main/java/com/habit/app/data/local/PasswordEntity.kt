package com.habit.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class PasswordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val encryptedUsername: ByteArray,
    val usernameIv: ByteArray,
    val encryptedPassword: ByteArray,
    val passwordIv: ByteArray,
)
