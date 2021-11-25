package com.example.coop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_dashboard.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayout)
        val navView : NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_home -> Toast.makeText(applicationContext, "Clicked home", Toast.LENGTH_SHORT).show()
                R.id.nav_settings -> Toast.makeText(applicationContext, "Clicked settings", Toast.LENGTH_SHORT).show()
                R.id.delete -> Toast.makeText(applicationContext, "Clicked delete", Toast.LENGTH_SHORT).show()
                R.id.topic_share -> Toast.makeText(applicationContext, "Topic share", Toast.LENGTH_SHORT).show()

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

        return super.onOptionsItemSelected(item)
    }
}
