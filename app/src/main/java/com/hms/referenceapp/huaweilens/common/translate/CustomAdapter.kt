package com.hms.referenceapp.huaweilens.common.translate

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.hms.referenceapp.huaweilens.R


class CustomAdapter(context: Context, @LayoutRes private val layoutRes: Int, private var languageList: MutableList<Language>)
    : ArrayAdapter<Language>(context, layoutRes, languageList) {

    private val mContext = context

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var mViewHolder = ViewHolder()
        var mConvertView: View? = convertView
        val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if (mConvertView == null) {
            mConvertView = inflater.inflate(R.layout.custom_language_spinner_row, parent, false)
            mViewHolder.isDownloaded = mConvertView!!.findViewById(R.id.isDownloaded) as ImageView
            mViewHolder.mName = mConvertView.findViewById(R.id.languageName) as CheckedTextView
            mConvertView.tag = mViewHolder
        } else {
            mViewHolder = convertView!!.tag as ViewHolder
        }
        mViewHolder.isDownloaded!!.setImageResource(if(languageList[position].isDownloaded) {
            0
        } else {
            R.drawable.ic_download_white_24dp
        })
        mViewHolder.mName?.text = languageList[position].name

        return mConvertView
    }



    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }

    override fun getCount(): Int {
        return languageList.size
    }


}

private class ViewHolder {
    var isDownloaded: ImageView? = null
    var mName: CheckedTextView? = null
}