package com.example.coop

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth as FirebaseAuth




class UserComments : Fragment() {
    private lateinit var mAuth: FirebaseAuth

    var mAdapter: UserCommentsAdapter? = null
    private var db = Firebase.firestore
    var recyclerView: RecyclerView? = null
    var poss: ArrayList<UserCommentsModel> = ArrayList<UserCommentsModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var view = inflater.inflate(R.layout.fragment_user_comments, container, false)
        mAuth = FirebaseAuth.getInstance()
        val tempComment = UserCommentsModel()
        tempComment.postTit = "HI"
        tempComment.comm = "HIIIII"
        tempComment.topicName = "fvrgfed"
        tempComment.id = "asf32ewd23"
        var a = ArrayList<UserCommentsModel>()
        a.add(tempComment)
        recyclerView = view.findViewById<View>(R.id.user_comment_recycler_view) as RecyclerView
        val mLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerView!!.layoutManager = mLayoutManager
        recyclerView!!.adapter = UserCommentsAdapter(a)
        getData(userID = mAuth.currentUser?.uid.toString()) { allposts ->
            poss = allposts
            Log.d(ContentValues.TAG, allposts[0].comm.toString())
            updateRecyclerView()
            recyclerView?.adapter?.notifyDataSetChanged()
        }
        return view
    }

    private fun updateRecyclerView() {
        recyclerView.apply {
            if(poss == null) {
                Log.d(ContentValues.TAG, "FUCKEDDDDDDD")
            }
            mAdapter = UserCommentsAdapter(poss)
            recyclerView!!.adapter = mAdapter
        }
    }

    private fun getData(userID: String, callback: (ArrayList<UserCommentsModel>) -> Unit) {
        var uid: String? = null
        db.collection("users").whereEqualTo("uid", userID)
            .get()
            .addOnSuccessListener { resu ->
                for(re in resu) {
                    uid = re.id.toString()
                    val collectionPath = "users/${uid}/comments"
                    Log.d(ContentValues.TAG, collectionPath)
                    val query = db.collection(collectionPath)
                    val posts: ArrayList<UserCommentsModel> = ArrayList()
                    query
                        .get()
                        .addOnSuccessListener { ps ->
                            for (p in ps) {
                                val tempPost = UserCommentsModel()
                                tempPost.id = p.id
                                tempPost.postTit = p.data["postTitle"] as String
                                tempPost.comm = p.data["comment"]?.toString()
                                tempPost.topicName = p.data["topicName"]?.toString()
                                Log.d(
                                    ContentValues.TAG,
                                    "${tempPost.id} => ${tempPost.postTit} => ${tempPost.comm}"
                                )

                                posts.add(tempPost)
                            }
                            callback(posts)
                        }
                        .addOnFailureListener { exception ->
                            Log.w(ContentValues.TAG, "Error in getting documents", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error in getting documents", exception)
            }

    }
}