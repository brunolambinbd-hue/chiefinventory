package com.example.parabdcollector.ui.actvity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivityCoinMeasureBinding
import com.example.parabdcollector.utils.BitmapUtils

/**
 * Activity to measure an object's dimensions using a 2 Euro coin as reference.
 */
class CoinMeasureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoinMeasureBinding
    private var imageUri: Uri? = null
    private var finalWidth: Double = 0.0
    private var finalHeight: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoinMeasureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Get the image URI from the intent
        val uriStr = intent.getStringExtra(EXTRA_IMAGE_URI)
        if (uriStr != null) {
            imageUri = uriStr.toUri()
            val bitmap = BitmapUtils.getBitmapFromUri(this, imageUri!!)
            if (bitmap != null) {
                binding.ivPhoto.setImageBitmap(bitmap)
            } else {
                finish()
            }
        } else {
            finish()
        }

        // Listen for dimension changes from the overlay view
        binding.overlayView.onDimensionsChanged = { width, height ->
            finalWidth = width
            finalHeight = height
            val wStr = getString(R.string.measure_format, width)
            val hStr = getString(R.string.measure_format, height)
            binding.tvResult.text = getString(R.string.measure_result_format, wStr, hStr)
        }
        
        // On force le placement initial (coins de la photo et pièce en bas à gauche)
        binding.overlayView.post {
            val imageRect = getImageViewRect(binding.ivPhoto)
            binding.overlayView.reset(binding.overlayView.width, binding.overlayView.height, imageRect)
        }

        binding.btnReset.setOnClickListener {
            val imageRect = getImageViewRect(binding.ivPhoto)
            binding.overlayView.reset(binding.overlayView.width, binding.overlayView.height, imageRect)
        }

        binding.btnValidate.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(EXTRA_RESULT_WIDTH, finalWidth)
            resultIntent.putExtra(EXTRA_RESULT_HEIGHT, finalHeight)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun getImageViewRect(imageView: android.widget.ImageView): android.graphics.RectF? {
        val drawable = imageView.drawable ?: return null
        
        // Taille réelle du bitmap
        val bitmapWidth = drawable.intrinsicWidth.toFloat()
        val bitmapHeight = drawable.intrinsicHeight.toFloat()
        
        // Taille de l'écran (ImageView)
        val viewWidth = imageView.width.toFloat()
        val viewHeight = imageView.height.toFloat()

        if ((viewWidth <= 0) || (viewHeight <= 0)) return null

        // Calcul de l'échelle fitCenter
        val scale = kotlin.math.min(viewWidth / bitmapWidth, viewHeight / bitmapHeight)
        
        // Calcul des marges (bandes noires/grises)
        val actualWidth = bitmapWidth * scale
        val actualHeight = bitmapHeight * scale
        
        val dx = (viewWidth - actualWidth) / 2f
        val dy = (viewHeight - actualHeight) / 2f

        // On renvoie le rectangle EXACT de la photo visible
        return android.graphics.RectF(dx, dy, dx + actualWidth, dy + actualHeight)
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT_WIDTH = "extra_result_width"
        const val EXTRA_RESULT_HEIGHT = "extra_result_height"
    }
}
