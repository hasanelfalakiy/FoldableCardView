package com.andihasan7.foldablecardview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.view.animation.Transformation
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.andihasan7.foldablecardview.databinding.FoldableCardviewBinding

class FoldableCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var binding: FoldableCardviewBinding

    private var title: String? = null
    private var innerView: View? = null
    private var typedArray: TypedArray? = null
    private var innerViewRes: Int = 0
    private var iconDrawable: Drawable? = null

    var animDuration = DEFAULT_ANIM_DURATION.toLong()
    var isExpanded = false
        private set

    private var isExpanding = false
    private var isCollapsing = false
    private var expandOnClick = false
    private var startExpanded = false
    private var previousHeight = 0

    private var listener: OnExpandedListener? = null

    private val defaultClickListener = OnClickListener {
        if (isExpanded) collapse() else expand()
    }

    private val isMoving: Boolean
        get() = isExpanding || isCollapsing

    init {
        initView(context)
        attrs?.let { initAttributes(context, it) }
    }

    private fun initView(context: Context) {
        binding = FoldableCardviewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private fun initAttributes(context: Context, attrs: AttributeSet) {
        typedArray = context.obtainStyledAttributes(attrs, R.styleable.FoldableCardView).apply {
            title = getString(R.styleable.FoldableCardView_title)
            iconDrawable = getDrawable(R.styleable.FoldableCardView_icon)
            innerViewRes = getResourceId(R.styleable.FoldableCardView_inner_view, View.NO_ID)
            expandOnClick = getBoolean(R.styleable.FoldableCardView_expandOnClick, false)
            animDuration = getInteger(
                R.styleable.FoldableCardView_animationDuration,
                DEFAULT_ANIM_DURATION
            ).toLong()
            startExpanded = getBoolean(R.styleable.FoldableCardView_startExpanded, false)
            recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!TextUtils.isEmpty(title)) binding.cardTitle.text = title

        iconDrawable?.let {
            binding.cardHeader.visibility = View.VISIBLE
            binding.cardIcon.background = it
        }

        setInnerView(innerViewRes)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            elevation = Utils.convertDpToPixels(context, 4f)
        }

        if (startExpanded) {
            animDuration = 0
            expand()
        }

        if (expandOnClick) {
            binding.cardLayout.setOnClickListener(defaultClickListener)
            binding.cardArrow.setOnClickListener(defaultClickListener)
        }
    }

    fun expand() {
        val initialHeight = binding.cardLayout.height
        if (!isMoving) previousHeight = initialHeight

        binding.cardLayout.measure(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        val targetHeight = binding.cardLayout.measuredHeight

        if (targetHeight - initialHeight != 0) {
            animateViews(initialHeight, targetHeight - initialHeight, EXPANDING)
        }
    }

    fun collapse() {
        val initialHeight = binding.cardLayout.measuredHeight
        if (initialHeight - previousHeight != 0) {
            animateViews(initialHeight, initialHeight - previousHeight, COLLAPSING)
        }
    }

    private fun animateViews(initialHeight: Int, distance: Int, animationType: Int) {
        val expandAnimation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                val newHeight = if (animationType == EXPANDING) {
                    (initialHeight + distance * interpolatedTime).toInt()
                } else {
                    (initialHeight - distance * interpolatedTime).toInt()
                }

                binding.cardLayout.layoutParams.height = newHeight
                binding.cardContainer.layoutParams.height = newHeight
                binding.cardContainer.requestLayout()

                if (interpolatedTime == 1f) {
                    isExpanding = false
                    isCollapsing = false
                    listener?.onExpandChanged(binding.cardLayout, animationType == EXPANDING)
                }
            }

            override fun willChangeBounds(): Boolean = true
        }

        val arrowAnimation = if (animationType == EXPANDING) {
            RotateAnimation(0f, 180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        } else {
            RotateAnimation(180f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        }.apply {
            fillAfter = true
            duration = animDuration
        }

        expandAnimation.duration = animDuration

        isExpanding = animationType == EXPANDING
        isCollapsing = animationType == COLLAPSING

        startAnimation(expandAnimation)
        binding.cardArrow.startAnimation(arrowAnimation)
        isExpanded = animationType == EXPANDING

        Log.d("SO", "Started animation: ${if (isExpanded) "Expanding" else "Collapsing"}")
    }

    fun setOnExpandedListener(listener: OnExpandedListener) {
        this.listener = listener
    }

    fun setOnExpandedListener(method: (v: View?, isExpanded: Boolean) -> Unit) {
        this.listener = object : OnExpandedListener {
            override fun onExpandChanged(v: View?, isExpanded: Boolean) {
                method(v, isExpanded)
            }
        }
    }

    fun removeOnExpandedListener() {
        this.listener = null
    }

    fun setTitle(@StringRes titleRes: Int = -1, titleText: String = "") {
        if (titleRes != -1)
            binding.cardTitle.setText(titleRes)
        else
            binding.cardTitle.text = titleText
    }

    fun setIcon(@DrawableRes drawableRes: Int = -1, drawable: Drawable? = null) {
        iconDrawable = when {
            drawableRes != -1 -> ContextCompat.getDrawable(context, drawableRes)
            else -> drawable
        }
        binding.cardIcon.background = iconDrawable
    }

    private fun setInnerView(resId: Int) {
        binding.cardStub.layoutResource = resId
        innerView = binding.cardStub.inflate()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.cardArrow.setOnClickListener(l)
        super.setOnClickListener(l)
    }

    interface OnExpandedListener {
        fun onExpandChanged(v: View?, isExpanded: Boolean)
    }

    companion object {
        private const val DEFAULT_ANIM_DURATION = 350
        private const val COLLAPSING = 0
        private const val EXPANDING = 1
    }
}