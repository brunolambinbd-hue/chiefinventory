package com.example.parabdcollector.ui.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt

class MeasurementOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val COIN_DIAMETER_CM = 2.575
    private var coinX = 0f
    private var coinY = 0f
    private var coinRadius = 60f
    private var objRect = RectF()

    private var draggingPart: DragPart = DragPart.NONE
    private val HANDLE_RADIUS = 100f // Zone de toucher TRÈS large pour le confort

    enum class DragPart { NONE, COIN_CENTER, COIN_RADIUS, OBJ_TOP, OBJ_BOTTOM, OBJ_LEFT, OBJ_RIGHT }

    private val coinPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
    }

    private val objPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f) // Pointillés discrets
        isAntiAlias = true
    }

    private val handlePaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    var onDimensionsChanged: ((width: Double, height: Double) -> Unit)? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(objRect, objPaint)
        canvas.drawCircle(coinX, coinY, coinRadius, coinPaint)
        
        // Poignées jaunes bien visibles
        canvas.drawCircle(coinX, coinY, 25f, handlePaint) // Centre
        canvas.drawCircle(coinX + coinRadius, coinY, 25f, handlePaint) // Rayon
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                draggingPart = detectPart(x, y)
                return draggingPart != DragPart.NONE
            }
            MotionEvent.ACTION_MOVE -> {
                updatePosition(x, y)
                invalidate()
                calculateDimensions()
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                draggingPart = DragPart.NONE
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun triggerInitialCalculation() {
        calculateDimensions()
    }

    private fun detectPart(x: Float, y: Float): DragPart {
        // Priorité absolue à la pièce de 2€
        if (dist(x, y, coinX + coinRadius, coinY) < HANDLE_RADIUS) return DragPart.COIN_RADIUS
        if (dist(x, y, coinX, coinY) < HANDLE_RADIUS) return DragPart.COIN_CENTER
        
        // Ensuite les bords du cadre, mais seulement si on est vraiment dessus
        val borderSensitivity = 60f
        if (kotlin.math.abs(y - objRect.top) < borderSensitivity) return DragPart.OBJ_TOP
        if (kotlin.math.abs(y - objRect.bottom) < borderSensitivity) return DragPart.OBJ_BOTTOM
        if (kotlin.math.abs(x - objRect.left) < borderSensitivity) return DragPart.OBJ_LEFT
        if (kotlin.math.abs(x - objRect.right) < borderSensitivity) return DragPart.OBJ_RIGHT
        
        return DragPart.NONE
    }

    private fun updatePosition(x: Float, y: Float) {
        when (draggingPart) {
            DragPart.COIN_CENTER -> { coinX = x; coinY = y }
            DragPart.COIN_RADIUS -> { coinRadius = kotlin.math.abs(x - coinX).coerceAtLeast(20f) }
            DragPart.OBJ_TOP -> objRect.top = y
            DragPart.OBJ_BOTTOM -> objRect.bottom = y
            DragPart.OBJ_LEFT -> objRect.left = x
            DragPart.OBJ_RIGHT -> objRect.right = x
            else -> {}
        }
    }

    private fun calculateDimensions() {
        if (coinRadius <= 0) return
        val pixelsPerCm = (coinRadius * 2) / COIN_DIAMETER_CM
        onDimensionsChanged?.invoke(objRect.width().toDouble() / pixelsPerCm, objRect.height().toDouble() / pixelsPerCm)
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt(((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)).toDouble()).toFloat()
    }
    
    fun reset(viewWidth: Int, viewHeight: Int, initialRect: RectF? = null) {
        if (initialRect != null) {
            objRect.set(initialRect)
        } else {
            objRect.set(100f, 100f, viewWidth - 100f, viewHeight - 100f)
        }
        
        // Pièce centrée en bas à gauche de L'IMAGE (pas de l'écran)
        coinX = objRect.left + (objRect.width() * 0.15f)
        coinY = objRect.bottom - (objRect.height() * 0.15f)
        coinRadius = 50f
        
        invalidate()
        calculateDimensions()
    }
}
