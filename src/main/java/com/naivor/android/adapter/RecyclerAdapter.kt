package com.naivor.android.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import androidx.viewbinding.ViewBinding
import com.naivor.android.adapter.RecyclerAdapter.ViewHolder
import com.naivor.android.adapter.holder.ViewHolderFactory.FooterHolder
import com.naivor.android.adapter.holder.ViewHolderFactory.HeaderHolder
import com.naivor.android.adapter.data.FootData
import com.naivor.android.adapter.data.HeadData
import com.naivor.android.adapter.data.ItemData
import com.naivor.android.adapter.diff.DiffItemsCallback
import com.naivor.android.adapter.holder.ItemHolder
import com.naivor.android.adapter.holder.ViewHolderFactory
import com.naivor.android.adapter.page.PageHelper
import com.naivor.android.adapter.page.PagedData
import com.naivor.android.adapter.select.SelectionObservable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

/**
 * RecyclerAdapter顶层封装
 */
abstract class RecyclerAdapter<T>(
    var itemClick: OnItemClick<T>? = null,
    val selections: HashSet<T>? = null  //多选
) :
    RecyclerView.Adapter<ViewHolder<T>>() {

    abstract val factory: ViewHolderFactory<T>

    private val selectObservable = object : SelectionObservable<T>(selections) {
        override fun getItems(): List<T> {
            return items
        }
    }

    var items = mutableListOf<T>()
        get() = field
    var headers = mutableListOf<HeadData<T>>()
        get() = field
    var footers = mutableListOf<FootData<T>>()
        get() = field

    //差异比较
    val diffCallback: DiffUtil.ItemCallback<ItemData<T>> = DiffItemsCallback<T>()

    //item数据
    protected val diff = AsyncListDiffer(this, diffCallback)

    var pageHelper: PageHelper<T>? = null
        set(value) {
            field = value
            value?.adapter = this
        }

    lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val viewHolder = when (viewType) {
            in (Int.MIN_VALUE until (Int.MIN_VALUE + headers.size)) ->
                factory.produceHeaderHolder(parent, headers[viewType - Int.MIN_VALUE].header())
            in ((Int.MAX_VALUE - footers.size) until Int.MAX_VALUE) ->
                factory.produceFooterHolder(parent, footers[Int.MAX_VALUE - viewType].footer())
            else -> {
                val holder = factory.produceViewHolder(parent, viewType)
                if (holder is ItemHolder) {
                    selectObservable.addObserver(holder)
                    holder.selectObservable = selectObservable
                }
                holder
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        holder.reset()
        val itemData = diff.currentList[position]
        holder.setOnItemClick(itemClick)
        when (getItemViewType(position)) {
            in (Int.MIN_VALUE until (Int.MIN_VALUE + headers.size)) -> {
                if (itemData is HeadData && holder is HeaderHolder) {
                    bindHeaderData(itemData, holder)
                } else
                    throw IllegalStateException("this item is not a HeadItem")
            }

            in ((Int.MAX_VALUE - footers.size) until Int.MAX_VALUE) -> {
                if (itemData is FootData && holder is FooterHolder) {
                    bindFooterData(itemData, holder)
                } else
                    throw IllegalStateException("this item is not a BottomItem")
            }

            else ->
                bindItemData(itemData, holder)

        }
    }

    open fun bindItemData(
        itemData: ItemData<T>,
        holder: ViewHolder<T>
    ) {
        if (holder is ItemHolder<T>) {
            holder.selectObservable = selectObservable
            holder.bindData(itemData.data()!!)
        }
    }

    open fun bindFooterData(
        itemData: FootData<T>,
        holder: ViewHolderFactory<T>.FooterHolder
    ) {
        holder.bindData(itemData.footer())
    }

    open fun bindHeaderData(
        itemData: HeadData<T>,
        holder: ViewHolderFactory<T>.HeaderHolder
    ) {
        holder.bindData(itemData.header())
    }

    override fun getItemCount(): Int {
        return diff.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        pageHelper?.let {

            val layoutManager = recyclerView.layoutManager

            if (position >= itemCount - it.loadNextMarginLastItems && items.isNotEmpty()) {  //该加载下一页了
                //是否充满一屏
                val fulledLayout = when (layoutManager) {
                    is LinearLayoutManager -> {
                        val lastVisible = layoutManager.findLastVisibleItemPosition()
                        if (lastVisible > 0) {
                            val firstVisible = layoutManager.findFirstVisibleItemPosition()
                            lastVisible - firstVisible < itemCount
                        } else false

                    }
                    is GridLayoutManager -> {
                        val lastVisible = layoutManager.findLastVisibleItemPosition()
                        if (lastVisible > 0) {
                            val firstVisible = layoutManager.findFirstVisibleItemPosition()
                            lastVisible - firstVisible < itemCount
                        } else false

                    }
                    else -> false
                }

                if (fulledLayout)  //充满一屏才加载下一页，防止不满一屏无限加载
                    it.loadNext()
            }
        }

        itemClick?.run {
            if (this is OnRecyclerScrollListener)
                scroll(position)
        }

        val count = headers.size + items.size
        return if (position < headers.size) {   //header  Int.MIN_VALUE+position  range： Int.MIN_VALUE until (Int.MIN_VALUE+headers.size)
            diff.currentList[position].type + position
        } else if (position >= count) {      //footer  Int.MAX_VALUE  range: (Int.MAX_VALUE-footer.size) until   Int.MAX_VALUE
            diff.currentList[position].type - footers.size + position - count
        } else {                             //items   custom  defined
            diff.currentList[position].type
        }
    }

    fun selectPosition(position: Int) {
        selectObservable.selectPosition(position)
    }

    fun enableMultiSelection() {
        selectObservable.run {
            multiSelection = true
            if (selections == null) {
                selections = HashSet()
            }
        }
    }

    fun getSelectedPositions(): List<Int> {
        return selectObservable.getSelectedPositions()
    }

    fun getSelectedDatas(): List<T> {
        return selectObservable.getSelectedDatas()
    }

    open fun addItems(newData: List<T>) {
        items.addAll(newData)
        update()
    }

    open fun insertItems(newData: List<T>, position: Int) {
        val index = if (position < 0) 0 else position
        if (index >= items.size) {
            items.addAll(newData)
        } else {
            items.addAll(index, newData)
        }

        update()
    }

    open fun submitItems(newList: List<T>) {
        items = newList.toMutableList()
        update()
    }

    open fun clearItems() {
        items.clear()
        update()
    }

    open fun submitHeaders(newHeaders: List<Any>) {
        headers = newHeaders.map { value -> HeadData<T>(value) }.toMutableList()
        update()
    }

    open fun addHeader(header: Any) {
        headers.add(HeadData(header))
        update()
    }

    open fun clearHeaders() {
        headers.clear()
        update()
    }

    open fun submitFooters(newFooters: List<Any>) {
        footers = newFooters.map { value -> FootData<T>(value) }.toMutableList()
        update()
    }

    open fun addFooter(footer: Any) {
        footers.add(FootData(footer))
        update()
    }

    open fun clearFooters() {
        footers.clear()
        update()
    }

    open fun submitPage(pagedData: PagedData<T>) {
        pageHelper?.submitPage(pagedData)
        if (pageHelper == null) {
            throw IllegalArgumentException("there is no pageHelper,please provide one first!")
        }
    }

    open fun clear() {
        headers.clear()
        items.clear()
        footers.clear()
        update()
    }

    @Suppress("UNCHECKED_CAST")
    protected fun update() {
        CoroutineScope(Dispatchers.Main).launch {

            val list = flow {
                headers.forEach {
                    emit(it as ItemData<T>)
                }
                items.map {
                    factory.produceItem(it) as ItemData<T>
                }.forEach { value -> emit(value) }
                footers.forEach {
                    emit(it as ItemData<T>)
                }
            }.flowOn(Dispatchers.Default).toList()

            diff.submitList(list)
        }
    }

    /**
     * holder顶层封装
     */
    abstract class ViewHolder<T>(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

        var itemClick: OnItemClick<T>? = null

        open fun setOnItemClick(onClick: OnItemClick<T>?) {
            this.itemClick = onClick
        }

        //复用的adapter恢复默认状态
        open fun reset() {}
    }

    //关于item的点击事件
    interface OnItemClick<T> {

        fun clickItem(position: Int, view: View, data: T)
    }
}

interface OnRecyclerScrollListener {

    fun scroll(position: Int)
}

