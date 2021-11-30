package com.example.coop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchAdapter(private val mDataList: ArrayList<Topics>) : RecyclerView.Adapter<SearchAdapter.MyViewHolder>() {

    private var clickListener: ClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_results, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.res.text = mDataList[position].topic
        holder.follow.text = "Followers: ${mDataList[position].followers}"
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        internal var res: TextView
        internal var follow: TextView
        var name: TextView? = null

        init {
            res = itemView.findViewById<View>(R.id.result) as TextView
            follow = itemView.findViewById<View>(R.id.followers) as TextView
            itemView.setOnClickListener(this)
            name = res
        }

        override fun onClick(v: View?) {
            clickListener!!.onItemClick(adapterPosition, v)
        }
    }

    fun setOnItemClickListener(clickListener: ClickListener) {
        this.clickListener = clickListener
    }

    interface ClickListener {
        fun onItemClick(position: Int, v: View?)
        fun onItemLongClick(position: Int, v: View?)
    }
}