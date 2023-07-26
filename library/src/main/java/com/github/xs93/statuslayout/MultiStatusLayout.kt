package com.github.xs93.statuslayout

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.util.containsKey

/**
 * 多状态布局
 *
 * @author XuShuai
 * @version v1.0
 * @date 2023/7/24 15:57
 * @email 466911254@qq.com
 */
class MultiStatusLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        const val STATE_CONTENT = 0
        const val STATE_LOADING = 1
        const val STATE_EMPTY = 2
        const val STATE_ERROR = 3
        const val INVALID_LAYOUT_ID = -1
    }

    interface OnViewStatusChangeListener {
        fun onStatusChange(oldViewStatus: Int, oldView: View?, newViewStatus: Int, newView: View?)
    }

    /**
     * View 创建监听,只在第一次View创建时被调用
     * @constructor
     */
    interface OnViewCreatedListener {
        fun onViewCreated(viewStatus: Int, view: View)
    }

    private var mContentLayoutId = INVALID_LAYOUT_ID
    private var mLoadingLayoutId = INVALID_LAYOUT_ID
    private var mEmptyLayoutId = INVALID_LAYOUT_ID
    private var mErrorLayoutId = INVALID_LAYOUT_ID

    private val mInflater: LayoutInflater
    private val mLayoutParams = LayoutParams(-1, -1)

    private val mViews: SparseArray<View?> = SparseArray(4)
    private var mShowStatus: Int = -1
    private var mDefaultStatus: Int = -1

    private var mRetryClickListener: OnClickListener? = null
    private var mViewStatusChangeListener: OnViewStatusChangeListener? = null
    private var mViewCreatedListener: OnViewCreatedListener? = null

    init {
        mInflater = LayoutInflater.from(context)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.MultiStatusLayout)
        mContentLayoutId = ta.getResourceId(R.styleable.MultiStatusLayout_msl_content_layout, INVALID_LAYOUT_ID)
        mLoadingLayoutId =
            ta.getResourceId(R.styleable.MultiStatusLayout_msl_loading_layout, R.layout.msl_layout_loading)
        mEmptyLayoutId = ta.getResourceId(R.styleable.MultiStatusLayout_msl_empty_layout, R.layout.msl_layout_empty)
        mErrorLayoutId = ta.getResourceId(R.styleable.MultiStatusLayout_msl_error_layout, R.layout.msl_layout_error)
        mDefaultStatus = ta.getInt(R.styleable.MultiStatusLayout_msl_default_state, STATE_CONTENT)
        ta.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (mContentLayoutId != INVALID_LAYOUT_ID) {
            removeAllViews()
        } else {
            if (childCount > 1) {
                throw IllegalStateException("The number of child Views must be less than 1")
            }
            if (childCount == 1) {
                val contentView = getChildAt(0)
                mViews.put(STATE_CONTENT, contentView)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        var contentView = mViews[STATE_CONTENT]
        if (contentView == null) {
            if (mContentLayoutId != INVALID_LAYOUT_ID) {
                val inflateView = mInflater.inflate(mContentLayoutId, this, false).also {
                    mViews[STATE_CONTENT] = it
                }
                mViewCreatedListener?.onViewCreated(STATE_CONTENT, inflateView)
                contentView = inflateView
                addView(contentView, mLayoutParams)
            }
        }
        showViewByStatus(mDefaultStatus)
    }

    fun showContent() {
        if (mShowStatus == STATE_CONTENT) {
            return
        }
        val oldViewStatus = mShowStatus
        removeOldViews()
        mShowStatus = STATE_CONTENT

        var contentView = mViews[STATE_CONTENT]
        if (contentView == null) {
            if (mContentLayoutId != INVALID_LAYOUT_ID) {
                val inflateView = mInflater.inflate(mContentLayoutId, this, false).also {
                    mViews[STATE_CONTENT] = it
                }
                mViewCreatedListener?.onViewCreated(STATE_CONTENT, inflateView)
                contentView = inflateView
                addView(contentView, mLayoutParams)
            }
        }
        contentView?.visibility = VISIBLE

        mViewStatusChangeListener?.onStatusChange(
            oldViewStatus, mViews[oldViewStatus], STATE_CONTENT, mViews[STATE_CONTENT]
        )
    }

    fun showLoading() {
        if (mShowStatus == STATE_LOADING) {
            return
        }
        val oldViewStatus = mShowStatus
        removeOldViews()
        mShowStatus = STATE_LOADING

        if (mLoadingLayoutId == INVALID_LAYOUT_ID) {
            mLoadingLayoutId = R.layout.msl_layout_loading
        }

        var loadingView = mViews[STATE_LOADING]
        if (loadingView == null) {
            val inflateView = mInflater.inflate(mLoadingLayoutId, this, false).also {
                mViews[STATE_LOADING] = it
            }
            mViewCreatedListener?.onViewCreated(STATE_LOADING, inflateView)
            loadingView = inflateView
        }
        loadingView?.let {
            addView(it, mLayoutParams)
        }
        mViewStatusChangeListener?.onStatusChange(
            oldViewStatus, mViews[oldViewStatus], STATE_LOADING, loadingView
        )
    }

    fun showEmpty() {
        if (mShowStatus == STATE_EMPTY) {
            return
        }
        val oldViewStatus = mShowStatus
        removeOldViews()
        mShowStatus = STATE_EMPTY

        if (mEmptyLayoutId == INVALID_LAYOUT_ID) {
            mEmptyLayoutId = R.layout.msl_layout_empty
        }

        var emptyView = mViews[STATE_EMPTY]
        if (emptyView == null) {
            val inflateView = mInflater.inflate(mEmptyLayoutId, this, false).also {
                mViews[STATE_EMPTY] = it
            }
            mViewCreatedListener?.onViewCreated(STATE_EMPTY, inflateView)
            emptyView = inflateView
        }
        emptyView?.let {
            addView(it, mLayoutParams)
        }
        mViewStatusChangeListener?.onStatusChange(
            oldViewStatus, mViews[oldViewStatus], STATE_EMPTY, emptyView
        )
    }

    fun showError() {
        if (mShowStatus == STATE_ERROR) {
            return
        }
        val oldViewStatus = mShowStatus
        removeOldViews()
        mShowStatus = STATE_ERROR

        if (mErrorLayoutId == INVALID_LAYOUT_ID) {
            mErrorLayoutId = R.layout.msl_layout_error
        }

        var errorView = mViews[STATE_ERROR]
        if (errorView == null) {
            val inflateView = mInflater.inflate(mErrorLayoutId, this, false).also {
                mViews[STATE_ERROR] = it
            }
            mRetryClickListener?.let {
                val retryButton = inflateView.findViewById<Button>(R.id.msl_error_retry)
                retryButton?.setOnClickListener(it)
            }
            mViewCreatedListener?.onViewCreated(STATE_EMPTY, inflateView)
            errorView = inflateView
        }
        errorView?.let {
            addView(it, mLayoutParams)
        }
        mViewStatusChangeListener?.onStatusChange(
            oldViewStatus, mViews[oldViewStatus], STATE_ERROR, errorView
        )
    }

    fun showViewByStatus(status: Int) {
        if (mShowStatus == status) {
            return
        }
        when (status) {
            STATE_CONTENT -> showContent()
            STATE_LOADING -> showLoading()
            STATE_EMPTY -> showEmpty()
            STATE_ERROR -> showError()
            else -> {
                if (mViews.containsKey(status)) {
                    val oldViewStatus = mShowStatus
                    removeOldViews()
                    mShowStatus = status
                    val showView = mViews[status]
                    if (showView != null) {
                        addView(showView, mLayoutParams)
                    }
                    mViewStatusChangeListener?.onStatusChange(
                        oldViewStatus, mViews[oldViewStatus], status, showView
                    )
                }
            }
        }
    }

    fun setRetryClickListener(listener: OnClickListener?) {
        mRetryClickListener = listener
        val errorView = mViews[STATE_ERROR]
        errorView?.let {
            val retryButton = it.findViewById<Button>(R.id.msl_error_retry)
            retryButton?.setOnClickListener(listener)
        }
    }

    fun setOnViewStatusChangeListener(listener: OnViewStatusChangeListener?) {
        mViewStatusChangeListener = listener
    }

    fun setOnViewCreateListener(listener: OnViewCreatedListener?) {
        mViewCreatedListener = listener
        val curView = mViews[mShowStatus]
        curView?.let {
            mViewCreatedListener?.onViewCreated(mShowStatus, it)
        }
    }

    fun setViewByStatus(status: Int, view: View) {
        mViews.put(status, view)
        if (mShowStatus == status) {
            removeOldViews()
            showViewByStatus(status)
        }
    }

    fun getViewStatus(): Int {
        return mShowStatus
    }

    private fun removeOldViews() {
        val contentView = mViews[STATE_CONTENT]
        contentView?.visibility = View.GONE
        if (mShowStatus != STATE_CONTENT) {
            removeView(mViews[mShowStatus])
        }
    }
}