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

import androidx.viewbinding.ViewBinding
import com.naivor.android.adapter.RecyclerAdapter
import com.naivor.android.adapter.select.SelectionObservable
import java.util.*

/**
 * holder顶层封装
 */
abstract class ItemHolder<T>(binding: ViewBinding) : RecyclerAdapter.ViewHolder<T>(binding),
    Observer {

    var selectObservable: SelectionObservable<T>? = null

    var itemData: T? = null

    open fun bindData(data: T) {
        itemView.setOnClickListener {
            itemClick?.clickItem(position, it, data)
        }

        itemData = data
        updateSelectStatus(data)
    }

    open fun ItemHolder<T>.updateSelectStatus(data: T?) {
        selectObservable?.run {
            if (multiSelection) {   //多选判断
                selections?.run {
                    itemView.isActivated = contains(data)
                }
            } else {  //单选判断
                itemView.isActivated = position == selectedPosition
            }
        }
    }

    override fun update(o: Observable?, arg: Any?) {
        updateSelectStatus(itemData)
    }

    //选中item
    open fun selectedItem(item: T) {
        selectObservable?.selectedItem(item)
    }
}