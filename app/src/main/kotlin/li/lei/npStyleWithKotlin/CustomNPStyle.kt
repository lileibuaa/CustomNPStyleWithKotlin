package li.lei.npStyleWithKotlin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.Scroller

/**
 * Created by lei on 3/2/16.
 * you can you up...
 */
class CustomNPStyle : LinearLayout {

    var paint: Paint
    var mFlingScroller: Scroller
    var mLastPosition = 0
    var mCurrentPosition = 0;
    var mStringArray: Array<String>
    var mTextHeight: Int
    var mVelocityTracker: VelocityTracker? = null
    var mMinFlingVelocity: Int
    var mAdjustScroller: Scroller
    var mLastScrollerPosition: Int = 0
    var mThresholdHeight = 0
    var mDividerDrawable: Drawable? = null
    val SIZE_UNSPECIFIED = -1
    var mMinWidth: Int = SIZE_UNSPECIFIED
    var mMinHeight: Int = SIZE_UNSPECIFIED

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        orientation = LinearLayout.HORIZONTAL
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.RED;
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.textSize = 50f
        setWillNotDraw(false)
        mFlingScroller = Scroller(getContext(), null, true)
        mStringArray = arrayOf("A", "B", "C")
        mTextHeight = paint.textSize.toInt()
        mMinFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
        mAdjustScroller = Scroller(getContext(), DecelerateInterpolator(2.5f))
        var attributeArray = context?.obtainStyledAttributes(attrs, R.styleable.NumberPicker, defStyleAttr, defStyleRes) ?: return
        mDividerDrawable = attributeArray.getDrawable(R.styleable.NumberPicker_selectionDivider)
        mMinWidth = attributeArray.getDimensionPixelOffset(R.styleable.NumberPicker_internalMinWidth, SIZE_UNSPECIFIED)
        mMinHeight = attributeArray.getDimensionPixelOffset(R.styleable.NumberPicker_internalMinHeight, SIZE_UNSPECIFIED)
        attributeArray.recycle()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mDividerDrawable?.setBounds(0, height / 3 - 1, width, measuredHeight / 3 + 1)
        mDividerDrawable?.draw(canvas)
        mDividerDrawable?.setBounds(0, height / 3 * 2 - 1, width, measuredHeight / 3 * 2 + 1)
        mDividerDrawable?.draw(canvas)
        var inputTextOffset = -(paint.descent() + paint.ascent()) / 2 + mCurrentPosition
        canvas?.drawText(mStringArray[0], measuredWidth / 2f, measuredHeight / 6f + inputTextOffset, paint)
        canvas?.drawText(mStringArray[1], measuredWidth / 2f, measuredHeight / 2f + inputTextOffset, paint)
        canvas?.drawText(mStringArray[2], measuredWidth / 2f, measuredHeight / 6f * 5 + inputTextOffset, paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null)
            return super.onTouchEvent(null)
        if (mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain()
        mVelocityTracker?.addMovement(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mLastPosition = event.y.toInt()
                if (!mFlingScroller.isFinished || !mAdjustScroller.isFinished) {
                    mFlingScroller.forceFinished(true)
                    mAdjustScroller.forceFinished(true)
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                scrollBy(0, event.y.toInt() - mLastPosition)
                mLastPosition = event.y.toInt()
                return true
            }
            MotionEvent.ACTION_UP -> {
                mVelocityTracker?.computeCurrentVelocity(1000, ViewConfiguration.get(context).scaledMaximumFlingVelocity / 8f)
                if (Math.abs(mVelocityTracker?.yVelocity ?: 0f) > mMinFlingVelocity)
                    startFling(mVelocityTracker?.yVelocity ?: 0f)
                else
                    makeSureScrollAdjust();
                mVelocityTracker?.recycle()
                mVelocityTracker = null
            }
        }
        return super.onTouchEvent(event)
    }

    private fun makeSureScrollAdjust() {
        var dy: Int
        if (Math.abs(mCurrentPosition) < mThresholdHeight)
            dy = -mCurrentPosition
        else
            dy = if (mCurrentPosition > 0) height / 3 - mCurrentPosition else -height / 3 - mCurrentPosition
        mAdjustScroller.startScroll(0, 0, 0, dy, 800)
        mLastScrollerPosition = mAdjustScroller.startY
        invalidate()
    }

    private fun startFling(velocity: Float) {
        mFlingScroller.fling(0, 0, 0, velocity.toInt(), 0, 0, Int.MIN_VALUE, Int.MAX_VALUE)
        mLastScrollerPosition = mFlingScroller.startY
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var widthAfterResolve = resolveSizeAndState(Math.max(mMinWidth, measuredWidth), widthMeasureSpec, 0)
        var heightAfterResolve = resolveSizeAndState(Math.max(mMinHeight, measuredHeight), heightMeasureSpec, 0)
        setMeasuredDimension(widthAfterResolve, heightAfterResolve)
        mThresholdHeight = (measuredHeight / 3 - mTextHeight) / 2 + mTextHeight
    }

    override fun scrollBy(x: Int, y: Int) {
        mCurrentPosition += y;
        if (mCurrentPosition > mThresholdHeight) {
            mCurrentPosition = mTextHeight - mThresholdHeight.toInt()
            var tmpStr = mStringArray[2]
            mStringArray[2] = mStringArray[1]
            mStringArray[1] = mStringArray[0]
            mStringArray[0] = tmpStr
        } else if (mCurrentPosition < -mThresholdHeight) {
            mCurrentPosition = mThresholdHeight.toInt() - mTextHeight
            var tmpStr = mStringArray[0]
            mStringArray[0] = mStringArray[1]
            mStringArray[1] = mStringArray[2]
            mStringArray[2] = tmpStr
        }
        invalidate()
    }

    override fun computeScroll() {
        if (mFlingScroller.isFinished && mAdjustScroller.isFinished)
            return
        var scroller = if (mFlingScroller.isFinished) mAdjustScroller else mFlingScroller
        scroller.computeScrollOffset()
        scrollBy(0, scroller.currY - mLastScrollerPosition)
        mLastScrollerPosition = scroller.currY
        invalidate()
        if (mFlingScroller == scroller && mFlingScroller.isFinished) {
            makeSureScrollAdjust()
        }
    }
}
