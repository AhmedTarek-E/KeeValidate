package com.baianat.app.makfy.moltenadapter

import android.support.v7.widget.RecyclerView
import kotlin.math.ceil

class MoltenPaging private constructor(recyclerView: RecyclerView){
    private var limit  = -1
    private var trigger = -1

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

        }
    }

    init {
        recyclerView.addOnScrollListener(onScrollListener)
    }

    companion object {
        fun with(recyclerView: RecyclerView): MoltenPaging {
            return MoltenPaging(recyclerView)
        }
    }

    fun setPageLimit(limit: Int) {
        this.limit = limit
        trigger = ceil(0.6*limit).toInt()
    }

    fun setTrigger(trigger: Int) {
        this.trigger = trigger
    }
}
