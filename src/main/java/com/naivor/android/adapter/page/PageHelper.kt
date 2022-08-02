/*
 * Copyright (c) 2022. Naivor. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.naivor.android.adapter.page

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.naivor.android.adapter.RecyclerAdapter

/**
 * 分页加载的辅助
 */
open class PageHelper<T>(val callback: OnPagedCallback, val refresh: SwipeRefreshLayout? = null) {

    lateinit var adapter: RecyclerAdapter<T>

    var pageData: PagedData<T> = PagedData<T>()

    var isLoading = false

    var loadNextMarginLastItems = 2
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("load next when position margin last item can't less then zero")
            } else {
                field = value
            }
        }

    init {
        refresh?.setOnRefreshListener {
            refresh()
        }
    }

    open fun submitPage(page: PagedData<T>) {
        pageData = page
        isLoading = false
        adapter.addItems(pageData.datas)
        callback.loadComplete()
        refresh?.isRefreshing = false
    }

    open fun loadNext() {
        if (pageData.hasNext()) {
            if (!isLoading) {
                callback.loadPageData(pageData.nextIndex())
            }
        } else {
            callback.onPageEnd()
        }
    }

    open fun reset() {
        adapter.clearItems()
        pageData.pageIndex = 0
    }

    open fun refresh() {
        adapter.items.clear()
        pageData.pageIndex = 0
        if (!isLoading) {
            callback.loadPageData(pageData.pageIndex)
        }
    }

    abstract class OnPagedCallback {

        abstract fun loadPageData(index: Int)

        fun onPageEnd() {}

        fun loadComplete() {}
    }
}
