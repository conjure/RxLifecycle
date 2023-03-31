# RxLifecycle
[![](https://jitpack.io/v/uk.co.conjure/RxLifecycle.svg)](https://jitpack.io/#uk.co.conjure/RxLifecycle)

A small library that allows smooth integration between the Android Fragment, Activity and ViewModel lifecycle and RxJava/RxKotlin.

View the [API documentation](https://developer.conjure.co.uk/api/RxLifecylce/index.html).

## Including the library

First add the following to your project level gradle repositories:

```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

Then add the RxLifecycle dependency in the module level build.gradle:

```gradle
dependencies {
	implementation 'uk.co.conjure:RxLifecycle:1.0.0-alpha03'
}
```

## Code Sample

Given an MVVM architecture app we need to consider the lifecycle of both the ViewModel and the View. The RxLifecycle library provides tools for both.

### RxViewModel

Often you want to store the information your view model provides and replay it to late subscribers. A typical pattern for doing this is to use `replay` and `refCount` like so:

```kotlin
interface Model {
	fun getUserInfo(): Observable<UserInfo>
}

class ProfileViewModel(private val model: Model): ViewModel() {
	val userName: Observable<String> = model
		.getUserInfo()
		.map { it.userName }
		.replay(1)
		.refCount()
}
```

`refCount` will ensure that the upstream stops being observed as soon as there are no more subscribers, which is good because it tidies things up for us automatically, but it also presents a problem. Often a view model only has one observer at a time (the view) so when the view is destroyed temporarily (e.g. during a configuration change) the upstream stops being observed and the value stored by `replay(1)` will be forgotten. 

We can get around this problem by adding our own subscriber to the observable, so that the view isn't the only subscriber, like so:

```kotlin
class ProfileViewModel(private val model: Model): ViewModel() {

	private val keepAlive = CompositeDisposable()

	val userName: Observable<String> = model
		.getUserInfo()
		.map { it.userName }
		.replay(1)
		.refCount()

	init {
		keepAlive.add(userName.subscribe())
	}

	override fun onCleared() {
		keepAlive.dispose()
		super.onCleared()
	}
}
```

The RxLifecycle library simplifies this code for us by providing the `RxViewModel` base class. `RxViewModel` stores this `CompositeDisposable` and provides the `hot` extension function which tells `RxViewModel` to replay the last value to late subscribers and keep hold of a subscription to the upstream until `onCleared` is called. Now our code is much cleaner:

```kotlin
class ProfileViewModel(private val model: Model): RxViewModel() {
	val userName: Observable<String> = model
		.getUserInfo()
		.map { it.userName }
		.hot()
}
```

(The `hot` extension function is also available for `Flowable`, `Single`, `Completable` and `Maybe`)


### RxView

You can use the `RxView` class to safely wrap view logic so that it is only executed during the view lifecycle. It can also be helpful to keep a clear distinction between the responsibilities of a `Fragment` or `Activity` (such as app navigation or launching an `Intent`) and the *view* layer of an MVVM app (presenting data from and sending events to the view model).

For example a fragment may define its view like so: 

```kotlin
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
```

(Notice we don't need to store a reference to the binding and set it to null when `onDestroyView` is called. `RxView` will clean up this memory leak for us.)

Now we can define our view logic as follows: 

```kotlin
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

        binding.tvOpenTerms.setOnClickListener {
            viewModel.showTermsClick.onNext(Unit)
        }
    }
}
```

(This example is taken directly [from the example app](https://github.com/conjure/RxLifecycle/blob/main/rxlifecycleexampleapp/src/main/java/co/uk/conjure/rxlifecycle/exampleapp/LoginView.kt).)

`RxView` will call `onStart` for us when the `Fragment` view lifecycle reaches the `STARTED` state. If we constructed our `RxView` using an `Activity` it would call on start when the `Activity` reached the `STARTED` state. Either way we can rely on the binding to have been initialized here so we don't have to worry about nullability.

We can also avoid having to store a `CompositeDisposable` in our `RxView` by using a few handy functions:

We provide the `whileStarted` and `whileCreated` extension functions for all of `Observable`, `Flowable`, `Single`, `Completable` and `Maybe`. These functions accept a `LifecycleOwner` and an `onNext` callback. (You can also optionally provide other callbacks for `onError`, `onStart`, `onComplete` etc.). `whileCreated` and `whileStarted` will subscribe to the upstream when the lifecycle reaches the `CREATED` or `STARTED` state and dispose the subscription automatically once it reaches the `DESTROYED` state. Notice that `RxView` is a `LifecycleOwner` and the lifecycle is the same as the fragments `viewLifecycleOwner` so we can pass `this` to `whileStarted` in the above example.

For common tasks like observing an `EditText` or `Button` we provide the `bind` extension functions. These accept an `Observer` to send events to, and an optional `Observable` to receive updates back from, the view model. These subscriptions are disposed for you automatically when the `RxView` reaches the `DESTROYED` state.

