package com.stickman.comedystudio

data class Scene(val title: String, val line: String, val action: String, val mood: String, val seconds: Int = 6)

object StoryEngine {
    fun makeStory(idea: String): List<Scene> {
        val topic = idea.trim().ifBlank { "الذكاء الاصطناعي" }
        return listOf(
            Scene("بداية الحكاية", "أنا عندي فكرة عبقرية عن $topic!", "phone", "happy"),
            Scene("الثقة الزيادة", "سيب الموضوع ليا... أنا خبير عالمي!", "robot", "proud"),
            Scene("أول اختبار", "طيب ورّينا شطارتك وشغّل المروحة!", "fan", "curious"),
            Scene("الكارثة", "تم بنجاح... هو ليه مفيش هوا؟!", "fail", "shocked"),
            Scene("الرد القاتل", "إنت محتاج تحديث... مش محتاج صلاحيات!", "laugh", "laugh"),
        )
    }
}
