package net.ienlab.sogangassist.ui.utils

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import kotlinx.coroutines.launch
import net.ienlab.sogangassist.R

sealed interface ActionMenuItem {
    val title: String
    val onClick: () -> Unit
    val isVisible: Boolean

    sealed interface IconMenuItem : ActionMenuItem {
        val icon: ImageVector

        data class AlwaysShown(
            override val title: String,
            override val onClick: () -> Unit,
            override val icon: ImageVector,
            override val isVisible: Boolean = true
        ) : IconMenuItem

        data class ShownIfRoom(
            override val title: String,
            override val onClick: () -> Unit,
            override val icon: ImageVector,
            override val isVisible: Boolean = true
        ) : IconMenuItem
    }

    data class NeverShown(
        override val title: String,
        override val onClick: () -> Unit,
        override val isVisible: Boolean = true
    ): ActionMenuItem

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ActionsMenu(
    items: List<ActionMenuItem>,
    isOpen: Boolean,
    closeDropdown: () -> Unit,
    onToggleOverflow: () -> Unit,
    maxVisibleItems: Int,
) {
    val menuItems = remember(items, maxVisibleItems) { splitMenuItems(items, maxVisibleItems) }
    val coroutineScope = rememberCoroutineScope()
    menuItems.alwaysShownItems.forEach { item ->
        val tooltipState = rememberMyBasicTooltipState(isPersistent = false)
        var width = 0
        val positionProvider = object: PopupPositionProvider {
            override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
                if (popupContentSize.width != 0) width = popupContentSize.width
                val x = anchorBounds.left + (anchorBounds.width - width) / 2
                val y = anchorBounds.bottom
                return IntOffset(x, y)
            }
        }

        if (item.isVisible) {
            MyBasicTooltipBox(
                positionProvider = positionProvider,
                state = tooltipState,
                focusable = false,
                enableUserInput = false,
                tooltip = {
                    Text(
                        text = item.title,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp)
                    )
                },
            ) {
                IconButton(
                    onClick = item.onClick,
                    onLongClick = {
                        coroutineScope.launch {
                            tooltipState.show()
                        }
                    },
                ) {
                    AnimatedContent(
                        targetState = item.icon,
                        label = "menu_icon"
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                        )
                    }
                }
            }
        }
    }

    if (menuItems.overflowItems.isNotEmpty()) {
        val tooltipState = rememberMyBasicTooltipState(isPersistent = false)
        var width = 0
        val positionProvider = object: PopupPositionProvider {
            override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
                if (popupContentSize.width != 0) width = popupContentSize.width
                val x = anchorBounds.left + (anchorBounds.width - width) / 2
                val y = anchorBounds.bottom
                return IntOffset(x, y)
            }
        }
        MyBasicTooltipBox(
            positionProvider = positionProvider,
            state = tooltipState,
            focusable = false,
            enableUserInput = false,
            tooltip = {
                Text(
                    text = stringResource(R.string.more_options),
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp)
                )
            },
        ) {
            IconButton(
                onClick = onToggleOverflow,
                onLongClick = {
                    coroutineScope.launch {
                        tooltipState.show()
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = stringResource(R.string.more_options),
                )
            }
        }
        DropdownMenu(
            expanded = isOpen,
            onDismissRequest = onToggleOverflow,
        ) {
            menuItems.overflowItems.forEach { item ->
                if (item.isVisible) {
                    DropdownMenuItem(
                        text = { Text(text = item.title,) },
                        onClick = {
                            closeDropdown()
                            item.onClick()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    @Suppress("DEPRECATION_ERROR")
    val containerColor = if (enabled) colors.containerColor else colors.disabledContainerColor
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(40.dp)
            .clip(CircleShape)
            .background(color = containerColor)
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick,
                role = Role.Button,
            ),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}

private data class MenuItems(
    val alwaysShownItems: List<ActionMenuItem.IconMenuItem>,
    val overflowItems: List<ActionMenuItem>,
)

private fun splitMenuItems(
    items: List<ActionMenuItem>,
    maxVisibleItems: Int,
): MenuItems {
    val alwaysShownItems: MutableList<ActionMenuItem.IconMenuItem> = items.filterIsInstance<ActionMenuItem.IconMenuItem.AlwaysShown>().toMutableList()
    val ifRoomItems: MutableList<ActionMenuItem.IconMenuItem> = items.filterIsInstance<ActionMenuItem.IconMenuItem.ShownIfRoom>().toMutableList()
    val overflowItems = items.filterIsInstance<ActionMenuItem.NeverShown>()

    val hasOverflow = overflowItems.isNotEmpty() || (alwaysShownItems.size + ifRoomItems.size - 1) > maxVisibleItems
    val usedSlots = alwaysShownItems.size + (if (hasOverflow) 1 else 0)
    val availableSlots = maxVisibleItems - usedSlots
    if (availableSlots > 0 && ifRoomItems.isNotEmpty()) {
        val visible = ifRoomItems.subList(0, availableSlots.coerceAtMost(ifRoomItems.size))
        alwaysShownItems.addAll(visible)
        ifRoomItems.removeAll(visible)
    }

    return MenuItems(
        alwaysShownItems = alwaysShownItems,
        overflowItems = ifRoomItems + overflowItems,
    )
}