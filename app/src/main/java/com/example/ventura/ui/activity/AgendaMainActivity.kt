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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*
import com.example.ventura.R
import com.example.ventura.database.DatabaseHelper
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder

class AgendaMainActivity : AppCompatActivity() {

    private var selectedDate: LocalDate? = null
    private lateinit var backButton: ImageView
    private lateinit var tasksListView: ListView
    private lateinit var addTaskButton: Button
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var selectedDateTasksLabel:  TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        // ============================== @UI initialization ==============================
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_cal)


        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val titlesContainer = findViewById<ViewGroup>(R.id.titlesContainer)
        tasksListView = findViewById(R.id.tasksListView)
        addTaskButton = findViewById(R.id.addTaskButton)
        selectedDateTasksLabel = findViewById(R.id.selectedDateTasksLabel)

        dbHelper = DatabaseHelper(this)

        // ============================== @backButton attributes ==========================
        backButton = findViewById(R.id.backButton_cal)
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
            if (selectedDate != null) {
                val intent = Intent(this, NewTaskActivity::class.java)
                intent.putExtra("selectedDate", selectedDate.toString())
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please select a date first", Toast.LENGTH_SHORT).show()
            }
        }

        updateTaskMessage()

    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        lateinit var day: CalendarDay

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        init {
            view.setOnClickListener {
                if (day.position == DayPosition.MonthDate) {
                    val currentSelection = selectedDate
                    if (currentSelection == day.date) {
                        selectedDate = null
                        calendarView.notifyDateChanged(currentSelection)
                    } else {
                        selectedDate = day.date
                        calendarView.notifyDateChanged(day.date)
                        if (currentSelection != null) {
                            calendarView.notifyDateChanged(currentSelection)
                        }
                    }
                    loadTasksForSelectedDate()
                }
            }
        }
    }

    private fun loadTasksForSelectedDate() {
        selectedDate?.let { date ->
            val tasks = dbHelper.getTasksForDate(date)
            val adapter = TaskAdapter(this, tasks)
            tasksListView.adapter = adapter
        }
    }

    private fun updateTaskMessage() {
        val message = if (selectedDate != null) {
            "Tasks for the selected date:"
        } else {
            "Please select a date to view your tasks"
        }
        selectedDateTasksLabel.text = message
    }

}
