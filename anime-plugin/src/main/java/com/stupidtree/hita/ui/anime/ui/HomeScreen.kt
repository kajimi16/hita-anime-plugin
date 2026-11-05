package com.stupidtree.hita.ui.anime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stupidtree.hita.api.TimetableService
import com.stupidtree.hita.ui.anime.data.BgmSource
import com.stupidtree.hita.ui.anime.data.CollectionEntry
import com.stupidtree.hita.ui.anime.data.Prefs
import com.stupidtree.hita.ui.anime.data.Schedule
import com.stupidtree.hita.ui.anime.data.ScheduleDay
import com.stupidtree.hita.ui.anime.data.SubjectSummary
import com.stupidtree.hita.ui.anime.data.buildAiringSchedule
import com.stupidtree.hita.ui.anime.data.todayIso
import com.stupidtree.hita.ui.anime.data.todayWeekday
import com.stupidtree.hita.ui.anime.data.weekDates
import com.stupidtree.hitax.data.model.timetable.EventItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

private data class HomeData(
    val today: List<SubjectSummary>,
    val schedule: Schedule,
    val doing: List<CollectionEntry>,
    val watched: List<CollectionEntry>,
    val todayEvents: List<EventItem>,
)

@Composable
fun HomeScreen(
    prefs: Prefs,
    bgm: BgmSource,
    timetable: TimetableService?,
    onOpenClass: (EventItem) -> Unit,
    onOpenSubject: (Int, String?) -> Unit,
    onOpenTab: (String) -> Unit,
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var data by remember { mutableStateOf<HomeData?>(null) }
    var refreshTick by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTick, prefs.bgmToken, prefs.bgmUsername) {
        loading = true
        error = null
        runCatching {
            withContext(Dispatchers.IO) {
                coroutineScope {
                    val calendarDeferred = async { bgm.calendar() }
                    val doingDeferred = async {
                        val username = prefs.bgmUsername
                        if (prefs.bgmToken.isBlank() || username.isBlank()) {
                            emptyList()
                        } else {
                            runCatching {
                                bgm.userCollections(username, type = 3, offset = 0, limit = 12).data
                            }.getOrDefault(emptyList())
                        }
                    }
                    val watchedDeferred = async {
                        val username = prefs.bgmUsername
                        if (prefs.bgmToken.isBlank() || username.isBlank()) {
                            emptyList()
                        } else {
                            runCatching {
                                bgm.userCollections(username, type = 2, offset = 0, limit = 12).data
                            }.getOrDefault(emptyList())
                        }
                    }
                    val eventsDeferred = async {
                        runCatching { timetable?.getTodayEventsSync().orEmpty() }
                            .getOrDefault(emptyList())
                    }
                    val calendarDays = calendarDeferred.await()
                    val weekday = todayWeekday()
                    HomeData(
                        today = calendarDays.firstOrNull { it.weekday == weekday }
                            ?.items.orEmpty()
                            .sortedByDescending { it.score ?: 0.0 }
                            .take(16),
                        schedule = buildAiringSchedule(calendarDays, 0, weekDates(0)),
                        doing = doingDeferred.await(),
                        watched = watchedDeferred.await(),
                        todayEvents = eventsDeferred.await(),
                    )
                }
            }
        }.onSuccess { data = it }
            .onFailure { error = it.message ?: "加载失败" }
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        HomeHeader(onRefresh = { refreshTick++ })
        when {
            loading && data == null -> LoadingState()
            error != null && data == null -> ErrorState(error!!) { refreshTick++ }
            data != null -> HomeContent(
                data = data!!,
                prefs = prefs,
                timetableAvailable = timetable != null,
                onOpenClass = onOpenClass,
                onOpenSubject = onOpenSubject,
                onOpenTab = onOpenTab,
            )
            else -> EmptyState("暂无数据")
        }
    }
}

@Composable
private fun HomeHeader(onRefresh: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colors.primary.copy(alpha = 0.14f),
                        MaterialTheme.colors.background,
                    ),
                ),
            )
            .padding(start = AnimeDimens.screenPadding, end = 8.dp, top = 10.dp, bottom = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "番剧",
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "课表 × Bangumi × 校内弹幕",
                    style = MaterialTheme.typography.caption,
                    color = mutedOnSurface(0.55f),
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "刷新", tint = MaterialTheme.colors.onBackground)
            }
        }
    }
}

@Composable
private fun HomeContent(
    data: HomeData,
    prefs: Prefs,
    timetableAvailable: Boolean,
    onOpenClass: (EventItem) -> Unit,
    onOpenSubject: (Int, String?) -> Unit,
    onOpenTab: (String) -> Unit,
) {
    var selectedDate by remember(data.schedule) {
        mutableStateOf(
            data.schedule.days.firstOrNull { it.date == todayIso() }?.date
                ?: data.schedule.days.firstOrNull()?.date,
        )
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (timetableAvailable) {
            item {
                TimetableModule(data.todayEvents, onOpenClass)
            }
        }

        item {
            SectionTitle(
                "继续观看",
                trailing = if (data.doing.isEmpty()) null else "全部",
                onTrailing = { onOpenTab("library") },
            )
        }
        item {
            if (data.doing.isEmpty()) {
                BindHint(prefs, onOpenTab)
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = AnimeDimens.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(data.doing, key = { it.collection.subjectId }) { entry ->
                        val subject = entry.subject
                        ContinueWatchingCard(
                            subject = subject,
                            title = subject?.displayName ?: "条目 ${entry.collection.subjectId}",
                            progressText = if (entry.collection.epStatus > 0) {
                                "看到第 ${entry.collection.epStatus} 集"
                            } else {
                                "尚未开始"
                            },
                            onClick = {
                                onOpenSubject(entry.collection.subjectId, subject?.displayName)
                            },
                        )
                    }
                }
            }
        }

        if (data.today.isNotEmpty()) {
            item { SectionTitle("今日更新") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = AnimeDimens.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(AnimeDimens.gridGap),
                ) {
                    items(data.today, key = { it.id }) { subject ->
                        PosterCard(
                            subject = subject,
                            modifier = Modifier.width(118.dp),
                            onClick = { onOpenSubject(subject.id, subject.displayName) },
                        )
                    }
                }
            }
        }

        item {
            SectionTitle(
                "放送表",
                trailing = "全部",
                onTrailing = { onOpenTab("schedule") },
            )
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = AnimeDimens.screenPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(data.schedule.days.size) { index ->
                    val day = data.schedule.days[index]
                    DaySegment(
                        date = day.date,
                        index = index,
                        count = day.items.size,
                        selected = day.date == selectedDate,
                        onClick = { selectedDate = day.date },
                    )
                }
            }
        }
        val selectedDay = data.schedule.days.firstOrNull { it.date == selectedDate }
        if (selectedDay == null || selectedDay.items.isEmpty()) {
            item { EmptyState("这一天没有在播番组") }
        } else {
            items(selectedDay.items.take(8), key = { it.id }) { subject ->
                SubjectRow(
                    subject,
                    subtitle = subject.tags.take(2).joinToString(" · ") { it.name }
                        .takeIf { it.isNotBlank() },
                ) {
                    onOpenSubject(subject.id, subject.displayName)
                }
            }
        }

        item {
            SectionTitle(
                "我的收藏",
                trailing = if (data.watched.isEmpty()) null else "全部",
                onTrailing = { onOpenTab("library") },
            )
        }
        item {
            if (data.watched.isEmpty()) {
                Text(
                    "还没有看过的收藏。绑定或标记后这里会出现海报墙。",
                    style = MaterialTheme.typography.caption,
                    color = mutedOnSurface(0.5f),
                    modifier = Modifier.padding(horizontal = AnimeDimens.screenPadding, vertical = 6.dp),
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = AnimeDimens.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(AnimeDimens.gridGap),
                ) {
                    items(data.watched, key = { "watched-${it.collection.subjectId}" }) { entry ->
                        val subject = entry.subject
                        if (subject != null) {
                            PosterCard(
                                subject = subject,
                                modifier = Modifier.width(118.dp),
                                onClick = { onOpenSubject(subject.id, subject.displayName) },
                                badge = entry.collection.rate.takeIf { it > 0 }?.let { "★ $it" },
                            )
                        } else {
                            PosterPlaceholder(entry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimetableModule(events: List<EventItem>, onOpenClass: (EventItem) -> Unit) {
    val now = System.currentTimeMillis()
    val sorted = events.sortedBy { it.from.time }
    val current = sorted.firstOrNull { now >= it.from.time && now < it.to.time }
    val next = sorted.firstOrNull { it.from.time > now } ?: sorted.firstOrNull()
    val anchor = current ?: next
    val detail = when {
        sorted.isEmpty() -> "今天没有课，安心补番"
        current != null -> "${timeRange(current)} · ${current.name} · 上课中"
        next != null -> "${timeRange(next)} · ${next.name}"
        else -> "今天没有课"
    }
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.padding(start = AnimeDimens.screenPadding, end = AnimeDimens.screenPadding, top = 4.dp),
    ) {
        TimetableCompactCard(
            title = "今日课表 · ${sorted.size} 节",
            detail = detail,
            inClass = current != null,
            onClick = { anchor?.let(onOpenClass) },
        )
    }
}

private val classTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

private fun timeRange(event: EventItem): String =
    "${classTimeFormat.format(event.from)}-${classTimeFormat.format(event.to)}"

@Composable
private fun BindHint(prefs: Prefs, onOpenTab: (String) -> Unit) {
    Surface(
        color = MaterialTheme.colors.primary.copy(alpha = 0.10f),
        shape = RoundedCornerShape(AnimeDimens.cardRadius),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AnimeDimens.screenPadding)
            .clickable { onOpenTab("profile") },
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                if (prefs.bgmToken.isBlank()) "绑定 Bangumi，开启追番同步" else "同步你的 Bangumi 收藏",
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (prefs.bgmToken.isBlank()) {
                    "填入个人访问令牌，即可读取收藏、回写进度与评分。"
                } else {
                    "前往设置校验令牌并查看账号状态。"
                },
                style = MaterialTheme.typography.caption,
                color = mutedOnSurface(),
            )
        }
    }
}

@Composable
private fun PosterPlaceholder(entry: CollectionEntry) {
    Column(Modifier.width(118.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(AnimeDimens.posterRadius))
                .background(MaterialTheme.colors.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Text("${entry.collection.subjectId}", fontSize = 11.sp, color = mutedOnSurface())
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "条目 ${entry.collection.subjectId}",
            style = MaterialTheme.typography.caption,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
