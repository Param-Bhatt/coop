package com.example.coop

import android.content.ContentValues
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
import java.sql.Date
import java.util.*
import kotlin.collections.ArrayList

class HomeActivity : AppCompatActivity() {

    var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mSearchView: SearchView? = null
    val db = Firebase.firestore
    lateinit var topicID: String
    var followerCount: Long = 0
    lateinit var topicName: String
    private lateinit var mAuth: FirebaseAuth
    private lateinit var username: String
    private lateinit var userUpdatePath: String
    private lateinit var userTopicID: String

    private lateinit var toggle : ActionBarDrawerToggle

    var i = 0
    var n = 0
    var topicList:ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

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
            for(i in topicList){
                if(it.itemId == i.toInt())
                //Toast.makeText(applicationContext, "Clicked item with id $i", Toast.LENGTH_SHORT).show()//
                    makeIntent(i)
            }
            true
        }

        mRecyclerView = findViewById(R.id.my_recycler_view) as RecyclerView
        var mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mRecyclerView!!.layoutManager = mLayoutManager
        mAdapter = HomeAdapter(ArrayList<UserPostsModel>())
        mRecyclerView!!.adapter = mAdapter

        getTopics(uid)
    }

    private fun getTopics(uid: String) {
        var listOfposts: ArrayList<UserPostsModel> = ArrayList()
        var collectionPath = "users"
        db.collection(collectionPath).whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { result ->
                var ids: String? = null
                for (r in result) {
                    ids = r.id.toString()
                    Log.d(ContentValues.TAG, "ID: $ids")
                }
                collectionPath = "users/$ids/topics"
                Log.d(ContentValues.TAG, "$collectionPath")
                db.collection(collectionPath)
                    .get()
                    .addOnSuccessListener { topics ->
                        Log.d(ContentValues.TAG, "${topics.size()}")
                        var listOfTopics = ArrayList<Topics>()
                        for (topic in topics) {
                            var tempTopic = Topics()
                            tempTopic.id = topic.data["topicID"].toString()
                            tempTopic.topic = topic.data["topicName"].toString()
                            listOfTopics.add(tempTopic)
                            Log.d(ContentValues.TAG, "${tempTopic.topic}")
                        }
                        n = listOfTopics.size
                        if (n != 0) {
                            findViewById<TextView>(R.id.no_results).text = ""
                        }
                        for(topic in listOfTopics) {
                            collectionPath = "topics/${topic.id}/posts"
                            db.collection(collectionPath).orderBy("time", Query.Direction.ASCENDING).limit(5)
                                .get()
                                .addOnSuccessListener { posts ->
                                    var topicsList = ArrayList<UserPostsModel>()
                                    for(post in posts) {
                                        var tempPost = UserPostsModel()
                                        tempPost.titleP = post.data["postTitle"].toString()
                                        tempPost.titleB = post.data["postBody"].toString()
                                        tempPost.topicName = topic.topic
                                        tempPost.topicID = topic.id
                                        tempPost.postId = post.id
                                        tempPost.time = post.data["time"].toString()
                                        topicsList.add(tempPost)
                                    }
                                    concat(listOfposts, topicsList)
                                }
                                .addOnFailureListener { e ->
                                    Log.w(ContentValues.TAG, "Error in getting the requested posts", e)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w(ContentValues.TAG, "Error in getting the requested list of topics", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error in getting the requested user", e)
            }
        }

    private fun concat(listOfPosts: ArrayList<UserPostsModel>, postsList:ArrayList<UserPostsModel>) {
        i++
        listOfPosts += postsList
        if(i == n) {
            listOfPosts.shuffle()
            var OfPosts = ArrayList<UserPostsModel>()
            OfPosts.addAll(listOfPosts.sortedWith(compareBy({it.time})))
            Log.d(ContentValues.TAG, "Got List   =>    ${OfPosts[0].topicName}")
            updateRecyclerView(OfPosts)
        }
    }

    private fun updateRecyclerView(poss: ArrayList<UserPostsModel>) {
        mRecyclerView.apply {
            mAdapter = HomeAdapter(poss)
            mRecyclerView!!.adapter = mAdapter
            (mAdapter as HomeAdapter).setOnItemClickListener(object : HomeAdapter.ClickListener {
                override fun onItemClick(position: Int, v: View?) {
                    val intent = Intent(this@HomeActivity, postViewActivity::class.java)
                    intent.putExtra("topic", poss[position].topicID)
                    intent.putExtra("post", poss[position].postId)
                    intent.putExtra("topicName", poss[position].topicName)
                    startActivity(intent)
                }

                override fun onItemLongClick(position: Int, v: View?) {
                    TODO("Not yet implemented")
                }
            })
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