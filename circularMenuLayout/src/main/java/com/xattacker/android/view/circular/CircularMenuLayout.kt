package com.xattacker.android.view.circular

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import com.xattacker.android.view.circular.R
import com.xattacker.util.WeakReferenceList
import java.lang.ref.WeakReference

class CircularMenuLayout : ConstraintLayout, View.OnClickListener
{
    companion object
    {
        private val CENTRAL_VIEW_ID = 99999
    }

    var mode = CircularMenuMode.MANUAL
        set(value)
        {
            field = value

            _animator?.cancel()
            _isRotating = false

            if (value == CircularMenuMode.AUTO)
            {
                _animator = createTurnAnimation(5000, true, true)
                _animator?.start()
            }
        }

    private var _angleUnit = 0f
    private var _radius = 0f
    private var _isRotating = false
    private var _initial = false
    private val _menuViews = WeakReferenceList<View>()
    private var _lastPoint: PointF? = null
    private var _animator: ValueAnimator? = null
    private var _listener: WeakReference<CircularMenuListener>? = null

    var listener: CircularMenuListener?
        get() = _listener?.get()
        set(value)
        {
            if (value != null)
            {
                _listener = WeakReference(value)
            }
            else
            {
                _listener = null
            }
        }

    constructor(aContext: Context) : super(aContext)
    {
    }

    constructor(aContext: Context, aAttrSet: AttributeSet) : super(aContext, aAttrSet)
    {
        initView(aContext, aAttrSet)
    }

    constructor(aContext: Context, aAttrSet: AttributeSet, aStyle: Int) : super(aContext, aAttrSet, aStyle)
    {
        initView(aContext, aAttrSet)
    }

    override fun addView(aSub: View)
    {
        super.addView(aSub)

        setupSubViewAngle()
    }

    override fun addView(aSub: View, aParams: ViewGroup.LayoutParams)
    {
        super.addView(aSub, aParams)

        setupSubViewAngle()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int)
    {
        super.onLayout(changed, l, t, r, b)

        setupSubViewAngle()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean
    {
        if (mode != CircularMenuMode.MANUAL || _isRotating)
        {
            return false
        }


        var intercepted = true

        when (event.action)
        {
            MotionEvent.ACTION_DOWN ->
            {
                _lastPoint = PointF()
                _lastPoint?.set(event.x, event.y)
            }

            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP ->
            {
                if (_lastPoint != null && !_isRotating)
                {
                    intercepted = handleTurnEvent(_lastPoint!!, event, true)
                }

                if (event.action == MotionEvent.ACTION_UP)
                {
                    _lastPoint = null
                }
            }

            else ->
            {
                _lastPoint = null
                intercepted = false
            }
        }

        return intercepted
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean
    {
        if (mode != CircularMenuMode.MANUAL || _isRotating)
        {
            return false
        }


        var intercepted = true

        when (event.action)
        {
            MotionEvent.ACTION_DOWN ->
            {
                _lastPoint = PointF()
                _lastPoint?.set(event.x, event.y)

                intercepted = false
            }

            MotionEvent.ACTION_MOVE ->
            {
                if (_lastPoint != null && !_isRotating)
                {
                    intercepted = handleTurnEvent(_lastPoint!!, event, false)
                }
            }

            MotionEvent.ACTION_UP -> intercepted = false

            else -> intercepted = false
        }

        return intercepted
    }

    override fun onClick(aView: View)
    {
        _listener?.get()?.onCircularMenuClicked(aView)
    }

    private fun initView(context: Context, attrSet: AttributeSet? = null)
    {
        if (attrSet != null)
        {
            val array = context.obtainStyledAttributes(attrSet, R.styleable.CircularMenuLayout)

            mode = CircularMenuMode.parse(array.getInt(R.styleable.CircularMenuLayout_mode, CircularMenuMode.MANUAL.value))
            array.recycle()
        }


        val central = CentralView(context)
        central.visibility = View.INVISIBLE
        central.id = CENTRAL_VIEW_ID

        val params = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.bottomToBottom = id
        params.endToEnd = id
        params.startToStart = id
        params.topToTop = id
        params.verticalBias = 0.5f
        params.horizontalBias = params.verticalBias

        addView(central, params)
    }

    private fun handleTurnEvent(lastPoint: PointF, event: MotionEvent, executed: Boolean): Boolean
    {
        var consumed = false
        val distance = Math.hypot((lastPoint.x - event.x).toDouble(), (lastPoint.y - event.y).toDouble())

        if (distance >= _radius / 4)
        {
            if (!executed)
            {
                return true
            }

            val x_offset = lastPoint.x - event.x
            val y_offset = lastPoint.y - event.y
            val left_to_right = x_offset < 0 // true = left to right
            val up_to_down = y_offset < 0 // true = up to down
            var turn_direction = left_to_right
            val last_x = lastPoint.x < width / 2
            val last_y = lastPoint.y < height / 2
            val current_x = event.x < width / 2
            val current_y = event.y < height / 2

            if (Math.abs(x_offset) > Math.abs(y_offset))
            {
                if (last_y && current_y)
                {
                    turn_direction = left_to_right
                }
                else if (last_y && !current_y)
                {
                    turn_direction = left_to_right
                }
                else if (!last_y && current_y)
                {
                    turn_direction = !left_to_right
                }
                else if (!last_y && !current_y)
                {
                    turn_direction = !left_to_right
                }
            }
            else
            {
                if (last_x && current_x)
                {
                    turn_direction = !up_to_down
                }
                else if (last_x && !current_x)
                {
                    turn_direction = up_to_down
                }
                else if (!last_x && current_x)
                {
                    turn_direction = !up_to_down
                }
                else if (!last_x && !current_x)
                {
                    turn_direction = up_to_down
                }
            }

            _isRotating = true

            _animator = createTurnAnimation(500, turn_direction, false)
            _animator?.start()

            if (event.action == MotionEvent.ACTION_MOVE)
            {
                lastPoint.set(event.x, event.y)
            }

            consumed = true
        }

        return consumed
    }

    private fun setupSubViewAngle()
    {
        if (width > 0 && !_initial)
        {
            _radius = (if (width <= height) (width / 3.5).toInt() else (height / 3.5).toInt()).toFloat()
            _menuViews.clear()

            for (i in 0 until childCount)
            {
                val sub = getChildAt(i)
                if (sub.visibility != View.GONE && sub !is CentralView)
                {
                    sub.setOnClickListener(this)
                    _menuViews.addReference(sub)
                }
            }

            _angleUnit = 360.0f / _menuViews.count()

            var start = 0f

            for (i in 0 until _menuViews.count())
            {
                val sub = _menuViews.get(i)
                if (sub != null)
                {
                    val params = sub.layoutParams as ConstraintLayout.LayoutParams
                    params.circleConstraint = CENTRAL_VIEW_ID
                    params.circleAngle = start
                    params.circleRadius = _radius.toInt()
                    sub.layoutParams = params

                    start += _angleUnit
                }
            }

            _initial = true
        }
    }

    private fun createTurnAnimation(aOrbitDuration: Long, direction: Boolean, aInfinite: Boolean): ValueAnimator
    {
        val anim = if (aInfinite) ValueAnimator.ofFloat(0f, if (direction) 359f else -359f)
        else ValueAnimator.ofFloat(0f, if (direction) _angleUnit else -_angleUnit)

        _menuViews.fetch {
            reference ->
            val layoutParams = reference.layoutParams as ConstraintLayout.LayoutParams
            val original_angle = layoutParams.circleAngle
            reference.tag = original_angle
        }

        anim.addUpdateListener {
            valueAnimator ->
            val value = valueAnimator.animatedValue as Float

            _menuViews.fetch {
                reference ->
                val original_angle = reference.tag as Float
                val layoutParams = reference.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.circleAngle = (original_angle + value) % 360
                reference.layoutParams = layoutParams
            }

            if (Math.abs(value) == _angleUnit)
            {
                _isRotating = false
            }
        }

        anim.addListener(
            object : AnimatorListenerAdapter()
            {
                override fun onAnimationEnd(animation: Animator, isReverse: Boolean)
                {
                    _isRotating = false
                }
            })

        anim.duration = aOrbitDuration
        anim.interpolator = LinearInterpolator()

        if (aInfinite)
        {
            anim.repeatMode = ValueAnimator.RESTART
            anim.repeatCount = ValueAnimator.INFINITE
        }

        return anim
    }
}
