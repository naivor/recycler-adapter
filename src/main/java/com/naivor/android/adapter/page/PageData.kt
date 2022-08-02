/*
 * Copyright (c) 2022-2022. Naivor. All rights reserved.
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

/**
 * 分页数据
 */
open class PagedData<T>(
    var pageIndex: Int = 0,         //当前页索引
    var pageSize: Int = 10,         //每页数据 条数
    var totalPage: Int? = null,     //总页数
    var totalSize: Int? = null,     //总的数据 条数
    var datas: List<T> = emptyList()
) {


    fun nextIndex(): Int {
        return ++pageIndex
    }

    open fun hasNext(): Boolean {
        if (totalPage == null && totalSize == null) {
            throw IllegalStateException("no pageTotal defined or no totalSize defined")
        }

        if (pageSize <= 0) {
            throw IllegalArgumentException("pageSize must more than zero")
        }

        if (pageIndex < 0) {
            throw IllegalStateException("index can't less than zero")
        }

        val total = totalPage ?: (totalSize?.div(pageSize))!!

        return pageIndex < total
    }


    override fun toString(): String {
        return "PagedData(index=$pageIndex, pageSize=$pageSize, pageTotal=$totalPage, totalSize=$totalSize, datas=$datas)"
    }
}