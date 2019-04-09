package com.example.keevalidate.moltenadapter

import com.baianat.app.makfy.moltenadapter.ResId

@Retention(AnnotationRetention.RUNTIME)
@Target( AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
annotation class BindTo(@ResId val resId: Int)

