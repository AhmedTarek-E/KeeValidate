package com.baianat.app.makfy.moltenadapter

interface OnItemLongClickListener<T> {
    fun onItemLongClick(position: Int, item: T)
}