package info.cemu.cemu.common.android.display

import android.app.Activity
import android.content.Context
import android.hardware.display.DisplayManager
import android.util.Log
import android.view.Display

object DisplayUtils {
    private const val TAG = "CemuDisplay"
    private var launchDisplayId: Int? = null

    fun init(activity: Activity) {
        if (launchDisplayId != null) {
            return
        }
        val displayId = activity.display?.displayId ?: Display.DEFAULT_DISPLAY
        launchDisplayId = displayId
    }

    fun getInternalDisplay(context: Context): Display? {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        return displayManager.getDisplay(Display.DEFAULT_DISPLAY)
    }

    fun getExternalDisplay(context: Context): Display? {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val internalDisplay = getInternalDisplay(context)
        val internalId = internalDisplay?.displayId ?: launchDisplayId

        // Handhelds with two built-in panels (e.g. AYANEO Pocket DS) keep both displays in
        // the same display group, so the second panel is never reported under
        // DISPLAY_CATEGORY_PRESENTATION. Consider every display and let the capability
        // checks below decide whether it can host the GamePad view.
        val candidates = LinkedHashMap<Int, Display>()
        displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
            .forEach { candidates[it.displayId] = it }
        displayManager.displays.forEach { candidates.putIfAbsent(it.displayId, it) }

        candidates.values.forEach { d ->
            Log.i(
                TAG,
                "candidate id=${d.displayId} name='${d.name}' flags=0x${d.flags.toString(16)} " +
                    "state=${d.state} valid=${d.isValid} " +
                    "mode=${d.mode.physicalWidth}x${d.mode.physicalHeight} " +
                    "usable=${d.isUsableExternalDisplay(internalDisplay)}",
            )
        }
        Log.i(TAG, "internalId=$internalId launchDisplayId=$launchDisplayId")

        val chosen = candidates.values.firstOrNull { display ->
            display.displayId != internalId && display.isUsableExternalDisplay(internalDisplay)
        }
        Log.i(TAG, "chosen external display = ${chosen?.displayId}")
        return chosen
    }

    private fun Display.isUsableExternalDisplay(internalDisplay: Display?): Boolean {
        val hasPresentationFlag = (flags and Display.FLAG_PRESENTATION) == Display.FLAG_PRESENTATION
        val isPrivateDisplay = (flags and Display.FLAG_PRIVATE) == Display.FLAG_PRIVATE
        val hasUsableMode = mode.physicalWidth > 0 && mode.physicalHeight > 0
        // Some handhelds (e.g. AYANEO Pocket DS) report both built-in panels with the
        // same display name, so comparing names rejects a perfectly usable second
        // screen. Identity is already established by displayId.
        val isDifferentDisplay = internalDisplay == null || displayId != internalDisplay.displayId
        return isValid &&
            state == Display.STATE_ON &&
            !isPrivateDisplay &&
            isDifferentDisplay &&
            hasPresentationFlag &&
            hasUsableMode
    }
}
