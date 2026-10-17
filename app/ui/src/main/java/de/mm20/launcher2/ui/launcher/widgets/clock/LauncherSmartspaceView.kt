package de.mm20.launcher2.ui.launcher.widgets.clock

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.annotation.RequiresApi
import androidx.viewpager.widget.ViewPager
import com.kieronquinn.app.smartspacer.sdk.client.views.BcSmartspaceView
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import de.mm20.launcher2.ui.R
import kotlin.math.absoluteValue

/**
 * [BcSmartspaceView] adjusted for the launcher's home screen:
 * - Claims horizontal swipes before the launcher's own swipe gestures do, as long as there is
 *   another card in that direction. Otherwise the swipe is left to the launcher.
 * - Keeps the currently visible card selected when targets are updated, unless a different card
 *   becomes the highest ranked one, in which case that card is shown. The base implementation
 *   only keeps the page index, so the visible card changes whenever Smartspacer re-ranks targets.
 */
@RequiresApi(29)
class LauncherSmartspaceView(context: Context, attrs: AttributeSet?) : BcSmartspaceView(context, attrs) {

    private val pager: ViewPager? by lazy { findViewById(R.id.smartspace_card_pager) }

    // Claim the gesture well before the launcher's touch slop is reached
    private val claimSlop = ViewConfiguration.get(context).scaledTouchSlop / 2f

    private var downX = 0f
    private var downY = 0f
    private var gestureDecided = false

    /**
     * Targets in the order they are currently displayed in, mirroring the sorting in [BcSmartspaceView]
     */
    private var displayedTargets: List<SmartspaceTarget> = emptyList()
    private var pagerScrollState = ViewPager.SCROLL_STATE_IDLE

    override fun onFinishInflate() {
        super.onFinishInflate()
        pager?.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrollStateChanged(state: Int) {
                pagerScrollState = state
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                gestureDecided = false
            }

            MotionEvent.ACTION_MOVE -> if (!gestureDecided) {
                val dx = ev.x - downX
                val dy = ev.y - downY
                if (dx.absoluteValue > claimSlop || dy.absoluteValue > claimSlop) {
                    gestureDecided = true
                    // Swiping left moves to the next card, swiping right to the previous one
                    val direction = if (dx < 0) 1 else -1
                    if (dx.absoluteValue > dy.absoluteValue && pager?.canScrollHorizontally(direction) == true) {
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onSmartspaceTargetsUpdate(targets: List<SmartspaceTarget>) {
        val pager = pager
        val previousCount = pager?.adapter?.count ?: 0
        // The base implementation defers updates while the pager is scrolling and re-applies them through this method later
        if (pager == null || previousCount > 1 && pagerScrollState != ViewPager.SCROLL_STATE_IDLE) {
            super.onSmartspaceTargetsUpdate(targets)
            return
        }

        val currentTargetId = displayedTargets.getOrNull(pager.currentItem)?.smartspaceTargetId
        val previousTopTargetId = displayedTargets.getOrNull(topIndex(displayedTargets))?.smartspaceTargetId

        super.onSmartspaceTargetsUpdate(targets)

        displayedTargets = targets.sortedByDescending { it.score }.let {
            if (layoutDirection == View.LAYOUT_DIRECTION_RTL) it.reversed() else it
        }
        if (displayedTargets.isEmpty()) return

        val topIndex = topIndex(displayedTargets)
        val currentIndex = displayedTargets.indexOfFirst { it.smartspaceTargetId == currentTargetId }

        val newIndex = when {
            // A different card became the most important one: show it
            displayedTargets[topIndex].smartspaceTargetId != previousTopTargetId -> topIndex
            // Stay on the card that was visible, even if its position changed
            currentIndex >= 0 -> currentIndex
            // The visible card is gone: go back to the most important card instead of whichever card took its place
            else -> topIndex
        }
        if (newIndex != pager.currentItem) {
            pager.setCurrentItem(newIndex, true)
        }
    }

    /**
     * Index of the highest ranked target; the base implementation reverses the order for RTL layouts
     */
    private fun topIndex(targets: List<SmartspaceTarget>): Int {
        return if (layoutDirection == View.LAYOUT_DIRECTION_RTL) targets.lastIndex else 0
    }
}
