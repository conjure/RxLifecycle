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
 * The ViewModel interface for the Login.
 * It only offers Observables and Observers for the UI.
 *
 */
interface LoginViewModel {
    // input
    val email: Observer<String>
    val password: Observer<String>
    val loginClick: Observer<Unit>
    val showTermsClick: Observer<Unit>

    // output
    val isLoginButtonEnabled: Observable<Boolean>
    val isLoading: Observable<Boolean>
    val error: Observable<LoginError>
}

/**
 * Navigation event when the Login completes.
 * It is separate from the [LoginViewModel] because it's not the Views job to perform the navigation.
 */
interface LoginNavigationModel {
    val onShowTerms: Observable<Unit>
    val onLoginComplete: Observable<Unit>
}

/**
 * Interface for the [TermsAndConditionsView]
 */
interface TermsAndConditionsViewModel {
    val termsContent: Observable<String>
    val isLoadingTerms: Observable<Boolean>
    val loadingTermsFailed: Observable<Boolean>
}

enum class LoginError {
    NONE,
    INVALID_CREDENTIALS,
    GENERIC_ERROR
}

sealed class Terms {
    object Loading : Terms()
    object LoadingFailed : Terms()
    class TermsAndConditions(val text: String) : Terms()
}

/**
 * Actual implementation of the [LoginViewModel].
 * It also implements [LoginNavigationModel] which will be observed by the Activity to navigate to
 * the next screen.
 */
class LoginViewModelImpl(private val api: LoginApi) : RxViewModel(), LoginViewModel,
    LoginNavigationModel, TermsAndConditionsViewModel {

    override val email: PublishSubject<String> = PublishSubject.create()
    override val password: PublishSubject<String> = PublishSubject.create()
    override val loginClick: PublishSubject<Unit> = PublishSubject.create()
    override val showTermsClick: PublishSubject<Unit> = PublishSubject.create()

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

    override val onShowTerms: Observable<Unit> = showTermsClick
    override val onLoginComplete: Observable<Unit> = state.filter { it is LoginState.Done }.map { }

    override val error: Observable<LoginError> = state.map { state ->
        when (state) {
            is LoginState.Error -> state.error
            else -> LoginError.NONE
        }
    }

    private val terms: Observable<Terms> = showTermsClick.switchMap {
        loadTermsAndConditions()
    }.takeUntil { it is Terms.TermsAndConditions }
        .hot(cacheOnComplete = true)

    override val termsContent: Observable<String>
        get() = terms.ofType(Terms.TermsAndConditions::class.java).map { it.text }
    override val isLoadingTerms: Observable<Boolean>
        get() = terms.map { it is Terms.Loading }
    override val loadingTermsFailed: Observable<Boolean>
        get() = terms.map { it is Terms.LoadingFailed }


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

    private fun loadTermsAndConditions() = api.getTermsAndConditions()
        .observeOn(AndroidSchedulers.mainThread())
        .map<Terms> { Terms.TermsAndConditions(it) }
        .toObservable()
        .startWithItem(Terms.Loading)
        .onErrorReturnItem(Terms.LoadingFailed)


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
