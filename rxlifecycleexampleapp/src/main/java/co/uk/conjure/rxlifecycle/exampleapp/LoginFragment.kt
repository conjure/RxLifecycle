package co.uk.conjure.rxlifecycle.exampleapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import co.uk.conjure.rxlifecycle.exampleapp.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {


    private lateinit var loginView: LoginView
    private val loginViewModel: LoginViewModelImpl by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        loginView = LoginView().apply {
            viewModel = loginViewModel
            registerBinding(
                FragmentLoginBinding.inflate(inflater, container, false),
                this@LoginFragment
            )
        }
        return loginView.requireBinding().root
    }
}