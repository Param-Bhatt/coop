package com.example.coop

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class makePostActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    val post = Post()
    val db = Firebase.firestore
    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_post)
        mAuth = FirebaseAuth.getInstance()
        val topicID = intent.getStringExtra("topic").toString()
        //working with the button on click
        val submit = findViewById<View>(R.id.postButton)
        submit.setOnClickListener{
            val map = HashMap<String, Any>()
            val time = sdf.format(Date())
            post.author = mAuth.currentUser?.displayName
            post.upvotes = 0
            post.downvotes = 0
            post.title = findViewById<TextView>(R.id.newPostTitle).text.toString()
            post.body = findViewById<TextView>(R.id.newPostBody).text.toString()
            map["downvotes"] = post.downvotes!!
            map["upvotes"] = post.upvotes!!
            map["postAuthor"] = post.author.toString()
            map["postBody"] = post.body.toString()
            map["postTitle"] = post.title.toString()
            map["time"] = time

            if (topicID != null) {
                addPost(map, topicID, time)
            }

        }
    }
    private fun addPost(map : HashMap<String, Any>, topicID : String, time : String){
        db.collection("topics/${topicID}/posts")
            .add(map)
            .addOnSuccessListener { ref ->
                Log.d("postAdditionSuccess", "post added with ID: ${ref.id}")
                val userMap = HashMap<String, Any>()
                userMap["postBody"] = post.body.toString()
                userMap["postID"] = ref.id
                userMap["postTitle"] = post.title.toString()
                userMap["time"] = time
                userMap["topicID"] =topicID
                val uid = mAuth.currentUser?.uid
                db.collection("users").whereEqualTo("uid", uid)
                    .get()
                    .addOnSuccessListener { result ->
                        if(!result.isEmpty){
                            for(each in result){
                                db.collection("users/${each.id}/posts").add(userMap)
                                    .addOnSuccessListener { ref ->
                                        Log.d("postAdditionSuccess", "post added with ID: ${ref.id} to user : ${uid}")
                                        val intent = Intent(this, topicViewActivity::class.java)
                                        intent.putExtra("topic", topicID)
                                        startActivity(intent)


                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("postAdditionFailure", "Error in adding post to user database", e)
                                    }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Couldnt find user", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("postAdditionFailure", "Error in making post", e)
            }


    }
}