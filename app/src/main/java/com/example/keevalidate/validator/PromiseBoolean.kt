package com.example.keevalidate.validator

import android.os.CountDownTimer

class PromiseBoolean {
    private var value: Boolean = false

    private var setDate: Long

    private var millisInFuture = 0L

    private var doOnPromise: () -> Unit = {}

    private var timer: CountDownTimer? = null

    init {
        setDate = System.currentTimeMillis()
    }


    fun set(value: Boolean) {
        this.value = value
        setDate = System.currentTimeMillis()

        timer?.cancel()
        timer = null

        if (value) {
            timer = object : CountDownTimer(millisInFuture, millisInFuture) {
                override fun onFinish() {
                    val timePassed = System.currentTimeMillis() - setDate
                    if (value && timePassed >= millisInFuture) {
                        doOnPromise()
                    }
                }

                override fun onTick(p0: Long) {

                }
            }

            timer?.start()
        }
    }

    fun setPromise(millisInFuture: Long, doOnPromise: () -> Unit) {
        this.millisInFuture = millisInFuture
        this.doOnPromise = doOnPromise
    }

    fun setPromiseTime(millisInFuture: Long) {
        this.millisInFuture = millisInFuture
    }
}