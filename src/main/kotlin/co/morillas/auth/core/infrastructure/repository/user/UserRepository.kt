package co.morillas.auth.core.infrastructure.repository.user

import co.morillas.auth.core.domain.User

interface UserRepository {
    fun getByUsername(username: String): User?
    fun getAll(): List<User>
    fun delete(username: String): Boolean
    fun add(entry: User): User?
    fun update(entry: User): User?
}

class MemUserRepository: UserRepository {
    private val users = mutableMapOf<String, User>()

    override fun getByUsername(username: String): User? {
        return users[username]
    }

    override fun getAll(): List<User> {
        return users.values.toList()
    }

    override fun delete(username: String): Boolean {
        return users.remove(username) != null
    }

    override fun add(entry: User): User? {
        users[entry.username] = entry
        return entry
    }

    override fun update(entry: User): User? {
        return users.put(entry.username, entry)
    }

}