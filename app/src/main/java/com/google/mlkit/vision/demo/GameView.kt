package com.google.mlkit.vision.demo

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class GameView @JvmOverloads constructor(
        context: Context,
        attributeSet: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attributeSet, defStyleAttr) {

    private var isGameInitialized = false
    private val playerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLUE
    }
    private val cakePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.RED
    }
    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        textSize = context.resources.getDimension(R.dimen.score_size)
    }
    private var score: Int = 0
    private var scorePoint = PointF()
    private var playerSize = 0
    private var playerRect = RectF()
    private var cakeSize = 0
    private var cakeRect = RectF()
    private val cakeSpeed = 1f / TimeUnit.SECONDS.toMillis(5) // Full screen width per millis
    private var flags: EmotionFlags = EmotionFlags(false, false, false)
    private var previousTimestamp = 0L

    fun setEmotionFlags(flags: EmotionFlags) {
        this.flags = flags
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        isGameInitialized = false
    }

    private fun isSizeZeros() = width == 0 && height == 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isSizeZeros()) {
            return
        }

        if (!isGameInitialized) {
            initializePlayer()
            initializeCake()
            initializeScore()
            initializeTimestamp()
            isGameInitialized = true
        }

        movePlayer()
        moveCake()

        checkPlayerCaughtCake()
        checkCakeIsOutOfScreenStart()

        drawPlayer(canvas)
        drawCake(canvas)
        drawScore(canvas)

        invalidate()
    }

    private fun initializePlayer() {
        playerSize = height / 4

        playerRect.left = playerSize / 2f
        playerRect.right = playerRect.left + playerSize
    }

    private fun initializeCake() {
        cakeSize = height / 8
        moveCakeToStartPoint()
    }

    private fun moveCakeToStartPoint() {
        // Random position outside of screen
        cakeRect.left = width + width * Random.nextFloat()
        cakeRect.right = cakeRect.left + cakeSize

        // Random top or bottom line
        val isTopLine = Random.nextBoolean()
        cakeRect.top = getObjectYTopForLine(cakeSize, isTopLine).toFloat()
        cakeRect.bottom = cakeRect.top + cakeSize
    }

    // Get top position of object with size,
    // that places object in the middle of top or bottom line
    private fun getObjectYTopForLine(size: Int, isTopLine: Boolean): Int {
        return if (isTopLine) {
            width / 2 - width / 4 - size / 2
        } else {
            width / 2 + width / 4 - size / 2
        }
    }

    private fun initializeScore() {
        val bounds = Rect()
        scorePaint.getTextBounds("0", 0, 1, bounds)
        val scoreMargin = resources.getDimension(R.dimen.score_margin)
        scorePoint = PointF(width / 2f, scoreMargin + bounds.height())
        score = 0
    }

    private fun initializeTimestamp() {
        previousTimestamp = System.currentTimeMillis()
    }

    private fun movePlayer() {
        playerRect.top = getObjectYTopForLine(playerSize, isTopLine = flags.isSmile).toFloat()
        playerRect.bottom = playerRect.top + playerSize
    }

    private fun moveCake() {
        val currentTime = System.currentTimeMillis()
        val deltaTime = currentTime - previousTimestamp
        val deltaX = cakeSpeed * width * deltaTime

        cakeRect.left -= deltaX
        cakeRect.right = cakeRect.left + cakeSize

        previousTimestamp = currentTime
    }

    private fun checkPlayerCaughtCake() {
        if (RectF.intersects(playerRect, cakeRect)) {
            score += if (flags.isLeftEyeOpen && flags.isRightEyeOpen) 1 else 2 // Double bonus for closed eye
            moveCakeToStartPoint()
        }
    }

    private fun checkCakeIsOutOfScreenStart() {
        if (cakeRect.right < 0) {
            moveCakeToStartPoint()
        }
    }

    private fun drawPlayer(canvas: Canvas) {
        canvas.drawRect(playerRect, playerPaint)
    }

    private fun drawCake(canvas: Canvas) {
        canvas.drawRect(cakeRect, cakePaint)
    }

    private fun drawScore(canvas: Canvas) {
        canvas.drawText(score.toString(), scorePoint.x, scorePoint.y, scorePaint)
    }

}
