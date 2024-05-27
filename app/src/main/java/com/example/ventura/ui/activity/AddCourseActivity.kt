package com.example.ventura.ui.activity

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.TouchDelegate
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ventura.R
import com.example.ventura.ui.viewmodel.CourseViewModel
import com.example.ventura.ui.viewmodel.CourseViewModelFactory

class AddCourseActivity : AppCompatActivity() {

    private lateinit var courseViewModel: CourseViewModel
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_course)


        val factory = CourseViewModelFactory(applicationContext)
        courseViewModel = ViewModelProvider(this, factory).get(CourseViewModel::class.java)

        val courseName = findViewById<EditText>(R.id.courseName)
        val courseProfessor = findViewById<EditText>(R.id.courseProfessor)
        val courseRoom = findViewById<EditText>(R.id.courseRoom)
        val courseSchedule = findViewById<EditText>(R.id.courseSchedule)
        val saveButton = findViewById<Button>(R.id.saveButton)

        saveButton.setOnClickListener {
            val name = courseName.text.toString()
            val professor = courseProfessor.text.toString()
            val room = courseRoom.text.toString()
            val schedule = courseSchedule.text.toString()

            courseViewModel.saveCourse(name, professor, room, schedule)
            finish()  // Close the activity after saving
        }

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
        backButton.post {
            val parent = backButton.parent as View

            val rect = Rect()
            backButton.getHitRect(rect)

            val extraPadding = 100
            rect.top -= extraPadding
            rect.bottom += extraPadding
            rect.left -= extraPadding
            rect.right += extraPadding

            parent.touchDelegate = TouchDelegate(rect, backButton)
        }
    }
}
