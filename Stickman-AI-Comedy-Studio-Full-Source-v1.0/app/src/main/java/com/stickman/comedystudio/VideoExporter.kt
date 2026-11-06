package com.stickman.comedystudio

import android.content.Context
import android.graphics.Bitmap
import android.media.*
import android.os.Environment
import java.io.File
import java.nio.ByteBuffer

/**
 * Exports silent H.264 MP4 using Android MediaCodec. Device encoder support varies.
 * Portrait 360x640 is intentionally lightweight for phones with 4 GB RAM.
 */
object VideoExporter {
    fun export(context: Context, view: StickmanView, done: (Result<File>) -> Unit) {
        Thread {
            try {
                val width=360; val height=640; val fps=15; val seconds=30
                val format=MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,width,height).apply {
                    setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
                    setInteger(MediaFormat.KEY_BIT_RATE, 900_000)
                    setInteger(MediaFormat.KEY_FRAME_RATE,fps)
                    setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,2)
                }
                val codec=MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
                codec.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE)
                codec.start()
                val info=MediaCodec.BufferInfo()
                val muxerFile=File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),"StickmanComedy_${System.currentTimeMillis()}.mp4")
                val muxer=MediaMuxer(muxerFile.absolutePath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                var track=-1; var muxerStarted=false
                val started = booleanArrayOf(false)
                val bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
                val canvasView=view
                val total=fps*seconds
                for (frame in 0 until total) {
                    val inputIndex=codec.dequeueInputBuffer(10000)
                    if(inputIndex>=0) {
                        val input=codec.getInputBuffer(inputIndex)!!
                        input.clear()
                        synchronized(canvasView) {
                            canvasView.layout(0,0,width,height)
                            canvasView.renderFrame(bitmap, (frame/(fps*6)).coerceAtMost(4), (frame%fps).toFloat()/fps)
                        }
                        val yuv=argbToNv12(bitmap)
                        input.put(yuv)
                        codec.queueInputBuffer(inputIndex,0,yuv.size,frame*1_000_000L/fps,0)
                    }
                    drain(codec,muxer,info,false,{t -> if(track<0){track=t; muxer.start(); muxerStarted=true; started[0]=true}},started)
                }
                val end=codec.dequeueInputBuffer(10000)
                if(end>=0) codec.queueInputBuffer(end,0,0,total*1_000_000L/fps,MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                drain(codec,muxer,info,true,{t -> if(track<0){track=t; muxer.start(); muxerStarted=true; started[0]=true}},started)
                if(muxerStarted) muxer.stop()
                muxer.release(); codec.stop(); codec.release(); bitmap.recycle()
                done(Result.success(muxerFile))
            } catch(e:Exception) { done(Result.failure(e)) }
        }.start()
    }

    private fun drain(codec:MediaCodec,muxer:MediaMuxer,info:MediaCodec.BufferInfo,end:Boolean,onFormat:(Int)->Unit,started:BooleanArray) {
        while(true) {
            val out=codec.dequeueOutputBuffer(info,if(end)10000 else 0)
            when {
                out==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> onFormat(muxer.addTrack(codec.outputFormat))
                out>=0 -> {
                    val b=codec.getOutputBuffer(out)!!
                    if(info.size>0 && started[0]) { b.position(info.offset); b.limit(info.offset+info.size); muxer.writeSampleData(0,b,info) }
                    codec.releaseOutputBuffer(out,false)
                    if(info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) return
                }
                else -> if(!end || out==MediaCodec.INFO_TRY_AGAIN_LATER) return
            }
        }
    }
    private fun muxerStartedSafe(m:MediaMuxer)=true

    private fun argbToNv12(bitmap:Bitmap):ByteArray {
        val w=bitmap.width; val h=bitmap.height
        val pixels=IntArray(w*h); bitmap.getPixels(pixels,0,w,0,0,w,h)
        val out=ByteArray(w*h*3/2); var yIndex=0; var uvIndex=w*h
        for(j in 0 until h) for(i in 0 until w) {
            val c=pixels[j*w+i]; val r=(c shr 16) and 255; val g=(c shr 8) and 255; val b=c and 255
            val y=((66*r+129*g+25*b+128) shr 8)+16
            val u=((-38*r-74*g+112*b+128) shr 8)+128
            val v=((112*r-94*g-18*b+128) shr 8)+128
            out[yIndex++]=y.coerceIn(0,255).toByte()
            if(j%2==0 && i%2==0 && uvIndex+1<out.size) { out[uvIndex++]=u.coerceIn(0,255).toByte(); out[uvIndex++]=v.coerceIn(0,255).toByte() }
        }
        return out
    }
}
