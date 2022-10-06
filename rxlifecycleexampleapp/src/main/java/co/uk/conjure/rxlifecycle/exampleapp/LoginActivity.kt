package co.uk.conjure.rxlifecycle.exampleapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import co.uk.conjure.rxlifecycle.exampleapp.databinding.ActivityLoginBinding
import uk.co.conjure.rxlifecycle.whileStarted

private const val TAG_TERMS_FRAGMENT = "TERMS_FRAGMENT"

/**
 * The LoginActivity creates the ViewModel and is hosting the [LoginFragment].
 * It's only other responsibility is to navigate to the Dashboard when the login succeeds.
 */
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModelImpl by viewModels { LoginViewModelImpl.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel.run { /* Initialize the lazy VM so it's ready for Fragments */ }
        super.onCreate(savedInstanceState)
        setContentView(ActivityLoginBinding.inflate(layoutInflater).root)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onLoginComplete.whileStarted(this, { onLoginComplete() })
        viewModel.onShowTerms.whileStarted(this, { showTermsAndConditions() })
    }

    private fun showTermsAndConditions() {
        if (supportFragmentManager.findFragmentByTag(TAG_TERMS_FRAGMENT) == null) {
            TermsAndConditionBottomSheet().show(supportFragmentManager, TAG_TERMS_FRAGMENT)
        }
    }

    /**
     * Here you would usually launch a DashboardActivity and finish this Activity.
     * We just show a text "LOGIN COMPLETE".
     */
    private fun onLoginComplete() {
        setContentView(TextView(this).apply { text = "LOGIN COMPLETE" })
    }
}