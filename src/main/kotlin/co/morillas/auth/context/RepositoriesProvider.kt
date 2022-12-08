package co.morillas.auth.context

import co.morillas.auth.core.infrastructure.repository.user.MemUserRepository

object RepositoriesProvider {

	val userRepository by lazy {
		MemUserRepository()
	}
}
