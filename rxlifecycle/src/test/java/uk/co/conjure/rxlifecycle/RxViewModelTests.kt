package uk.co.conjure.rxlifecycle

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.Test

class RxViewModelTests {

    /**
     * GIVEN the upstream of a hot() Observable completes
     * WHEN cacheOnComplete is false
     * THEN the hot() Observable completes
     * AND late subscribers will create a new subscription to the upstream
     */
    @Test
    fun `Observable_hot() subject`() {
        val subject = PublishSubject.create<Int>()

        val uut = object : RxViewModel() {
            val hot = subject
                .hot()
        }

        subject.onNext(1)
        subject.onNext(2)
        subject.onNext(3)
        subject.onComplete()

        uut.hot.test()
            .assertComplete()
            .assertValueCount(0)
    }

    /**
     * GIVEN the upstream of a hot() Observable completes
     * WHEN cacheOnComplete is true
     * THEN the hot() Observable does not complete
     * AND late subscribers receive the last value
     */
    @Test
    fun `Observable_hot(true)`() {
        val subject = PublishSubject.create<Int>()

        val uut = object : RxViewModel() {
            val hot = subject
                .hot(true)
        }

        subject.onNext(1)
        subject.onNext(2)
        subject.onNext(3)
        subject.onComplete()

        uut.hot.test()
            .assertNotComplete()
            .assertValueCount(1)
            .assertValue(3)

    }

    @Test
    fun `Observable_hot error`() {
        val subject = PublishSubject.create<Int>()

        val uut = object : RxViewModel() {
            val hot = subject
                .hot()
        }

        subject.onError(RuntimeException())

        uut.hot.test()
            .assertNotComplete()
            .assertError(RuntimeException::class.java)
    }
}