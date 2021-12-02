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
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
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
    private var mSearchView: SearchView? = null
    private lateinit var username: String
    var topicList:ArrayList<String> = ArrayList()

    private lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_post)

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
                                db.collection("topics").document(topicID)
                                    .get()
                                    .addOnSuccessListener { topic ->
                                        userMap["topicName"] = topic.data?.get("name").toString()
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
                                    .addOnFailureListener { e ->
                                        Log.w(
                                            "postAdditionFailure",
                                            "Error in adding post to user database",
                                            e
                                        )
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