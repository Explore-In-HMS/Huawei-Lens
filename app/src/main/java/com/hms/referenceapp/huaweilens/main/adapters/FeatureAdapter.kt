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

package com.hms.referenceapp.huaweilens.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.main.interfaces.FeatureClickCallback
import kotlinx.android.synthetic.main.item_feature_cell.view.*

class FeatureAdapter(
    private val context: Context,
    private val items: List<String>,
    private val callback: FeatureClickCallback
) : RecyclerView.Adapter<FeatureAdapter.FeatureViewHolder>() {

    private var selectedIndex: Int = 0
    private var selectedView: TextView? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val inflater = LayoutInflater.from(context)
        val itemView = inflater.inflate(R.layout.item_feature_cell, null, false)
        return FeatureViewHolder(
            itemView
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        holder.textFeatureCell.text = items[position]
        if (selectedIndex == position) {
            holder.textFeatureCell.background =
                ContextCompat.getDrawable(context,
                    R.drawable.ic_feature_bg
                )
            holder.textFeatureCell.alpha = 1f
            holder.textFeatureCell.setTextColor(context.getColor(R.color.white))
            selectedIndex = position
            selectedView = holder.textFeatureCell
        } else {
            holder.textFeatureCell.background =
                ContextCompat.getDrawable(context,
                    R.drawable.ic_feature_bg
                )
            holder.textFeatureCell.alpha = 0.6f
            holder.textFeatureCell.setTextColor(context.getColor(R.color.white))
        }

        holder.textFeatureCell.setOnClickListener {
            if (selectedIndex != position) {
                selectedView?.background = ContextCompat.getDrawable(context,
                    R.drawable.ic_feature_bg
                )
                selectedView?.alpha = 0.6f
                selectedView?.setTextColor(context.getColor(R.color.white))
            }
            selectedView = holder.textFeatureCell
            selectedIndex = position
            callback.onClicked(position)
        }
    }

    fun setSelectedItem(position: Int) {
        selectedIndex = position
    }

    class FeatureViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textFeatureCell: TextView = view.text_view_feature_cell
    }
}