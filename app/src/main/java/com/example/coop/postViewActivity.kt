package com.example.coop

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.type.DateTime
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class postViewActivity : AppCompatActivity() {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    var listOfComments: ArrayList<Comments> = ArrayList()
    private lateinit var mAuth: FirebaseAuth
    class Post{
        var author : String ?= null
        var time : Timestamp ?= null
        var title : String ?= null
        var body : String ?=  null
        var upvotes : Long ?= null
        var downvotes : Long ?= null
    }
    val post = Post()
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_view)
        val topicID = intent.getStringExtra("topic")
        val postID = intent.getStringExtra("post")
        mAuth = FirebaseAuth.getInstance()
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")

        // Taking the action buttons
        val commentButton : Button = findViewById<Button>(R.id.commentButton)
        val upvoteButton : Button = findViewById<Button>(R.id.upvoteButton)
        val downvoteButton : Button = findViewById<Button>(R.id.downvoteButton)

        //Setting the onclick listeners of the buttons we took
        commentButton.setOnClickListener{
            val comment = findViewById<EditText>(R.id.newComment).text
            findViewById<EditText>(R.id.newComment).setText("")
            val userName = mAuth.currentUser?.displayName
            val uid = mAuth.currentUser?.uid
            val time = sdf.format(Date())
            val map = HashMap<String, Any>()
            map["commentBody"] = comment.toString()
            map["time"] = time
            map["userName"] = userName.toString()

            //first add the comment to our topics db
            db.collection("topics/${topicID}/posts/${postID}/comments")
                .add(map)
                .addOnSuccessListener { ref ->
                    Log.d("commentAdditionSuccess", "DocumentSnapshot written with ID: ${ref.id}")
                }
                .addOnFailureListener { e ->
                    Log.w("commentAdditionFailure", "Error adding user", e)
                }
            //now, update the comments list within the program
            val commentObj = Comments()
            commentObj.body = comment.toString()
            commentObj.timeStamp = Timestamp.now().toString()
            commentObj.userName = userName.toString()
            listOfComments!!.add(commentObj)
            //and ab karo swaha, user table me add the comments so you can retrieve them later on
            var collectionPath = "users"
            db.collection(collectionPath).whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        for(each in result){
                            collectionPath += "/${each.id}/comments"
                            val map = HashMap<String, Any>()
                            map["commentBody"] = comment.toString()
                            map["postID"] = postID.toString()
                            map["timestamp"] = time
                            map["topicID"] = topicID.toString()
                            db.collection(collectionPath).add(map)
                                .addOnSuccessListener { ref ->
                                    Log.d("userAdditionSuccess", "comment added with ID: ${ref.id}")
                                }
                                .addOnFailureListener { e ->
                                    Log.w("userAdditionFailure", "Error adding comment", e)
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("userNotFound", "Cant find user ${userName}", e)
                }

            updateRecyclerView()

        }
        upvoteButton.setOnClickListener{
            post.upvotes = post.upvotes?.plus(1)
            val upvoteUpdateMap = mutableMapOf<String, Any>()
            upvoteUpdateMap["upvotes"] = post.upvotes!!
            db.collection("topics/${topicID}/posts").document("${postID}").update(upvoteUpdateMap)
                .addOnSuccessListener {
                    Log.d(TAG, "Upvoted post ${postID} successfully")
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error in upvoting", e)
                }
            val upvoteCount = findViewById<TextView>(R.id.upvote)
            upvoteCount.text = post.upvotes.toString()

        }
        downvoteButton.setOnClickListener{
            post.downvotes = post.downvotes?.plus(1)
            val upvoteUpdateMap = mutableMapOf<String, Any>()
            upvoteUpdateMap["upvotes"] = post.downvotes!!
            db.collection("topics/${topicID}/posts").document("${postID}").update(upvoteUpdateMap)
                .addOnSuccessListener {
                    Log.d(TAG, "Downvoted post ${postID} successfully")
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error in downvoting", e)
                }
            val downvoteCount = findViewById<TextView>(R.id.dowvote)
            downvoteCount.text = post.downvotes.toString()

        }

        //Come to the real part of db update and taking information
        db.collection("topics/${topicID}/posts").document("${postID}")
            .get()
            .addOnSuccessListener { result ->
                post.author = result.data?.get("postAuthor") as String?
                post.body = result.data?.get("postBody") as String?
                post.title = result.data?.get("postTitle") as String?
                post.time = result.data?.get("time") as Timestamp?
                post.upvotes = result.data?.get("upvotes") as Long?
                post.downvotes = result.data?.get("downvotes") as Long?
                val postTitle = findViewById<TextView>(R.id.postTitle)
                val postBody = findViewById<TextView>(R.id.postBody)
                val upvoteCount = findViewById<TextView>(R.id.upvote)
                val downvoteCount = findViewById<TextView>(R.id.dowvote)
                postTitle.text = post.title
                postBody.text = post.body
                upvoteCount.text = post.upvotes.toString()
                downvoteCount.text = post.downvotes.toString()
                db.collection("topics/${topicID}/posts/${postID}/comments")
                    .get()
                    .addOnSuccessListener { res ->
                        for(each in res){
                            val comment = Comments()
                            comment.body = each.data["commentBody"].toString()
                            //var time = each.data["time"]
                            comment.timeStamp = each.data["time"] as String?
                            //comment.timeStamp = sdf.format(each.data["time"] as Timestamp?)
                            comment.userName = each.data["userName"] as String?
                            listOfComments!!.add(comment)
                            Log.d(TAG, each.data["commentBody"].toString())
                        }
                        mRecyclerView = findViewById(R.id.post_recycler_view)
                        var mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                        mRecyclerView!!.layoutManager = mLayoutManager
                        mAdapter = commentAdapter(listOfComments)
                        mRecyclerView!!.adapter = mAdapter
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error in finding comments of post ${postID} in topic ${topicID}", e)
                    }

            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error in finding post ${postID} in topic ${topicID}", e)
            }
    }
    private fun updateRecyclerView(){
        mRecyclerView.apply {
            mAdapter = commentAdapter(listOfComments)
            mRecyclerView!!.adapter = mAdapter
            (mAdapter as commentAdapter).setOnItemClickListener(object : commentAdapter.ClickListener {
                override fun onItemClick(position: Int, v: View?) {
                    val intent = Intent(this@postViewActivity, postViewActivity::class.java)
                    //intent.putExtra("topic", listOfComments[position].id)
                    //startActivity(intent)
                    Log.d("Recycle refresh", "${ listOfComments[position].body }")
                }

                override fun onItemLongClick(position: Int, v: View?) {
                    TODO("Not yet implemented")
                }
            })
        }
    }
}