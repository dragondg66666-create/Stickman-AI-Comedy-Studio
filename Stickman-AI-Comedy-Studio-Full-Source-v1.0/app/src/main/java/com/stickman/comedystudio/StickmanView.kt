package com.stickman.comedystudio

import android.content.Context
import android.graphics.*
import android.view.View
import kotlin.math.sin

class StickmanView(context: Context) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    var sceneIndex = 0
    var progress = 0f
    private val bg = Paint().apply { color = Color.rgb(247, 249, 252) }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat(); val h = height.toFloat()
        canvas.drawRect(0f, 0f, w, h, bg)
        p.color = Color.rgb(226, 233, 242)
        canvas.drawRoundRect(w*.08f,h*.72f,w*.92f,h*.75f,18f,18f,p)
        // desk
        p.color = Color.rgb(166, 125, 91)
        canvas.drawRoundRect(w*.12f,h*.68f,w*.88f,h*.72f,12f,12f,p)
        p.color = Color.rgb(35,42,55)
        canvas.drawRoundRect(w*.40f,h*.54f,w*.62f,h*.675f,12f,12f,p)
        p.color = Color.rgb(75,145,235)
        canvas.drawRoundRect(w*.415f,h*.555f,w*.605f,h*.65f,6f,6f,p)
        p.color = Color.WHITE
        p.textSize = w*.025f
        canvas.drawText("AI",w*.49f,h*.61f,p)
        val bob = sin(progress * Math.PI * 2).toFloat() * h*.008f
        drawPerson(canvas,w*.28f,h*.48f+bob,w*.105f,sceneIndex==4)
        drawRobot(canvas,w*.73f,h*.49f-bob,sceneIndex)
        p.color = Color.rgb(35,45,60)
        p.textSize = w*.045f
        p.typeface = Typeface.create("sans-serif",Typeface.BOLD)
        val caption = when(sceneIndex) {
            0 -> "عندي فكرة عبقرية!"
            1 -> "سيبها عليّ.. أنا خبير!"
            2 -> "شغّل المروحة يا عبقري"
            3 -> "تم بنجاح... تقريبًا!"
            else -> "إنت محتاج تحديث 😂"
        }
        val tw=p.measureText(caption)
        p.color=Color.WHITE
        canvas.drawRoundRect(w/2-tw/2-w*.04f,h*.12f,w/2+tw/2+w*.04f,h*.21f,24f,24f,p)
        p.color=Color.rgb(30,40,55)
        canvas.drawText(caption,w/2-tw/2,h*.18f,p)
    }

    private fun line(c:Canvas,x1:Float,y1:Float,x2:Float,y2:Float,sw:Float= wStroke()) {
        p.color=Color.rgb(25,28,34); p.strokeWidth=sw; p.strokeCap=Paint.Cap.ROUND; p.style=Paint.Style.STROKE
        c.drawLine(x1,y1,x2,y2,p); p.style=Paint.Style.FILL
    }
    private fun wStroke()=width*.012f
    private fun drawPerson(c:Canvas,x:Float,y:Float,r:Float,laugh:Boolean) {
        p.color=Color.WHITE; c.drawCircle(x,y,r,p)
        p.color=Color.BLACK; p.style=Paint.Style.STROKE; p.strokeWidth=width*.006f; c.drawCircle(x,y,r,p); p.style=Paint.Style.FILL
        p.color=Color.BLACK; c.drawCircle(x-r*.32f,y-r*.1f,r*.07f,p); c.drawCircle(x+r*.32f,y-r*.1f,r*.07f,p)
        p.style=Paint.Style.STROKE; p.strokeWidth=width*.008f
        c.drawArc(x-r*.28f,y+r*.05f,x+r*.28f,y+r*.48f,10f,160f,false,p); p.style=Paint.Style.FILL
        line(c,x,y+r,x,y+r*2.7f); line(c,x,y+r*1.5f,x-r*.8f,y+r*2.15f); line(c,x,y+r*1.5f,x+r*.8f,y+r*2.1f)
        line(c,x,y+r*2.7f,x-r*.65f,y+r*3.7f); line(c,x,y+r*2.7f,x+r*.65f,y+r*3.7f)
    }
    private fun drawRobot(c:Canvas,x:Float,y:Float,index:Int) {
        val r=width*.07f
        p.color=Color.rgb(67,153,235); c.drawRoundRect(x-r,y-r,x+r,y+r, r*.3f,r*.3f,p)
        p.color=Color.rgb(15,30,50); c.drawRoundRect(x-r*.75f,y-r*.55f,x+r*.75f,y+r*.35f,r*.2f,r*.2f,p)
        p.color=Color.CYAN; c.drawCircle(x-r*.32f,y-r*.08f,r*.1f,p); c.drawCircle(x+r*.32f,y-r*.08f,r*.1f,p)
        p.color=Color.rgb(67,153,235); c.drawRoundRect(x-r*1.25f,y-r*.35f,x-r,y+r*.25f,8f,8f,p); c.drawRoundRect(x+r,y-r*.35f,x+r*1.25f,y+r*.25f,8f,8f,p)
        line(c,x-r*.55f,y+r,x-r*.7f,y+r*1.9f,width*.015f); line(c,x+r*.55f,y+r,x+r*.7f,y+r*1.9f,width*.015f)
        p.color=Color.rgb(250,190,45); c.drawCircle(x,y-r*1.2f,r*.12f,p)
    }

    fun renderFrame(target: Bitmap, index: Int, t: Float) {
        sceneIndex=index; progress=t
        val c=Canvas(target); draw(c)
    }
}
