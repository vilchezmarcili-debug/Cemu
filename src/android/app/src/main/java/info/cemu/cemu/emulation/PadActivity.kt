package info.cemu.cemu.emulation

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout

/**
 * Muestra la pantalla del GamePad en la segunda pantalla integrada.
 *
 * Se lanza con ActivityOptions.setLaunchDisplayId sobre la pantalla elegida. A diferencia
 * de una Presentation, una actividad desplaza al lanzador propio del sistema que en
 * algunas consolas (AYANEO Pocket DS) retiene ese panel.
 */
class PadActivity : Activity() {
    private var surfaceView: SurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setBackgroundDrawableResource(android.R.color.black)

        val holderCallback = PadSurfaceBridge.holderCallback
        if (holderCallback == null) {
            Log.w(TAG, "sin callback de superficie, cierro la actividad del GamePad")
            finish()
            return
        }

        val display = display
        val mode = display?.mode
        val (surfaceWidth, surfaceHeight) = computeSurfaceSize(
            width = mode?.physicalWidth ?: 0,
            height = mode?.physicalHeight ?: 0,
            rotateLeft = PadSurfaceBridge.rotateLeft,
        )

        val view = SurfaceView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            if (surfaceWidth > 0 && surfaceHeight > 0) {
                holder.setFixedSize(surfaceWidth, surfaceHeight)
            }
            holder.addCallback(holderCallback)
            PadSurfaceBridge.touchListener?.let { setOnTouchListener(it) }
        }
        surfaceView = view

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            addView(view)
        }
        setContentView(root)

        Log.i(
            TAG,
            "PadActivity creada en pantalla ${display?.displayId} " +
                "superficie ${surfaceWidth}x$surfaceHeight",
        )
    }

    override fun onResume() {
        super.onResume()
        instancia = java.lang.ref.WeakReference(this)
    }

    override fun onDestroy() {
        if (instancia?.get() === this) {
            instancia = null
        }
        surfaceView?.setOnTouchListener(null)
        PadSurfaceBridge.holderCallback?.let { surfaceView?.holder?.removeCallback(it) }
        surfaceView = null
        Log.i(TAG, "PadActivity destruida")
        super.onDestroy()
    }

    /** La actividad del GamePad no debe reaccionar al boton de atras. */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Intencionadamente vacio.
    }

    private fun computeSurfaceSize(width: Int, height: Int, rotateLeft: Boolean): Pair<Int, Int> {
        if (width <= 0 || height <= 0) {
            return 0 to 0
        }
        var surfaceWidth = width
        var surfaceHeight = height

        if (surfaceWidth < surfaceHeight) {
            val tmp = surfaceWidth
            surfaceWidth = surfaceHeight
            surfaceHeight = tmp
        }

        if (rotateLeft) {
            val tmp = surfaceWidth
            surfaceWidth = surfaceHeight
            surfaceHeight = tmp
        }

        return surfaceWidth to surfaceHeight
    }

    companion object {
        private const val TAG = "CemuDisplay"

        @Volatile
        private var instancia: java.lang.ref.WeakReference<PadActivity>? = null

        /** Cierra la actividad del GamePad si sigue abierta. */
        fun cerrar() {
            val actividad = instancia?.get() ?: return
            instancia = null
            actividad.runOnUiThread {
                if (!actividad.isFinishing) {
                    actividad.finish()
                    actividad.overridePendingTransition(0, 0)
                }
            }
        }
    }
}
