package com.example.keevalidate.validator

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import java.lang.StringBuilder

class KeeValidate private constructor(val tag: String) : LifecycleObserver {

    private var isValidationStarted = false

    /**
     *  event-driven functions
     */
    private val observables = ArrayList<ObservableText>()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun startValidation() {
        if (!isValidationStarted) {
            isValidationStarted = true

            observables.forEach {
                it.startWatching()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun stopValidation() {
        if (isValidationStarted) {

            isValidationStarted = false
            observables.forEach {
                it.stopWatching()
            }
        }
    }

    /**
     *  functions for use
     */

    fun cancelValidation() {
        stopValidation()
        observables.clear()
    }

    fun getFormStatus(): ValidationStatus {
        observables.forEach { observable ->
            if (observable.isValid != ValidationStatus.VALID) {
                return ValidationStatus.INVALID
            }
        }
        return ValidationStatus.VALID
    }


    /**
     *  Builder
     */

    inner class Builder (private val observableText: ObservableText) {

        /**
         *  used for adding the observable EditText into observables
         */
        fun build(): KeeValidate {
            observableText.apply {
                if (observer == null && onStatusChange == null) {
                    onStatusChange = { status, editText ->
                       /* if (status == ValidationStatus.VALID) {
                            editText.setBackgroundResource(R.drawable.edit_text_drawable)
                        } else {
                            editText.setBackgroundResource(R.drawable.error_edit_text_drawable)
                        }*/

                    }
                }
            }
            observables.add(observableText)
            return this@KeeValidate
        }

        /**
         *  is used to return the status to the consumer when something happens
         */
        fun observe(observer: Observer): Builder {
            observableText.observer = observer
            return this
        }

        fun observe(onStatusChanged: (status: ValidationStatus, editText: EditText) -> Unit): Builder {
            observableText.onStatusChange = onStatusChanged
            return this
        }

        /**
         *  default Rules
         */

        fun forPasswordLength(length: Int, errorMessage: String?): Builder {
            val rule = object : Rule() {
                override fun validate(text: String): ValidationStatus {
                    return if (text.length >= length)
                        ValidationStatus.VALID
                    else
                        ValidationStatus.INVALID
                }

                override fun getErrorMessage(context: Context): String? {
                    return errorMessage
                }
            }

            observableText.addRule(rule)
            return this
        }

        fun forEmailPattern(errorMessage: String?): Builder {
            val rule = object : Rule() {
                override fun validate(text: String): ValidationStatus {
                    return if (Patterns.EMAIL_ADDRESS.matcher(text).matches())
                        ValidationStatus.VALID
                    else
                        ValidationStatus.INVALID
                }

                override fun getErrorMessage(context: Context): String? {
                    return errorMessage
                }
            }

            observableText.addRule(rule)
            return this
        }

        fun forNotEmpty(errorMessage: String?): Builder {
            val rule = object : Rule() {
                override fun validate(text: String): ValidationStatus {
                    return if (text.isNotEmpty() && text.isNotBlank())
                        ValidationStatus.VALID
                    else
                        ValidationStatus.INVALID
                }

                override fun getErrorMessage(context: Context): String? {
                    return errorMessage
                }
            }

            observableText.addRule(rule)
            return this
        }

        fun forPhonePattern(errorMessage: String?): Builder {
            val rule = object : Rule() {
                override fun validate(text: String): ValidationStatus {
                    return if (Patterns.PHONE.matcher(text).matches())
                        ValidationStatus.VALID
                    else
                        ValidationStatus.INVALID
                }

                override fun getErrorMessage(context: Context): String? {
                    return errorMessage
                }
            }

            observableText.addRule(rule)
            return this
        }

        fun forConfirmPasswordRule(editTextToMatch: EditText, errorMessage: String?): Builder {
            val rule = object : Rule() {
                override fun validate(text: String): ValidationStatus {
                    return if (text == editTextToMatch.editableText.toString()) {
                        ValidationStatus.VALID
                    } else {
                        ValidationStatus.INVALID
                    }
                }

                override fun getErrorMessage(context: Context): String? {
                    return errorMessage
                }
            }

            observableText.addRule(rule)
            return this
        }

        fun forCustomRule(rule: Rule): Builder {
            observableText.addRule(rule)
            return this
        }
    }

    /**
     *  returns a builder to add rules and observers
     */
    fun watch(editText: EditText): Builder {
        return Builder(ObservableText(editText))
    }

    /**
     *  Observables
     */


    /**
     *  an interface to be called when an EditText status changes
     */

    interface Observer {
        fun onStatusChanged(status: ValidationStatus, editText: EditText)
    }


    /**
     * A wrapper for the EditText that provides the observing functionality
     */
    class ObservableText(val editText: EditText) {
        var isValid = ValidationStatus.INVALID
        private var rules = ArrayList<Rule>()
        val promiseBoolean = PromiseBoolean()

        init {
            promiseBoolean.setPromise(700) {
                val text = editText.editableText?.toString() ?: ""

                val errorMsgBuilder = StringBuilder()

                isValid = ValidationStatus.VALID

                rules.forEach {
                    isValid = it.validate(text)

                    if (isValid == ValidationStatus.INVALID) {
                        if (errorMsgBuilder.isNotEmpty()) {
                            it.getErrorMessage(editText.context)
                                    ?.let { msg -> errorMsgBuilder.append("\n").append(msg) }
                        } else {
                            it.getErrorMessage(editText.context)?.let { msg -> errorMsgBuilder.append(msg) }
                        }
                    }
                }

                if (isValid == ValidationStatus.INVALID) {
                    if (errorMsgBuilder.isNotEmpty()) {
                        editText.error = errorMsgBuilder.toString()
                    }
                } else {
                    editText.error = null
                }

                onStatusChange?.invoke(isValid, editText)
                observer?.onStatusChanged(isValid, editText)
            }
        }

        var onStatusChange: ((status: ValidationStatus, editText: EditText) -> Unit)? = null
            set(value) {
                field = value
                if (value != null) {
                    observer = null
                }
            }

        var observer: Observer? = null
            set(value) {
                field = value
                if (value != null) {
                    onStatusChange = null
                }
            }


        fun addRule(rule: Rule): ObservableText {
            rules.add(rule)
            return this
        }

        fun stopWatching() {
            promiseBoolean.set(false)
            editText.removeTextChangedListener(watcher)
        }

        fun startWatching() {
            editText.addTextChangedListener(watcher)
        }

        private val watcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                promiseBoolean.set(true)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                editText.error = null
            }
        }
    }


    /**
     * Rule class : is responsible for providing the rule to verify against
     */
    abstract class Rule {

        abstract fun validate(text: String): ValidationStatus

        abstract fun getErrorMessage(context: Context): String?

    }

    /**
     * Status of the validation
     */
    enum class ValidationStatus {
        VALID,
        INVALID
    }


    /**
     * Creator Functions
     */
    companion object {
        //private val validators by lazy { HashMap<String, KeeValidate>() }

        /**
         *  Factory functions
         */

        fun with(fragment: Fragment): KeeValidate {
            return with(fragment, fragment::class.toString())
        }

        fun with(activity: AppCompatActivity): KeeValidate {
            return with(activity, activity::class.toString())
        }

        fun with(lifecycleOwner: LifecycleOwner, tag: String): KeeValidate {
            /*return (validators.getOrPut(tag) {
                KeeValidate(tag)
            }).also {
                lifecycleOwner.lifecycle.addObserver(it)
            }*/

            return KeeValidate(tag).also { lifecycleOwner.lifecycle.addObserver(it)  }
        }

        fun with(tag: String): KeeValidate {
            /*return validators.getOrPut(tag) {
                KeeValidate(tag)
            }*/
            return KeeValidate(tag)
        }
    }
}