package net.ienlab.sogangassist.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

@Composable
@ExperimentalFoundationApi
fun MyBasicTooltipBox(
    positionProvider: PopupPositionProvider,
    tooltip: @Composable () -> Unit,
    state: MyBasicTooltipState,
    modifier: Modifier = Modifier,
    focusable: Boolean,
    enableUserInput: Boolean,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    Box {
        MyTooltipPopup(
            positionProvider = positionProvider,
            state = state,
            scope = scope,
            focusable = focusable,
            content = tooltip
        )

        MyWrappedAnchor(
            enableUserInput = enableUserInput,
            state = state,
            modifier = modifier,
            content = content
        )
    }

    DisposableEffect(state) {
        onDispose { state.onDispose() }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun MyWrappedAnchor(
    enableUserInput: Boolean,
    state: MyBasicTooltipState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val longPressLabel = stringResource(androidx.compose.foundation.R.string.tooltip_label)
    Box(modifier = modifier
        .myHandleGestures(enableUserInput, state)
        .myAnchorSemantics(longPressLabel, enableUserInput, state, scope)
    ) { content() }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun MyTooltipPopup(
    positionProvider: PopupPositionProvider,
    state: MyBasicTooltipState,
    scope: CoroutineScope,
    focusable: Boolean,
    content: @Composable () -> Unit
) {
    val tooltipDescription = stringResource(androidx.compose.foundation.R.string.tooltip_description)
    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = {
            if (state.isVisible) {
                scope.launch { state.dismiss() }
            }
        },
        properties = PopupProperties(focusable = focusable)
    ) {
        AnimatedVisibility(
            visible = state.isVisible,
            enter = fadeIn(tween(700)),
            exit = fadeOut(tween(700)),
//            modifier = Modifier.background(Color.Red)
        ) {
            Box(
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Assertive
                    paneTitle = tooltipDescription
                }
            ) { content() }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.myHandleGestures(
    enabled: Boolean,
    state: MyBasicTooltipState
): Modifier =
    if (enabled) {
        this
            .pointerInput(state) {
                coroutineScope {
                    awaitEachGesture {
                        val longPressTimeout = viewConfiguration.longPressTimeoutMillis
                        val pass = PointerEventPass.Initial

                        // wait for the first down press
                        val inputType = awaitFirstDown(pass = pass).type

                        if (inputType == PointerType.Touch || inputType == PointerType.Stylus) {
                            try {
                                // listen to if there is up gesture
                                // within the longPressTimeout limit
                                withTimeout(longPressTimeout) {
                                    waitForUpOrCancellation(pass = pass)
                                }
                            } catch (_: PointerEventTimeoutCancellationException) {
                                // handle long press - Show the tooltip
                                launch { state.show(MutatePriority.UserInput) }

                                // consume the children's click handling
                                val changes = awaitPointerEvent(pass = pass).changes
                                for (i in 0 until changes.size) {
                                    changes[i].consume()
                                }
                            }
                        }
                    }
                }
            }
            .pointerInput(state) {
                coroutineScope {
                    awaitPointerEventScope {
                        val pass = PointerEventPass.Main

                        while (true) {
                            val event = awaitPointerEvent(pass)
                            val inputType = event.changes[0].type
                            if (inputType == PointerType.Mouse) {
                                when (event.type) {
                                    PointerEventType.Enter -> {
                                        launch { state.show(MutatePriority.UserInput) }
                                    }

                                    PointerEventType.Exit -> {
                                        state.dismiss()
                                    }
                                }
                            }
                        }
                    }
                }
            }
    } else this

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.myAnchorSemantics(
    label: String,
    enabled: Boolean,
    state: MyBasicTooltipState,
    scope: CoroutineScope
): Modifier =
    if (enabled) {
        this.semantics(mergeDescendants = true) {
            onLongClick(
                label = label,
                action = {
                    scope.launch { state.show() }
                    true
                }
            )
        }
    } else this


/**
 * Create and remember the default [MyBasicTooltipState].
 *
 * @param initialIsVisible the initial value for the tooltip's visibility when drawn.
 * @param isPersistent [Boolean] that determines if the tooltip associated with this
 * will be persistent or not. If isPersistent is true, then the tooltip will
 * only be dismissed when the user clicks outside the bounds of the tooltip or if
 * [MyBasicTooltipState.dismiss] is called. When isPersistent is false, the tooltip will dismiss after
 * a short duration. Ideally, this should be set to true when there is actionable content
 * being displayed within a tooltip.
 * @param mutatorMutex [MutatorMutex] used to ensure that for all of the tooltips associated
 * with the mutator mutex, only one will be shown on the screen at any time.
 */
@Composable
@ExperimentalFoundationApi
fun rememberMyBasicTooltipState(
    initialIsVisible: Boolean = false,
    isPersistent: Boolean = true,
    mutatorMutex: MutatorMutex = MyBasicTooltipDefaults.GlobalMutatorMutex
): MyBasicTooltipState =
    remember(
        isPersistent,
        mutatorMutex
    ) {
        MyBasicTooltipStateImpl(
            initialIsVisible = initialIsVisible,
            isPersistent = isPersistent,
            mutatorMutex = mutatorMutex
        )
    }

/**
 * Constructor extension function for [MyBasicTooltipState]
 *
 * @param initialIsVisible the initial value for the tooltip's visibility when drawn.
 * @param isPersistent [Boolean] that determines if the tooltip associated with this
 * will be persistent or not. If isPersistent is true, then the tooltip will
 * only be dismissed when the user clicks outside the bounds of the tooltip or if
 * [MyBasicTooltipState.dismiss] is called. When isPersistent is false, the tooltip will dismiss after
 * a short duration. Ideally, this should be set to true when there is actionable content
 * being displayed within a tooltip.
 * @param mutatorMutex [MutatorMutex] used to ensure that for all of the tooltips associated
 * with the mutator mutex, only one will be shown on the screen at any time.
 */
@Stable
@ExperimentalFoundationApi
fun MyBasicTooltipState(
    initialIsVisible: Boolean = false,
    isPersistent: Boolean = true,
    mutatorMutex: MutatorMutex = MyBasicTooltipDefaults.GlobalMutatorMutex
): MyBasicTooltipState =
    MyBasicTooltipStateImpl(
        initialIsVisible = initialIsVisible,
        isPersistent = isPersistent,
        mutatorMutex = mutatorMutex
    )

@Stable
@OptIn(ExperimentalFoundationApi::class)
private class MyBasicTooltipStateImpl(
    initialIsVisible: Boolean,
    override val isPersistent: Boolean,
    private val mutatorMutex: MutatorMutex
) : MyBasicTooltipState {
    override var isVisible by mutableStateOf(initialIsVisible)

    /**
     * continuation used to clean up
     */
    private var job: (CancellableContinuation<Unit>)? = null

    /**
     * Show the tooltip associated with the current [MyBasicTooltipState].
     * When this method is called, all of the other tooltips associated
     * with [mutatorMutex] will be dismissed.
     *
     * @param mutatePriority [MutatePriority] to be used with [mutatorMutex].
     */
    override suspend fun show(
        mutatePriority: MutatePriority
    ) {
        val cancellableShow: suspend () -> Unit = {
            suspendCancellableCoroutine { continuation ->
                isVisible = true
                job = continuation
            }
        }

        // Show associated tooltip for [TooltipDuration] amount of time
        // or until tooltip is explicitly dismissed depending on [isPersistent].
        mutatorMutex.mutate(mutatePriority) {
            try {
                if (isPersistent) {
                    cancellableShow()
                } else {
                    withTimeout(MyBasicTooltipDefaults.TooltipDuration) {
                        cancellableShow()
                    }
                }
            } finally {
                // timeout or cancellation has occurred
                // and we close out the current tooltip.
                isVisible = false
            }
        }
    }

    /**
     * Dismiss the tooltip associated with
     * this [MyBasicTooltipState] if it's currently being shown.
     */
    override fun dismiss() {
        isVisible = false
    }

    /**
     * Cleans up [mutatorMutex] when the tooltip associated
     * with this state leaves Composition.
     */
    override fun onDispose() {
        job?.cancel()
    }
}

/**
 * The state that is associated with an instance of a tooltip.
 * Each instance of tooltips should have its own [MyBasicTooltipState].
 */
@Stable
@ExperimentalFoundationApi
interface MyBasicTooltipState {
    /**
     * [Boolean] that indicates if the tooltip is currently being shown or not.
     */
    val isVisible: Boolean

    /**
     * [Boolean] that determines if the tooltip associated with this
     * will be persistent or not. If isPersistent is true, then the tooltip will
     * only be dismissed when the user clicks outside the bounds of the tooltip or if
     * [MyBasicTooltipState.dismiss] is called. When isPersistent is false, the tooltip will
     * dismiss after a short duration. Ideally, this should be set to true when there
     * is actionable content being displayed within a tooltip.
     */
    val isPersistent: Boolean

    /**
     * Show the tooltip associated with the current [MyBasicTooltipState].
     * When this method is called all of the other tooltips currently
     * being shown will dismiss.
     *
     * @param mutatePriority [MutatePriority] to be used.
     */
    suspend fun show(mutatePriority: MutatePriority = MutatePriority.Default)

    /**
     * Dismiss the tooltip associated with
     * this [MyBasicTooltipState] if it's currently being shown.
     */
    fun dismiss()

    /**
     * Clean up when the this state leaves Composition.
     */
    fun onDispose()
}

/**
 * BasicTooltip defaults that contain default values for tooltips created.
 */
@ExperimentalFoundationApi
object MyBasicTooltipDefaults {
    /**
     * The global/default [MutatorMutex] used to sync Tooltips.
     */
    val GlobalMutatorMutex: MutatorMutex = MutatorMutex()

    /**
     * The default duration, in milliseconds, that non-persistent tooltips
     * will show on the screen before dismissing.
     */
    const val TooltipDuration = 2500L
}
