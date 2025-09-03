package com.jmbar.mixandmunch.presentation.ui.accessibility

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp

/**
 * Accessibility utilities following WCAG guidelines
 */
object AccessibilityUtils {
    
    /**
     * Minimum touch target size as per Material Design guidelines
     */
    val MinTouchTargetSize = 48.dp
    
    /**
     * Ensures minimum touch target size for better accessibility
     */
    @Composable
    fun AccessibleClickable(
        onClick: () -> Unit,
        onClickLabel: String? = null,
        role: Role = Role.Button,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        content: @Composable () -> Unit
    ) {
        Box(
            modifier = modifier
                .size(minWidth = MinTouchTargetSize, minHeight = MinTouchTargetSize)
                .clip(CircleShape)
                .clickable(
                    enabled = enabled,
                    onClick = onClick,
                    role = role,
                    onClickLabel = onClickLabel,
                    indication = rememberRipple(
                        bounded = false,
                        radius = MinTouchTargetSize / 2
                    ),
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/**
 * Adds comprehensive accessibility semantics for screen readers
 */
fun Modifier.accessibilitySemantics(
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
    role: Role? = null,
    stateDescription: String? = null,
    isTraversalGroup: Boolean = false,
    heading: Boolean = false
): Modifier = this.semantics {
    contentDescription?.let {
        this.contentDescription = it
    }
    
    onClick?.let { clickAction ->
        this.onClick {
            clickAction()
            true
        }
    }
    
    role?.let {
        this.role = it
    }
    
    stateDescription?.let {
        this.stateDescription = it
    }
    
    if (isTraversalGroup) {
        this.isTraversalGroup = true
    }
    
    if (heading) {
        this.heading()
    }
}

/**
 * Adds live region semantics for dynamic content
 */
fun Modifier.liveRegion(
    politeness: LiveRegionMode = LiveRegionMode.Polite
): Modifier = this.semantics {
    this.liveRegion = politeness
}

/**
 * Adds collection semantics for lists and grids
 */
fun Modifier.collectionInfo(
    rowCount: Int = -1,
    columnCount: Int = -1
): Modifier = this.semantics {
    this.collectionInfo = CollectionInfo(
        rowCount = rowCount,
        columnCount = columnCount
    )
}

/**
 * Adds collection item semantics
 */
fun Modifier.collectionItemInfo(
    rowIndex: Int,
    rowSpan: Int = 1,
    columnIndex: Int = 0,
    columnSpan: Int = 1
): Modifier = this.semantics {
    this.collectionItemInfo = CollectionItemInfo(
        rowIndex = rowIndex,
        rowSpan = rowSpan,
        columnIndex = columnIndex,
        columnSpan = columnSpan
    )
}

/**
 * Adds progress semantics for loading states
 */
fun Modifier.progressSemantics(
    value: Float? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0
): Modifier = this.semantics {
    if (value != null) {
        this.progressBarRangeInfo = ProgressBarRangeInfo(
            current = value,
            range = valueRange,
            steps = steps
        )
    } else {
        // Indeterminate progress
        this.progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
    }
}

/**
 * Adds text field semantics
 */
fun Modifier.textFieldSemantics(
    value: String,
    label: String? = null,
    placeholder: String? = null,
    error: String? = null,
    isPassword: Boolean = false
): Modifier = this.semantics {
    this.editableText = AnnotatedString(value)
    
    label?.let {
        this.contentDescription = it
    }
    
    placeholder?.let {
        if (value.isEmpty()) {
            this.placeholder = it
        }
    }
    
    error?.let {
        this.error = it
        this.invalid = true
    }
    
    if (isPassword) {
        this.password = true
    }
}