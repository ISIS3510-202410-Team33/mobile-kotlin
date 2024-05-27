package com.example.ventura.ui.activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.ventura.R
import android.graphics.Rect
import android.util.Log
import android.view.KeyEvent
import android.view.TouchDelegate
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ventura.ui.viewmodel.CourseViewModel
import com.example.ventura.ui.viewmodel.CourseViewModelFactory
import com.example.ventura.repository.Course
import com.example.ventura.ui.adapter.CoursesAdapter
import kotlinx.coroutines.*

class CoursesActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var welcomeText: TextView
    private lateinit var userEmail: String
    private lateinit var addCourseBanner: FrameLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var courseViewModel: CourseViewModel
    private lateinit var courses: List<Course>
    private lateinit var filteredCourses: MutableList<Course>
    private lateinit var adapter: CoursesAdapter
    private lateinit var snackbar: TextView
    private lateinit var searchEditText: EditText
    private val connectivityManager by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private var isConnected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courses)
        userEmail = intent.getStringExtra("user_email").toString()
        welcomeText = findViewById(R.id.hiUser)
        welcomeText.text = "Hi, ${extractUsername(userEmail)}!"
        addCourseBanner = findViewById(R.id.addCourseBanner)
        recyclerView = findViewById(R.id.coursesRecyclerView)
        snackbar = findViewById(R.id.snackBar)
        searchEditText = findViewById(R.id.editTextText)

        val factory = CourseViewModelFactory(applicationContext)
        courseViewModel = ViewModelProvider(this, factory)[CourseViewModel::class.java]
        courses = courseViewModel.getCourses()
        filteredCourses = courses.toMutableList()

        adapter = CoursesAdapter(filteredCourses) { course ->
            courseViewModel.deleteCourse(course)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

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

        addCourseBanner.setOnClickListener {
            val intent = Intent(this, AddCourseActivity::class.java)
            startActivity(intent)
        }

        // Set the listener for the EditText to filter courses when Enter is pressed
        searchEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                filterCourses(searchEditText.text.toString())
                true
            } else {
                false
            }
        }

        listenToNetworkChanges()
    }

    private fun filterCourses(query: String) {
        val filteredList = courses.filter {
            it.courseName.contains(query, true)
        }
        filteredCourses.clear()
        filteredCourses.addAll(filteredList)
        adapter.notifyDataSetChanged()
    }

    private fun extractUsername(email: String?): String {
        return email?.substringBefore("@") ?: ""
    }

    private fun listenToNetworkChanges() {
        CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val currentlyConnected = isNetworkAvailable()
                if (currentlyConnected != isConnected) {
                    isConnected = currentlyConnected
                    if (!isConnected) {
                        snackbar.visibility = View.VISIBLE
                    } else {
                        snackbar.visibility = View.GONE
                        showNetworkRecoveredNotification()
                    }
                }
                delay(2000) // Verifica la conectividad cada 2 segundos
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    private fun showNetworkRecoveredNotification() {
        Toast.makeText(this, "Internet recovered", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancelar todas las corutinas cuando la actividad se destruye
        CoroutineScope(Dispatchers.Main).cancel()
    }
}