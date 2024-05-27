package com.example.ventura.ui.viewmodel

import androidx.lifecycle.ViewModel
import android.content.Context
import com.example.ventura.repository.Course
import com.example.ventura.repository.CourseRepository

class CourseViewModel(context: Context) : ViewModel() {

    private val courseRepository = CourseRepository(context)

    fun saveCourse(name: String, professor: String, room: String, schedule: String) {
        courseRepository.saveCourse(name, professor, room, schedule)
    }

    fun getCourses(): List<Course> {
        return courseRepository.getCourses()
    }

    fun deleteCourse(course: Course) {
        courseRepository.deleteCourse(course)
    }
}
