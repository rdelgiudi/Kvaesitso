package de.mm20.launcher2.ui.launcher.widgets.clock.parts

import android.content.Context
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.ui.launcher.widgets.clock.SmartspacerView
import kotlinx.coroutines.flow.flow

@RequiresApi(29)
class SmartspacerPartProvider : PartProvider {
    override fun getRanking(context: Context) = flow {
        emit(Int.MAX_VALUE)
    }

    @Composable
    override fun Component(compactLayout: Boolean) {
        SmartspacerView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = if (compactLayout) 0.dp else 12.dp)
                .height(104.dp),
        )
    }
}
