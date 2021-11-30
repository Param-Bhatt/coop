package com.example.coop

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_dashboard.profile_image

class DashboardActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    lateinit var toggle : ActionBarDrawerToggle
    //var topicList:HashMap<String, String> = HashMap<String, String>()
    var topicList:ArrayList<String> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayout)
        val navView : NavigationView = findViewById(R.id.nav_view)
        val db = Firebase.firestore

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_home -> Toast.makeText(applicationContext, "Clicked home", Toast.LENGTH_SHORT).show()
                R.id.nav_settings -> Toast.makeText(applicationContext, "Clicked settings", Toast.LENGTH_SHORT).show()
            }
            for(i in topicList){
                if(it.itemId == i.toInt())
                //Toast.makeText(applicationContext, "Clicked item with id $i", Toast.LENGTH_SHORT).show()//
                makeIntent(i)
            }
            true
        }

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        Log.d("User", "$currentUser")
        id_txt.text = currentUser?.uid
        name_txt.text = currentUser?.displayName
        email_txt.text = currentUser?.email

        Glide.with(this).load(currentUser?.photoUrl).into(profile_image as ImageView?)

        navView.getHeaderView(0).findViewById<TextView>(R.id.user_name_side).text = currentUser?.displayName
        navView.getHeaderView(0).findViewById<TextView>(R.id.email_side).text = currentUser?.email
        Glide.with(this).load(currentUser?.photoUrl).into(navView.getHeaderView(0).findViewById(R.id.profile_image_side) as ImageView?)

        var collectionPath = "users"
        val query: Query = db.collection("users").whereEqualTo("name", currentUser?.displayName.toString())
        query
            .get()
            .addOnSuccessListener { userResult ->
                for(document in userResult){
                    Log.d(TAG, "${document.id} => ${document.data}")
                    collectionPath = collectionPath +"/"+ document.id + "/topics"
                    db.collection(collectionPath)
                        .get()
                        .addOnSuccessListener{ userTopics ->
                            for(each in userTopics){
                                Log.d(TAG, "${each.id} => ${each.data["topicName"]}")

                                val mMenu = navView.menu
                                var menuSize = mMenu.size()
                                var myItemID:String = each.data["topicID"] as String
                                mMenu.add(1, myItemID.toInt(), menuSize, each.data["topicName"] as String)
                                //topicList.put(each.id, each.data["topicName"] as String)
                                topicList.add(myItemID)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Error in getting documents posts", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error in getting documents", exception)
            }

        sign_out_btn.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
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

    private fun makeIntent(topicID : String){
        val intent = Intent(this, topicViewActivity::class.java)
        intent.putExtra("topic", topicID)
        startActivity(intent)
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
}
