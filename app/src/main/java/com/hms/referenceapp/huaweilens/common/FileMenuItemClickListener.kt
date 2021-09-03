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

package com.hms.referenceapp.huaweilens.common


import android.view.MenuItem
import androidx.appcompat.widget.PopupMenu
import com.hms.referenceapp.huaweilens.R
import java.io.File


class FileMenuItemClickListener(var position: Int,var file:File) : PopupMenu.OnMenuItemClickListener {

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item != null) {
            when (item.itemId) {
                R.id.rename -> click(0)
                R.id.delete -> click(1)
                R.id.share -> click(2)
                else -> {
                     return  false
                }
            }
        }
        return true
    }

    private fun click(button:Int){
        when (button) {
            0 -> {
                FileActivity.renameFile(position,file,file.extension)
            }
            1 -> {
                file.delete()
                FileActivity.updateView(position)
            }
            2 -> {
                FileActivity.share(file)
            }
        }
    }
}
