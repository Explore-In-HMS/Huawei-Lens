/*
 * Copyright 2020. Explore in HMS. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.hms.referenceapp.huaweilens.audio.media

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import com.hms.referenceapp.huaweilens.R
import com.hms.referenceapp.huaweilens.audio.utils.AudioTranscriptionUtils
import com.hms.referenceapp.huaweilens.common.Constants
import java.io.IOException
import java.util.*

class AudioPlayer : MediaPlayer() {
    var viewGroupPlayer: ViewGroup? = null
    var playerDurationTimeTextView: TextView? = null
    var playerFileTextView: TextView? = null
    var playerSeekBar: SeekBar? = null
    var playPauseButton: ImageButton? = null
    var isPlayingCompleted = false
    var fileName: String? = null
    private var rewindForwardLevel = 0
    private var playerTimer: Timer? = null
    private var playerTimerTask: TimerTask? = null
    lateinit var ctx: Context

    init {
        this.setOnPreparedListener{
            AudioTranscriptionUtils.setClickable(ctx as Activity, viewGroupPlayer!!, true)
        }
    }


    @SuppressLint("Recycle")
    fun prepareIt(playerFileUri: Uri) {
        try {
            this.reset()
            this.setDataSource(this.ctx, playerFileUri)
            this.prepare()
            this.fileName = if (playerFileUri.scheme.equals("content")) {
                val returnCursor = ctx.applicationContext.contentResolver.query(
                    playerFileUri, arrayOf(
                        MediaStore.Audio.AudioColumns.DISPLAY_NAME
                    ), null, null, null
                )!!.also {
                    it.moveToFirst()
                }
                returnCursor.getString(0)
            } else {
                playerFileUri.lastPathSegment.toString()
            }

            // Set Duration and Filename TextViews
            this.playerSeekBar?.max = this.duration
            this.playerDurationTimeTextView?.text =
                AudioTranscriptionUtils.strActiveTime(this.duration, this.duration)
            this.playerFileTextView?.text = this.fileName

            setLevelSeekingWithButton()
            this.setOnCompletionListener {
                playPauseButton!!.setImageResource(R.drawable.ic_play_arrow_white_48dp)
                isPlayingCompleted = true
            }

        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun playIt() {
        playPauseButton!!.setImageResource(R.drawable.ic_pause_white_48dp)
        this.start()

    }

    fun replay() {
        isPlayingCompleted = false
        this.seekTo(0)
        this.playIt()
    }

    fun pauseIt() {
        playPauseButton!!.setImageResource(R.drawable.ic_play_arrow_white_48dp)
        this.pause()
    }

    fun seekBackward() {
        val seekBackward = this.currentPosition - rewindForwardLevel
        this.seekTo(seekBackward)
        playerSeekBar?.progress = seekBackward
    }

    fun seekForward() {
        val seekBackward = this.currentPosition + rewindForwardLevel
        this.seekTo(seekBackward)
        playerSeekBar?.progress = seekBackward
    }

    fun runSeekBar(seekBarRunner: Runnable) {
        resetSeekBarTimer()
        playerTimer = Timer()
        playerTimerTask = object : TimerTask() {
            override fun run() {
                (ctx as Activity).runOnUiThread(seekBarRunner)
            }
        }
        playerTimer?.scheduleAtFixedRate(
            playerTimerTask,
            0,
            Constants.AUDIO_PLAYER_RUN_PER_MILLIS.toLong()
        )
    }

    fun resetSeekBarTimer() {
        if (playerTimer != null && playerTimer is Timer) {
            playerTimer!!.cancel()
            playerTimerTask!!.cancel()
            playerTimer = null
            playerTimerTask = null
        }
    }

    private fun setLevelSeekingWithButton() {
        // set seeking value in milliseconds when users clicks on rewind/forward buttons
        if (this.duration > 10000) {
            rewindForwardLevel = Constants.AUDIO_PLAYER_SEEK_LEVEL_1
        } else if (this.duration <= 10000) {
            rewindForwardLevel = Constants.AUDIO_PLAYER_SEEK_LEVEL_2
        }
    }

}