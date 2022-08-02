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

package com.naivor.android.adapter.holder

import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.naivor.android.adapter.RecyclerAdapter
import com.naivor.android.adapter.data.ItemData

abstract class ViewHolderFactory<T> {

    val DEFAULT_TYPE = 0

    open fun produceItem(data: T): ItemData<T> {
        return ItemData(getViewType(data), data)
    }

    abstract fun produceViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder<T>
    open fun produceHeaderHolder(parent: ViewGroup, header: Any): HeaderHolder {
        return object : HeaderHolder(ViewBinding { parent }) {
            override fun bindData(data: Any) {
            }

            override fun reset() {
            }
        }
    }

    open fun produceFooterHolder(parent: ViewGroup, footer: Any): FooterHolder {
        return object : FooterHolder(ViewBinding { parent }) {
            override fun bindData(data: Any) {
            }

            override fun reset() {
            }
        }
    }

    open fun getViewType(data: T): Int {
        return DEFAULT_TYPE
    }

    /**
     * holder顶层封装
     */
    abstract inner class HeaderHolder(binding: ViewBinding) : RecyclerAdapter.ViewHolder<T>(binding) {

        @Suppress("UNCHECKED_CAST")
        abstract fun bindData(data: Any)

        inline fun <reified H> isHeader(value: Any) = value is H
    }

    /**
     * holder顶层封装
     */
    abstract inner class FooterHolder(binding: ViewBinding) : RecyclerAdapter.ViewHolder<T>(binding) {

        @Suppress("UNCHECKED_CAST")
        abstract fun bindData(data: Any)

        inline fun <reified F> isFooter(value: Any) = value is F
    }
}