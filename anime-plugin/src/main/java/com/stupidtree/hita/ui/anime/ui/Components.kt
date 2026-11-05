package com.stupidtree.hita.ui.anime.ui

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stupidtree.hita.ui.anime.data.CoverLoader
import com.stupidtree.hita.ui.anime.data.ImageSet
import com.stupidtree.hita.ui.anime.data.SubjectSummary
import com.stupidtree.hita.ui.anime.data.formatShortDay
import com.stupidtree.hita.ui.anime.data.weekdayCn

@Composable
fun AsyncCover(
    url: String?,
    modifier: Modifier = Modifier,
    radius: Dp = AnimeDimens.posterRadius,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val placeholder = Brush.verticalGradient(
        listOf(
            AnimePalette.Seed.copy(alpha = 0.30f),
            AnimePalette.Seed.copy(alpha = 0.12f),
        ),
    )
    BoxWithConstraints(
        modifier
            .clip(RoundedCornerShape(radius))
            .background(placeholder),
    ) {
        val density = LocalDensity.current
        val targetPx = with(density) { maxWidth.toPx().toInt() }.coerceIn(160, 1200)
        var bitmap by remember(url, targetPx) { mutableStateOf<Bitmap?>(null) }
        LaunchedEffect(url, targetPx) {
            bitmap = CoverLoader.load(url, maxWidth = targetPx)
        }
        val loaded = bitmap
        if (loaded != null) {
            Image(
                bitmap = remember(loaded) { loaded.asImageBitmap() },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
            )
        }
    }
}

@Composable
fun PosterBadge(
    text: String,
    modifier: Modifier = Modifier,
    container: Color = Color.Black.copy(alpha = 0.66f),
    content: Color = Color.White,
) {
    Surface(
        color = container,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier,
    ) {
        Text(
            text,
            color = content,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            maxLines = 1,
        )
    }
}

/**
 * Animeko 的封面卡：9:16 海报、大圆角、顶部评分角标、底部渐变。
 */
@Composable
fun PosterCard(
    subject: SubjectSummary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    footer: String? = null,
    badge: String? = null,
    showTitle: Boolean = true,
) {
    Column(modifier.clickable(onClick = onClick)) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(AnimeDimens.PosterAspect)
                .clip(RoundedCornerShape(AnimeDimens.posterRadius)),
        ) {
            AsyncCover(
                subject.images.hero ?: subject.images.cover,
                Modifier.fillMaxSize(),
                radius = 0.dp,
            )
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0.5f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.70f),
                        ),
                    ),
            )
            val score = subject.score
            if (score != null && score > 0) {
                PosterBadge("★ %.1f".format(score), Modifier.align(Alignment.TopEnd).padding(8.dp))
            }
            if (!badge.isNullOrBlank()) {
                PosterBadge(
                    badge,
                    Modifier.align(Alignment.BottomStart).padding(8.dp),
                    container = MaterialTheme.colors.primary,
                    content = MaterialTheme.colors.onPrimary,
                )
            }
        }
        if (showTitle) {
            Spacer(Modifier.height(7.dp))
            Text(
                subject.displayName,
                style = MaterialTheme.typography.body2,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val secondary = footer ?: subject.airDate
            if (!secondary.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    secondary,
                    style = MaterialTheme.typography.caption,
                    color = mutedOnSurface(0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun PosterGridCard(
    subject: SubjectSummary,
    onClick: () -> Unit,
    footer: String? = null,
    badge: String? = null,
) {
    PosterCard(subject, Modifier.fillMaxWidth(), onClick, footer = footer, badge = badge)
}

@Composable
fun ContinueWatchingCard(
    subject: SubjectSummary?,
    title: String,
    progressText: String,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(AnimeDimens.cardRadius),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 0.dp,
        modifier = Modifier.width(300.dp).clickable(onClick = onClick),
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(82.dp).height(112.dp)) {
                AsyncCover(
                    subject?.images?.hero ?: subject?.images?.cover,
                    Modifier.fillMaxSize(),
                    radius = 14.dp,
                )
                if (subject?.score != null) {
                    PosterBadge(
                        "★ %.1f".format(subject.score),
                        Modifier.align(Alignment.TopEnd).padding(4.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (progressText.isBlank()) "尚未开始" else progressText,
                    style = MaterialTheme.typography.caption,
                    color = mutedOnSurface(),
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colors.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(30.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = "继续观看",
                                tint = MaterialTheme.colors.onPrimary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Spacer(Modifier.width(7.dp))
                    Text(
                        "继续观看",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
fun TimetableCompactCard(
    title: String,
    detail: String,
    inClass: Boolean,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(AnimeDimens.compactRadius),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 0.dp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = MaterialTheme.colors.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.subtitle2, fontWeight = FontWeight.SemiBold)
                Text(
                    detail,
                    style = MaterialTheme.typography.caption,
                    color = mutedOnSurface(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (inClass) {
                TagPill("上课中", selected = true)
            } else {
                Icon(
                    Icons.Filled.KeyboardArrowRight,
                    contentDescription = "查看课表",
                    tint = mutedOnSurface(0.55f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
fun DaySegment(
    date: String,
    index: Int,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.surface
    val foreground = if (selected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
    Column(
        Modifier
            .width(66.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(weekdayCn(index + 1), fontSize = 10.sp, color = foreground.copy(alpha = 0.72f))
        Text(
            formatShortDay(date),
            style = MaterialTheme.typography.subtitle2,
            fontWeight = FontWeight.SemiBold,
            color = foreground,
        )
        Text("$count 部", fontSize = 9.sp, color = foreground.copy(alpha = 0.6f))
    }
}

@Composable
fun AnimeTabRow(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    ScrollableTabRow(
        selectedTabIndex = selected,
        edgePadding = 12.dp,
        backgroundColor = Color.Transparent,
        contentColor = MaterialTheme.colors.primary,
        divider = {},
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selected == index,
                onClick = { onSelect(index) },
                text = {
                    Text(
                        title,
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = if (selected == index) FontWeight.Bold else FontWeight.Normal,
                    )
                },
            )
        }
    }
}

@Composable
fun ScorePill(score: Double?, modifier: Modifier = Modifier) {
    if (score == null || score <= 0) return
    Surface(
        color = Color.Black.copy(alpha = 0.62f),
        shape = RoundedCornerShape(7.dp),
        modifier = modifier,
    ) {
        Text(
            text = "★ %.1f".format(score),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun TagPill(text: String, selected: Boolean = false, onClick: (() -> Unit)? = null) {
    Surface(
        color = if (selected) MaterialTheme.colors.primary else Color.Transparent,
        shape = RoundedCornerShape(AnimeDimens.chipRadius),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colors.primary else mutedOnSurface(0.22f),
        ),
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            maxLines = 1,
        )
    }
}

@Composable
fun HorizontalChips(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Composable
fun TopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Surface(color = MaterialTheme.colors.background, elevation = 0.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = if (onBack == null) 18.dp else 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "返回",
                        tint = MaterialTheme.colors.onBackground,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            actions()
        }
    }
}

@Composable
fun SectionTitle(
    text: String,
    trailing: String? = null,
    onTrailing: (() -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = AnimeDimens.screenPadding, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.primary,
                modifier = if (onTrailing != null) Modifier.clickable(onClick = onTrailing) else Modifier,
            )
        }
    }
}

@Composable
fun LoadingState(message: String = "加载中…") {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colors.primary)
            Spacer(Modifier.height(12.dp))
            Text(message, color = mutedOnSurface())
        }
    }
}

@Composable
fun EmptyState(message: String, detail: String? = null) {
    Box(Modifier.fillMaxWidth().padding(vertical = 44.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("(´･_･`)", fontSize = 26.sp, color = mutedOnSurface(0.35f))
            Spacer(Modifier.height(10.dp))
            Text(message, style = MaterialTheme.typography.body1)
            if (detail != null) {
                Spacer(Modifier.height(4.dp))
                Text(detail, style = MaterialTheme.typography.caption, color = mutedOnSurface(0.55f))
            }
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: (() -> Unit)? = null) {
    Box(Modifier.fillMaxWidth().padding(vertical = 44.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, style = MaterialTheme.typography.body1, color = MaterialTheme.colors.error)
            if (onRetry != null) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colors.primary,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.clickable(onClick = onRetry),
                ) {
                    Text(
                        "重试",
                        color = MaterialTheme.colors.onPrimary,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectRow(
    subject: SubjectSummary,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AnimeDimens.screenPadding, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(52.dp).height(72.dp)) {
            AsyncCover(
                subject.images.common ?: subject.images.cover,
                Modifier.fillMaxSize(),
                radius = 10.dp,
            )
            if (subject.score != null) {
                PosterBadge(
                    "★ %.1f".format(subject.score),
                    Modifier.align(Alignment.TopEnd).padding(3.dp),
                )
            }
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(
                subject.displayName,
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val extra = subtitle ?: subject.airDate
            if (!extra.isNullOrBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    extra,
                    style = MaterialTheme.typography.caption,
                    color = mutedOnSurface(0.55f),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun SubjectGridCard(subject: SubjectSummary, onClick: () -> Unit) {
    PosterGridCard(subject, onClick)
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Surface(
        color = if (enabled) MaterialTheme.colors.primary else mutedOnSurface(0.08f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text,
                style = MaterialTheme.typography.caption,
                color = if (enabled) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface,
            )
        }
    }
}

@Composable
fun KeyValueRow(key: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = AnimeDimens.screenPadding, vertical = 4.dp)) {
        Text(
            key,
            style = MaterialTheme.typography.caption,
            color = mutedOnSurface(0.55f),
            modifier = Modifier.width(84.dp),
        )
        Text(value, style = MaterialTheme.typography.caption, modifier = Modifier.weight(1f))
    }
}

@Composable
fun EpisodeProgressBar(progress: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = progress.coerceIn(0f, 1f),
        modifier = modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
        color = MaterialTheme.colors.primary,
        backgroundColor = mutedOnSurface(0.10f),
    )
}

@Composable
fun DividerSoft() {
    Divider(color = mutedOnSurface(0.08f), thickness = 0.5.dp)
}

fun coverOf(images: ImageSet): String? = images.cover
