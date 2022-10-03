package uk.co.conjure.rxlifecycle

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer

/**
 * Base class for the view component on an MVVM architecture app. Provides convenience functions
 * for observing sources during the [Lifecycle.State.STARTED] state of a views lifecycle.
 */
open class RxView<B : ViewBinding> : LifecycleView<B>() {

    /**
     * Used for binding an [EditText] to a [androidx.lifecycle.ViewModel].
     *
     * @param observer Pass the view models observer to receive text updates in the view model
     * while the view is [Lifecycle.State.STARTED]
     * @param observable Pass the view models observable. The given [EditText] text will be updated
     * whenever this emits a new string if it's text is not the same as the emitted string
     * while the view is [Lifecycle.State.STARTED]
     */
    @SuppressLint("RxSubscribeOnError")
    protected fun EditText.bind(
        observer: Observer<String>,
        observable: Observable<String>
    ) {
        this.changes().whileStarted(this@RxView,
            onNext = { observer.onNext(it) },
            onError = { observer.onError(it) },
            onStart = { observer.onSubscribe(it) },
            onComplete = { observer.onComplete() }
        )
        observable.whileStarted(this@RxView,
            onNext = { text ->
                if (text != this.text.toString()) {
                    this.setText(text)
                    this.setSelection(text.length)
                }
            }
        )
    }

    /**
     * Used for binding an [TextView] to a [androidx.lifecycle.ViewModel].
     *
     * @param observable Pass the view models observable. The given [TextView] text will be updated
     * whenever this emits a new string while the view is [Lifecycle.State.STARTED]
     */
    protected fun TextView.bind(observable: Observable<String>) {
        observable.whileStarted(this@RxView,
            onNext = { text -> this.text = text }
        )
    }

    /**
     * Used for binding a [Button] to a [androidx.lifecycle.ViewModel].
     *
     * @param observer Pass the view models observer. The given [Button] clicks will be sent to
     * this observer while the view is [Lifecycle.State.STARTED]
     * @param observable An opional observable that enables/disables the button while the view is
     * [Lifecycle.State.STARTED]
     */
    protected fun Button.bind(
        observer: Observer<Unit>,
        observable: Observable<Boolean> = Observable.just(true)
    ) {
        this.clicks().whileStarted(this@RxView,
            onNext = { observer.onNext(it) },
            onError = { observer.onError(it) },
            onStart = { observer.onSubscribe(it) },
            onComplete = { observer.onComplete() }
        )
        observable.whileStarted(this@RxView,
            onNext = { isEnabled -> this.isEnabled = isEnabled }
        )
    }

    /**
     * Used for binding clicks on a [View] to a [androidx.lifecycle.ViewModel].
     *
     * @param observer Pass the view models observer. The given [View] clicks will be sent to
     * this observer while the view is [Lifecycle.State.STARTED]
     * @param observable An opional observable that enables/disables the view while the view is
     * [Lifecycle.State.STARTED]
     */
    protected fun View.bindClicks(
        observer: Observer<Unit>,
        observable: Observable<Boolean> = Observable.just(true)
    ) {
        this.clicks().whileStarted(this@RxView,
            onNext = { observer.onNext(it) },
            onError = { observer.onError(it) },
            onStart = { observer.onSubscribe(it) },
            onComplete = { observer.onComplete() }
        )
        observable.whileStarted(this@RxView,
            onNext = { isEnabled -> this.isEnabled = isEnabled }
        )
    }
}