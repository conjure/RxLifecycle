# Module rxlifecycle

RxLifecycle provides classes and extension functions for a better lifecycle management with RxJava.

RxView is a lifecycle aware class that provides callback for all common lifecycle events.

RxViewModel offers a CompositeDisposable that is disposed in the ViewModel's onCleared() method.
Additionally it provides the `hot()` extension functions to create a hot observable from a cold one.

Use `whileCreated()` and `whileStarted()` in RxView, Activities or other LifecycleOwners to
automatically dispose subscriptions with the lifecycle.

Visit the [GitHub page](https://github.com/conjure/RxLifecycle) for examples and integration
guides.

# Package uk.co.conjure.rxlifecycle


