package com.example.keevalidate.moltenadapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.baianat.app.makfy.moltenadapter.OnItemLongClickListener
import com.baianat.app.makfy.moltenadapter.OnItemSelectedListener
import com.baianat.app.makfy.moltenadapter.ResId
import com.baianat.app.makfy.moltenadapter.ResLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties

class KotlinMoltenAdapter<T : Any>(
        @ResLayout private val layout: Int,
        private var items: MutableList<T>?,
        @ResId resIds : IntArray,
        names : Array<String>?) :
        RecyclerView.Adapter<KotlinMoltenAdapter<T>.MoltenViewHolder>() {
    //private val fields = ArrayList<Field>()

    private var resIds : IntArray = IntArray(0)
    private var names : Array<String>? = null
    private val properties by lazy { ArrayList<KProperty1<*, *>>() }
    private val functions by lazy { ArrayList<KFunction<*>>() }

    private var listener : OnItemSelectedListener<T>? = null
    private var onItemSelected: (selectedItem: T) -> Unit = {}

    private var onItemLongClickListener: OnItemLongClickListener<T>? = null
    private var onItemLongClick: (pos: Int, item: T) -> Unit = { _,_ -> }

    private var placeholder = -1
    private var animate = true
    private val TAG = "MoltenAdapter"
    //private val itemIds by lazy { ArrayList<Long>() }
    private var showProgressBar = false
    private var emptyPageResource = -1

    private var doAtBind: (Int, MoltenViewHolder) -> Unit = { _, _ ->}

    companion object {
        private const val NORMAL = 1
        private const val LOADING = 2
    }

    constructor(
            @ResLayout layout: Int,
            items: MutableList<T>?,
            modelClass: Class<T>
    ) : this(layout, items, IntArray(0), null) {

        modelClass.kotlin.declaredMemberProperties
                .filter { it.annotations.any { annotation -> annotation is BindTo } }
                .forEach {
                    properties.add(it)
                }
        modelClass.kotlin.declaredMemberFunctions
                .filter { it.annotations.any { annotation -> annotation is BindTo } }
                .forEach {
                    functions.add(it)
                }
    }

    init {
        setHasStableIds(true)
        val start = System.currentTimeMillis()
        if (this.properties.isEmpty()) {
            this.resIds = resIds
            this.names = names
        }
        Log.v("$TAG init", (System.currentTimeMillis() - start).toString())
    }

    /*private fun generateIdsForItems() {
        val no = items?.size ?: 0
        for (i in 0 until no) {
            itemIds.add(getItemId())
        }
    }

    companion object {
        private var idCount = 0L

        private fun getItemId() : Long {
            return idCount++
        }
    }*/

    fun setOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener<T>) {
        this.onItemLongClickListener = onItemLongClickListener
    }

    fun setOnItemLongClickListener(onItemLongClickListener: (position:Int, item:T) -> Unit) {
        onItemLongClick = onItemLongClickListener
    }

    fun removeOnItemLongClickListener() {
        onItemLongClickListener = null
        onItemLongClick = { _,_ -> }
    }

    private fun getProgressViewGroup(context: Context) : ViewGroup {
        // progressBar
        val progressBar = ProgressBar(context)
        progressBar.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // ---------------------------------------------------
        // imageView
        val imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.FIT_XY

        val imageParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
        imageParams.addRule(RelativeLayout.CENTER_IN_PARENT)

        imageView.layoutParams = imageParams

        // ---------------------------------------------------
        // viewGroup
        val viewGroup = RelativeLayout(context)
        viewGroup.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )

        return viewGroup
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoltenViewHolder {
        return when (viewType) {
            /*LOADING -> {
                ProgressViewHolder(getProgressViewGroup(parent.context))
            }*/

            NORMAL -> {
                MoltenViewHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(layout, parent, false)
                )
            }

            else -> {
                MoltenViewHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(layout, parent, false)
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        /*return if (items?.isEmpty() != false && showProgressBar) {
            LOADING
        } else {
            NORMAL
        }*/
        return super.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        /*return if (items?.isEmpty() != false) {
            if (showProgressBar) {
                1
            } else {
                0
            }
        } else {
            items!!.size
        }*/
        return items?.size ?: 0
    }

    fun enableProgressBarWhenEmpty() {
        showProgressBar = true
    }

    fun disableProgressBar() {
        showProgressBar = false
        notifyDataSetChanged()
    }

    fun enableProgressBarWithEmptyPage(resDrawable: Int) {
        showProgressBar = true
        emptyPageResource = resDrawable
    }

    fun removeEmptyPage() {
        emptyPageResource = -1
        notifyDataSetChanged()
    }

    fun addItem(t: T) {
        if (items == null) { items = ArrayList() }
        items?.add(t)
        notifyItemInserted(items!!.size-1)
    }

    fun removeItem(position: Int) {
        items?.removeAt(position)
        notifyItemRemoved(position)
    }

    fun doAtBind(doAtBind: (Int, MoltenViewHolder) -> Unit) {
        this.doAtBind = doAtBind
    }

    fun setItems(newItems: MutableList<T>) {
        if (items == null) {
            items = newItems
        } else {
            clearItems()
            items?.addAll(newItems)
        }

        notifyItemRangeInserted(0, newItems.size)
    }

    fun addItems(newItems: MutableList<T>) {
        if (items == null) { items = ArrayList() }
        items?.addAll(newItems)
        val noOfNewItems = newItems.size
        notifyItemRangeInserted(items!!.size - noOfNewItems, noOfNewItems)
    }

    fun clearItems() {
        items?.clear()
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: MoltenViewHolder, position: Int) {
        /*when (getItemViewType(position)) {
            LOADING -> {
                if (emptyPageResource != -1) {
                    holder.emptyImageView?.let {
                        Glide.with(it.context)

                                .load(emptyPageResource)
                                .into(it)
                    }
                }
            }

            NORMAL -> {
                bindItems(holder, position)
            }

            else -> {
                bindItems(holder, position)
            }
        }*/

        bindItems(holder, position)
        doAtBind(position, holder)
    }

    private fun bindItems(holder: MoltenViewHolder, position: Int) {
        val start = System.currentTimeMillis()
        if (resIds.isEmpty()) {
            for (property in properties) {
                val view = holder.viewsMap[property.name]
                bindValueFromProperty(view, property.name, position)
            }

            for (function in functions) {
                val view = holder.viewsMap[function.name]
                bindValueFromFunction(view, function.name, position)
            }
        } else {
            for (i : Int in 0..(resIds.size-1)) {
                val item = names?.get(i)
                val view = holder.viewsMap[item]
                item?.let {
                    bindValueFromProperty(view, it, position)
                    bindValueFromFunction(view, it, position)
                }

            }
        }

        Log.v("$TAG onBind", (System.currentTimeMillis() - start).toString())
    }

    private fun bindValueFromProperty(view : View?, propertyName: String, position: Int) {
        val model = items?.get(position)
        val value = model?.let { readProperty(it, propertyName) }
        bindThis(view, value)
    }

    private fun bindValueFromFunction(view: View?, functionName: String, position: Int) {
        val model = items?.get(position)
        val value = model?.let { readFunction(it, functionName) }
        bindThis(view, value)
    }

    private fun bindThis(view: View?, value: Any?) {
        when (view) {
            is TextView -> {
                view.text = getTextValue(value)
            }

            is RatingBar -> {
                view.rating = value?.toString()?.toFloat() ?: 0f
            }

            is ImageView -> {
                val image = when (value) {
                    is String -> {
                        value
                    }

                    is Int -> {
                        value
                    }

                    else -> {
                        -1
                    }
                }

                var requestOptions = if (placeholder != -1) {
                    RequestOptions.placeholderOf(placeholder)
                } else {
                    RequestOptions()
                }

                if (!animate) {
                    requestOptions = requestOptions.dontAnimate()
                }

                Glide.with(view.context)
                        .load(image)
                        .apply(requestOptions)
                        .into(view)

            }
        }
    }

    private fun getTextValue(value: Any?) : String {
        return when(value) {
            is String -> {
                value
            }
            is Int -> {
                value.toString()
            }
            is Float -> {
                value.toString()
            }
            is Long -> {
                value.toString()
            }
            is Double -> {
                value.toString()
            }
            else -> {
                ""
            }
        }
    }

    private fun readProperty(instance: T, propertyName: String): Any? {
        val clazz = instance.javaClass.kotlin

        return try {
            clazz.declaredMemberProperties.firstOrNull { it.name == propertyName }?.get(instance)
        } catch (e: InvocationTargetException) {
            Log.e("Molten", e.targetException.message, e.targetException)
            null
        }
        //return clazz.declaredMemberFunctions.first { it.name == propertyName }.call(instance)
    }

    private fun readFunction(instance: T, functionName: String): Any? {
        val clazz = instance.javaClass.kotlin
        return try {
            clazz.declaredMemberFunctions.firstOrNull { it.name == functionName }?.call(instance)
        } catch (e: InvocationTargetException) {
            Log.e("Molten", e.targetException.message, e.targetException)
            null
        }
    }

    fun setOnItemSelectedListener(listener: OnItemSelectedListener<T>) {
        this.listener = listener
    }

    fun setOnItemSelectedListener(onItemSelected: (selectedItem: T) -> Unit ) {
        this.onItemSelected = onItemSelected
    }

    fun removeOnItemSelectedListener() {
        listener = null
    }

    fun dontAnimateGlide() {
        animate = false
    }

    fun animateGlide() {
        animate = true
    }

    fun setPlaceholderForImageView(resId: Int) {
        placeholder = resId
    }

    /*inner class ProgressViewHolder(itemView: ViewGroup) : RecyclerView.ViewHolder(itemView) {
        var progressBar: ProgressBar? = null
        var emptyPageView: ImageView? = null

        init {
            for (element in 0 until itemView.childCount) {
                val view = itemView.getChildAt(element)
                when (view) {
                    is ProgressBar -> {
                        progressBar = view
                    }

                    is ImageView -> {
                        emptyPageView = view
                    }
                }
            }
        }

        private fun showProgressBar() {
            progressBar?.visibility = View.VISIBLE
        }

        private fun hideProgressBar() {
            progressBar?.visibility = View.GONE
            emptyPageView?.let {
                Glide.with(it.context)
                        .load(emptyPageResource)
                        .into(it)
            }
        }
    }*/

    inner class MoltenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener, View.OnLongClickListener {
        //var emptyImageView: ImageView? = null
        val viewsMap = HashMap<String, View>()

        init {
            prepareViewHolder()
        }

        private fun prepareViewHolder() {
            val start = System.currentTimeMillis()
            if (resIds.isEmpty()) {
                for (property in properties) {
                    var resId = -1
                    property.annotations.filter { it is BindTo }
                            .forEach {
                                if (it is BindTo) {
                                    resId = it.resId
                                }
                            }

                    val v: View? = itemView?.findViewById(resId)

                    v?.let {
                        viewsMap[property.name] = it
                    }
                }

                for (function in functions) {
                    var resId = -1
                    function.annotations.filter { it is BindTo }
                            .forEach {
                                if (it is BindTo) {
                                    resId = it.resId
                                }
                            }

                    val v: View? = itemView?.findViewById(resId)

                    v?.let {
                        viewsMap[function.name] = it
                    }
                }
            } else {
                for (i : Int in 0..(resIds.size - 1)) {
                    val resId = resIds[i]

                    val v: View? = itemView?.findViewById(resId)
                    v?.let {
                        viewsMap[names!![i]] = it
                    }

                }
            }
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            Log.v("$TAG Holder", (System.currentTimeMillis() - start).toString())
        }

        override fun onClick(v: View?) {
            items?.get(adapterPosition)?.let {
                listener?.onItemSelected(it) ?: onItemSelected(it)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            items?.get(adapterPosition)?.let {
                onItemLongClickListener?.onItemLongClick(adapterPosition, it)
                onItemLongClick.invoke(adapterPosition, it)
                return true
            }
            return false
        }
    }
}

