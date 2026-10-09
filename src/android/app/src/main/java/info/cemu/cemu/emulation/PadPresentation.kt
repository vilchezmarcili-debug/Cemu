package info.cemu.cemu.emulation

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.view.Display
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import android.view.WindowManager

class PadPresentation(
    context: Context,
    display: Display,
    private val rotateLeft: Boolean,
    private val holderCallback: SurfaceHolder.Callback,
    private val touchListener: CanvasOnTouchListener,
) : Presentation(context, display) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.addFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )

        val mode = display.mode
        val (surfaceWidth, surfaceHeight) = computeSurfaceSize(
            width = mode.physicalWidth,
            height = mode.physicalHeight,
            rotateLeft = rotateLeft,
        )

        val surfaceView = SurfaceView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )

            holder.setFixedSize(surfaceWidth, surfaceHeight)
            holder.addCallback(holderCallback)
            setOnTouchListener(touchListener)
        }

        // Diagnostico Pocket DS: fondo negro opaco y una marca roja en la esquina.
        // Si en la pantalla de abajo se ve el negro y la marca, la ventana se compone
        // bien y el problema esta solo en el contenido de la superficie.
        val root = android.widget.FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setBackgroundColor(android.graphics.Color.BLACK)
            addView(surfaceView)
            addView(
                android.view.View(context).apply {
                    setBackgroundColor(android.graphics.Color.RED)
                    layoutParams = android.widget.FrameLayout.LayoutParams(80, 80).apply {
                        gravity = android.view.Gravity.TOP or android.view.Gravity.START
                    }
                }
            )
        }

        window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.BLACK))
        setContentView(root)
        android.util.Log.i(
            "CemuDisplay",
            "PadPresentation onCreate: superficie fija ${surfaceWidth}x$surfaceHeight " +
                "en pantalla ${display.displayId}",
        )
    }

    private fun computeSurfaceSize(width: Int, height: Int, rotateLeft: Boolean): Pair<Int, Int> {
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
}
