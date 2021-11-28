package com.example.coop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Myadapter(private val mDataList: ArrayList<Users>) : RecyclerView.Adapter<Myadapter.MyViewHolder>() {
    private var clickListener: ClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_user, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.post_title.text = mDataList[position].title
        holder.post_body.text  = mDataList[position].body
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        internal var post_title: TextView
        internal var post_body: TextView
        var name: TextView? = null
        init {
            post_title = itemView.findViewById<View>(R.id.post_title) as TextView
            post_body = itemView.findViewById<View>(R.id.post_body) as TextView
            itemView.setOnClickListener(this);
            name = itemView.findViewById(R.id.post_title);
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
