package co.uk.conjure.rxlifecycle.exampleapp

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import uk.co.conjure.rxlifecycle.RxViewModel

/**
 * The ViewModel interface
 */
interface LoginViewModel {
    // input
    val email: Observer<String>
    val password: Observer<String>
    val loginClick: Observer<Unit>

    // output
    val isLoginButtonEnabled: Observable<Boolean>
    val isLoading: Observable<Boolean>
    val onLoginComplete: Observable<Unit>
    val error: Observable<LoginError>
}

enum class LoginError {
    NONE,
    INVALID_CREDENTIALS,
    GENERIC_ERROR
}


class LoginViewModelImpl(private val api: LoginApi) : RxViewModel(), LoginViewModel {

    override val email: PublishSubject<String> = PublishSubject.create()
    override val password: PublishSubject<String> = PublishSubject.create()
    override val loginClick: PublishSubject<Unit> = PublishSubject.create()

    private val state: BehaviorSubject<LoginState> =
        BehaviorSubject.createDefault(LoginState.Idle("", ""))


    private val userInput = Observable.merge(
        email.map { UiEvent.EmailChange(it) },
        password.map { UiEvent.PasswordChange(it) },
        loginClick.map { UiEvent.LoginClick }
    )

    init {
        userInput.withLatestFrom(state) { event, state ->
            updateState(state, event)
        }
            .distinctUntilChanged()
            .publish { currentState ->
                Observable.merge(
                    currentState,
                    performLogin(currentState)
                )
            }
            .subscribe { state.onNext(it) }
            .addTo(keepAlive)
    }


    override val isLoginButtonEnabled: Observable<Boolean> = state.map {
        (it is LoginState.Idle || it is LoginState.Error) &&
                it.email.isNotBlank() && it.password.isNotBlank()
    }

    override val isLoading: Observable<Boolean> =
        state.map { it is LoginState.Loading || it is LoginState.Done }

    override val onLoginComplete: Observable<Unit> = state.filter { it is LoginState.Done }.map { }

    override val error: Observable<LoginError> = state.map { state ->
        when (state) {
            is LoginState.Error -> state.error
            else -> LoginError.NONE
        }
    }


    private fun performLogin(stateStream: Observable<LoginState>) =
        stateStream.ofType(LoginState.Loading::class.java)
            .flatMapSingle { state ->
                api.login(state.email, state.password)
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { result ->
                        when (requireNotNull(result)) {
                            LoginApi.Result.SUCCESS -> state.success()
                            LoginApi.Result.INVALID_CREDENTIALS -> state.error(
                                LoginError.INVALID_CREDENTIALS
                            )
                        }
                    }
                    .onErrorReturnItem(state.error(LoginError.GENERIC_ERROR))
            }

    private fun updateState(
        state: LoginState,
        event: UiEvent
    ) = when (state) {
        is LoginState.Done -> state
        is LoginState.Error, is LoginState.Idle -> {
            when (event) {
                is UiEvent.EmailChange -> LoginState.Idle(event.email, state.password)
                UiEvent.LoginClick -> LoginState.Loading(state.email, state.password)
                is UiEvent.PasswordChange -> LoginState.Idle(state.email, event.password)
            }
        }
        is LoginState.Loading -> state
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LoginViewModelImpl(LoginApi())
            }
        }
    }
}


/**
 * States for the [LoginViewModelImpl]
 */
private sealed class LoginState(
    val email: String,
    val password: String
) {
    class Idle(email: String, password: String) : LoginState(email, password)
    class Loading(email: String, password: String) : LoginState(email, password) {
        fun success(): Done {
            return Done(email, password)
        }

        fun error(error: LoginError): Error {
            return Error(email, password, error)
        }
    }

    class Error(email: String, password: String, val error: LoginError) :
        LoginState(email, password)

    class Done(email: String, password: String) : LoginState(email, password)
}

/**
 * User input events
 */
sealed class UiEvent {
    class EmailChange(val email: String) : UiEvent()
    class PasswordChange(val password: String) : UiEvent()
    object LoginClick : UiEvent()
}
