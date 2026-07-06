package com.mark.pockettv.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// =====================================================================
// Cinematic Ember design system — colors
// =====================================================================

val Charcoal = Color(0xFF131313)              // background / surface
val SurfaceLowest = Color(0xFF0E0E0E)         // surface-container-lowest
val SurfaceLow = Color(0xFF1C1B1B)            // surface-container-low
val SurfaceContainer = Color(0xFF201F1F)      // surface-container
val SurfaceHigh = Color(0xFF2A2A2A)           // surface-container-high
val SurfaceHighest = Color(0xFF353534)        // surface-container-highest
val EmberPrimary = Color(0xFFFFB68A)          // primary
val OnEmberPrimary = Color(0xFF522300)        // on-primary
val EmberContainer = Color(0xFFE68A4D)        // primary-container
val OnEmberContainer = Color(0xFF5D2900)      // on-primary-container
val EmberDeep = Color(0xFFB86129)             // gradient bottom stop
val Gold = Color(0xFFFFDF9E)                  // secondary
val GoldDim = Color(0xFFFABD00)               // secondary-container / glow
val OnGold = Color(0xFF3F2E00)                // on-secondary
val OnSurfaceEmber = Color(0xFFE5E2E1)        // on-surface
val OnSurfaceVariantEmber = Color(0xFFDAC2B5) // on-surface-variant
val OutlineEmber = Color(0xFFA28C81)          // outline
val OutlineVariantEmber = Color(0xFF54433A)   // outline-variant

// Legacy aliases so older code keeps compiling
val Night = Charcoal
val SurfaceNavy = SurfaceContainer
val SurfaceRaised = SurfaceHigh
val Accent = GoldDim
val TextPrimary = OnSurfaceEmber
val TextSecondary = OnSurfaceVariantEmber

val PocketColors = darkColorScheme(
    primary = EmberPrimary,
    onPrimary = OnEmberPrimary,
    primaryContainer = EmberContainer,
    onPrimaryContainer = OnEmberContainer,
    secondary = Gold,
    onSecondary = OnGold,
    secondaryContainer = GoldDim,
    onSecondaryContainer = Color(0xFF6A4E00),
    background = Charcoal,
    onBackground = OnSurfaceEmber,
    surface = Charcoal,
    onSurface = OnSurfaceEmber,
    surfaceVariant = SurfaceHighest,
    onSurfaceVariant = OnSurfaceVariantEmber,
    outline = OutlineEmber,
    outlineVariant = OutlineVariantEmber,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

// =====================================================================
// Typography — Lexend with tight tracking
// =====================================================================

/**
 * App font. MainActivity replaces this with the bundled Lexend at startup,
 * but ONLY after verifying the font file actually loads on this device.
 * If loading fails for any reason, the system font is used and the app
 * still opens instead of crashing on the first frame.
 */
var Lexend: FontFamily = FontFamily.Default

private val base = Typography()
fun pocketTypography(): Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = Lexend, letterSpacing = (-2.2).sp),
    displayMedium = base.displayMedium.copy(fontFamily = Lexend, letterSpacing = (-1.8).sp),
    displaySmall = base.displaySmall.copy(fontFamily = Lexend, letterSpacing = (-1.1).sp),
    headlineLarge = base.headlineLarge.copy(
        fontFamily = Lexend, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-1.3).sp
    ),
    headlineMedium = base.headlineMedium.copy(
        fontFamily = Lexend, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = (-0.7).sp
    ),
    headlineSmall = base.headlineSmall.copy(fontFamily = Lexend, letterSpacing = (-0.7).sp),
    titleLarge = base.titleLarge.copy(fontFamily = Lexend, letterSpacing = (-0.4).sp),
    titleMedium = base.titleMedium.copy(fontFamily = Lexend, letterSpacing = (-0.3).sp),
    titleSmall = base.titleSmall.copy(fontFamily = Lexend),
    bodyLarge = base.bodyLarge.copy(
        fontFamily = Lexend, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = (-0.3).sp
    ),
    bodyMedium = base.bodyMedium.copy(
        fontFamily = Lexend, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = (-0.15).sp
    ),
    bodySmall = base.bodySmall.copy(fontFamily = Lexend),
    labelLarge = base.labelLarge.copy(fontFamily = Lexend, fontWeight = FontWeight.Bold),
    labelMedium = base.labelMedium.copy(fontFamily = Lexend, fontWeight = FontWeight.Bold),
    labelSmall = base.labelSmall.copy(
        fontFamily = Lexend, fontWeight = FontWeight.Bold,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp
    )
)

// =====================================================================
// Components
// =====================================================================

/** Solid warm-orange primary action with top-to-bottom gradient ("squishy" feel). */
@Composable
fun GradientPlayButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(EmberContainer, EmberDeep)))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(26.dp))
            Text(
                text,
                color = Color.White,
                fontFamily = Lexend,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

/** Circular glass icon button used in the top bar. */
@Composable
fun CircleGlassButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(SurfaceHigh.copy(alpha = 0.6f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
            .clickable(onClick = onClick)
    ) { content() }
}

/** Hero card: 4/5 image, bottom charcoal gradient, tracked uppercase title, play button. */
@Composable
fun HeroCard(
    title: String,
    subtitle: String?,
    imageUrl: String?,
    isFavorite: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 5f)
            .clip(RoundedCornerShape(32.dp))
            .background(SurfaceContainer)
            .border(1.dp, EmberContainer.copy(alpha = 0.25f), RoundedCornerShape(32.dp))
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.55f to Charcoal.copy(alpha = 0.55f),
                        1f to Charcoal
                    )
                )
        )
        // Favorite badge
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceLowest.copy(alpha = 0.8f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                .clickable(onClick = onToggleFavorite),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = "Favorite",
                tint = if (isFavorite) GoldDim else OnSurfaceVariantEmber.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title.uppercase(),
                color = Color.White,
                fontFamily = Lexend,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                letterSpacing = 3.1.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle.uppercase(),
                    color = OnSurfaceVariantEmber.copy(alpha = 0.85f),
                    fontFamily = Lexend,
                    fontSize = 11.sp,
                    letterSpacing = 2.2.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            GradientPlayButton(
                text = "Play",
                modifier = Modifier.padding(top = 18.dp),
                onClick = onPlay
            )
        }
    }
}

/** Poster tile with 16dp radius, bottom text gradient, optional gold rating badge. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PosterCard(
    title: String,
    imageUrl: String?,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    rating: String? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = modifier
            .width(140.dp)
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceLow)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        if (imageUrl.isNullOrBlank()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.Movie, null,
                    tint = OnSurfaceVariantEmber.copy(alpha = 0.4f),
                    modifier = Modifier.size(36.dp)
                )
            }
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.9f)
                    )
                )
        )
        if (rating != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star, null,
                        tint = GoldDim, modifier = Modifier.size(11.dp)
                    )
                    Text(
                        rating,
                        color = Gold,
                        fontFamily = Lexend,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(start = 3.dp)
                    )
                }
            }
        }
        if (isFavorite) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = "Favorite",
                tint = GoldDim,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(16.dp)
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                title,
                color = Color.White,
                fontFamily = Lexend,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    color = OnSurfaceVariantEmber,
                    fontFamily = Lexend,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Warm glass list row for channels and episodes. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChannelRow(
    title: String,
    imageUrl: String?,
    subtitle: String?,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainer.copy(alpha = 0.75f))
            .border(1.dp, EmberPrimary.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = SurfaceHigh,
            modifier = Modifier.size(46.dp)
        ) {
            if (imageUrl.isNullOrBlank()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.LiveTv, null,
                        tint = OnSurfaceVariantEmber,
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(6.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                title,
                color = OnSurfaceEmber,
                fontFamily = Lexend,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    color = OnSurfaceVariantEmber.copy(alpha = 0.8f),
                    fontFamily = Lexend,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (isFavorite) {
            Icon(
                Icons.Filled.Favorite, "Favorite",
                tint = GoldDim, modifier = Modifier.size(16.dp)
            )
        }
    }
}

/** Pill category chips: gold glass when active. */
@Composable
fun CategoryChips(
    categories: List<Pair<String, String>>,
    selectedId: String?,
    onSelect: (String?) -> Unit
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = SurfaceHigh,
        labelColor = OnSurfaceVariantEmber,
        selectedContainerColor = EmberContainer.copy(alpha = 0.18f),
        selectedLabelColor = Gold
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        item {
            FilterChip(
                selected = selectedId == null,
                onClick = { onSelect(null) },
                shape = RoundedCornerShape(50),
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true, selected = selectedId == null,
                    borderColor = Color.Transparent,
                    selectedBorderColor = EmberContainer.copy(alpha = 0.5f),
                    selectedBorderWidth = 1.dp
                ),
                label = { Text("All", fontFamily = Lexend, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }
        items(categories) { (id, name) ->
            FilterChip(
                selected = selectedId == id,
                onClick = { onSelect(id) },
                shape = RoundedCornerShape(50),
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true, selected = selectedId == id,
                    borderColor = Color.Transparent,
                    selectedBorderColor = EmberContainer.copy(alpha = 0.5f),
                    selectedBorderWidth = 1.dp
                ),
                label = {
                    Text(
                        name, fontFamily = Lexend, fontWeight = FontWeight.Bold,
                        fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = OnSurfaceEmber,
        fontFamily = Lexend,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = (-0.6).sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

/** Floating pill bottom navigation with gold glowing active state. */
@Composable
fun FloatingBottomNav(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val icons = listOf(
        Icons.Filled.Home to "Home",
        Icons.Filled.LiveTv to "Live",
        Icons.Filled.Movie to "Movies",
        Icons.Filled.Tv to "Series"
    )
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp)
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(50))
            .background(SurfaceHighest.copy(alpha = 0.72f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
    ) {
        icons.forEachIndexed { index, (icon, label) ->
            val active = index == selected
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (active) GoldDim else OnSurfaceVariantEmber.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (active) GoldDim else Color.Transparent)
                )
            }
        }
    }
}

/** Category row for category-first browsing (name + item count + chevron). */
@Composable
fun CategoryRow(
    name: String,
    count: Int,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainer.copy(alpha = 0.75f))
            .border(1.dp, EmberPrimary.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                name,
                color = OnSurfaceEmber,
                fontFamily = Lexend,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "$count items",
                color = OnSurfaceVariantEmber.copy(alpha = 0.8f),
                fontFamily = Lexend,
                fontSize = 12.sp
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = GoldDim.copy(alpha = 0.8f),
            modifier = Modifier.size(22.dp)
        )
    }
}

/** Header shown inside a category: back chevron + category title. */
@Composable
fun CategoryHeader(
    name: String,
    onBack: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        CircleGlassButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Back",
                tint = Gold,
                modifier = Modifier
                    .padding(8.dp)
                    .graphicsLayer(scaleX = -1f)
            )
        }
        Text(
            name,
            color = Gold,
            fontFamily = Lexend,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
