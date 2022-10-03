package co.uk.conjure.rxlifecycle.exampleapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import co.uk.conjure.rxlifecycle.exampleapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityLoginBinding.inflate(layoutInflater).root)
    }
}