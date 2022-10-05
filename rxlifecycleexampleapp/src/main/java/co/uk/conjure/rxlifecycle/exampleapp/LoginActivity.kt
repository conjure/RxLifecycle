package co.uk.conjure.rxlifecycle.exampleapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import co.uk.conjure.rxlifecycle.exampleapp.databinding.ActivityLoginBinding
import uk.co.conjure.rxlifecycle.whileStarted

/**
 * The LoginActivity creates the ViewModel and is hosting the [LoginFragment].
 * It's only other responsibility is to navigate to the Dashboard when the login succeeds.
 */
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModelImpl by viewModels { LoginViewModelImpl.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        println(viewModel)
        super.onCreate(savedInstanceState)
        setContentView(ActivityLoginBinding.inflate(layoutInflater).root)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onLoginComplete.whileStarted(this, { onLoginComplete() })
    }

    /**
     * Here you would usually launch a DashboardActivity and finish this Activity.
     * We just show a text "LOGIN COMPLETE".
     */
    private fun onLoginComplete() {
        setContentView(TextView(this).apply { text = "LOGIN COMPLETE" })
    }
}