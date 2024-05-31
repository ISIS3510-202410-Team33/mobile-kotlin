package com.example.ventura.ui.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.Observer
import com.example.ventura.R
import com.example.ventura.ui.adapter.TaskAdapter
import com.example.ventura.viewmodel.TasksViewModel
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.MonthDayBinder
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

class AgendaMainActivity : AppCompatActivity() {

    private val tasksViewModel: TasksViewModel by viewModels()
    private lateinit var backButton: ImageView
    private lateinit var addTaskButton: Button
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var calendarView: CalendarView
    private var selectedDate: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_cal)

        calendarView = findViewById(R.id.calendarView)
        val titlesContainer = findViewById<ViewGroup>(R.id.titlesContainer)
        backButton = findViewById(R.id.backButton_cal)
        addTaskButton = findViewById(R.id.addTaskButton)
        val tasksListView = findViewById<ListView>(R.id.tasksListView)

        taskAdapter = TaskAdapter(this, emptyList())
        tasksListView.adapter = taskAdapter

        tasksViewModel.tasks.observe(this, Observer { tasks ->
            taskAdapter.updateTasks(tasks)
        })

        tasksViewModel.selectedDate.observe(this, Observer { date ->
            selectedDate = date
            calendarView.notifyCalendarChanged()
            tasksViewModel.loadTasksForDate(date)
        })

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
}
