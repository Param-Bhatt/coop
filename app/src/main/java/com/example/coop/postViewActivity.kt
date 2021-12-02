package com.example.coop

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
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
    val post = Post()
    val db = Firebase.firestore
    private var mSearchView: SearchView? = null
    private lateinit var username: String
    var topicList: java.util.ArrayList<String> = java.util.ArrayList()

    private lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_view)

        mSearchView = findViewById(R.id.search_bar)
        with(mSearchView) {
            this?.isIconified = false
        }

        val mDrawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        toggle = ActionBarDrawerToggle(this, mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        mDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        navView.getHeaderView(0).findViewById<TextView>(R.id.user_name_side).text = currentUser?.displayName
        navView.getHeaderView(0).findViewById<TextView>(R.id.email_side).text = currentUser?.email
        Glide.with(this).load(currentUser?.photoUrl).into(navView.getHeaderView(0).findViewById(R.id.profile_image_side) as ImageView?)

        //adding items in list
        username = currentUser?.displayName.toString()
        val uid = currentUser?.uid.toString()

        var collectionPath = "users"
        val query: Query = db.collection("users").whereEqualTo("uid", currentUser?.uid.toString())
        query
            .get()
            .addOnSuccessListener { userResult ->
                for(document in userResult){
                    Log.d(ContentValues.TAG, "${document.id} => ${document.data}")
                    collectionPath = collectionPath +"/"+ document.id + "/topics"
                    db.collection(collectionPath)
                        .get()
                        .addOnSuccessListener{ userTopics ->
                            for(each in userTopics){
                                Log.d(ContentValues.TAG, "${each.id} => ${each.data["topicName"]}")

                                val mMenu = navView.menu
                                var menuSize = mMenu.size()
                                var myItemID:String = each.data["topicID"] as String
                                mMenu.add(1, myItemID.toInt(), menuSize, each.data["topicName"] as String)
                                //topicList.put(each.id, each.data["topicName"] as String)
                                topicList.add(myItemID)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w(ContentValues.TAG, "Error in getting documents posts", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error in getting documents", exception)
            }


        navView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.nav_home) {
                var intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }

            for(i in topicList){
                if(it.itemId == i.toInt())
                //Toast.makeText(applicationContext, "Clicked item with id $i", Toast.LENGTH_SHORT).show()//
                    makeIntent(i)
            }
            true
        }

        val topicID = intent.getStringExtra("topic")
        val postID = intent.getStringExtra("post")
        val topicName = intent.getStringExtra("topicName")
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
                            map["topicName"] = topicName.toString()
                            map["postTitle"] = post.title.toString()
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
                //post.time = result.data?.get("time") as String?
                post.upvotes = result.data?.get("upvotes") as Long?
                post.downvotes = result.data?.get("downvotes") as Long?
                Log.d("post body", result.data.toString())
                val postTitle = findViewById<TextView>(R.id.postTitle)
                val postBody = findViewById<TextView>(R.id.postBody)
                val upvoteCount = findViewById<TextView>(R.id.upvote)
                val downvoteCount = findViewById<TextView>(R.id.dowvote)
                postTitle.text = post.title
                postBody.text = post.body
                upvoteCount.text = post.upvotes.toString()
                downvoteCount.text = post.downvotes.toString()
                db.collection("topics/${topicID}/posts/${postID}/comments").orderBy("time", Query.Direction.ASCENDING)
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
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        else if(item.itemId == R.id.search) {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        val header = findViewById<LinearLayout>(R.id.nav_header)
        header.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        }

        return true
    }

    private fun makeIntent(topicID : String){
        val intent = Intent(this, topicViewActivity::class.java)
        intent.putExtra("topic", topicID)
        startActivity(intent)
    }
}