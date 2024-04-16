package com.example.ventura.view

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ventura.R

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Retrieve the user email from intent extras
        val userEmail = intent.getStringExtra("user_email")

        // Find views by their IDs
        val emailTextView = findViewById<TextView>(R.id.emailTextView)
        val usernameTextView = findViewById<TextView>(R.id.usernameTextView)

        // Populate the email into the TextView
        emailTextView.text = userEmail

        // Extract username from email
        val username = extractUsername(userEmail)

        // Populate the username into the TextView
        usernameTextView.text = username

        // Handle Edit Profile button click if needed
        val editProfileButton = findViewById<Button>(R.id.editProfileButton)
        editProfileButton.setOnClickListener {
            // Handle edit profile action
        }
    }

    // Extract username from email (part before the "@")
    private fun extractUsername(email: String?): String {
        return email?.substringBefore("@") ?: ""
    }

    // Define a data class to hold user profile information
    data class UserProfile(
        val profileImage: Int,
        val username: String,
        val email: String
    )
}


