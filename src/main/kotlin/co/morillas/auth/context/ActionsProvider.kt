package co.morillas.auth.context

import co.morillas.auth.context.AuthProvider.passwordHasher
import co.morillas.auth.core.application.action.CustomSignIn
import co.morillas.auth.core.application.action.CustomSignUp

object ActionsProvider {

	val signUp by lazy {
		CustomSignUp(RepositoriesProvider.userRepository, passwordHasher)
	}

	val signIn by lazy {
		CustomSignIn(RepositoriesProvider.userRepository, passwordHasher)
	}
}
