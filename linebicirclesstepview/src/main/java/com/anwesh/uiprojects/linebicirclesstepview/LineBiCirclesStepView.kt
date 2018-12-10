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

class LineBiCirclesStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN ->  {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {
        fun update(cb : (Float) -> Unit) {
            scale += scale.updateScale(dir, circles, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LBCSNode(var i : Int  = 0, val state : State = State()) {

        private var next : LBCSNode? = null

        private var prev : LBCSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = LBCSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawLBCSNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : LBCSNode {
            var curr : LBCSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LineBiCirclesStep(var i : Int) {

        private val root : LBCSNode = LBCSNode()

        private var curr : LBCSNode = root

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : LineBiCirclesStepView) {

        private val animator : Animator = Animator(view)
        private var lbcs : LineBiCirclesStep = LineBiCirclesStep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            lbcs.draw(canvas, paint)
            animator.animate {
                lbcs.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            lbcs.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity: Activity) : LineBiCirclesStepView {
            val view : LineBiCirclesStepView = LineBiCirclesStepView(activity)
            activity.setContentView(view)
            return view 
        }
    }
}