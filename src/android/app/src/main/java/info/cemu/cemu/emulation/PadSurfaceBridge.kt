package info.cemu.cemu.emulation

import android.view.SurfaceHolder
import android.view.View

/**
 * Puente entre la pantalla de emulacion y la actividad que muestra el GamePad en la
 * segunda pantalla.
 *
 * En consolas con dos paneles integrados (AYANEO Pocket DS) el lanzador del sistema
 * retiene la segunda pantalla de forma que una ventana de tipo Presentation nunca llega
 * a componerse en el panel. Una actividad normal lanzada en esa pantalla si desplaza al
 * lanzador, asi que el GamePad se muestra con [PadActivity] en lugar de con Presentation.
 */
object PadSurfaceBridge {
    @Volatile
    var holderCallback: SurfaceHolder.Callback? = null

    @Volatile
    var touchListener: View.OnTouchListener? = null

    @Volatile
    var rotateLeft: Boolean = false

    fun clear() {
        holderCallback = null
        touchListener = null
    }
}
