package com.example.coop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserPostsAdapter(private val mDataList: ArrayList<UserPostsModel>) : RecyclerView.Adapter<UserPostsAdapter.MyViewHolder>() {

    private var clickListener: ClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_post, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.titleP.text = mDataList[position].titleP
        holder.titleB.text = mDataList[position].titleB
        holder.topicNam.text = mDataList[position].topicName
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        internal var titleP: TextView
        internal var titleB: TextView
        internal var topicNam: TextView
        var name: TextView? = null

        init {
            titleP = itemView.findViewById<View>(R.id.post_title) as TextView
            titleB = itemView.findViewById<View>(R.id.post_body) as TextView
            topicNam = itemView.findViewById<View>(R.id.topic_name) as TextView
            itemView.setOnClickListener(this)
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
    }
}