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

package com.naivor.android.adapter.select

import java.util.*

abstract class SelectionObservable<T>(var selections: HashSet<T>? = null, var multiSelection: Boolean = false) :
    Observable() {

    var selectedPosition = -1

    override fun notifyObservers(arg: Any?) {
        setChanged()
        super.notifyObservers(arg)
    }

    abstract fun getItems(): List<T>

    open fun getSelectedPositions(): List<Int> {
        val items = getItems()
        val list = selections?.map { items.indexOf(it) }?.toList()
        return if (list.isNullOrEmpty()) {
            if (selectedPosition in items.indices) {
                listOf(selectedPosition)
            } else emptyList()
        } else {
            list
        }
    }

    open fun getSelectedDatas(): List<T> {
        val list = selections?.toList()

        return if (list.isNullOrEmpty()) {
            val items = getItems()
            if (selectedPosition in items.indices) {
                listOf(items[selectedPosition])
            } else emptyList()
        } else {
            list
        }
    }

    open fun isItemSelected(item: T): Boolean {
        val contains = selections?.contains(item)
        return if (contains == null) {
            val items = getItems()
            if (selectedPosition in items.indices) {
                items[selectedPosition] == item
            } else false
        } else {
            contains
        }
    }

    open fun isPositionSelected(position: Int): Boolean {

        return if (selections == null) {
            selectedPosition == position
        } else {
            val items = getItems()
            if (position in items.indices) {
                selections?.contains(items[position]) ?: false
            } else {
                false
            }
        }
    }

    //选中位置
    open fun selectPosition(position: Int) {
        val items = getItems()
        if (multiSelection) {  //多选
            selections?.run {
                if (position in items.indices) {
                    val item = items[position]
                    if (contains(item)) remove(item) else add(item)
                }
            }
        } else {  //单选
            selectedPosition = if (selectedPosition == position) -1 else position

            //selections不为空，将单选结果加入其中
            selections?.run {
                if (position in items.indices) {
                    val item = items[position]
                    if (contains(item)) clear() else {
                        clear()
                        add(item)
                    }
                }
            }
        }

        notifyObservers()
    }

    //选中item
    open fun selectedItem(item: T) {
        val items = getItems()

        if (multiSelection) {
            selections?.run {
                if (contains(item)) remove(item) else add(item)
            }
        } else {  //单选
            val position = if (items.contains(item)) items.indexOf(item) else -1
            selectedPosition = if (selectedPosition == position) -1 else position

            //selections不为空，将单选结果加入其中
            selections?.run {
                if (contains(item)) clear() else {
                    clear()
                    add(item)
                }
            }
        }

        notifyObservers()
    }
}