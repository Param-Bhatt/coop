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




class UserPosts : Fragment() {
    private lateinit var mAuth: FirebaseAuth

    var mAdapter: UserPostsAdapter? = null
    private var db = Firebase.firestore
    var recyclerView: RecyclerView? = null
    var poss: ArrayList<UserPostsModel>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var view = inflater.inflate(R.layout.fragment_user_posts, container, false)
        mAuth = FirebaseAuth.getInstance()
        val tempPost = UserPostsModel()
        tempPost.titleB = "HI"
        tempPost.titleP = "HIIIII"
        tempPost.id = "asf32ewd23"
        var a = ArrayList<UserPostsModel>()
        a.add(tempPost)
        recyclerView = view.findViewById<View>(R.id.user_post_recycler_view) as RecyclerView
        val mLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerView!!.layoutManager = mLayoutManager
        recyclerView!!.adapter = UserPostsAdapter(a)
        getData(userID = mAuth.currentUser?.uid.toString()) { allposts ->
            poss = allposts
            Log.d(ContentValues.TAG, allposts[0].titleB.toString())
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
            mAdapter = UserPostsAdapter(poss!!)
            recyclerView!!.adapter = mAdapter
        }
    }

    private fun getData(userID: String, callback: (ArrayList<UserPostsModel>) -> Unit) {
        var uid: String? = null
        db.collection("users").whereEqualTo("uid", userID)
            .get()
            .addOnSuccessListener { resu ->
                for(re in resu) {
                    uid = re.id.toString()
                    val collectionPath = "users/${uid}/posts"
                    val query = db.collection(collectionPath)
                    val posts: ArrayList<UserPostsModel> = ArrayList()
                    query
                        .get()
                        .addOnSuccessListener { ps ->
                            for (p in ps) {
                                val tempPost = UserPostsModel()
                                tempPost.id = p.id
                                tempPost.titleP = p.data["postTitle"] as String
                                tempPost.titleB = p.data["postBody"]?.toString()
                                tempPost.topicName = p.data["topicName"]?.toString()
                                Log.d(
                                    ContentValues.TAG,
                                    "${tempPost.id} => ${tempPost.titleP} => ${tempPost.titleB}"
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