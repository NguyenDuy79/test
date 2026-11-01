package com.example.fitness.myapplication.data

import android.content.Context
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

data class QuestionItem(
    val id: String,
    val num: String,
    val tag: String,
    val level: String,
    val category: String,
    val question: String,
    val answer: String,
    val code: String,
    val tips: String,
    val projectRef: String,
    val relatedLab: LabType? = null
)

enum class LabType(val title: String) {
    LIFECYCLE("Biểu đồ Stock (Vòng đời & onDraw)"),
    MEASURE_SPEC("MeasureSpec & Layout Bounds"),
    TOUCH("Touch Dispatch & Intercept"),
    RECYCLER_VIEW("RecyclerView & DiffUtil Payload"),
    FRAGMENT_NAV("Fragment Lifecycle & Predictive Back")
}

object QuestionRepository {
    private var cachedQuestions: List<QuestionItem>? = null

    fun getQuestions(context: Context): List<QuestionItem> {
        cachedQuestions?.let { return it }

        val list = mutableListOf<QuestionItem>()
        try {
            val fileName = "questions_50.json"
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val jsonString = reader.use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val num = obj.optString("num", "")
                val numInt = num.replace("#", "").toIntOrNull() ?: 0

                val lab = when (numInt) {
                    1, 3, 6, 9 -> LabType.LIFECYCLE
                    2, 10, 14, 17, 18 -> LabType.MEASURE_SPEC
                    7 -> LabType.TOUCH
                    30, 34, 35, 36, 38, 41 -> LabType.RECYCLER_VIEW
                    42, 44, 45, 46, 47, 49, 50 -> LabType.FRAGMENT_NAV
                    else -> null
                }

                list.add(
                    QuestionItem(
                        id = obj.optString("id", ""),
                        num = num,
                        tag = obj.optString("tag", "XML / View"),
                        level = obj.optString("level", "Middle"),
                        category = obj.optString("category", "Custom View & Vòng đời"),
                        question = obj.optString("question", ""),
                        answer = obj.optString("answer", ""),
                        code = obj.optString("code", ""),
                        tips = obj.optString("tips", ""),
                        projectRef = obj.optString("projectRef", ""),
                        relatedLab = lab
                    )
                )
            }
            cachedQuestions = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
