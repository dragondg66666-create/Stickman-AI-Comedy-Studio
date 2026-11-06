package com.stickman.comedystudio

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import android.speech.tts.TextToSpeech
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var preview: StickmanView
    private lateinit var status: TextView
    private lateinit var speech: TextToSpeech
    private var scenes=StoryEngine.makeStory("الذكاء الاصطناعي")
    private var current=0
    private var animating=false

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        speech=TextToSpeech(this,this)
        val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(28,24,28,20); setBackgroundColor(Color.rgb(246,248,252)) }
        val title=TextView(this).apply { text="🎬 Stickman AI Comedy Studio"; textSize=23f; setTextColor(Color.rgb(24,38,60)); gravity=Gravity.CENTER; setPadding(0,8,0,18) }
        root.addView(title)
        val subtitle=TextView(this).apply { text="حوّل فكرتك إلى قصة كرتونية كوميدية"; textSize=16f; gravity=Gravity.CENTER; setTextColor(Color.DKGRAY) }
        root.addView(subtitle)
        val idea=EditText(this).apply { hint="اكتب فكرة الفيديو هنا..."; setText("الذكاء الاصطناعي قرر يحكم العالم"); textSize=16f; setPadding(18,12,18,12); setBackgroundColor(Color.WHITE) }
        root.addView(idea,LinearLayout.LayoutParams(-1,58).apply { topMargin=16; bottomMargin=12 })
        val create=button("✨ إنشاء القصة")
        root.addView(create)
        preview=StickmanView(this)
        root.addView(preview,LinearLayout.LayoutParams(-1,0,1f).apply { topMargin=14; bottomMargin=12 })
        status=TextView(this).apply { text="جاهز لإنشاء فيديو كوميدي"; textSize=14f; gravity=Gravity.CENTER; setTextColor(Color.DKGRAY); setPadding(0,8,0,8) }
        root.addView(status)
        val row=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        val play=button("▶ تشغيل / إيقاف")
        val voice=button("🔊 نطق الحوار")
        row.addView(play,LinearLayout.LayoutParams(0,54,1f).apply { rightMargin=5 })
        row.addView(voice,LinearLayout.LayoutParams(0,54,1f).apply { leftMargin=5 })
        root.addView(row)
        val export=button("📽 تصدير فيديو MP4 (30 ثانية)")
        root.addView(export,LinearLayout.LayoutParams(-1,58).apply { topMargin=10 })
        setContentView(root)
        create.setOnClickListener { scenes=StoryEngine.makeStory(idea.text.toString()); current=0; preview.sceneIndex=0; preview.invalidate(); status.text="تم إنشاء ${scenes.size} مشاهد محليًا" }
        play.setOnClickListener {
            animating=!animating
            if(animating) runAnimation() else status.text="تم إيقاف المعاينة"
        }
        voice.setOnClickListener { val s=scenes[current]; speech.speak(s.line,TextToSpeech.QUEUE_FLUSH,null,"scene$current") }
        export.setOnClickListener {
            export.isEnabled=false; status.text="جاري تجهيز الفيديو... قد يستغرق عدة دقائق"
            VideoExporter.export(this,preview) { result -> runOnUiThread {
                export.isEnabled=true
                status.text=result.fold({ "تم الحفظ: ${it.absolutePath}" },{ "تعذر التصدير على هذا الجهاز: ${it.message}" })
            } }
        }
    }
    private fun button(label:String)=Button(this).apply { text=label; textSize=14f; isAllCaps=false }
    private fun runAnimation() {
        if(!animating) return
        preview.sceneIndex=current
        preview.progress=(preview.progress+.04f)%1f
        preview.invalidate()
        if(preview.progress<.05f) { current=(current+1)%scenes.size; speech.speak(scenes[current].line,TextToSpeech.QUEUE_FLUSH,null,"auto$current") }
        preview.postDelayed({runAnimation()},100)
    }
    override fun onInit(statusCode:Int) { if(statusCode==TextToSpeech.SUCCESS) speech.language=Locale("ar") }
    override fun onDestroy() { if(::speech.isInitialized) { speech.stop(); speech.shutdown() }; super.onDestroy() }
}
