package co.uk.conjure.rxlifecycle.exampleapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import co.uk.conjure.rxlifecycle.exampleapp.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var binding: FragmentLoginBinding? = null
    private lateinit var loginView: LoginView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        loginView = LoginView().apply {
            registerBinding(binding!!, this@LoginFragment)
        }
        return loginView.requireBinding().root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //Release resources
        binding = null
    }
}