package de.rki.coronawarnapp.util.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rki.coronawarnapp.util.coroutine.DefaultDispatcherProvider
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

abstract class CWAViewModel constructor(
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider(),
    private val childViewModels: List<CWAViewModel> = emptyList()
) : ViewModel() {

    private val tag: String = this::class.simpleName!!
    var launchErrorHandler: CoroutineExceptionHandler? = null

    init {
        Timber.tag(tag).v("Initialized")
    }

    /**
     * This launches a coroutine on another thread
     * Remember to switch to the main thread if you want to update the UI directly
     */
    fun launch(
        scope: CoroutineScope = viewModelScope,
        context: CoroutineContext = dispatcherProvider.Default,
        errorHandler: CoroutineExceptionHandler? = launchErrorHandler,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val combinedContext = errorHandler?.let { context + it } ?: context
        try {
            scope.launch(context = combinedContext, block = block)
        } catch (e: CancellationException) {
            Timber.w(e, "launch()ed coroutine was canceled (scope=%s).", scope)
        }
    }

    fun <T> Flow<T>.launchInViewModel() = this.launchIn(viewModelScope + dispatcherProvider.Default)

    @CallSuper
    override fun onCleared() {
        Timber.tag(tag).v("onCleared()")
        childViewModels.forEach {
            Timber.tag(tag).v("Clearing child VM: %s", it)
            it.onCleared()
        }
        super.onCleared()
    }
}
