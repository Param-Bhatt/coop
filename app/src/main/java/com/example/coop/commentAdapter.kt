package com.example.coop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class commentAdapter(private val mDataList: ArrayList<Comments>) : RecyclerView.Adapter<commentAdapter.commentViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): commentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_user, parent, false)
        return commentViewHolder(view)
    }

    override fun onBindViewHolder(holder: commentViewHolder, position: Int) {
        holder.userName.text = mDataList[position].userName
        holder.commentBody.text = mDataList[position].body
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class commentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var userName: TextView
        internal var commentBody : TextView
        var name: TextView? = null

        init {
            userName = itemView.findViewById<View>(R.id.post_title) as TextView
            commentBody = itemView.findViewById<View>(R.id.post_body) as TextView
            name = itemView.findViewById(R.id.post_title);
        }
    }
}

