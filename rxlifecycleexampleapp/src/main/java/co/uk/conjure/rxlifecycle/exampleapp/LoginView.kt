package co.uk.conjure.rxlifecycle.exampleapp

import android.view.View
import co.uk.conjure.rxlifecycle.exampleapp.databinding.FragmentLoginBinding
import uk.co.conjure.rxlifecycle.RxView
import uk.co.conjure.rxlifecycle.whileStarted

class LoginView : RxView<FragmentLoginBinding>() {

    lateinit var viewModel: LoginViewModel

    override fun onStart() {
        super.onStart()

        binding.etEmail.bind(viewModel.email)
        binding.etPassword.bind(viewModel.password)
        binding.btnLogin.bind(viewModel.loginClick, viewModel.isLoginButtonEnabled)

        viewModel.isLoading.whileStarted(this, { loading ->
            if (loading) {
                binding.etEmail.isEnabled = false
                binding.etPassword.isEnabled = false
                binding.pbLoading.visibility = View.VISIBLE
            } else {
                binding.etEmail.isEnabled = true
                binding.etPassword.isEnabled = true
                binding.pbLoading.visibility = View.GONE
            }
        })
    }
}