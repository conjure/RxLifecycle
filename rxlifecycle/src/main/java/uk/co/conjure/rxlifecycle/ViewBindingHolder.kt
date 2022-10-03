package uk.co.conjure.rxlifecycle

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import java.lang.Exception

/**
 * Abstract base class for anything that holds reference to a [ViewBinding]. The binding instance
 * should be released once it is no longer needed after the view is destroyed.
 */
abstract class ViewBindingHolder<B : ViewBinding> : LifecycleOwner {

    private var _binding: B? = null

    protected val binding: B get() = _binding!!

    protected val context: Context get() = _binding!!.root.context

    private var lifecycle: Lifecycle? = null

    override fun getLifecycle(): Lifecycle {
        return lifecycle
            ?: throw Exception("You must call registerBinding on this View before accessing its lifecycle")
    }

    /**
     * Only valid for the lifecycle of the View
     * Call [registerBinding] to set the binding.
     *
     * For Activities it will be available until onDestroy().
     * For Fragments it will be available unit onDestroyView().
     */
    fun requireBinding() = checkNotNull(_binding)

    fun requireBinding(lambda: (B) -> Unit) {
        _binding?.run { lambda(this) }
    }

    /**
     * Register a ViewBinding for an Activity
     */
    fun registerBinding(binding: B, activity: AppCompatActivity) {
        registerBinding(binding, activity.lifecycle)
        onBindingRegistered(binding, activity)
    }

    /**
     * Register a ViewBinding for a Fragment
     */
    fun registerBinding(binding: B, fragment: Fragment) {
        registerBinding(binding, fragment.viewLifecycleOwner.lifecycle)
        onBindingRegistered(binding, fragment)
    }

    /**
     * Called when a Fragment or Activity registered a binding.
     * Subclasses may use this to register their View subscriptions.
     */
    abstract fun onBindingRegistered(binding: B, owner: LifecycleOwner)


    private fun registerBinding(binding: B, lifecycle: Lifecycle) {
        this._binding = binding
        this.lifecycle = lifecycle
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)
                this@ViewBindingHolder._binding = null
            }
        })
    }

}