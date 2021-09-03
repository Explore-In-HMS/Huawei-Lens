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

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.common.RecyclerViewAdapter.MyViewHolder
import java.io.File


class RecyclerViewAdapter(private val mContext: Context, private val mData: List<FileModel>) :
    RecyclerView.Adapter<MyViewHolder>() {


    companion object {
        var updateController = 0
        var viewController: Boolean = true
        var viewCounter: Int = 0
        var counter: Int = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View
        val mInflater = LayoutInflater.from(mContext)
        view = mInflater.inflate(R.layout.cardview_item_file, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvBookTitle.text = mData[position].title
        holder.imgBookThumbnail.setImageBitmap(mData[position].thumbnail)

        if (mData[position].selected == true) {
            val cb: CheckBox = holder.layout.findViewById<CheckBox>(R.id.file_checkbox)
            val cl: ConstraintLayout =
                holder.layout.findViewById<ConstraintLayout>(R.id.card_view_layout)
            cl.background =
                ContextCompat.getDrawable(mContext, R.drawable.file_activity_frame)
            cb.visibility = View.VISIBLE
        } else {
            val cb: CheckBox = holder.layout.findViewById<CheckBox>(R.id.file_checkbox)
            val cl: ConstraintLayout =
                holder.layout.findViewById<ConstraintLayout>(R.id.card_view_layout)
            val transparentDrawable: Drawable = ColorDrawable(Color.TRANSPARENT)

            cl.background = transparentDrawable

            cb.visibility = View.GONE

        }

        for (element in mData) {
            if (element.selected == true) {
                viewController = false
                FileActivity.mymenu?.setGroupVisible(R.id.tool_items, true)
            }
        }
        //select all
        if (updateController == 1) {
            counter++
            val cb: CheckBox = holder.layout.findViewById<CheckBox>(R.id.file_checkbox)
            val cl: ConstraintLayout =
                holder.layout.findViewById<ConstraintLayout>(R.id.card_view_layout)

            cl.background =
                ContextCompat.getDrawable(mContext, R.drawable.file_activity_frame)
            cb.visibility = View.VISIBLE
            mData[position].selected = true
            viewController = false

            if (counter == mData.size) {
                updateController = 0
                counter = 0
            }
        }
        //undoAll
        else if (updateController == 2) {
            counter++
            if (!viewController) {
                viewController = true
                FileActivity.mymenu?.setGroupVisible(R.id.tool_items, false)
            }
            if (mData[position].selected == true) {
                val cb: CheckBox = holder.layout.findViewById<CheckBox>(R.id.file_checkbox)
                val cl: ConstraintLayout =
                    holder.layout.findViewById<ConstraintLayout>(R.id.card_view_layout)
                val transparentDrawable: Drawable = ColorDrawable(Color.TRANSPARENT)

                cl.background = transparentDrawable

                cb.visibility = View.GONE
                mData[position].selected = false

            }

            if (counter == mData.size || counter == viewCounter) {
                updateController = 0
                viewCounter = 0
                counter = 0
            }
        } else if (updateController == 3) {
            counter++
            val cb: CheckBox = holder.layout.findViewById<CheckBox>(R.id.file_checkbox)
            val cl: ConstraintLayout =
                holder.layout.findViewById<ConstraintLayout>(R.id.card_view_layout)
            val transparentDrawable: Drawable = ColorDrawable(Color.TRANSPARENT)

            cl.background = transparentDrawable

            cb.visibility = View.GONE
            mData[position].selected = false
            FileActivity.mymenu?.setGroupVisible(R.id.tool_items, false)
            viewController = true
            if (counter == mData.size || counter == viewCounter) {
                updateController = 0
                viewCounter = 0
                counter = 0
            }
        }


        //to open file
        holder.imgBookThumbnail.setOnClickListener {

            if (viewController) {
                when (mData[position].file.extension) {
                    "pdf" -> {
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.type = "application/pdf"
                        val uri = mData[position].file.let { it1 ->
                            FileProvider.getUriForFile(
                                mContext,
                                mContext.resources.getString(R.string.package_name),
                                it1
                            )
                        }
                        intent.data = uri
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        mContext.startActivity(Intent.createChooser(intent, "Pdf File"))
                    }
                    "txt" -> {
                        //open txt file
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.type = "text/plain"
                        val uri = mData[position].file.let { it1 ->
                            FileProvider.getUriForFile(
                                mContext,
                                mContext.resources.getString(R.string.package_name),
                                it1
                            )
                        }
                        intent.data = uri
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        mContext.startActivity(Intent.createChooser(intent, "Txt File"))
                    }
                    "docx" -> {
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.type =
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        val uri = mData[position].file.let { it1 ->
                            FileProvider.getUriForFile(
                                mContext,
                                mContext.resources.getString(R.string.package_name),
                                it1
                            )
                        }
                        intent.data = uri
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        mContext.startActivity(Intent.createChooser(intent, "Word File"))
                    }
                    "xlsx" -> {
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.type =
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        val uri = mData[position].file.let { it1 ->
                            FileProvider.getUriForFile(
                                mContext,
                                mContext.resources.getString(R.string.package_name),
                                it1
                            )
                        }
                        intent.data = uri
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        mContext.startActivity(Intent.createChooser(intent, "Excel File"))
                    }
                }

            } else {

                if (mData[position].selected == true) {
                    val cb: CheckBox = holder.layout.findViewById<CheckBox>(R.id.file_checkbox)
                    val cl: ConstraintLayout =
                        holder.layout.findViewById<ConstraintLayout>(R.id.card_view_layout)
                    val transparentDrawable: Drawable = ColorDrawable(Color.TRANSPARENT)

                    cl.background = transparentDrawable

                    cb.visibility = View.GONE
                    viewCounter--
                    mData[position].selected = false
                    notifyDataSetChanged()

                    if (viewCounter == 0) {
                        FileActivity.mymenu?.setGroupVisible(R.id.tool_items, false)
                        viewController = true
                    }

                } else {
                    //secili degil yani secicez counter artirilcak
                    val cb: CheckBox = holder.layout.findViewById<CheckBox>(R.id.file_checkbox)
                    val cl: ConstraintLayout =
                        holder.layout.findViewById<ConstraintLayout>(R.id.card_view_layout)

                    cl.background =
                        ContextCompat.getDrawable(it.context, R.drawable.file_activity_frame)
                    cb.visibility = View.VISIBLE
                    viewCounter++
                    mData[position].selected = true
                    notifyDataSetChanged()

                }


            }
        }
        holder.cardView.setOnClickListener {

            if (!viewController) {

                if (mData[position].selected == true) {

                    val cb: CheckBox = holder.layout.findViewById<CheckBox>(R.id.file_checkbox)
                    val cl: ConstraintLayout =
                        holder.layout.findViewById<ConstraintLayout>(R.id.card_view_layout)
                    val transparentDrawable: Drawable = ColorDrawable(Color.TRANSPARENT)

                    cl.background = transparentDrawable

                    cb.visibility = View.GONE
                    viewCounter--
                    mData[position].selected = false
                    notifyDataSetChanged()

                    if (viewCounter == 0) {
                        FileActivity.mymenu?.setGroupVisible(R.id.tool_items, false)
                        viewController = true
                    }

                } else {

                    val cb: CheckBox = holder.layout.findViewById<CheckBox>(R.id.file_checkbox)
                    val cl: ConstraintLayout =
                        holder.layout.findViewById<ConstraintLayout>(R.id.card_view_layout)

                    cl.background =
                        ContextCompat.getDrawable(it.context, R.drawable.file_activity_frame)
                    cb.visibility = View.VISIBLE
                    viewCounter++
                    mData[position].selected = true
                    notifyDataSetChanged()
                }
            }
        }




        holder.imgBookThumbnail.setOnLongClickListener {

            if (viewController) {
                val cb: CheckBox = holder.layout.findViewById<CheckBox>(R.id.file_checkbox)
                val cl: ConstraintLayout =
                    holder.layout.findViewById<ConstraintLayout>(R.id.card_view_layout)

                cl.background =
                    ContextCompat.getDrawable(it.context, R.drawable.file_activity_frame)
                cb.visibility = View.VISIBLE
                viewController = false
                viewCounter++
                mData[position].selected = true
                notifyDataSetChanged()
                FileActivity.mymenu?.setGroupVisible(R.id.tool_items, true)
            }
            return@setOnLongClickListener true
        }
        holder.cardView.setOnLongClickListener {
            if (viewController) {
                val cb: CheckBox = it.findViewById<CheckBox>(R.id.file_checkbox)
                val cl: ConstraintLayout = it.findViewById<ConstraintLayout>(R.id.card_view_layout)
                cl.background =
                    ContextCompat.getDrawable(it.context, R.drawable.file_activity_frame)
                cb.visibility = View.VISIBLE
                viewController = false
                viewCounter++
                mData[position].selected = true
                notifyDataSetChanged()
                FileActivity.mymenu?.setGroupVisible(R.id.tool_items, true)

            }
            return@setOnLongClickListener true
        }



        holder.threedot.setOnClickListener {
            if (viewController) {
                showPopupMenu(holder.threedot, position, mData[position].file)
            }
        }
    }

    private fun showPopupMenu(view: View, position: Int, file: File) {
        val popup = PopupMenu(view.context, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.file_activity_menu, popup.menu)
        popup.setOnMenuItemClickListener(FileMenuItemClickListener(position, file))
        popup.show()
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvBookTitle: TextView = itemView.findViewById<View>(R.id.book_title_id) as TextView
        var imgBookThumbnail: ImageView = itemView.findViewById<View>(R.id.book_img_id) as ImageView
        var cardView: CardView = itemView.findViewById<View>(R.id.cardview_id) as CardView
        var threedot: ImageView = itemView.findViewById(R.id.threedot)
        var layout: ConstraintLayout =
            itemView.findViewById(R.id.card_view_layout) as ConstraintLayout
    }
}