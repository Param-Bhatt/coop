package com.example.coop

import android.content.ContentValues
import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class topicViewActivity : AppCompatActivity() {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    val db = Firebase.firestore
    lateinit var topicID:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_view)
        topicID = intent.getStringExtra("topic").toString()

        //adding items in list

        getData(){listoftopics ->
            mRecyclerView = findViewById(R.id.my_recycler_view)
            Log.d(TAG, "Making the recyclerview now")
            var mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            mRecyclerView!!.layoutManager = mLayoutManager
            mAdapter = Myadapter(listoftopics)
            mRecyclerView!!.adapter = mAdapter
        }
    }

    private fun getData(callback:(ArrayList<Users>) -> Unit){
        var listOfusers: ArrayList<Users> = ArrayList()
        var collectionPath = "topics"
        if (topicID != null) {
            db.collection(collectionPath).document(topicID)
                .get()
                .addOnSuccessListener { result ->
                    if (result != null) {
                        val topicName = result.data?.get("name")
                    } else {
                        Log.d(TAG, "No such document")
                    }
                    collectionPath = collectionPath +"/${topicID}/posts"
                    db.collection(collectionPath)
                        .get()
                        .addOnSuccessListener { postResult ->
                            for(post in postResult){
                                val user = Users()
                                user.title = post.data["postTitle"] as String?
                                user.body = post.data["postBody"] as String?
                                listOfusers!!.add(user)
                                Log.d(TAG, "Added post ${user.title}")
                            }
                            callback(listOfusers)
                        }
                        .addOnFailureListener { e ->
                            Log.w(ContentValues.TAG, "Error in getting the posts of the topic", e)
                        }
                    //}*/
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error in getting the requested topic", e)
                }
        }
    }
}