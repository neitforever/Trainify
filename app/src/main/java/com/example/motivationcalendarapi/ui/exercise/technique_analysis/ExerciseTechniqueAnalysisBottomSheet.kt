package com.example.motivationcalendarapi.ui.exercise.technique_analysis

import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.getIconForBodyPart
import com.example.motivationcalendarapi.model.technique_analysis.ExerciseTechniqueAnalysisResult
import com.example.motivationcalendarapi.model.technique_analysis.LocalizedText
import com.example.motivationcalendarapi.model.technique_analysis.TechniqueIssue
import com.example.motivationcalendarapi.model.technique_analysis.TechniqueIssueSeverity
import com.example.motivationcalendarapi.model.technique_analysis.TechniqueVerdict
import com.example.motivationcalendarapi.viewmodel.technique_analysis.ExerciseTechniqueAnalysisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseTechniqueAnalysisBottomSheet(
    currentExercise: Exercise,
    allExercises: List<Exercise>,
    lang: String,
    viewModel: ExerciseTechniqueAnalysisViewModel,
    onMatchedExerciseClick: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let(viewModel::selectVideo)
    }

    LaunchedEffect(currentExercise.id) {
        viewModel.openForExercise(currentExercise.id)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
                shape = RoundedCornerShape(100.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            ) { Spacer(modifier = Modifier.size(width = 44.dp, height = 4.dp)) }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TechniqueAnalysisHeader(
                title = techniqueText(lang, "AI Technique Check", "AI-проверка техники", "AI-праверка тэхнікі"),
                subtitle = currentExercise.getName(lang).replaceFirstChar { it.uppercase() }
            )

            VideoPickerCard(
                videoUri = state.selectedVideoUri,
                isPreparingVideo = state.isPreparingVideo,
                isAnalyzing = state.isAnalyzing,
                loadingDots = state.loadingDots,
                lang = lang,
                onPickClick = { videoPicker.launch("video/*") },
                onAnalyzeClick = { viewModel.analyze(currentExercise, allExercises, lang) }
            )

            state.errorMessage?.let { ErrorCard(it) }

            state.result?.let { result ->
                TechniqueResultCard(result = result, allExercises = allExercises, lang = lang)
            }

            state.matchedExercise?.let { exercise ->
                MatchedExerciseCard(
                    exercise = exercise,
                    lang = lang,
                    onClick = { onMatchedExerciseClick(exercise) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TechniqueAnalysisHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_camera),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(12.dp).size(30.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun VideoPickerCard(
    videoUri: Uri?,
    isPreparingVideo: Boolean,
    isAnalyzing: Boolean,
    loadingDots: String,
    lang: String,
    onPickClick: () -> Unit,
    onAnalyzeClick: () -> Unit
) {
    val hasVideo = videoUri != null
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            UploadIntro(lang = lang)

            if (videoUri != null) {
                SelectedVideoPreview(videoUri = videoUri)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onPickClick,
                    enabled = !isPreparingVideo && !isAnalyzing,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (hasVideo) {
                            techniqueText(lang, "Change", "Заменить", "Замяніць")
                        } else {
                            techniqueText(lang, "Select", "Выбрать", "Выбраць")
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Button(
                    onClick = onAnalyzeClick,
                    enabled = hasVideo && !isPreparingVideo && !isAnalyzing,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = techniqueText(lang, "Analyze", "Анализ", "Аналіз"),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            LoadingStageCard(
                isVisible = isPreparingVideo || isAnalyzing,
                text = if (isPreparingVideo) {
                    techniqueText(lang, "Preparing video", "Подготовка видео", "Падрыхтоўка відэа")
                } else {
                    techniqueText(lang, "Uploading and analyzing", "Отправка и анализ", "Адпраўка і аналіз")
                },
                dots = loadingDots
            )
        }
    }
}

@Composable
private fun UploadIntro(lang: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_play_circle),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
            Text(
                text = techniqueText(lang, "Add a short exercise video", "Добавьте видео упражнения", "Дадайце відэа практыкавання"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = techniqueText(
                    lang,
                    "Check the preview, then start the technique analysis.",
                    "Проверьте предпросмотр и запустите анализ техники.",
                    "Праверце папярэдні прагляд і запусціце аналіз тэхнікі."
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun SelectedVideoPreview(videoUri: Uri) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val player = remember(videoUri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = false
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
            AndroidView(
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = true
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { it.player = player }
            )
        }
    }
}

@Composable
private fun LoadingStageCard(isVisible: Boolean, text: String, dots: String) {
    if (!isVisible) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text + dots.padEnd(3, ' '),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TechniqueResultCard(result: ExerciseTechniqueAnalysisResult, allExercises: List<Exercise>, lang: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, verdictColor(result.overallVerdict).copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VerdictBadge(result.overallVerdict, lang)
                Spacer(modifier = Modifier.weight(1f))
                ConfidenceBadge(result.confidence, lang)
            }

            Text(
                text = result.summary.get(lang),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Justify,
                modifier = Modifier.fillMaxWidth()
            )

            val detectedName = result.localizedDetectedExerciseName(allExercises, lang)
            if (detectedName.isNotBlank()) {
                Text(
                    text = techniqueText(lang, "Detected: ", "Определено: ", "Вызначана: ") + detectedName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            TechniquePointSection(
                title = techniqueText(lang, "What is correct", "Что выполнено правильно", "Што выканана правільна"),
                items = result.correctPoints,
                lang = lang
            )

            if (result.mistakes.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                Text(
                    text = techniqueText(lang, "Mistakes", "Ошибки", "Памылкі"),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                result.mistakes.forEach { TechniqueIssueItem(issue = it, lang = lang) }
            }

            TechniquePointSection(
                title = techniqueText(lang, "Safety warnings", "Предупреждения по безопасности", "Папярэджанні па бяспецы"),
                items = result.safetyWarnings,
                lang = lang
            )
            TechniquePointSection(
                title = techniqueText(lang, "Recommendations", "Рекомендации", "Рэкамендацыі"),
                items = result.recommendations,
                lang = lang
            )
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: Float, lang: String) {
    val percent = (confidence.coerceIn(0f, 1f) * 100).toInt()
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Text(
            text = techniqueText(lang, "Confidence $percent%", "Уверенность $percent%", "Упэўненасць $percent%"),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TechniquePointSection(title: String, items: List<LocalizedText>, lang: String) {
    val visibleItems = items.map { it.get(lang) }.filter { it.isNotBlank() }
    if (visibleItems.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        visibleItems.forEach { item ->
            Text(
                text = "• $item",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Justify,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TechniqueIssueItem(issue: TechniqueIssue, lang: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
        border = BorderStroke(1.dp, severityColor(issue.severity).copy(alpha = 0.36f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = issue.title.get(lang),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                issue.timeHint?.let {
                    Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Text(
                text = issue.description.get(lang),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Justify,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun VerdictBadge(verdict: TechniqueVerdict, lang: String) {
    Surface(shape = RoundedCornerShape(100.dp), color = verdictColor(verdict).copy(alpha = 0.14f)) {
        Text(
            text = when (verdict) {
                TechniqueVerdict.GOOD -> techniqueText(lang, "Good", "Хорошо", "Добра")
                TechniqueVerdict.NEEDS_IMPROVEMENT -> techniqueText(lang, "Needs work", "Нужны правки", "Патрэбныя праўкі")
                TechniqueVerdict.UNSAFE -> techniqueText(lang, "Unsafe", "Небезопасно", "Небяспечна")
                TechniqueVerdict.WRONG_EXERCISE -> techniqueText(lang, "Wrong exercise", "Другое упражнение", "Іншае практыкаванне")
                TechniqueVerdict.UNCLEAR_VIDEO -> techniqueText(lang, "Unclear video", "Видео неясное", "Відэа няяснае")
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = verdictColor(verdict),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MatchedExerciseCard(exercise: Exercise, lang: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = techniqueText(lang, "The video seems to show another exercise", "На видео, похоже, другое упражнение", "На відэа, здаецца, іншае практыкаванне"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Surface(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)) {
                        Icon(
                            painter = painterResource(id = getIconForBodyPart(exercise.getBodyPart(lang))),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(8.dp).size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exercise.getName(lang).replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = exercise.getTarget(lang).ifBlank { exercise.getBodyPart(lang) },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun verdictColor(verdict: TechniqueVerdict) = when (verdict) {
    TechniqueVerdict.GOOD -> MaterialTheme.colorScheme.primary
    TechniqueVerdict.NEEDS_IMPROVEMENT -> MaterialTheme.colorScheme.tertiary
    TechniqueVerdict.UNSAFE -> MaterialTheme.colorScheme.error
    TechniqueVerdict.WRONG_EXERCISE -> MaterialTheme.colorScheme.error
    TechniqueVerdict.UNCLEAR_VIDEO -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun severityColor(severity: TechniqueIssueSeverity) = when (severity) {
    TechniqueIssueSeverity.LOW -> MaterialTheme.colorScheme.primary
    TechniqueIssueSeverity.MEDIUM -> MaterialTheme.colorScheme.tertiary
    TechniqueIssueSeverity.HIGH -> MaterialTheme.colorScheme.error
}

private fun ExerciseTechniqueAnalysisResult.localizedDetectedExerciseName(
    allExercises: List<Exercise>,
    lang: String
): String {
    matchedExerciseId
        ?.let { id -> allExercises.firstOrNull { it.id == id } }
        ?.let { return it.getName(lang).replaceFirstChar { char -> char.uppercase() } }

    val rawName = detectedExerciseName.trim()
    if (rawName.isBlank()) return ""

    val matchedByName = allExercises.firstOrNull { exercise ->
        exercise.nameLocalized.values.any { localizedName ->
            rawName.equals(localizedName, ignoreCase = true)
        }
    }

    return matchedByName?.getName(lang)?.replaceFirstChar { it.uppercase() } ?: rawName
}

private fun techniqueText(lang: String, en: String, ru: String, be: String): String = when (lang) {
    "ru" -> ru
    "be" -> be
    else -> en
}
