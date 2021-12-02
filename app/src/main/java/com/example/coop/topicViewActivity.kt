package com.example.coop

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
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
    private var mSearchView: SearchView? = null
    private lateinit var username: String
    var topicList: java.util.ArrayList<String> = java.util.ArrayList()

    private lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_view)
        topicID = intent.getStringExtra("topic").toString()

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
                    makeIntent1(i)
            }
            true
        }

        //adding items in list
        mAuth = FirebaseAuth.getInstance()
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
                    db.collection(collectionPath).orderBy("time", Query.Direction.ASCENDING)
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
        intent.putExtra("topicName", topicName)
        startActivity(intent)
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

    private fun makeIntent1(topicID : String){
        val intent = Intent(this, topicViewActivity::class.java)
        intent.putExtra("topic", topicID)
        startActivity(intent)
    }
}