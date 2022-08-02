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

package com.naivor.android.adapter.data

//头部
open class HeadData<T>(private val header: Any) : ItemData<T>(Int.MIN_VALUE, null) {

    open fun header() = header

    override fun data(): T? {
        throw IllegalStateException("this is a header, please use header()")
    }
}