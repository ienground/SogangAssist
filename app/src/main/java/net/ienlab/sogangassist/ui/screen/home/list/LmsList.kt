package net.ienlab.sogangassist.ui.screen.home.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.WorkOff
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gigamole.composefadingedges.FadingEdgesGravity
import com.gigamole.composefadingedges.content.FadingEdgesContentType
import com.gigamole.composefadingedges.content.scrollconfig.FadingEdgesScrollConfig
import com.gigamole.composefadingedges.verticalFadingEdges
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.launch
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.data.lms.Lms
import net.ienlab.sogangassist.ui.AppViewModelProvider
import net.ienlab.sogangassist.ui.navigation.NavigationDestination
import net.ienlab.sogangassist.ui.screen.edit.LmsDetails
import net.ienlab.sogangassist.ui.theme.AppTheme
import net.ienlab.sogangassist.ui.utils.DeleteAlertDialog
import net.ienlab.sogangassist.ui.utils.Utils.getTypeIcon
import net.ienlab.sogangassist.ui.utils.Utils.leftTimeToLabel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object LmsListDestination: NavigationDestination {
    override val route: String = "lms_list"
    const val timeArg: String = "time"
    val routeWithArgs = "${route}?${timeArg}={${timeArg}}"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LmsList(
    modifier: Modifier = Modifier,
    navigateToItemDetail: (Long) -> Unit,
    viewModel: LmsListViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val uiStateList by viewModel.uiStateList.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<LmsDetails?>(null) }

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = uiStateList.isInitialized && uiStateList.lmsList.isEmpty(),
            enter = fadeIn(tween(700)),
            exit = fadeOut(tween(700))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WorkOff, contentDescription = stringResource(id = R.string.no_todos),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(128.dp)
                            .alpha(0.4f)
                    )
                    Text(
                        text = stringResource(id = R.string.no_todos),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.alpha(0.4f)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !uiStateList.isInitialized,
            enter = fadeIn(tween(700)),
            exit = fadeOut(tween(700))
        ) {
            val scrollState = rememberScrollState()
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .verticalScroll(state = scrollState, enabled = false)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .fillMaxHeight(1f)
            ) {
                repeat(6) {
                    LmsEventShimmerRow()
                }
            }
        }

        AnimatedVisibility(
            visible = uiStateList.isInitialized && uiStateList.lmsList.isNotEmpty(),
            enter = fadeIn(tween(700)),
            exit = fadeOut(tween(700))
        ) {
            val lazyListState = rememberLazyListState()
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .verticalFadingEdges(
                        gravity = FadingEdgesGravity.Start,
                        length = 16.dp,
                        contentType = FadingEdgesContentType.Dynamic.Lazy.List(
                            scrollConfig = FadingEdgesScrollConfig.Full(),
                            state = lazyListState
                        )
                    )
                    .fillMaxHeight(1f)
            ) {
                items(items = uiStateList.lmsList, key = { it.id }) { lms ->
                    LmsEventRow(
                        item = lms,
                        onClick = { navigateToItemDetail(lms.id) },
                        onLongClick = {
                            coroutineScope.launch {
                                viewModel.saveItem(lms.copy(isFinished = !lms.isFinished))
                            }
                        },
                        onDelete = { showDeleteDialog = lms },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }

    if (showDeleteDialog != null) {
        DeleteAlertDialog(
            onCancel = { showDeleteDialog = null },
            onConfirm = {
                coroutineScope.launch {
                    showDeleteDialog?.let { lms -> viewModel.deleteItem(lms) }
                    showDeleteDialog = null
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LmsEventRow(
    modifier: Modifier = Modifier,
    item: LmsDetails,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    val apmFormat = DateTimeFormatter.ofPattern("a")
    val timeFormat = DateTimeFormatter.ofPattern("h:mm")

    val fontWeight = if (!item.isFinished) FontWeight.Bold else FontWeight.Normal
    val fontColor by animateColorAsState(targetValue = if (!item.isFinished) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.outline, label = "font_color")
    val textDecoration = TextDecoration.None

    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .combinedClickable(
                    onLongClick = onLongClick,
                    onClick = onClick
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getTypeIcon(item.type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        .size(36.dp)
                        .padding(6.dp)
                )
                Text(
                    text = when (item.type) {
                        Lms.Type.LESSON, Lms.Type.SUP_LESSON -> stringResource(id = R.string.week_lesson_format, item.week, item.lesson)
                        else -> item.homeworkName
                    },
                    fontSize = 20.sp,
                    style = TextStyle(textDecoration = textDecoration),
                    fontWeight = fontWeight,
                    color = fontColor,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f)
                        .height(46.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .basicMarquee(iterations = Int.MAX_VALUE)
                )
                FilledTonalIconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.Top)
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close, contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (Locale.getDefault() == Locale.KOREA) {
                    Text(
                        text = item.endTime.format(apmFormat),
                        fontSize = 18.sp,
                        fontWeight = fontWeight,
                        color = fontColor,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                Text(
                    text = item.endTime.format(timeFormat),
                    fontSize = 36.sp,
                    fontWeight = fontWeight,
                    color = fontColor,
                    modifier = Modifier.alignByBaseline()
                )
                if (Locale.getDefault() != Locale.KOREA) {
                    Text(
                        text = item.endTime.format(apmFormat),
                        fontSize = 18.sp,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = leftTimeToLabel(item.endTime),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.endTime.isBefore(LocalDateTime.now())) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .clip(CircleShape)
                        .background(if (item.endTime.isBefore(LocalDateTime.now())) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary)
                        .padding(horizontal = 8.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = item.className,
                    color = fontColor,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .basicMarquee()
                )
                AnimatedVisibility(
                    visible = item.isFinished
                ) {
                    Icon(imageVector = Icons.Rounded.DoneAll, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun LmsEventShimmerRow(
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.shimmer()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        .size(36.dp)
                        .padding(6.dp)
                )
                Text(
                    text = "  ",
                    fontSize = 20.sp,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .width(140.dp)
                        .height(46.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f))
                )
                Spacer(modifier = Modifier.weight(1f))
                FilledTonalIconButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .align(Alignment.Top)
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close, contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = " ",
                    fontSize = 36.sp,
                    modifier = Modifier
                        .width(100.dp)
                        .alignByBaseline()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f))
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .width(60.dp)
                        .align(Alignment.CenterVertically)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .padding(horizontal = 8.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "",
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f))
                )
            }
        }
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun LmsEventRowPreview() {
    AppTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LmsEventRow(
                item = LmsDetails(
                    type = Lms.Type.LESSON,
                    week = "3",
                    lesson = "2",
                    className = "Math",
                    isFinished = true,
                    endTime = LocalDateTime.now().plusMinutes(10).plusHours(1)
                ),
                onClick = {},
                onLongClick = {},
                onDelete = {}
            )
            LmsEventRow(
                item = LmsDetails(
                    type = Lms.Type.HOMEWORK,
                    homeworkName = "This is homework",
                    className = "Korean",
                    endTime = LocalDateTime.now().minusDays(1)
                ),
                onClick = {},
                onLongClick = {},
                onDelete = {}
            )
            LmsEventShimmerRow()
        }
    }
}