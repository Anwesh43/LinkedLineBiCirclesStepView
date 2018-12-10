package com.anwesh.uiprojects.linebicirclesstepview

/**
 * Created by anweshmishra on 10/12/18.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.RectF

val nodes : Int = 5
val circles : Int = 6
val strokeFactor : Int = 90
val sizeFactor : Float = 2.5f
val scDiv : Double = 0.51
val scGap : Float = 0.05f
val color : Int = Color.parseColor("#F57F17")

fun Int.getInverse() : Float = 1f / this

fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.getInverse(), Math.max(0f, this - i * n.getInverse())) * n

fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()

fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.getInverse() + scaleFactor() * b.getInverse()

fun Float.updateScale(dir : Float, a : Int, b : Int) : Float = dir * scGap * mirrorValue(a, b)

fun Canvas.drawStrokedArcCircle(r : Float, sc : Float, paint : Paint)  {
    paint.style = Paint.Style.STROKE
    drawArc(RectF(-r, -r, r, r), -90f, 360f * sc, false, paint)
}

fun Canvas.setStrokeStyle(color : Int, sw :Float, paint : Paint) {
    paint.color = color
    paint.strokeWidth = sw
    paint.strokeCap = Paint.Cap.ROUND
}

fun Canvas.drawFilledCircle(r : Float, sc : Float, paint : Paint) {
    paint.style = Paint.Style.FILL
    drawCircle(0f, 0f, r * sc, paint)
}

fun Canvas.drawLBCSNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    setStrokeStyle(color, Math.min(w, h) / strokeFactor, paint)
    val xGap = size / (circles / 2)
    save()
    translate(gap * (i + 1), h/2)
    rotate(90f * sc2)
    drawLine(-size, 0f, size, 0f, paint)
    for (j in 0..circles) {
        val sf : Float = 1f - 2 * (j % 2)
        val sc : Float = sc1.divideScale(j, circles)
        val sc01 : Float = sc.divideScale(0, 2)
        val sc02 : Float = sc.divideScale(1, 2)
        save()
        scale(sf, sf)
        translate(-size/2 + xGap  * j, -xGap/2 - size/12)
        drawStrokedArcCircle(xGap/2, sc01,paint)
        drawFilledCircle(xGap/2, sc02, paint)
        restore()
    }
    restore()
}