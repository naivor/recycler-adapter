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

package com.naivor.android.adapter.diff

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import com.naivor.android.adapter.data.FootData
import com.naivor.android.adapter.data.HeadData
import com.naivor.android.adapter.data.ItemData

/**
 * 增量更新
 */
open class DiffItemsCallback<T> : DiffUtil.ItemCallback<ItemData<T>>() {

    override fun areItemsTheSame(oldItem: ItemData<T>, newItem: ItemData<T>): Boolean {
        return oldItem.type == newItem.type
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ItemData<T>, newItem: ItemData<T>): Boolean {
        return if (oldItem.javaClass == newItem.javaClass) {
            when (oldItem) {
                is HeadData -> oldItem.header() == (newItem as HeadData).header()
                is FootData -> oldItem.footer() == (newItem as FootData).footer()
                else -> oldItem.data() == newItem.data()
            }
        } else false
    }
}