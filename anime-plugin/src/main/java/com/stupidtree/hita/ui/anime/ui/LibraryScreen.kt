package com.stupidtree.hita.ui.anime.ui

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stupidtree.hita.ui.anime.data.BgmSource
import com.stupidtree.hita.ui.anime.data.CollectionEntry
import com.stupidtree.hita.ui.anime.data.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val statusOptions = listOf(
    3 to "在看",
    1 to "想看",
    2 to "看过",
    4 to "搁置",
    5 to "抛弃",
)

@Composable
fun LibraryScreen(
    prefs: Prefs,
    bgm: BgmSource,
    onOpenSubject: (Int, String?) -> Unit,
    onOpenProfile: () -> Unit,
) {
    var status by remember { mutableStateOf(3) }
    var entries by remember { mutableStateOf<List<CollectionEntry>>(emptyList()) }
    var total by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf(prefs.bgmUsername) }
    var tick by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(status, tick, prefs.bgmToken) {
        if (prefs.bgmToken.isBlank()) {
            entries = emptyList()
            loading = false
            return@LaunchedEffect
        }
        loading = true
        error = null
        runCatching {
            withContext(Dispatchers.IO) {
                val name = if (username.isNotBlank()) {
                    username
                } else {
                    bgm.me().also {
                        username = it.username
                        prefs.bgmUsername = it.username
                    }.username
                }
                bgm.userCollections(name, status, offset = 0, limit = 30)
            }
        }.onSuccess {
            entries = it.data
            total = it.total
        }.onFailure {
            entries = emptyList()
            error = it.message ?: "加载失败"
        }
        loading = false
    }

    fun loadMore() {
        if (loading || entries.size >= total) return
        loading = true
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    bgm.userCollections(username, status, offset = entries.size, limit = 30)
                }
            }.onSuccess {
                entries = (entries + it.data).distinctBy { entry -> entry.collection.subjectId }
                total = it.total
            }.onFailure { error = it.message ?: "加载失败" }
            loading = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.padding(start = AnimeDimens.screenPadding, end = AnimeDimens.screenPadding, top = 10.dp),
        ) {
            Text("我的追番", style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)
            Text(
                if (prefs.bgmToken.isBlank()) "未绑定 Bangumi" else "Bangumi 收藏 · $username",
                style = MaterialTheme.typography.caption,
                color = mutedOnSurface(0.55f),
            )
        }

        if (prefs.bgmToken.isBlank()) {
            Surface(
                color = MaterialTheme.colors.primary.copy(alpha = 0.10f),
                shape = RoundedCornerShape(AnimeDimens.cardRadius),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AnimeDimens.screenPadding)
                    .clickable(onClick = onOpenProfile),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "绑定 Bangumi 后这里会显示你的收藏",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Bangumi → 设置 → 开放接口 → 创建个人访问令牌。",
                        style = MaterialTheme.typography.caption,
                        color = mutedOnSurface(),
                    )
                }
            }
        } else {

        AnimeTabRow(
            tabs = statusOptions.map { it.second },
            selected = statusOptions.indexOfFirst { it.first == status },
            onSelect = { index ->
                val value = statusOptions[index].first
                if (status != value) {
                    status = value
                    entries = emptyList()
                    total = 0
                }
            },
        )

        Row(
            Modifier.fillMaxWidth().padding(horizontal = AnimeDimens.screenPadding, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (total > 0) "共 $total 部" else "",
                style = MaterialTheme.typography.caption,
                color = mutedOnSurface(0.5f),
                modifier = Modifier.weight(1f),
            )
            Text(
                "刷新",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.clickable { tick++ },
            )
        }

        when {
            loading && entries.isEmpty() -> LoadingState()
            error != null && entries.isEmpty() -> ErrorState(error!!) { tick++ }
            entries.isEmpty() -> EmptyState("这一分组还没有收藏")
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = AnimeDimens.screenPadding,
                    end = AnimeDimens.screenPadding,
                    bottom = 96.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(AnimeDimens.gridGap),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(entries, key = { it.collection.subjectId }) { entry ->
                    val subject = entry.subject
                    val progress = entry.collection.epStatus
                    if (subject != null) {
                        PosterGridCard(
                            subject = subject,
                            onClick = {
                                onOpenSubject(entry.collection.subjectId, subject.displayName)
                            },
                            badge = if (progress > 0) "第 $progress 集" else null,
                            footer = buildString {
                                if (entry.collection.rate > 0) append("★ ${entry.collection.rate}")
                                entry.collection.comment?.takeIf { it.isNotBlank() }?.let {
                                    if (isNotEmpty()) append(" · ")
                                    append(it)
                                }
                            }.takeIf { it.isNotBlank() } ?: subject.airDate,
                        )
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        if (entries.size < total) {
                            TagPill("加载更多", onClick = { loadMore() })
                        } else {
                            Text(
                                "已经到底了",
                                style = MaterialTheme.typography.caption,
                                color = mutedOnSurface(0.4f),
                            )
                        }
                    }
                }
            }
        }
        }
    }
}
