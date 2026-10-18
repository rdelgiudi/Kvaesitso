package de.mm20.launcher2.ui.launcher.widgets.clock.clocks

import android.content.Intent
import android.os.Process
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import de.mm20.launcher2.applications.AppRepository
import de.mm20.launcher2.ktx.isAtLeastApiLevel
import de.mm20.launcher2.ktx.tryStartActivity
import de.mm20.launcher2.ui.R
import de.mm20.launcher2.ui.launcher.widgets.clock.SmartspacerView
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject

private const val SmartspacerPackageName = "com.kieronquinn.app.smartspacer"

@Composable
fun SmartspacerClock() {
    // The Smartspacer layout only exists in layout-v29
    if (!isAtLeastApiLevel(29)) return

    val context = LocalContext.current

    val appRepository: AppRepository = koinInject()
    val isInstalled by remember {
        appRepository.findOne(SmartspacerPackageName, Process.myUserHandle()).map { it != null }
    }.collectAsState(null)

    // Unknown yet, avoid flashing the install button
    if (isInstalled == null) return

    if (isInstalled == false) {
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

    // Clock appearance settings (layout, colors) don't apply, Smartspacer is styled on its own
    SmartspacerView(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
    )
}
