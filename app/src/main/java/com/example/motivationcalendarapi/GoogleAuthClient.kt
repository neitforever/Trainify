import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthClient(
    private val context: Context,
) {


    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun isSingedIn(): Boolean {
        if (firebaseAuth.currentUser != null) {
            return true
        }

        return false
    }

    suspend fun signIn(): Boolean {
        Log.d("signIn123", "start")

        try {
            Log.d("signIn123", "start try")

            val result = buildCredentialRequest()
            Log.d("signIn123", "after buildCredentialRequest")

            return handleSingIn(result)
        } catch (e: Exception) {
            Log.d("signIn123", "Sign-in error: ${e.message}")

            Log.e("GoogleAuth", "Sign-in error: ${e.message}")
            if (e is CancellationException) throw e
            return false
        }
        Log.d("signIn123", "end")

    }

    private suspend fun handleSingIn(result: GetCredentialResponse): Boolean {
        val credential = result.credential
        Log.d("asdasd", "1")

        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            Log.d("asdasd", "2")

            try {

                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)


                val authCredential = GoogleAuthProvider.getCredential(
                    tokenCredential.idToken, null
                )
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                Log.d("asdasd", "3")

                return authResult.user != null

            } catch (e: GoogleIdTokenParsingException) {
                return false
            }
            Log.d("asdasd", "4")

        } else {
            return false
        }
        Log.d("asdasd", "5")

    }

    private suspend fun buildCredentialRequest(): GetCredentialResponse {

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(
                        "311399588202-k718ga4og8iahofenhd9pbr4k8oo4lj2.apps.googleusercontent.com"
                    )
                    .setAutoSelectEnabled(false)
                    .build()
            )
            .build()



        return credentialManager.getCredential(
            request = request, context = context
        )
    }

    fun getCurrentUser() = firebaseAuth.currentUser

    suspend fun signOut() {
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        firebaseAuth.signOut()
    }

}