package com.example.coop

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class topicViewActivity : AppCompatActivity() {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    val db = Firebase.firestore
    lateinit var topicID: String
    var followerCount: Long = 0
    var postList: ArrayList<String> = ArrayList()
    lateinit var topicName: String
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userID: String
    private lateinit var userUpdatePath: String
    private lateinit var userTopicID: String
    private lateinit var newPostButton : View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_view)
        topicID = intent.getStringExtra("topic").toString()

        //adding items in list
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        userID = currentUser?.uid.toString()
        getData() { listoftopics ->
            findViewById<TextView>(R.id.topicHeader).text = topicName
            findViewById<TextView>(R.id.followerCount).text = followerCount.toString()
            mRecyclerView = findViewById(R.id.my_recycler_view)
            var mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            mRecyclerView!!.layoutManager = mLayoutManager
            mAdapter = Myadapter(listoftopics)
            mRecyclerView!!.adapter = mAdapter
            (mAdapter as Myadapter).setOnItemClickListener(object : Myadapter.ClickListener {
                override fun onItemClick(position: Int, v: View?) {
                    makeIntent(topicID, postList[position])
                    //Log.d(TAG, "onItemClick position: $position post ${postList[position]}")
                }

                override fun onItemLongClick(position: Int, v: View?) {
                    TODO("Not yet implemented")
                }
            })
            val button = findViewById<Button>(R.id.followButton) as Button
            checkUserSubscription() { subscribed ->
                if(subscribed){
                    button.text = "Unfollow this"
                }
            }

            button.setOnClickListener {
                checkUserSubscription () { subscribed ->
                    if (!subscribed) {
                        followed()
                        Log.d(TAG, "followed topic ${topicName}")
                    } else {
                        unfollowed()
                        Log.d(TAG, "unfollowed topic ${topicName}")
                    }
                }
            }

            newPostButton = findViewById(R.id.fab)
            newPostButton.setOnClickListener{
                val intent = Intent(this, makePostActivity::class.java)
                intent.putExtra("topic", topicID)
                //Log.d(TAG, "Topic id : ${topicID}")
                startActivity(intent)
            }
        }
    }
    private fun unfollowed(){
        if(topicID!=null){
            var collectionPath = "topics"
            val followUpdateMap = mutableMapOf<String, Any>()
            followerCount = followerCount - 1
            followUpdateMap["followers"] = followerCount
            db.collection(collectionPath).document(topicID).update(followUpdateMap)
                .addOnSuccessListener {
                    Log.d(TAG, "Updated follower count for ${topicName} successfully")
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error in updating follower count", e)
                }
            findViewById<TextView>(R.id.followerCount).text = followerCount.toString()
            db.collection(userUpdatePath).document(userTopicID)
                .delete()
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
            val button = findViewById<Button>(R.id.followButton) as Button
            button.text = "Follow This"
        }
    }
    private fun followed() {
        if (topicID != null) {
            var collectionPath = "topics"
            val followUpdateMap = mutableMapOf<String, Any>()
            followerCount = followerCount + 1
            followUpdateMap["followers"] = followerCount
            db.collection(collectionPath).document(topicID).update(followUpdateMap)
                .addOnSuccessListener {
                    Log.d(TAG, "Updated follower count for ${topicName} successfully")
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error in updating follower count", e)
                }
            findViewById<TextView>(R.id.followerCount).text = followerCount.toString()
            val userUpdateMap = mutableMapOf<String, Any>()
            userUpdateMap["topicID"] = topicID
            userUpdateMap["topicName"] = topicName
            checkUserSubscription() { subscribed ->
                if (!subscribed) {
                    db.collection(userUpdatePath)
                        .add(userUpdateMap)
                        .addOnSuccessListener { ref ->
                            Log.d(
                                "userAdditionSuccess",
                                "Entry created in users/topics with ID: ${ref.id}"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.w(
                                "userAdditionFailure",
                                "Error adding topic to user following",
                                e
                            )
                        }
                }
            }
            val button = findViewById<Button>(R.id.followButton) as Button
            button.text = "Unfollow This"
        }

    }

    private fun checkUserSubscription( callback: (Boolean) -> Unit) {
        var flag: Boolean = false
        var collectionPath = "users"
        val query = db.collection(collectionPath).whereEqualTo("uid", userID)
        query
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    for (each in result) {
                        collectionPath += "/${each.id}/topics"
                        userUpdatePath = collectionPath
                        db.collection(collectionPath)
                            .get()
                            .addOnSuccessListener { result ->
                                for (each in result) {
                                    if (each.data["topicID"].toString() == topicID) {
                                        //user is already subscribed
                                            userTopicID = each.id
                                        flag = true

                                    }
                                }
                                Log.d("Flag val", flag.toString())
                                callback(flag)
                            }
                            .addOnFailureListener { e ->
                                Log.w(ContentValues.TAG, "Couldnt find topics within the user", e)
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
            }
        }

    private fun getData(callback: (ArrayList<Users>) -> Unit) {
        var listOfusers: ArrayList<Users> = ArrayList()
        var collectionPath = "topics"
        if (topicID != null) {
            db.collection(collectionPath).document(topicID)
                .get()
                .addOnSuccessListener { result ->
                    if (result != null) {
                        topicName = result.data?.get("name") as String
                        followerCount = result.data?.get("followers") as Long
                    } else {
                        Log.d(TAG, "No such document")
                    }
                    collectionPath = collectionPath + "/${topicID}/posts"
                    db.collection(collectionPath)
                        .get()
                        .addOnSuccessListener { postResult ->
                            for (post in postResult) {
                                val user = Users()
                                user.title = post.data["postTitle"] as String?
                                user.body = post.data["postBody"] as String?
                                listOfusers!!.add(user)
                                Log.d(TAG, "Added post ${post.id}")
                                postList.add(post.id)
                            }
                            callback(listOfusers)
                        }
                        .addOnFailureListener { e ->
                            Log.w(ContentValues.TAG, "Error in getting the posts of the topic", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error in getting the requested topic", e)
                }
        }
    }
    private fun makeIntent(topicID : String, postID : String){
        val intent = Intent(this, postViewActivity::class.java)
        intent.putExtra("topic", topicID)
        intent.putExtra("post", postID)
        startActivity(intent)
    }
}