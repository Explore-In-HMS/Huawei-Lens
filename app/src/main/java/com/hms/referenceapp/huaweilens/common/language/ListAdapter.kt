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

package com.hms.referenceapp.huaweilens.common.language

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hms.referenceapp.huaweilens.R

class ListAdapter(private val list: List<Language>) :
    RecyclerView.Adapter<ListAdapter.LanguageViewHolder>() {
    var clickListener: ClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {

        val inflater = LayoutInflater.from(parent.context)
            .inflate(R.layout.listview_language_item, parent, false)
        return LanguageViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language: Language = list[position]
        holder.bind(language)
    }

    fun setOnItemClickListener(clickListener: ClickListener) {
        this.clickListener = clickListener
    }

    override fun getItemCount(): Int = list.size

    fun getItem(position: Int): Language {
        return list[position]
    }

    inner class LanguageViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private var mTitleView: TextView? = null
        private var mYearView: TextView? = null
        private var imageView: ImageView? = null
        private var isExist: Boolean? = null
        private var isoCode: String? = null


        init {
            mTitleView = v.findViewById(R.id.list_title)
            imageView = v.findViewById(R.id.imagebutton_delete)
            if (clickListener != null) {
                itemView.setOnClickListener(this)
            }
        }

        fun bind(movie: Language) {
            mTitleView?.text = movie.title
            isoCode = movie.isoCode
            isExist = movie.isExist
            if (isExist != null) {

                if (isExist!!) {
                    imageView!!.setBackgroundResource(R.drawable.delete)
                } else {
                    if (isoCode.equals("en")){
                        imageView!!.setBackgroundResource(android.R.color.transparent)

                    } else{
                        imageView!!.setBackgroundResource(R.drawable.download)
                    }

                }
            }
        }


        override fun onClick(p0: View?) {
            if (p0 != null) {
                clickListener?.onItemClick(p0, adapterPosition)
            }
        }

    }

    interface ClickListener {
        fun onItemClick(v: View, position: Int)
    }
}




