package com.example.data

import org.yaml.snakeyaml.Yaml

data class BlfScheduleDay(
    val date: String,
    val day: String,
    val items: List<BlfScheduleItem>
)

data class BlfScheduleItem(
    val time: String = "",
    val title: String,
    val subtitle: String = "",
    val isFocus: Boolean = false
)

object BlfParser {
    fun parseSchedule(content: String): List<BlfScheduleDay> {
        val days = mutableListOf<BlfScheduleDay>()
        try {
            val yaml = Yaml()
            val data = yaml.load<Any>(content) as? Map<*, *> ?: return emptyList()

            // Handle Holiday Master Schedule
            val holidayPlan = data["holiday_plan"] as? List<*>
            if (holidayPlan != null) {
                for (dayObj in holidayPlan) {
                    val dayPlan = dayObj as? Map<*, *> ?: continue
                    val date = dayPlan["date"]?.toString() ?: ""
                    val day = dayPlan["day"]?.toString() ?: ""
                    val items = mutableListOf<BlfScheduleItem>()

                    (dayPlan["board_revision"] as? List<*>)?.forEach { 
                        items.add(BlfScheduleItem(time = "Revision", title = it.toString(), subtitle = "Board Prep", isFocus = true))
                    }
                    (dayPlan["homework"] as? List<*>)?.forEach {
                        items.add(BlfScheduleItem(time = "Homework", title = it.toString(), subtitle = "Assignment", isFocus = false))
                    }
                    if (items.isNotEmpty()) {
                        days.add(BlfScheduleDay(date, day, items))
                    }
                }
            }

            // Handle standard multiple events or routine
            val routine = data["routine"] as? Map<*, *>
            val events = data["events"] as? List<*>
            
            if (events != null && days.isEmpty()) {
                 val items = mutableListOf<BlfScheduleItem>()
                 for (eventObj in events) {
                     val event = eventObj as? Map<*, *> ?: continue
                     items.add(BlfScheduleItem(
                        time = event["start"]?.toString() ?: "", 
                        title = event["title"]?.toString() ?: "", 
                        subtitle = event["subject"]?.toString() ?: "", 
                        isFocus = event["focus_mode"]?.toString()?.toBoolean() == true
                     ))
                 }
                 days.add(BlfScheduleDay("Today", "", items))
            } else if (routine != null && days.isEmpty()) {
                val karateDays = routine["karate_days"] as? Map<*, *>
                val schedule = karateDays?.get("schedule") as? List<*>
                if (schedule != null) {
                    val items = mutableListOf<BlfScheduleItem>()
                    for (entry in schedule) {
                        val map = entry as? Map<*, *> ?: continue
                        items.add(BlfScheduleItem(
                            time = map["start"]?.toString() ?: "",
                            title = map["title"]?.toString() ?: "",
                            subtitle = map["type"]?.toString() ?: "Routine",
                            isFocus = map["type"]?.toString() == "study"
                        ))
                    }
                    days.add(BlfScheduleDay("Standard Schedule", "", items))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return days
    }

    fun parseTasks(content: String): List<String> {
        val taskList = mutableListOf<String>()
        try {
            val yaml = Yaml()
            val data = yaml.load<Any>(content) as? Map<*, *> ?: return emptyList()
            
            val tasks = data["tasks"] as? Map<*, *>
            (tasks?.get("pending") as? List<*>)?.forEach { taskList.add(it.toString()) }
            
            val backlogs = data["backlogs"] as? Map<*, *>
            backlogs?.forEach { (subject, list) ->
                (list as? List<*>)?.forEach {
                    taskList.add("$subject: $it")
                }
            }
        } catch(e: Exception) {}
        return taskList
    }

    fun parseName(content: String): String? {
        try {
            val yaml = Yaml()
            val data = yaml.load<Any>(content) as? Map<*, *> ?: return null
            val user = data["user"] as? Map<*, *>
            return user?.get("name")?.toString()
        } catch(e: Exception) {}
        return null
    }
}
