package uk.co.conjure.rxlifecycle

import android.util.Log
import androidx.lifecycle.ViewModel as AndroidViewModel
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.flowables.ConnectableFlowable
import io.reactivex.rxjava3.observables.ConnectableObservable

/**
 * Base class for a view model that stores a [CompositeDisposable] of all active subscriptions
 * and destroys them on [AndroidViewModel.onCleared]
 */
abstract class RxViewModel : AndroidViewModel() {

    /**
     * This CompositeDisposable will be disposed in onCleared()
     * Therefore it survives orientation changes
     */
    protected val keepAlive: CompositeDisposable = CompositeDisposable()

    override fun onCleared() {
        keepAlive.dispose()
        super.onCleared()
    }

    /**
     * Applies a replay(1).refCount() to the Flowable and subscribes to it.
     * The Flowable will be alive as long as the ViewModel is alive (until the ViewModels
     * [onCleared] method is called).
     *
     *  @see [Observable.replay], [ConnectableFlowable.refCount]
     */
    protected fun <T : Any> Flowable<T>.hot(cacheOnComplete: Boolean): Flowable<T> {
        return this
            .let { upstream ->
                if (cacheOnComplete) {
                    upstream.concatWith(Flowable.never())
                } else {
                    upstream.doOnComplete {
                        Log.w(
                            "RxViewModel",
                            "WARNING: A .hot() Flowable completed. Late subscribers will NOT get the last item. You might want to use .hot(true)"
                        )
                    }
                }
            }
            .replay(1).refCount().also {
            keepAlive.add(it.subscribe({}, {}))
        }
    }

    /**
     * Applies a replay(1).refCount() to the Observable and subscribes to it.
     * The Observable will be alive as long as the ViewModel is alive (until the ViewModels
     * [onCleared] method is called).
     * @see [Observable.replay], [ConnectableObservable.refCount]
     */
    protected fun <T : Any> Observable<T>.hot(cacheOnComplete: Boolean = false): Observable<T> {
        return this
            .let { upstream ->
                if (cacheOnComplete) {
                    upstream.concatWith(Observable.never())
                } else {
                    upstream.doOnComplete {
                        Log.w(
                            "RxViewModel",
                            "WARNING: A .hot() Observable completed. Late subscribers will NOT get the last item. You might want to use .hot(true)"
                        )
                    }
                }
            }
            .replay(1).refCount().also {
                keepAlive.add(it.subscribe({}, {}))
            }
    }

    /**
     * Caches the Single and subscribes to it.
     * The Single will return it's result for any later subscriber as long as the ViewModel is
     * alive (until the ViewModels [onCleared] method is called).
     * @see [Single.cache]
     */
    protected fun <T : Any> Single<T>.hot(): Single<T> {
        return this.cache().also {
            keepAlive.add(it.subscribe({}, {}))
        }
    }

    /**
     * Caches the Maybe and subscribes to it.
     * The Maybe will return it's result for any later subscriber as long as the ViewModel is
     * alive (until the ViewModels [onCleared] method is called).
     * @see [Maybe.cache]
     */
    protected fun <T : Any> Maybe<T>.hot(): Maybe<T> {
        return this.cache().also {
            keepAlive.add(it.subscribe({}, {}))
        }
    }

    /**
     * Caches the Completable and subscribes to it.
     * The Completable will return it's result for any later subscriber as long as the ViewModel is
     * alive (until the ViewModels [onCleared] method is called).
     * @see [Completable.cache]
     */
    protected fun Completable.hot(): Completable {
        return this.cache().also {
            keepAlive.add(it.subscribe({}, {}))
        }
    }
}