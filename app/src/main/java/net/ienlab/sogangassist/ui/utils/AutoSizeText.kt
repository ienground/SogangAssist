@file:Suppress(
    "CANNOT_OVERRIDE_INVISIBLE_MEMBER",
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE",
)

package net.ienlab.sogangassist.ui.utils

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.InternalFoundationTextApi
import androidx.compose.foundation.text.TextDelegate
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import net.ienlab.sogangassist.ui.theme.Pretendard
import net.ienlab.sogangassist.ui.utils.SuggestedFontSizesStatus.Companion.rememberSuggestedFontSizesStatus
import kotlin.math.min

@Composable
fun AutoSizeText(
    modifier: Modifier = Modifier,
    text: String,
    isBlink: Boolean = false,
    color: Color = Color.Unspecified,
    suggestedFontSizes: ImmutableWrapper<List<TextUnit>> = emptyList<TextUnit>().toImmutableWrapper(),
    suggestedFontSizesStatus: SuggestedFontSizesStatus = suggestedFontSizes.rememberSuggestedFontSizesStatus,
    stepGranularityTextSize: TextUnit = TextUnit.Unspecified,
    minTextSize: TextUnit = TextUnit.Unspecified,
    maxTextSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = Pretendard,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    alignment: Alignment = Alignment.TopStart,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
    lineSpacingRatio: Float = style.lineHeight.value / style.fontSize.value,
) {
    AutoSizeText(
        text = AnnotatedString(text),
        modifier = modifier,
        isBlink = isBlink,
        color = color,
        suggestedFontSizes = suggestedFontSizes,
        suggestedFontSizesStatus = suggestedFontSizesStatus,
        stepGranularityTextSize = stepGranularityTextSize,
        minTextSize = minTextSize,
        maxTextSize = maxTextSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        alignment = alignment,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style,
        lineSpacingRatio = lineSpacingRatio,
    )
}

/**
 * Composable function that automatically adjusts the text size to fit within given constraints using AnnotatedString, considering the ratio of line spacing to text size.
 *
 * Features:
 *  Similar to AutoSizeText(String), with support for AnnotatedString.
 *
 * @param inlineContent a map storing composables that replaces certain ranges of the text, used to
 * insert composables into text layout. See [InlineTextContent].
 * @see AutoSizeText
 */
@Composable
fun AutoSizeText(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    isBlink: Boolean = false,
    color: Color = Color.Unspecified,
    suggestedFontSizes: ImmutableWrapper<List<TextUnit>> = emptyList<TextUnit>().toImmutableWrapper(),
    suggestedFontSizesStatus: SuggestedFontSizesStatus = suggestedFontSizes.rememberSuggestedFontSizesStatus,
    stepGranularityTextSize: TextUnit = TextUnit.Unspecified,
    minTextSize: TextUnit = TextUnit.Unspecified,
    maxTextSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = Pretendard,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    alignment: Alignment = Alignment.TopStart,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: ImmutableWrapper<Map<String, InlineTextContent>> = mapOf<String, InlineTextContent>().toImmutableWrapper(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
    lineSpacingRatio: Float = style.lineHeight.value / style.fontSize.value,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    val alpha = if (isBlink) infiniteTransition.animateFloat(initialValue = 1f, targetValue = 0f, animationSpec = infiniteRepeatable(
        animation = tween(700),
        repeatMode = RepeatMode.Reverse
    ), label = "").value else 1f

    val density = LocalDensity.current
    // Change font scale to 1F
    CompositionLocalProvider(
        LocalDensity provides Density(density = density.density, fontScale = 1F)
    ) {
        BoxWithConstraints(
            modifier = modifier.alpha(alpha),
            contentAlignment = alignment,
        ) {
            val combinedTextStyle = LocalTextStyle.current + style.copy(
                color = color.takeIf { it.isSpecified } ?: style.color,
                fontStyle = fontStyle ?: style.fontStyle,
                fontWeight = fontWeight ?: style.fontWeight,
                fontFamily = fontFamily ?: style.fontFamily,
                letterSpacing = letterSpacing.takeIf { it.isSpecified } ?: style.letterSpacing,
                textDecoration = textDecoration ?: style.textDecoration,
                textAlign = when (alignment) {
                    Alignment.TopStart, Alignment.CenterStart, Alignment.BottomStart -> TextAlign.Start
                    Alignment.TopCenter, Alignment.Center, Alignment.BottomCenter -> TextAlign.Center
                    Alignment.TopEnd, Alignment.CenterEnd, Alignment.BottomEnd -> TextAlign.End
                    else -> TextAlign.Unspecified
                },
            )

            val layoutDirection = LocalLayoutDirection.current
            val currentDensity = LocalDensity.current
            val fontFamilyResolver = LocalFontFamilyResolver.current
            val coercedLineSpacingRatio = lineSpacingRatio.takeIf { it.isFinite() && it >= 1 } ?: 1F
            val shouldMoveBackward: (TextUnit) -> Boolean = {
                shouldShrink(
                    text = text,
                    textStyle = combinedTextStyle.copy(
                        fontSize = it,
                        lineHeight = it * coercedLineSpacingRatio,
                    ),
                    maxLines = maxLines,
                    minLines = minLines,
                    softWrap = softWrap,
                    layoutDirection = layoutDirection,
                    density = currentDensity,
                    fontFamilyResolver = fontFamilyResolver,
                )
            }

            val electedFontSize = kotlin.run {
                if (suggestedFontSizesStatus == SuggestedFontSizesStatus.VALID)
                    suggestedFontSizes.value
                else
                    remember(suggestedFontSizes) {
                        suggestedFontSizes.value
                            .filter { it.isSp }
                            .takeIf { it.isNotEmpty() }
                            ?.sortedBy { it.value }
                    }
            }
                ?.findElectedValue(shouldMoveBackward = shouldMoveBackward)
                ?: rememberCandidateFontSizesIntProgress(
                    density = density,
                    dpSize = DpSize(maxWidth, maxHeight),
                    maxTextSize = maxTextSize,
                    minTextSize = minTextSize,
                    stepGranularityTextSize = stepGranularityTextSize,
                ).findElectedValue(
                    transform = { density.toSp(it) },
                    shouldMoveBackward = shouldMoveBackward,
                )

            Text(
                text = text,
                overflow = overflow,
                softWrap = softWrap,
                maxLines = maxLines,
                minLines = minLines,
                inlineContent = inlineContent.value,
                onTextLayout = onTextLayout,
                style = combinedTextStyle.copy(
                    fontSize = electedFontSize,
                    lineHeight = electedFontSize * coercedLineSpacingRatio,
                ),
            )
        }
    }
}

@OptIn(InternalFoundationTextApi::class)
private fun BoxWithConstraintsScope.shouldShrink(
    text: AnnotatedString,
    textStyle: TextStyle,
    maxLines: Int,
    minLines: Int,
    softWrap: Boolean,
    layoutDirection: LayoutDirection,
    density: Density,
    fontFamilyResolver: FontFamily.Resolver,
) = TextDelegate(
    text = text,
    style = textStyle,
    maxLines = maxLines,
    minLines = minLines,
    softWrap = softWrap,
    overflow = TextOverflow.Clip,
    density = density,
    fontFamilyResolver = fontFamilyResolver,
).layout(
    constraints = constraints,
    layoutDirection = layoutDirection,
).hasVisualOverflow

@Composable
private fun rememberCandidateFontSizesIntProgress(
    density: Density,
    dpSize: DpSize,
    minTextSize: TextUnit = TextUnit.Unspecified,
    maxTextSize: TextUnit = TextUnit.Unspecified,
    stepGranularityTextSize: TextUnit = TextUnit.Unspecified,
): IntProgression {
    val max = remember(maxTextSize, dpSize, density) {
        val intSize = density.toIntSize(dpSize)
        min(intSize.width, intSize.height).let { max ->
            maxTextSize
                .takeIf { it.isSp }
                ?.let { density.roundToPx(it) }
                ?.coerceIn(range = 0..max)
                ?: max
        }
    }

    val min = remember(minTextSize, max, density) {
        minTextSize
            .takeIf { it.isSp }
            ?.let { density.roundToPx(it) }
            ?.coerceIn(range = 0..max)
            ?: 0
    }

    val step = remember(stepGranularityTextSize, min, max, density) {
        stepGranularityTextSize
            .takeIf { it.isSp }
            ?.let { density.roundToPx(it) }
            ?.coerceAtLeast(minimumValue = 1)
            ?: 1
    }

    return remember(min, max, step) {
        min..max step step
    }
}

// This function works by using a binary search algorithm
fun <E> List<E>.findElectedValue(shouldMoveBackward: (E) -> Boolean) = run {
    indices.findElectedValue(
        transform = { this[it] },
        shouldMoveBackward = shouldMoveBackward,
    )
}

// This function works by using a binary search algorithm
private fun <E> IntProgression.findElectedValue(
    transform: (Int) -> E,
    shouldMoveBackward: (E) -> Boolean,
) = run {
    var low = first
    var high = last
    while (low <= high) {
        val mid = low + (high - low) / 2
        if (shouldMoveBackward(transform(mid)))
            high = mid - 1
        else
            low = mid + 1
    }
    transform(high.coerceAtLeast(minimumValue = first))
}

enum class SuggestedFontSizesStatus {
    VALID, INVALID, UNKNOWN;

    companion object {
        val List<TextUnit>.suggestedFontSizesStatus
            get() = if (isNotEmpty() && all { it.isSp } && sortedBy { it.value } == this)
                VALID
            else
                INVALID
        val ImmutableWrapper<List<TextUnit>>.rememberSuggestedFontSizesStatus
            @Composable get() = remember(this) { value.suggestedFontSizesStatus }
    }
}