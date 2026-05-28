package com.example.parabdcollector.ui.actvity

import android.app.Activity
import android.content.Intent
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parabdcollector.databinding.ActivityMeasureBinding
import com.example.parabdcollector.utils.ARCoreHelper
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.sqrt

/**
 * Activity to measure physical dimensions using ARCore.
 */
class MeasureActivity : AppCompatActivity(), GLSurfaceView.Renderer {

    private lateinit var binding: ActivityMeasureBinding
    private var session: Session? = null
    private var installRequested = false

    private val points = mutableListOf<Pose>()
    private var lastDistance: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeasureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupSurfaceView()
        setupClickListeners()
        
        binding.tvInstructions.text = getString(com.example.parabdcollector.R.string.measure_instructions_plane)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupSurfaceView() {
        binding.surfaceview.preserveEGLContextOnPause = true
        binding.surfaceview.setEGLContextClientVersion(2)
        binding.surfaceview.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        binding.surfaceview.setRenderer(this)
        binding.surfaceview.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    private fun setupClickListeners() {
        binding.surfaceview.setOnClickListener {
            handleTap()
        }

        binding.btnValidate.setOnClickListener {
            val intent = Intent()
            // We return the last measured distance as a simplistic implementation
            // In a real app, we'd return width and height from multiple points
            intent.putExtra(EXTRA_WIDTH, lastDistance)
            intent.putExtra(EXTRA_HEIGHT, 0.0) // To be refined
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun handleTap() {
        val frame = session?.update() ?: return
        if (frame.camera.trackingState != TrackingState.TRACKING) return

        // Hit test at center of screen
        val hits = frame.hitTest(binding.surfaceview.width / 2f, binding.surfaceview.height / 2f)
        for (hit in hits) {
            val trackable = hit.trackable
            if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                addPoint(hit.hitPose)
                break
            }
        }
    }

    private fun addPoint(pose: Pose) {
        points.add(pose)
        if (points.size > 2) points.removeAt(0)

        if (points.size == 2) {
            val p1 = points[0]
            val p2 = points[1]
            val dx = p1.tx() - p2.tx()
            val dy = p1.ty() - p2.ty()
            val dz = p1.tz() - p2.tz()
            
            // Distance in meters converted to cm
            lastDistance = sqrt((dx * dx + dy * dy + dz * dz).toDouble()) * 100.0
            
            binding.tvDimensions.text = getString(com.example.parabdcollector.R.string.measure_format, lastDistance)
            binding.tvDimensions.visibility = View.VISIBLE
            binding.btnValidate.visibility = View.VISIBLE
            binding.tvInstructions.text = getString(com.example.parabdcollector.R.string.measure_instructions_done)
        } else {
            binding.tvInstructions.text = getString(com.example.parabdcollector.R.string.measure_instructions_point2)
        }
    }

    override fun onResume() {
        super.onResume()
        if (session == null) {
            try {
                if (!ARCoreHelper.isARCoreSupported(this)) {
                    Toast.makeText(this, "ARCore non supporté sur cet appareil", Toast.LENGTH_LONG).show()
                    finish()
                    return
                }

                when (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        installRequested = true
                        return
                    }
                    ArCoreApk.InstallStatus.INSTALLED -> {}
                }

                session = Session(this)
            } catch (e: Exception) {
                Toast.makeText(this, "Erreur ARCore : ${e.message}", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }

        try {
            session?.resume()
        } catch (e: CameraNotAvailableException) {
            Toast.makeText(this, "Caméra non disponible", Toast.LENGTH_LONG).show()
            session = null
            finish()
        }
        binding.surfaceview.onResume()
    }

    override fun onPause() {
        super.onPause()
        session?.pause()
        binding.surfaceview.onPause()
    }

    override fun onDestroy() {
        super.onPause()
        session?.close()
        session = null
        super.onDestroy()
    }

    // --- GL Renderer Implementation ---

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        session?.setDisplayGeometry(0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        
        val frame = session?.update() ?: return
        // In a full implementation, we would draw the camera background here.
        // For this step, we just manage the AR logic.
    }

    companion object {
        const val EXTRA_WIDTH = "extra_width"
        const val EXTRA_HEIGHT = "extra_height"
    }
}
