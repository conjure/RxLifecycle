package co.uk.conjure.rxlifecycle.exampleapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import co.uk.conjure.rxlifecycle.exampleapp.databinding.FragmentLoginBinding

/**
 * The LoginFragment retrieves the [LoginViewModelImpl] from the Activity, creates the [LoginView]
 * and connects the two.
 *
 * The actual View binding is done in the [LoginView].
 * The navigation implemented in the [LoginActivity].
 *
 * ## Responsibilities
 *
 * This architecture follows the "Single Responsibility" pattern. It allows splitting complex UIs into
 * reusable components.
 * 
 * - Activity: High level layout and navigation.
 * - Fragments: Connecting View and ViewModel
 * - ViewModels: Logic
 * - Views: Binding the Layout <-> ViewModel
 *
 *
 * A single Activity can host multiple Fragments and ViewModels!
 */
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