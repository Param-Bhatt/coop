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


class SearchActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    private var mRecyclerView: RecyclerView? = null
    private var mSearchView: SearchView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var db = Firebase.firestore
    private lateinit var username: String
    private var listOfresults: ArrayList<Topics> = ArrayList()
    var topicList:ArrayList<String> = ArrayList()

    private lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

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

        //adding items in list
        getData { alltopics ->
            mRecyclerView = findViewById(R.id.result_recycler_view)
            val mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            mRecyclerView!!.layoutManager = mLayoutManager
            mAdapter = SearchAdapter(alltopics)
            mRecyclerView!!.adapter = mAdapter
            (mAdapter as SearchAdapter).setOnItemClickListener(object : SearchAdapter.ClickListener {
                override fun onItemClick(position: Int, v: View?) {
                    val intent = Intent(this@SearchActivity, topicViewActivity::class.java)
                    intent.putExtra("topic", alltopics[position].id)
                    startActivity(intent)
                }
            })
            doSearch(alltopics)
        }
    }

    private fun getData(callback: (ArrayList<Topics>) -> Unit) {
        val collectionPath = "topics"
        val query = db.collection(collectionPath).orderBy("followers", Query.Direction.DESCENDING)
        val listoftopics: ArrayList<Topics> = ArrayList()
        query
            .get()
            .addOnSuccessListener { topics ->
                Log.d(TAG, "Getting Data")
                for (topic in topics) {
                    val tempTopic = Topics()
                    tempTopic.id = topic.id
                    tempTopic.topic = topic.data["name"] as String
                    tempTopic.followers = topic.data["followers"]?.toString()
                    Log.d(
                        TAG,
                        "${tempTopic.id} => ${tempTopic.topic} => ${tempTopic.followers}"
                    )

                    listoftopics.add(tempTopic)
                }
                callback(listoftopics)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error in getting documents", exception)
            }
    }

    private fun doSearch(alltopics: ArrayList<Topics>) {
        mSearchView = findViewById(R.id.search_bar)
        with(mSearchView) {
            this?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    search(query, alltopics)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    search(newText, alltopics)
                    return true
                }
            })
        }
    }
    private fun search(text: String?, alltopics: ArrayList<Topics>) {
        listOfresults = arrayListOf()

        text?.let {
            for (topic in alltopics) {
                if (topic.topic!!.contains(text, true)) {
                    listOfresults.add(topic)
                }
            }
            updateRecyclerView()
            if (listOfresults.isEmpty()) {
                Toast.makeText(this, "No match found!", Toast.LENGTH_SHORT).show()
            }
            updateRecyclerView()
        }
    }

    private fun updateRecyclerView() {
        mRecyclerView.apply {
            mAdapter = SearchAdapter(listOfresults)
            mRecyclerView!!.adapter = mAdapter
            (mAdapter as SearchAdapter).setOnItemClickListener(object : SearchAdapter.ClickListener {
                override fun onItemClick(position: Int, v: View?) {
                    val intent = Intent(this@SearchActivity, topicViewActivity::class.java)
                    intent.putExtra("topic", listOfresults[position].id)
                    startActivity(intent)
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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