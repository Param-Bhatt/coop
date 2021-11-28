package com.example.coop

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_dashboard.*
import com.google.firebase.auth.FirebaseAuth as FirebaseAuth

class UserComments : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_user_comments, container, false)
    }

    override fun onStart() {
        super.onStart()

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        Log.d("User", "$currentUser")

        name_txt.text = currentUser?.displayName
        email_txt.text = currentUser?.email
        Glide.with(view).load(currentUser?.photoUrl).into(profile_image as ImageView?)

        sign_out_btn.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(activity, SignInActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }
}