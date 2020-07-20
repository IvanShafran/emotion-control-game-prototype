package com.google.mlkit.vision.demo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Size

class AnimatedGameObject(
        private val bitmaps: List<Bitmap>,
        private val duration: Long
) {
    fun getBitmap(timeInMillis: Long): Bitmap {
        val mod = timeInMillis % duration
        val index = (mod / duration.toFloat()) * bitmaps.size
        return bitmaps[index.toInt()]
    }
}

class GameAssets(
        val player: AnimatedGameObject,
        val cake: Bitmap,
        val background: Bitmap
)

fun loadGameAssets(
        context: Context,
        playerSize: Size,
        cakeSize: Size,
        gameSize: Size
): GameAssets {
    val playerBitmaps = mutableListOf<Bitmap>()
    for (i in 1..8) {
        playerBitmaps.add(loadBitmap(context, "game/player/Run ($i).png", playerSize))
    }

    return GameAssets(
            player = AnimatedGameObject(playerBitmaps, 1000L),
            cake = loadBitmap(context, "game/cake.png", cakeSize),
            background = loadBitmap(context, "game/background.jpg", gameSize)
    )
}

private fun loadBitmap(context: Context, path: String, size: Size): Bitmap {
    val bitmap = context.assets.open(path).use {
        BitmapFactory.decodeStream(it)
    }
    return Bitmap.createScaledBitmap(bitmap, size.width, size.height, true)
}
