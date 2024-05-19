package com.example.ventura.ui.activity

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.ventura.R
import com.example.ventura.utils.FeatureCrashHandler
import android.graphics.Rect
import android.view.TouchDelegate
import android.view.View
import android.widget.TextView


class CoursesActivity: AppCompatActivity() {

    private val featureCrashHandler = FeatureCrashHandler("courses_view")
    private lateinit var backButton: ImageView
    private lateinit var welcomeText: TextView
    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {

        try {

            // ============================== @UI initialization ==============================
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_courses)
            userEmail = intent.getStringExtra("user_email").toString()
            welcomeText = findViewById(R.id.hiUser)
            welcomeText.text = "Hi, ${extractUsername(userEmail)}!"


            // ============================== @backButton attributes ==========================
            backButton = findViewById(R.id.backButton)
            backButton.setOnClickListener{
                finish()
            }
            backButton.post {
                val parent = backButton.parent as View

                val rect = Rect()
                backButton.getHitRect(rect)

                val extraPadding = 100 // Extra hitbox for the back button
                rect.top -= extraPadding
                rect.bottom += extraPadding
                rect.left -= extraPadding
                rect.right += extraPadding

                parent.touchDelegate = TouchDelegate(rect, backButton)
            }


        }

        catch (e: Exception){
            featureCrashHandler.logCrash("display", e)
        }

    }

    private fun extractUsername(email: String?): String {
        return email?.substringBefore("@") ?: ""
    }

}