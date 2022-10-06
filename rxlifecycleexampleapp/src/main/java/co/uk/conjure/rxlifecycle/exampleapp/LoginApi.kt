package co.uk.conjure.rxlifecycle.exampleapp

import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit

class LoginApi {

    fun login(email: String, password: String): Single<Result> {
        return Single.just(Result.SUCCESS).delay(2, TimeUnit.SECONDS)
    }

    fun getTermsAndConditions(): Single<String> {
        return Single.just("Terms and condition content").delay(1, TimeUnit.SECONDS)
    }

    enum class Result {
        SUCCESS,
        INVALID_CREDENTIALS
    }
}