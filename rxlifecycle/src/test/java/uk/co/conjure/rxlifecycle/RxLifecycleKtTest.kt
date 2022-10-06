package uk.co.conjure.rxlifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.CompletableSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

internal class RxLifecycleKtTest {

    @Before
    @OptIn(ExperimentalCoroutinesApi::class)
    fun before() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `Observable whileCreated`() {
        //PREPARE
        val observable = PublishSubject.create<Int>()
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.INITIALIZED)
        val onNext = ListConsumer<Int>()
        val onError = mock<Consumer<in Throwable>>()
        val onStart = mock<Consumer<Disposable>>()
        val onComplete = mock<Action>()
        observable.whileCreated(
            lifecycleOwner,
            onNext = onNext,
            onError = onError,
            onStart = onStart,
            onComplete = onComplete
        )

        //EXECUTE
        observable.onNext(1)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        observable.onNext(2)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        observable.onNext(3)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        observable.onNext(4)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        observable.onNext(5)

        //VERIFY
        assertEquals(listOf(2, 3, 4), onNext)
        verify(onError, never()).accept(any())
        verify(onStart, times(1)).accept(any())
        verify(onComplete, never()).run()
    }

    @Test
    fun `Observable whileStarted`() {
        //PREPARE
        val observable = PublishSubject.create<Int>()
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.INITIALIZED)
        val onNext = ListConsumer<Int>()
        val onError = mock<Consumer<in Throwable>>()
        val onStart = mock<Consumer<Disposable>>()
        val onComplete = mock<Action>()
        observable.whileStarted(
            lifecycleOwner,
            onNext = onNext,
            onError = onError,
            onStart = onStart,
            onComplete = onComplete
        )

        //EXECUTE
        observable.onNext(1)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        observable.onNext(2)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        observable.onNext(3)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        observable.onNext(4)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        observable.onNext(5)

        //VERIFY
        assertEquals(3, onNext.value)
        verify(onError, never()).accept(any())
        verify(onStart, times(1)).accept(any())
        verify(onComplete, never()).run()
    }

    @Test
    fun `Flowable whileCreated`() {
        //PREPARE
        val observable = PublishSubject.create<Int>()
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.INITIALIZED)
        val onNext = ListConsumer<Int>()
        val onError = mock<Consumer<in Throwable>>()
        val onStart = mock<Consumer<Disposable>>()
        val onComplete = mock<Action>()
        observable.toFlowable(BackpressureStrategy.ERROR).whileCreated(
            lifecycleOwner,
            onNext = onNext,
            onError = onError,
            onStart = onStart,
            onComplete = onComplete
        )

        //EXECUTE
        observable.onNext(1)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        observable.onNext(2)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        observable.onNext(3)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        observable.onNext(4)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        observable.onNext(5)

        //VERIFY
        assertEquals(listOf(2, 3, 4), onNext)
        verify(onError, never()).accept(any())
        verify(onStart, times(1)).accept(any())
        verify(onComplete, never()).run()
    }

    @Test
    fun `Flowable whileStarted`() {
        //PREPARE
        val observable = PublishSubject.create<Int>()
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.INITIALIZED)
        val onNext = ListConsumer<Int>()
        val onError = mock<Consumer<in Throwable>>()
        val onStart = mock<Consumer<Disposable>>()
        val onComplete = mock<Action>()
        observable.toFlowable(BackpressureStrategy.ERROR).whileStarted(
            lifecycleOwner,
            onNext = onNext,
            onError = onError,
            onStart = onStart,
            onComplete = onComplete
        )

        //EXECUTE
        observable.onNext(1)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        observable.onNext(2)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        observable.onNext(3)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        observable.onNext(4)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        observable.onNext(5)

        //VERIFY
        assertEquals(3, onNext.value)
        verify(onError, never()).accept(any())
        verify(onStart, times(1)).accept(any())
        verify(onComplete, never()).run()
    }

    @Test
    fun `Maybe whileCreated`() {
        //PREPARE
        val observable = PublishSubject.create<Int>()
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.INITIALIZED)
        val onSuccess = ListConsumer<Int>()
        val onError = mock<Consumer<in Throwable>>()
        val onStart = mock<Consumer<Disposable>>()
        val onComplete = mock<Action>()
        observable.firstElement().whileCreated(
            lifecycleOwner,
            onSuccess = onSuccess,
            onError = onError,
            onStart = onStart,
            onComplete = onComplete
        )

        //EXECUTE
        observable.onNext(1)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        observable.onNext(2)

        //VERIFY
        assertEquals(2, onSuccess.value)
        verify(onError, never()).accept(any())
        verify(onStart, times(1)).accept(any())
        verify(onComplete, never()).run()
    }

    @Test
    fun `Maybe whileStarted`() {
        //PREPARE
        val observable = PublishSubject.create<Int>()
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.INITIALIZED)
        val onSuccess = ListConsumer<Int>()
        val onError = mock<Consumer<in Throwable>>()
        val onStart = mock<Consumer<Disposable>>()
        val onComplete = mock<Action>()
        observable.firstElement().whileStarted(
            lifecycleOwner,
            onSuccess = onSuccess,
            onError = onError,
            onStart = onStart,
            onComplete = onComplete
        )

        //EXECUTE
        observable.onNext(1)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        observable.onNext(2)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        observable.onNext(3)

        //VERIFY
        assertEquals(3, onSuccess.value)
        verify(onError, never()).accept(any())
        verify(onStart, times(1)).accept(any())
        verify(onComplete, never()).run()
    }

    @Test
    fun `Completable whileCreated`() {
        //PREPARE
        val completable = CompletableSubject.create()
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.INITIALIZED)
        val onComplete = mock<Action>()
        val onError = mock<Consumer<in Throwable>>()
        val onStart = mock<Consumer<Disposable>>()
        completable.whileCreated(
            lifecycleOwner,
            onComplete = onComplete,
            onError = onError,
            onStart = onStart
        )

        //EXECUTE
        completable.onComplete()
        verify(onComplete, never()).run()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        completable.onComplete()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)

        //VERIFY
        verify(onComplete, times(1)).run()
        verify(onError, never()).accept(any())
        verify(onStart, times(1)).accept(any())
    }

    @Test
    fun testWhileStarted2() {
        //PREPARE
        val completable = CompletableSubject.create()
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.INITIALIZED)
        val onComplete = mock<Action>()
        val onError = mock<Consumer<in Throwable>>()
        val onStart = mock<Consumer<Disposable>>()
        completable.whileStarted(
            lifecycleOwner,
            onComplete = onComplete,
            onError = onError,
            onStart = onStart
        )

        //EXECUTE
        completable.onComplete()
        verify(onComplete, never()).run()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        verify(onComplete, never()).run()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)

        //VERIFY
        verify(onComplete, times(1)).run()
        verify(onError, never()).accept(any())
        verify(onStart, times(1)).accept(any())
    }

    private class ListConsumer<T : Any>(
        private val list: MutableList<T> = mutableListOf<T>()
    ) : Consumer<T>, List<T> by list {
        val value: T
            get() {
                assertEquals(1, list.size)
                return list[0]
            }

        override fun accept(t: T) {
            list.add(t)
        }

        override fun equals(other: Any?): Boolean {
            return list == other
        }

        override fun toString(): String {
            return list.toString()
        }

        override fun hashCode(): Int {
            return list.hashCode()
        }
    }
}