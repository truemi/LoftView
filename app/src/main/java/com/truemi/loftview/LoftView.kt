package com.truemi.loftview

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Scroller

class LoftView : LinearLayout {
    var headlayout: ViewGroup? = null//顶部view的子控件,由用户添加
    var mScroller: Scroller = Scroller(context, DecelerateInterpolator())//滑动器
    var mTouchEvent: Boolean = false//是否拦截点击事件
    var scrollYValue = 0f//手指Y轴滑动的距离
    var PLACE = 0//顶部view的显示位置(顶部或底部,)
    var subViewHeight = 0//顶部view的height
    var refreshView: ListView? = null
    var decay_ratio = 0.5   //阻尼系数
    var mpaddingBottom = 0//顶部view的paddingBottom
    var mPaddingTop = 0//顶部view的PaddingTop
    var mlastY = 0f//手指安下的y轴坐标值
    var mlastX = 0f//手指安下的y轴坐标值
    var stateView = VIewState.CLOSE//顶部view的显示状态,默认是关闭状态
    var stateMove = TouchState_Move.NORMAL//手势滑动状态
    var dotView: DotView? = null//小圆点
    var topLayout: LinearLayout? = null//顶部view的父控件
    var currentLayout: LinearLayout? = null//住view的父控件
    var background_top: Int = 0xFFe7e7e7.toInt() //默认颜色

    constructor(context: Context) : super(context) {
        LoftView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LoftView(context, null, -1)
        setOrientation(VERTICAL)//必须添加
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val obtainStyledAttributes = context.theme.obtainStyledAttributes(attrs, R.styleable.LoftView,defStyleAttr,0)
        PLACE = obtainStyledAttributes.getInt(R.styleable.LoftView_place, 0)
        obtainStyledAttributes.recycle()
//        background_top = obtainStyledAttributes.getColor(R.styleable.LoftView_background_top, 0)
//        var tests = obtainStyledAttributes.getInteger(R.styleable.LoftView_test,0)
        println("-------颜色---->>"+PLACE)

    }

    /**
     * 可滑动的主view
     */
    fun addRefreshView(refreshView: ListView) {
        this.refreshView = refreshView
    }

    /**
     * 头部view
     */
    fun addHeadView(context: Context?, resLayout: Int): ViewGroup? {
        headlayout = View.inflate(context, resLayout, null) as ViewGroup?
        headlayout?.visibility = View.INVISIBLE
        return headlayout
    }

    /**
     * 构建顶部view布局
     */
    fun buildView() {
        when (refreshView) {
            null -> {
                refreshView = ListView(context)
            }
        }
        dotView = DotView(context!!)
        val layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 100)
        topLayout = LinearLayout(context)
        topLayout?.isClickable = true
        topLayout?.setBackgroundColor(background_top)
        topLayout?.orientation = LinearLayout.VERTICAL
        topLayout?.post {
            subViewHeight = topLayout?.height as Int
            mPaddingTop = -subViewHeight
            val paddingLeft = topLayout?.paddingLeft as Int
            val paddingRight = topLayout?.paddingRight as Int
            mpaddingBottom = topLayout?.paddingTop as Int
            topLayout?.setPadding(paddingLeft, mPaddingTop, paddingRight, mpaddingBottom)
        }
        //暂不支持底部显示
        when (PLACE) {
            0, 1 -> {
                topLayout?.addView(headlayout)
                topLayout?.addView(dotView, layoutParams)
                addView(topLayout)
                addView(refreshView)
            }
//可以忽略
//                    1 -> {
//                        this.addView(refreshView)
//                        topLayout?.addView(expendPoint, layoutParams)
//                        topLayout?.addView(headlayout)
//                        this.addView(topLayout)
//                    }
        }
    }

    /**
     * 设置顶部背景颜色
     */
    fun setBackgroundTop(color: Int) {
        this.background_top = color
    }

    /**
     * 处理触摸事件
     */
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN ->//安下
            {
                mlastY = ev?.getY()
                mlastX = ev?.getX()
                mTouchEvent = false
            }
            MotionEvent.ACTION_MOVE ->//滑动
            {
                val flX = ev?.getX() - mlastX
                val fl = ev?.getY() - mlastY
                val abs = Math.abs(fl)
                val scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop//手指滑动阈值
                if (abs > scaledTouchSlop) {

                    mTouchEvent = refreshView?.firstVisiblePosition == 0

                    if (fl < 0 && stateView == VIewState.CLOSE) {
                        mTouchEvent = false
                    }

                    if (stateView == VIewState.OPEN) {
                        if (Math.abs(flX) > abs) {
                            mTouchEvent = false
                        }
                        if (mlastY < subViewHeight){//顶部区域打开后不消费事件
                            mTouchEvent = false
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> mTouchEvent = false//抬起
        }
        return mTouchEvent
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        val action = ev?.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mTouchEvent = true
            }//安下

            MotionEvent.ACTION_MOVE ->//滑动
            {
                scrollYValue = (ev?.getY() - mlastY)
                val abs = Math.abs(scrollYValue)
                val scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
                if (abs > scaledTouchSlop) {
                    mTouchEvent = true
                    if (scrollYValue > 0) {
                        if (refreshView?.firstVisiblePosition == 0) {
                            if (stateView == VIewState.CLOSE) {
                                if (mPaddingTop < 0) {//向下滑动但是头部空间没完全显示
                                    mPaddingTop = (decay_ratio * scrollYValue - subViewHeight).toInt()
                                    topLayout?.setPadding(paddingLeft, mPaddingTop, paddingRight, mpaddingBottom)
                                    stateMove = TouchState_Move.DOWN_NO_OVER
                                    dotView?.setPercent(1 - (mPaddingTop.toFloat() / (-subViewHeight)))
                                    if (mPaddingTop > -subViewHeight / 2) {
                                        showTodown(headlayout!!, 200)
                                    }
                                    headlayout?.alpha =1 - (mPaddingTop.toFloat() / (-subViewHeight))
                                } else if (mPaddingTop >= 0) {//头部空间没哇完全显示依然向下滑动
                                    mPaddingTop = (0.5 * decay_ratio * scrollYValue + 0.5 * (-subViewHeight)).toInt()
                                    topLayout?.setPadding(paddingLeft, mPaddingTop, paddingRight, mPaddingTop)
                                    stateMove = TouchState_Move.DOWN_OVER
                                    dotView?.setPercent(1 - (mPaddingTop.toFloat() / (-subViewHeight)))
                                    headlayout?.alpha =1 - (mPaddingTop.toFloat() / (-subViewHeight))

                                }
                            } else {
                                mPaddingTop = (0.5 * decay_ratio * scrollYValue).toInt()
                                topLayout?.setPadding(paddingLeft, mPaddingTop, paddingRight, mPaddingTop)
                                stateMove = TouchState_Move.DOWN_OVER
                            }
                        }
                    } else {
                        if (refreshView?.firstVisiblePosition == 0) {
                            if (stateView == VIewState.CLOSE) {
                                mPaddingTop = -subViewHeight
                            } else {
                                mPaddingTop = (decay_ratio * scrollYValue).toInt()
                                if (mPaddingTop <= -subViewHeight) {
                                    topLayout?.setPadding(paddingLeft, mPaddingTop, paddingRight, mpaddingBottom)
                                    mPaddingTop = -subViewHeight
                                    stateView = VIewState.CLOSE
                                } else {
                                    topLayout?.setPadding(paddingLeft, mPaddingTop, paddingRight, mpaddingBottom)
                                    stateMove = TouchState_Move.UP
                                }
                            }
                        }
                    }
                }else{

                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->//抬起
            {
                if (mPaddingTop > -subViewHeight / 3 && mPaddingTop < 0 && stateMove == TouchState_Move.DOWN_NO_OVER) {
                    moveAnimation(-mPaddingTop, mPaddingTop)
                    stateView = VIewState.OPEN
                    dotHideAnim()
                }
                if (mPaddingTop <= -subViewHeight / 3 && mPaddingTop < 0 && stateMove == TouchState_Move.DOWN_NO_OVER) {
                    moveAnimation(-mPaddingTop, subViewHeight)
                    stateView = VIewState.CLOSE
                    headlayout?.visibility = View.INVISIBLE
                    dotView?.alpha = 1.0f
                }
                if (stateMove == TouchState_Move.DOWN_OVER) {
                    moveAnimation(-mPaddingTop, mPaddingTop)
                    stateView = VIewState.OPEN
                    dotHideAnim()
                }
                if (stateMove == TouchState_Move.UP) {
                    moveAnimation(-mPaddingTop, subViewHeight)
                    headlayout?.visibility = View.INVISIBLE
                    stateView = VIewState.CLOSE
                    dotView?.alpha = 1.0f
                }
                mTouchEvent = false
                scrollYValue = 0f
                mlastY = 0f
            }
        }

        return mTouchEvent
    }

    /**
     * view滚动回弹动画
     */
    fun moveAnimation(startY: Int, y: Int) {
        mScroller?.startScroll(0, startY, 0, y, 400);
        invalidate()
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            val currY = mScroller?.getCurrY()
            topLayout?.setPadding(paddingLeft, -currY, paddingRight, mpaddingBottom)
        }
        invalidate()//刷新view
    }

    /**
     * 触摸状态
     * DOWN_NO_OVER 向下滑动但是没有超出view的height值
     * DOWN_OVER 向下滑动并且超出了height值
     * UP 向上滑动
     * NORMAL 无状态
     */
    enum class TouchState_Move {
        DOWN_NO_OVER, DOWN_OVER, UP, NORMAL
    }

    /**
     * 顶部view的显示状态
     * CLOSE 顶部为关闭
     * OPEN 顶部为打开状态
     */
    enum class VIewState {
        CLOSE, OPEN
    }

    /**
     * 顶部view向下平移动画
     * @param view
     * @param time 动画时间
     */
    fun showTodown(view: View, time: Long) {
        if (view.visibility != View.VISIBLE) {
            val animator1 = ObjectAnimator.ofFloat(view, "translationY", -30f, 0f)
            //渐变动画
            var alphaAnimator = ObjectAnimator.ofFloat(view,"alpha",0f,1f)
            var animatorSet = AnimatorSet()
            animatorSet.playTogether(animator1,alphaAnimator)
            animatorSet.setDuration(time)
            animatorSet.interpolator = LinearInterpolator()
            animatorSet.start()
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {
                    view.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animator: Animator) {
                    view.visibility = View.VISIBLE
                }

                override fun onAnimationCancel(animator: Animator) {}

                override fun onAnimationRepeat(animator: Animator) {

                }
            })
        }
    }

    /**
     * 小圆点的隐藏动画
     */
    fun dotHideAnim() {
        val alpha = dotView?.animate()?.alpha(0f)
        alpha?.duration = 400
        alpha?.start()
    }
}