package me.fernandesleite.alagou.ui.mainmap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.tasks.Task
import me.fernandesleite.alagou.R
import me.fernandesleite.alagou.models.User


class LoginFragment : Fragment() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var viewModel: MapsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(MapsViewModel::class.java)
        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(
                "1080519531723-bu7li9b7qe49l53scd7oml814gr9s960.apps.googleusercontent.com"
            ).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        return inflater.inflate(R.layout.fragment_login, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                viewModel.setTokenId(task.result)
                viewModel.createUser(User(task.result.idToken, "${task.result.displayName}"))
                findNavController().popBackStack()
            } catch (e: Exception) {
                Log.i("LoginFragment", e.toString())
            }
        }
    }
}