package uk.co.conjure.rxlifecycle

import android.annotation.SuppressLint
import androidx.lifecycle.*
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.observers.DisposableMaybeObserver
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.subscribers.DisposableSubscriber

/**
 * Emits events from this [Observable] to the consumers as long as the lifecycle owner remains
 * in the state [Lifecycle.State.CREATED] and then disposes the subscription.
 */
fun <T : Any> Observable<T>.whileCreated(
    owner: LifecycleOwner,
    onNext: Consumer<T>,
    onError: Consumer<in Throwable>? = null,
    onStart: Consumer<Disposable>? = null,
    onComplete: Action? = null,
) = whileCreated(owner) {
    subscribeWith(
        observer(
            onNext = onNext,
            onError = onError,
            onStart = onStart,
            onComplete = onComplete
        )
    )
}

/**
 * Emits events from this [Observable] to the consumers as long as the lifecycle owner remains
 * in the state [Lifecycle.State.STARTED] and then disposes the subscription.
 */
fun <T : Any> Observable<T>.whileStarted(
    owner: LifecycleOwner,
    onNext: Consumer<T>,
    onError: Consumer<in Throwable>? = null,
    onStart: Consumer<Disposable>? = null,
    onComplete: Action? = null,
) = whileStarted(owner) {
    subscribeWith(
        observer(
            onNext = onNext,
            onError = onError,
            onStart = onStart,
            onComplete = onComplete
        )
    )
}

/**
 * Emits events from this [Flowable] to the consumers as long as the lifecycle owner remains
 * in the state [Lifecycle.State.CREATED] and then disposes the subscription.
 */
fun <T : Any> Flowable<T>.whileCreated(
    owner: LifecycleOwner,
    onNext: Consumer<T>,
    onError: Consumer<in Throwable>? = null,
    onStart: Consumer<Disposable>? = null,
    onComplete: Action? = null
) = whileCreated(owner) {
    subscribeWith(
        subscriber(
            onNext = onNext,
            onError = onError,
            onStart = onStart,
            onComplete = onComplete
        )
    )
}

/**
 * Emits events from this [Flowable] to the consumers as long as the lifecycle owner remains
 * in the state [Lifecycle.State.STARTED] and then disposes the subscription.
 */
fun <T : Any> Flowable<T>.whileStarted(
    owner: LifecycleOwner,
    onNext: Consumer<T>,
    onError: Consumer<in Throwable>? = null,
    onStart: Consumer<Disposable>? = null,
    onComplete: Action? = null
) = whileStarted(owner) {
    subscribeWith(
        subscriber(
            onNext = onNext,
            onError = onError,
            onStart = onStart,
            onComplete = onComplete
        )
    )
}

/**
 * Emits events from this [Maybe] to the consumers as long as the lifecycle owner remains
 * in the state [Lifecycle.State.CREATED] and then disposes the subscription.
 */
fun <T : Any> Maybe<T>.whileCreated(
    owner: LifecycleOwner,
    onSuccess: Consumer<T>,
    onError: Consumer<in Throwable>? = null,
    onStart: Consumer<Disposable>? = null,
    onComplete: Action? = null,
) = whileCreated(owner) {
    subscribeWith(
        maybeObserver(
            onSuccess = onSuccess,
            onError = onError,
            onStart = onStart,
            onComplete = onComplete
        )
    )
}

/**
 * Emits events from this [Maybe] to the consumers as long as the lifecycle owner remains
 * in the state [Lifecycle.State.STARTED] and then disposes the subscription.
 */
fun <T : Any> Maybe<T>.whileStarted(
    owner: LifecycleOwner,
    onSuccess: Consumer<T>,
    onError: Consumer<in Throwable>? = null,
    onStart: Consumer<Disposable>? = null,
    onComplete: Action? = null,
) = whileStarted(owner) {
    subscribeWith(
        maybeObserver(
            onSuccess = onSuccess,
            onError = onError,
            onStart = onStart,
            onComplete = onComplete
        )
    )
}

/**
 * Emits events from this [Completable] to the consumers as long as the lifecycle owner remains
 * in the state [Lifecycle.State.CREATED] and then disposes the subscription.
 */
fun Completable.whileCreated(
    owner: LifecycleOwner,
    onComplete: Action? = null,
    onError: Consumer<in Throwable>?,
    onStart: Consumer<Disposable>? = null
) = whileCreated(owner) {
    subscribeWith(
        completableObserver(
            onStart = onStart,
            onComplete = onComplete,
            onError = onError
        )
    )
}

/**
 * Emits events from this [Completable] to the consumers as long as the lifecycle owner remains
 * in the state [Lifecycle.State.STARTED] and then disposes the subscription.
 */
fun Completable.whileStarted(
    owner: LifecycleOwner,
    onComplete: Action? = null,
    onError: Consumer<in Throwable>?,
    onStart: Consumer<Disposable>? = null
) = whileStarted(owner) {
    subscribeWith(
        completableObserver(
            onStart = onStart,
            onComplete = onComplete,
            onError = onError
        )
    )
}

private fun whileCreated(
    owner: LifecycleOwner,
    subscribe: () -> Disposable
) {
    val observer = object : DefaultLifecycleObserver {
        var disposable: Disposable? = null

        @SuppressLint("RxSubscribeOnError")
        override fun onCreate(owner: LifecycleOwner) {
            super.onStart(owner)
            disposable = subscribe()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            disposable?.dispose()
            owner.lifecycle.removeObserver(this)
            super.onStop(owner)
        }
    }

    owner.lifecycle.addObserver(observer)
}

private fun whileStarted(
    owner: LifecycleOwner,
    subscribe: () -> Disposable
) {
    val observer = object : DefaultLifecycleObserver {
        var disposable: Disposable? = null

        @SuppressLint("RxSubscribeOnError")
        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            disposable = subscribe()
        }

        override fun onStop(owner: LifecycleOwner) {
            disposable?.dispose()
            owner.lifecycle.removeObserver(this)
            super.onStop(owner)
        }
    }

    owner.lifecycle.addObserver(observer)
}

private fun <T : Any> observer(
    onNext: Consumer<T>,
    onError: Consumer<in Throwable>? = null,
    onStart: Consumer<Disposable>? = null,
    onComplete: Action? = null,
) = object : DisposableObserver<T>() {
    override fun onStart() {
        onStart?.accept(this)
    }

    override fun onNext(t: T) {
        onNext.accept(t)
    }

    override fun onError(e: Throwable) {
        onError?.accept(e)
    }

    override fun onComplete() {
        onComplete?.run()
    }
}

private fun <T : Any> subscriber(
    onNext: Consumer<T>,
    onError: Consumer<in Throwable>? = null,
    onStart: Consumer<Disposable>? = null,
    onComplete: Action? = null
) = object : DisposableSubscriber<T>() {
    override fun onStart() {
        onStart?.accept(this)
    }

    override fun onNext(t: T) {
        onNext.accept(t)
    }

    override fun onError(t: Throwable) {
        onError?.accept(t)
    }

    override fun onComplete() {
        onComplete?.run()
    }
}

private fun <T : Any> maybeObserver(
    onSuccess: Consumer<T>,
    onError: Consumer<in Throwable>? = null,
    onStart: Consumer<Disposable>? = null,
    onComplete: Action? = null,
) = object : DisposableMaybeObserver<T>() {

    override fun onStart() {
        onStart?.accept(this)
    }

    override fun onSuccess(t: T) {
        onSuccess.accept(t)
    }

    override fun onError(e: Throwable) {
        onError?.accept(e)
    }

    override fun onComplete() {
        onComplete?.run()
    }
}

private fun completableObserver(
    onComplete: Action? = null,
    onError: Consumer<in Throwable>? = null,
    onStart: Consumer<Disposable>? = null
) = object : DisposableCompletableObserver() {

    override fun onStart() {
        onStart?.accept(this)
    }

    override fun onError(e: Throwable) {
        onError?.accept(e)
    }

    override fun onComplete() {
        onComplete?.run()
    }
}
