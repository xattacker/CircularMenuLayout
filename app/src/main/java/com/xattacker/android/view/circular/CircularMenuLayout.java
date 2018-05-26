package com.xattacker.android.view.circular;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.xattacker.util.WeakReferenceList;

import java.lang.ref.WeakReference;

public class CircularMenuLayout extends ConstraintLayout implements View.OnClickListener
{
    private final static int CENTRAL_VIEW_ID = 99999;

    private float _angleUnit = 0;
    private float _radius = 0;
    private boolean _isRotating = false;
    private boolean _initial = false;
    private WeakReferenceList<View> _menuViews = new WeakReferenceList<>();
    private PointF _lastPoint;
    private ValueAnimator _animator;

    private CircularMenuMode _mode = CircularMenuMode.MANUAL;
    private WeakReference<CircularMenuListener> _listener;

    public CircularMenuLayout(Context aContext)
    {
        super(aContext);
    }

    public CircularMenuLayout(Context aContext, AttributeSet aAttrSet)
    {
        super(aContext, aAttrSet);

        initView(aContext);
    }

    public CircularMenuLayout(Context aContext, AttributeSet aAttrSet, int aStyle)
    {
        super(aContext, aAttrSet, aStyle);

        initView(aContext);
    }

    public void setListener(CircularMenuListener aListener)
    {
        _listener = new WeakReference<>(aListener);
    }

    public void setMode(CircularMenuMode aMode)
    {
        if (_mode != aMode)
        {
            _mode = aMode;

            if (_animator != null)
            {
                _animator.cancel();
                _isRotating = false;
            }

            if (aMode == CircularMenuMode.AUTO)
            {
                _animator = createTurnAnimation(5000, true, true);
                _animator.start();
            }
        }
    }

    @Override
    public void addView(View aSub)
    {
        super.addView(aSub);

        setupSubViewAngle();
    }

    @Override
    public void addView(View aSub, ViewGroup.LayoutParams aParams)
    {
        super.addView(aSub, aParams);

        setupSubViewAngle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        super.onLayout(changed, l, t, r, b);

        setupSubViewAngle();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (_mode != CircularMenuMode.MANUAL || _isRotating)
        {
            return false;
        }


        boolean intercepted = true;

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                _lastPoint = new PointF();
                _lastPoint.set(event.getX(), event.getY());
            }
                break;

            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            {
                if (_lastPoint != null && !_isRotating)
                {
                    intercepted = handleTurnEvent(event, true);
                }

                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    _lastPoint = null;
                }
            }
                break;

            default:
                _lastPoint = null;
                intercepted = false;
                break;
        }

        return intercepted;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event)
    {
        if (_mode != CircularMenuMode.MANUAL || _isRotating)
        {
            return false;
        }


        boolean intercepted = true;

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                _lastPoint = new PointF();
                _lastPoint.set(event.getX(), event.getY());

                intercepted = false;
            }
                break;

            case MotionEvent.ACTION_MOVE:
                if (_lastPoint != null && !_isRotating)
                {
                    intercepted = handleTurnEvent(event, false);
                }
                break;

            case MotionEvent.ACTION_UP:
                intercepted = false;
                break;

            default:
                intercepted = false;
                break;
        }

        return intercepted;
    }

    @Override
    public void onClick(View aView)
    {
        if (_listener != null && _listener.get() != null)
        {
            _listener.get().onCircularMenuClicked(aView);
        }
    }

    private void initView(Context aContext)
    {
        CentralView central = new CentralView(aContext);
        central.setVisibility(View.INVISIBLE);
        central.setId(CENTRAL_VIEW_ID);

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomToBottom = getId();
        params.endToEnd = getId();
        params.startToStart = getId();
        params.topToTop = getId();
        params.horizontalBias = params.verticalBias = 0.5f;

        addView(central, params);
    }

    private boolean handleTurnEvent(MotionEvent event, boolean executed)
    {
        boolean consumed = false;
        double distance = Math.hypot(_lastPoint.x - event.getX(), _lastPoint.y - event.getY());

        if (distance >= _radius/4)
        {
            if (!executed)
            {
                return true;
            }


            float x_offset = _lastPoint.x - event.getX();
            float y_offset = _lastPoint.y - event.getY();
            boolean left_to_right = x_offset < 0; // true = left to right
            boolean up_to_down = y_offset < 0; // true = up to down

            boolean turn_direction = left_to_right;

            boolean last_x = _lastPoint.x < (getWidth()/2);
            boolean last_y = _lastPoint.y < (getHeight()/2);
            boolean current_x = event.getX() < (getWidth()/2);
            boolean current_y = event.getY() < (getHeight()/2);

            if (Math.abs(x_offset) > Math.abs(y_offset))
            {
                if (last_y && current_y)
                {
                    turn_direction = left_to_right;
                }
                else if (last_y && !current_y)
                {
                    turn_direction = left_to_right;
                }
                else if (!last_y && current_y)
                {
                    turn_direction = !left_to_right;
                }
                else if (!last_y && !current_y)
                {
                    turn_direction = !left_to_right;
                }
            }
            else
            {
                if (last_x && current_x)
                {
                    turn_direction = !up_to_down;
                }
                else if (last_x && !current_x)
                {
                    turn_direction = up_to_down;
                }
                else if (!last_x && current_x)
                {
                    turn_direction = !up_to_down;
                }
                else if (!last_x && !current_x)
                {
                    turn_direction = up_to_down;
                }
            }

            _isRotating = true;

            _animator = createTurnAnimation(500, turn_direction, false);
            _animator.start();

            if (event.getAction() == MotionEvent.ACTION_MOVE)
            {
                _lastPoint.set(event.getX(), event.getY());
            }

            consumed = true;
        }

        return consumed;
    }

    private void setupSubViewAngle()
    {
        if (getWidth() > 0 && !_initial)
        {
            _radius = getWidth() <= getHeight() ? (int) (getWidth() / 3.5) : (int) (getHeight() / 3.5);
            _menuViews.clearReferences();

            for (int i = 0; i < getChildCount(); i++)
            {
                View sub = getChildAt(i);
                if (sub.getVisibility() != View.GONE && !(sub instanceof CentralView))
                {
                    sub.setOnClickListener(this);
                    _menuViews.addReference(sub);
                }
            }

            _angleUnit = 360.0f / _menuViews.size();
            float start = 0;

            for (int i = 0; i < _menuViews.size(); i++)
            {
                View sub = _menuViews.get(i);
                if (sub != null)
                {
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) sub.getLayoutParams();
                    params.circleConstraint = CENTRAL_VIEW_ID;
                    params.circleAngle = start;
                    params.circleRadius = (int)_radius;
                    sub.setLayoutParams(params);

                    start += _angleUnit;
                }
            }

            _initial = true;
        }
    }

    private ValueAnimator createTurnAnimation(long aOrbitDuration, boolean direction, boolean aInfinite)
    {
        ValueAnimator anim = aInfinite ?
                             ValueAnimator.ofFloat(0, direction ? 359 : - 359) :
                             ValueAnimator.ofFloat(0, direction ? _angleUnit : - _angleUnit);

        _menuViews.fetchReference
        (
        new WeakReferenceList.WeakReferenceListVisitor<View>()
        {
            @Override
            public void onReferenceFetched(View aReference)
            {
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams)aReference.getLayoutParams();
                float original_angle = layoutParams.circleAngle;
                aReference.setTag(original_angle);
            }
        }
        );


        anim.addUpdateListener
        (
        new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator aValueAnimator)
            {
                final float value = (float)aValueAnimator.getAnimatedValue();

                _menuViews.fetchReference
                (
                new WeakReferenceList.WeakReferenceListVisitor<View>()
                {
                    @Override
                    public void onReferenceFetched(View aReference)
                    {
                        float original_angle = (float)aReference.getTag();
                        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams)aReference.getLayoutParams();
                        layoutParams.circleAngle = (original_angle + value)%360;
                        aReference.setLayoutParams(layoutParams);
                    }
                }
                );

                if (Math.abs(value) == _angleUnit)
                {
                    _isRotating = false;
                }
            }
        }
        );

        anim.addListener
        (
        new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse)
            {
                _isRotating = false;
            }
        }
        );

        anim.setDuration(aOrbitDuration);
        anim.setInterpolator(new LinearInterpolator());

        if (aInfinite)
        {
            anim.setRepeatMode(ValueAnimator.RESTART);
            anim.setRepeatCount(ValueAnimator.INFINITE);
        }

        return anim;
    }
}
