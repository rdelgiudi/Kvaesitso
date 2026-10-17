package de.mm20.launcher2.ui.launcher.widgets.clock.clocks

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import de.mm20.launcher2.ktx.isAtLeastApiLevel
import de.mm20.launcher2.ktx.tryStartActivity
import de.mm20.launcher2.ui.R
import de.mm20.launcher2.ui.launcher.widgets.clock.SmartspacerView
import de.mm20.launcher2.ui.locals.LocalDarkTheme

private const val SmartspacerPackageName = "com.kieronquinn.app.smartspacer"

@Composable
fun SmartspacerClock(
    compact: Boolean,
    useThemeColor: Boolean,
    darkColors: Boolean,
) {
    // The Smartspacer layout only exists in layout-v29; the style may still be restored from a backup on older devices
    if (!isAtLeastApiLevel(29)) return

    val context = LocalContext.current

    val isInstalled = remember {
        try {
            context.packageManager.getPackageInfo(SmartspacerPackageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    if (!isInstalled) {
        TextButton(
            modifier = Modifier.padding(16.dp),
            onClick = {
                context.tryStartActivity(
                    Intent(Intent.ACTION_VIEW).apply {
                        data = "https://github.com/KieronQuinn/Smartspacer".toUri()
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
            },
            contentPadding = ButtonDefaults.TextButtonWithIconContentPadding,
            colors = ButtonDefaults.textButtonColors(
                contentColor = LocalContentColor.current,
            ),
        ) {
            Icon(
                modifier = Modifier
                    .padding(end = ButtonDefaults.IconSpacing)
                    .size(ButtonDefaults.IconSize),
                painter = painterResource(R.drawable.smartspacer),
                contentDescription = null,
            )
            Text(stringResource(R.string.clock_style_smartspacer_get))
        }
        return
    }

    val color = if (useThemeColor) {
        if (!darkColors) {
            if (LocalDarkTheme.current) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.primaryContainer
        } else {
            if (LocalDarkTheme.current) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.primary
        }
    } else {
        LocalContentColor.current
    }

    CompositionLocalProvider(LocalContentColor provides color) {
        SmartspacerView(
            modifier = if (compact) {
                Modifier
                    .widthIn(max = 200.dp)
                    .height(64.dp)
            } else {
                Modifier
                    .fillMaxWidth()
                    .height(104.dp)
            }
        )
    }
}
