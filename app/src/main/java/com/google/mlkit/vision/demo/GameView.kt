package com.google.mlkit.vision.demo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.AsyncTask
import android.util.AttributeSet
import android.util.Size
import android.view.View
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class GameView @JvmOverloads constructor(
        context: Context,
        attributeSet: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attributeSet, defStyleAttr) {

    private var isGameInitialized = false
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        textSize = context.resources.getDimension(R.dimen.score_size)
    }

    private var gameAssets: GameAssets? = null
    private var assetsLoadingTask: AsyncTask<Void, Void, GameAssets>? = null
    private var score: Int = 0
    private var scorePoint = PointF()
    private var playerSize = 0
    private var playerRect = RectF()
    private var cakeSize = 0
    private var cakeRect = RectF()
    private var backgroundX = 0f
    private val speed = 1f / TimeUnit.SECONDS.toMillis(5) // Full screen width per millis
    private var flags: EmotionFlags = EmotionFlags(false, false, false)
    private var previousTimestamp = 0L

    fun setEmotionFlags(flags: EmotionFlags) {
        this.flags = flags
    }

    @SuppressLint("StaticFieldLeak")
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (isSizeZeros()) {
            return
        }

        isGameInitialized = false
        assetsLoadingTask?.cancel(false)

        playerSize = h / 4
        cakeSize = h / 8
        assetsLoadingTask = object : AsyncTask<Void, Void, GameAssets>() {
            override fun doInBackground(vararg params: Void?): GameAssets {
                return loadGameAssets(
                        context,
                        playerSize = Size(playerSize, playerSize),
                        cakeSize = Size(cakeSize, cakeSize),
                        gameSize = Size(w, h)
                )
            }

            override fun onPostExecute(result: GameAssets) {
                gameAssets = result
                invalidate()
            }
        }
        assetsLoadingTask?.execute()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        assetsLoadingTask?.cancel(true)
    }

    private fun isSizeZeros() = width == 0 && height == 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val gameAssets = gameAssets ?: return

        if (!isGameInitialized) {
            initializePlayer()
            initializeCake()
            initializeScore()
            initializeTimestamp()
            isGameInitialized = true
        }

        movePlayer()
        moveCakeAndBackground()

        checkPlayerCaughtCake()
        checkCakeIsOutOfScreenStart()
        checkBackgroundOutOfScreenStart()

        drawBackground(canvas, gameAssets)
        drawPlayer(canvas, gameAssets)
        drawCake(canvas, gameAssets)
        drawScore(canvas)

        invalidate()
    }

    private fun initializePlayer() {
        playerRect.left = playerSize / 2f
        playerRect.right = playerRect.left + playerSize
    }

    private fun initializeCake() {
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

    private fun moveCakeAndBackground() {
        val currentTime = System.currentTimeMillis()
        val deltaTime = currentTime - previousTimestamp
        val deltaX = speed * width * deltaTime

        cakeRect.left -= deltaX
        cakeRect.right = cakeRect.left + cakeSize

        backgroundX -= deltaX

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

    private fun checkBackgroundOutOfScreenStart() {
        if (-backgroundX > width) {
            backgroundX = 0f
        }
    }

    private fun drawBackground(canvas: Canvas, gameAssets: GameAssets) {
        canvas.drawBitmap(gameAssets.background, backgroundX, 0f, bitmapPaint)
        canvas.drawBitmap(gameAssets.background, width + backgroundX, 0f, bitmapPaint)
    }

    private fun drawPlayer(canvas: Canvas, gameAssets: GameAssets) {
        val bitmap = gameAssets.player.getBitmap(System.currentTimeMillis())
        canvas.drawBitmap(bitmap, playerRect.left, playerRect.top, bitmapPaint)
    }

    private fun drawCake(canvas: Canvas, gameAssets: GameAssets) {
        canvas.drawBitmap(gameAssets.cake, cakeRect.left, cakeRect.top, bitmapPaint)
    }

    private fun drawScore(canvas: Canvas) {
        canvas.drawText(score.toString(), scorePoint.x, scorePoint.y, scorePaint)
    }

}
