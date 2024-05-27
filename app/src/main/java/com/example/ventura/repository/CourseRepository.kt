package com.example.ventura.repository

import android.content.Context
import android.util.LruCache
import java.io.File
import java.io.FileOutputStream

class CourseRepository(private val context: Context) {

    private val courseCache = LruCache<String, List<Course>>(10)

    fun saveCourse(name: String, professor: String, room: String, schedule: String) {
        val course = Course(name, professor, room, schedule)
        val file = File(context.filesDir, "courses.txt")
        FileOutputStream(file, true).bufferedWriter().use { out ->
            out.write("${course.courseName},${course.professor},${course.room},${course.schedule}")
            out.newLine()
        }

        // Add the course to the cache
        val courses = getCoursesFromCache().toMutableList()
        courses.add(course)
        courseCache.put("courses", courses)
    }

    fun getCourses(): List<Course> {
        val cachedCourses = getCoursesFromCache()
        return if (cachedCourses.isNotEmpty()) {
            cachedCourses
        } else {
            val courses = getCoursesFromFile()
            courseCache.put("courses", courses)
            courses
        }
    }

    private fun getCoursesFromCache(): List<Course> {
        return courseCache.get("courses") ?: emptyList()
    }

    private fun getCoursesFromFile(): List<Course> {
        val file = File(context.filesDir, "courses.txt")
        val courses = mutableListOf<Course>()

        if (file.exists()) {
            file.forEachLine { line ->
                val parts = line.split(",")
                if (parts.size == 4) {
                    courses.add(Course(parts[0], parts[1], parts[2], parts[3]))
                }
            }
        }
        return courses
    }

    fun deleteCourse(course: Course) {
        val file = File(context.filesDir, "courses.txt")
        val courses = getCoursesFromFile().toMutableList()
        courses.remove(course)

        // Write the remaining courses back to the file
        file.writeText("")
        courses.forEach {
            FileOutputStream(file, true).bufferedWriter().use { out ->
                out.write("${it.courseName},${it.professor},${it.room},${it.schedule}")
                out.newLine()
            }
        }

        // Update the cache
        courseCache.put("courses", courses)
    }

}

data class Course(
    val courseName: String,
    val professor: String,
    val room: String,
    val schedule: String
)
