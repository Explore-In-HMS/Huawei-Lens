/*
 * Copyright 2020. Explore in HMS. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.hms.referenceapp.huaweilens.fr.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TableBody {
    @SerializedName("startRow")
    @Expose
    var startRow: Int? = null

    @SerializedName("endRow")
    @Expose
    var endRow: Int? = null

    @SerializedName("startCol")
    @Expose
    var startCol: Int? = null

    @SerializedName("endCol")
    @Expose
    var endCol: Int? = null

    @SerializedName("cellCoordinate")
    @Expose
    var cellCoordinate: CellCoordinate? = null

    @SerializedName("textInfo")
    @Expose
    var textInfo: String? = null
}