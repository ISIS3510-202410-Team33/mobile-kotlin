package com.example.ventura.ui.activity


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ventura.R
import com.example.ventura.ui.adapter.TaskAdapter
import com.example.ventura.viewmodel.TasksViewModel
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.MonthDayBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

class AgendaMainActivity : AppCompatActivity() {

    private lateinit var findAvailableTimeButton: Button
    private val tasksViewModel: TasksViewModel by viewModels()
    private lateinit var backButton: ImageView
    private lateinit var userEmail: String
    private lateinit var addTaskButton: Button
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var calendarView: CalendarView
    private var selectedDate: LocalDate? = null
    private val connectivityManager by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private var isConnected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_cal)

        userEmail = intent.getStringExtra("user_email").toString()
        calendarView = findViewById(R.id.calendarView)
        val titlesContainer = findViewById<ViewGroup>(R.id.titlesContainer)
        backButton = findViewById(R.id.backButton_cal)
        addTaskButton = findViewById(R.id.addTaskButton)
        val tasksRecyclerView = findViewById<RecyclerView>(R.id.tasksRecyclerView)

        taskAdapter = TaskAdapter(mutableListOf(), tasksViewModel)
        tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        tasksRecyclerView.adapter = taskAdapter

        tasksViewModel.tasks.observe(this, Observer { tasks ->
            Log.d("AgendaMainActivity", "tasks: $tasks" )
            taskAdapter.updateTasks(tasks)
        })

        findAvailableTimeButton = findViewById(R.id.findAvailableTimeButton)
        findAvailableTimeButton.setOnClickListener {
            findAvailableTime()
        }

        tasksViewModel.selectedDate.observe(this, Observer { date ->
            selectedDate = date
            calendarView.notifyCalendarChanged()
            tasksViewModel.loadTasksForDate(date)
            Log.d("AgendaMainActivity", "selectedDate: $selectedDate" )
        })

        listenToNetworkChanges()

        backButton.setOnClickListener {

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

        // Setup day titles
        val daysOfWeek = daysOfWeek()
        titlesContainer.children
            .map { it as TextView }
            .forEachIndexed { index, textView ->
                val dayOfWeek = daysOfWeek[index]
                val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                textView.text = title
            }

        // Setup CalendarView
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(10)
        val endMonth = currentMonth.plusMonths(10)
        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        calendarView.scrollToMonth(currentMonth)

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                container.textView.text = data.date.dayOfMonth.toString()

                if (data.date == selectedDate) {
                    container.textView.setBackgroundResource(R.drawable.selected_date_bg)
                } else {
                    container.textView.background = null
                }

                if (data.position == DayPosition.MonthDate) {
                    container.textView.setTextColor(Color.BLACK)
                } else {
                    container.textView.setTextColor(Color.GRAY)
                }
            }
        }


        addTaskButton.setOnClickListener {
            if (selectedDate == null) {
                Toast.makeText(this, "Please select a date first", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, NewTaskActivity::class.java)
                intent.putExtra("selectedDate", selectedDate.toString())
                startActivity(intent)
            }
        }
    }

    private fun listenToNetworkChanges() {
        CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val currentlyConnected = isNetworkAvailable()
                if (currentlyConnected != isConnected) {
                    isConnected = currentlyConnected
                    addTaskButton.isEnabled = isConnected

                }
                delay(2000) // Check connectivity every 2 seconds
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }




    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        lateinit var day: CalendarDay

        init {
            view.setOnClickListener {
                if (day.position == DayPosition.MonthDate) {
                    tasksViewModel.setSelectedDate(day.date)
                }
            }
        }
    }

    private fun findAvailableTime() {
        val tasks = tasksViewModel.tasks.value ?: emptyList()

        // totalTasks represents the total number of tasks.
        val totalTasks = tasks.size

        // completedTasks represents the number of completed tasks.
        val completedTasks = tasks.count { it.completed }

        val tasksByDate = tasks.groupBy { it.date }

        // dateWithLeastTasks represents the date in the next 7 days that has the least number of tasks.
        // If there are multiple dates with the same least number of tasks, it represents the first one found.
        val dateWithLeastTasks = (1..7)
            .map { LocalDate.now().plusDays(it.toLong()) }
            .minByOrNull { date -> tasksByDate[date]?.size ?: 0 }

        // nextDayWithoutTasks represents the next date starting from tomorrow that doesn't have any tasks.
        // If all future dates have tasks, it will be null.
        val nextDayWithoutTasks = generateSequence(LocalDate.now().plusDays(1)) { it.plusDays(1) }
            .firstOrNull { date -> tasksByDate[date]?.isEmpty() ?: true }

        val message = StringBuilder()
        if (totalTasks == 0 && completedTasks == 0) {
            message.append("You have no tasks. Create some tasks to get more useful info.\n\n")
        } else {
            message.append("Total tasks today: $totalTasks\n\n")
            message.append("Completed tasks for today: $completedTasks\n\n")
            if (dateWithLeastTasks != null) {
                message.append("The day in the next 7 days with the least tasks is: $dateWithLeastTasks\n\n")
            }
            if (nextDayWithoutTasks != null) {
                message.append("The next day without tasks is: $nextDayWithoutTasks\n\n")
            }
            if (dateWithLeastTasks == nextDayWithoutTasks) {
                message.append("The best day to add a new task is: $dateWithLeastTasks. This day has the least tasks in the next 7 days and is the next day without tasks. It's an excellent day to add a new task!\n")
            } else {
                message.append("Both $dateWithLeastTasks and $nextDayWithoutTasks are good days to add a new task.\n")
            }
        }
        if (message.isEmpty()) {
            message.append("No tasks found.")
        }

        AlertDialog.Builder(this)
            .setTitle("Available Time")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    override fun onDestroy() {
        super.onDestroy()
        // Cancel all coroutines when the activity is destroyed
        CoroutineScope(Dispatchers.Main).cancel()
    }
}

