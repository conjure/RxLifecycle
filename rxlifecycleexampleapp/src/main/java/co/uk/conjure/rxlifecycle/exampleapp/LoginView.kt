package co.uk.conjure.rxlifecycle.exampleapp

import co.uk.conjure.rxlifecycle.exampleapp.databinding.FragmentLoginBinding
import uk.co.conjure.rxlifecycle.RxView

class LoginView : RxView<FragmentLoginBinding>() {
    override fun onStart() {
        super.onStart()

        //TODO
        binding.etEmail.bind()
        binding.etPassword.bind()
        binding.btnLogin.bind()
    }
}