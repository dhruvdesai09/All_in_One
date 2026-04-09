package com.habit.app.data.repository

import com.habit.app.data.local.PasswordDao
import com.habit.app.data.local.PasswordEntity
import com.habit.app.data.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class PasswordItem(
    val id: Long,
    val name: String,
    val username: String,
    val password: String,
)

@Singleton
class PasswordRepository @Inject constructor(
    private val dao: PasswordDao,
    private val crypto: CryptoManager,
) {
    fun observeAll(): Flow<List<PasswordEntity>> = dao.getAllPasswords()

    suspend fun getDecrypted(id: Long): PasswordItem? = withContext(Dispatchers.IO) {
        val entity = dao.getPasswordById(id) ?: return@withContext null
        PasswordItem(
            id = entity.id,
            name = entity.name,
            username = crypto.decryptString(entity.encryptedUsername, entity.usernameIv),
            password = crypto.decryptString(entity.encryptedPassword, entity.passwordIv),
        )
    }

    suspend fun getAllDecrypted(): List<PasswordItem> = withContext(Dispatchers.IO) {
        dao.getAllPasswordsOnce().map { entity ->
            PasswordItem(
                id = entity.id,
                name = entity.name,
                username = crypto.decryptString(entity.encryptedUsername, entity.usernameIv),
                password = crypto.decryptString(entity.encryptedPassword, entity.passwordIv),
            )
        }
    }

    suspend fun insert(name: String, username: String, password: String) = withContext(Dispatchers.IO) {
        val encUser = crypto.encryptString(username)
        val encPass = crypto.encryptString(password)
        dao.insertPassword(
            PasswordEntity(
                name = name,
                encryptedUsername = encUser.data,
                usernameIv = encUser.iv,
                encryptedPassword = encPass.data,
                passwordIv = encPass.iv,
            )
        )
    }

    suspend fun update(id: Long, name: String, username: String, password: String) = withContext(Dispatchers.IO) {
        val encUser = crypto.encryptString(username)
        val encPass = crypto.encryptString(password)
        dao.updatePassword(
            PasswordEntity(
                id = id,
                name = name,
                encryptedUsername = encUser.data,
                usernameIv = encUser.iv,
                encryptedPassword = encPass.data,
                passwordIv = encPass.iv,
            )
        )
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        val entity = dao.getPasswordById(id) ?: return@withContext
        dao.deletePassword(entity)
    }
}
