package com.library.KTLibrary.authPhoneNumber

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.CountDownTimer
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.library.KTLibrary.R
import com.library.KTLibrary.dialog.BasicDialog
import com.library.KTLibrary.base.MyEditText
import com.library.KTLibrary.base.MyToast
import com.library.KTLibrary.databinding.AuthPhoneBinding
import com.library.KTLibrary.dialog.ActionCancelDialogListener
import com.library.KTLibrary.dialog.ActionDialogListener
import com.library.KTLibrary.server.RetrofitServerController
import com.library.KTLibrary.server.checkError
import com.library.KTLibrary.server.checkIOError
import com.library.KTLibrary.server.checkTimeOut
import com.library.KTLibrary.server.requestFalse
import com.library.KTLibrary.server.requestTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.ibrahimsn.lib.PhoneNumberKit


class AuthPhoneNumber(phoneNumber: String? = null, context: Context, private val checkDup: Boolean): Dialog(context), OnCountryClickListener {
    interface OnSuccessAuthPhoneNumber {
        fun onSuccessCallback(phoneNumber: String)
    }

    interface OnSuccessCancelAuthPhoneNumber {
        fun onSuccessCallback(phoneNumber: String)
        fun onCancelCallback()
    }

    private val dialogScope = CoroutineScope(Dispatchers.Main + Job())

    private var _binding: AuthPhoneBinding? = null
    private val binding get() = _binding!!

    private var authOnSuccess: OnSuccessAuthPhoneNumber? = null
    private var cancelOnSuccess: OnSuccessCancelAuthPhoneNumber? = null

    private val phoneNumberKit = PhoneNumberKit.Builder(context).build()
    private var countryMap: MutableMap<String, Int>? = null

    private var countryCode: Int = 82

    private var phoneNumber: String? = null
    private var authNumber: String? = null

    private var phoneNumberRegex: Boolean = false
    private var authNumberRegex: Boolean = false

    private var checkSendSMS: Boolean = false
    private var timer: CountDownTimer? = null



    init {
        _binding = AuthPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setDialog(phoneNumber)

        setDialogSize()

        initCountry()
    }


    override fun dismiss() {
        dialogScope.cancel()
        super.dismiss()
    }



    /**
     * interface
     */
    fun onSuccess(listener: OnSuccessAuthPhoneNumber) {
        authOnSuccess = listener
    }

    fun onCancel(listener: OnSuccessCancelAuthPhoneNumber) {
        cancelOnSuccess = listener
    }





    /**
     * init Country
     */
    private fun initCountry() {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simCountryIso = telephonyManager.simCountryIso
        val iconDrawable: Drawable?
        val editTextHint: String?

        if (countryMap == null) setCountyList()

        if (simCountryIso.isNotEmpty()) {
            val name = simCountryIso.uppercase()
            val code = countryMap!![name]

            editTextHint = "(+$code)"
            iconDrawable = phoneNumberKit.getFlagIcon(simCountryIso)

            binding.authPhoneNationalButton.text = name
            countryCode = code!!
        } else {
            editTextHint = "(+82)"
            iconDrawable = phoneNumberKit.getFlagIcon("kr")
        }

        binding.authPhoneIcon.setImageDrawable(iconDrawable)
        binding.authPhoneEdittext.hint = editTextHint
    }





    /**
     * Set User interface
     */
    private fun setDialog(phoneNumber: String?) {
        // EditText
        phoneNumberEditText(phoneNumber)

        // Button
        nationalButtonEvent()
        verifyButtonEvent()

        okButtonEvent()
        cancelButtonEvent()
    }


    /**
     * National Button Event
     * Country List set Visibility
     */
    private fun nationalButtonEvent() {
        binding.authPhoneNationalButton.setOnClickListener {
            setListVisibility()
        }
    }


    /**
     * Verify Button Event
     * - phoneNumberRegex
     * - true: Second Terms SignUp, Check Phone Number Dup
     * - false: Send SMS
     */
    private fun verifyButtonEvent() {
        binding.authPhoneVerifyButton.setOnClickListener {
            MyEditText.closeKeyboard(context, binding.authPhoneEdittext)

            if (phoneNumberRegex) {
                if (checkDup) checkDupPhoneNumber()
                else sendSMS()
            } else MyToast.showToast(context, context.resources.getString(R.string.setupGuardianTxt))
        }
    }


    /**
     * Compare Auth Number Button Event
     */
    private fun okButtonEvent() {
        binding.authPhoneOkButton.setOnClickListener {
            binding.authPhoneVerifyEdittext.let { MyEditText.closeKeyboard(context, binding.authPhoneVerifyEdittext) }

            if (authNumberRegex) checkVerifyCode()
            else Toast.makeText(context, context.resources.getText(R.string.authHelpText), Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * Cancel Button Event
     */
    private fun cancelButtonEvent() {
        binding.authPhoneCancelButton.setOnClickListener {
            cancelOnSuccess?.onCancelCallback()
            dismiss()
        }
    }


    /**
     * PhoneNumber EditText
     */
    private fun phoneNumberEditText(number: String?) {
        val phoneNumberPattern = "^[0-9]{6,12}$"

        setPhoneNumber(number, phoneNumberPattern)

        binding.authPhoneEdittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                phoneNumber = s.toString()
                phoneNumberRegex = phoneNumber!!.trim { it <= ' ' }.matches(phoneNumberPattern.toRegex())
            }
        })

        binding.authPhoneEdittext.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus && checkSendSMS) {
                // Resend Event
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                v.clearFocus()
                showReSendDialog()
            }
        }
    }

    private fun setPhoneNumber(number: String?, phoneNumberPattern: String) {
        number?.let {
            phoneNumber = it
            phoneNumberRegex = phoneNumber!!.trim { it <= ' ' }.matches(phoneNumberPattern.toRegex())
            binding.authPhoneEdittext.setText(it)
            binding.authPhoneEdittext.isEnabled = false
        }
    }

    private fun showReSendDialog() {
        BasicDialog(
            body = context.resources.getString(R.string.reSendHelpText),
            ok = context.resources.getString(R.string.ok),
            back = context.resources.getString(R.string.cancel),
            context = context,
            type = BasicDialog.DialogType.ACTION_CANCEL
        ).apply {
            setActionCancelButtonListener(object : ActionCancelDialogListener {
                override fun onCancel() { dismiss() }
                override fun onConfirm() {
                    dismiss()

                    timer?.onFinish()

                    binding.authPhoneEdittext.text.clear()
                    binding.authPhoneEdittext.requestFocus()

                    binding.authPhoneEdittext.postDelayed({
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(binding.authPhoneEdittext, InputMethodManager.SHOW_IMPLICIT)
                    }, 100)
                }
            })
            setCancelable(false)
        }.show()
    }



    /**
     * Auth Number EditText
     */
    private fun authEditTextEvent() {
        val authNumberPattern = "^[0-9]{6}$"

        binding.authPhoneVerifyEdittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                authNumber = s.toString()
                authNumberRegex = authNumber!!.trim { it <= ' ' }.matches(authNumberPattern.toRegex())
            }
        })

        binding.authPhoneVerifyEdittext.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.authPhoneOkButton.callOnClick()
                true
            } else {
                false
            }
        }
    }



    /**
     * Check Dup PhoneNumber
     * 회원가입 등록된 번호 확인
     */
    private fun checkDupPhoneNumber() {
        phoneNumber?.let { phoneNumber ->
            dialogScope.launch {
                RetrofitServerController.executeRequest(
                    RetrofitServerController.HTTPMethod.GET,
                    RetrofitServerController.EndPoint.GET_CHECK_PHONE_NUMBER.endPoint,
                    null,
                    hashMapOf("phone" to phoneNumber)
                )?.let { result ->
                    Log.i("GET_CHECK_PHONE_NUMBER", result)

                    when {
                        checkError(result) || checkIOError(result) -> showDialog(context.resources.getString(R.string.serverError))
                        requestFalse(result) -> showDialog(context.resources.getString(R.string.dupPhoneNumber))
                        requestTrue(result) -> sendSMS()
                    }
                }
            }
        }
    }

    private fun showDialog(body: String) {
        BasicDialog(
            title = context.resources.getString(R.string.noti), body = body, ok = context.resources.getString(R.string.ok),
            context = context,
            type = BasicDialog.DialogType.ACTION
        ).apply {
            setActionButtonListener(object : ActionDialogListener {
                override fun onConfirm() { dismiss() }
            })
            setCancelable(false)
        }.show()
    }




    /**
     * SendSMS
     */
    private fun sendSMS() {
        val mapParam: MutableMap<String, String> = hashMapOf(
            "phone" to phoneNumber!!,
            "nationalCode" to countryCode.toString()
        )

        binding.authPhoneEdittext.clearFocus()
        isEnabled(binding.authPhoneVerifyButton)

        dialogScope.launch {
            RetrofitServerController.executeRequest(
                RetrofitServerController.HTTPMethod.GET,
                RetrofitServerController.EndPoint.GET_SEND_SMS.endPoint,
                null,
                mapParam
            )?.let { result ->
                Log.i("sendSMS Response", result)

                checkSendSMS = true

                when {
                    checkError(result) || checkIOError(result) || checkTimeOut(result) -> smsEvent(null)
                    else -> smsEvent(result)
                }
            }
        }
    }


    private fun smsEvent(result: String?) {
        var toastMessage: CharSequence = context.resources.getText(R.string.serverError)

        result?.let {
            if (result.contains("true")) {
                toastMessage = context.resources.getText(R.string.sendVerification)
                authUserInterfaceVisibility()
                startTimer()
            }
        } ?: run {
            isEnabled(binding.authPhoneVerifyButton)
        }

        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    }



    private fun authUserInterfaceVisibility() {
        val authHelpText = findViewById<TextView>(R.id.auth_phone_verify_text)
        val underline = findViewById<View>(R.id.auth_phone_verify_underline)

        binding.authPhoneVerifyEdittext.visibility = View.VISIBLE

        authHelpText.visibility = View.VISIBLE
        underline.visibility = View.VISIBLE

        authEditTextEvent()
    }



    private fun startTimer() {
        timer = object : CountDownTimer(180000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                val time = String.format("%02d:%02d", minutes, seconds)
                binding.authPhoneVerifyButton.text = time
            }
            override fun onFinish() { timerOnFinishEvent() }
        }

        timer?.start()
    }

    private fun timerOnFinishEvent() {
        timer?.cancel()
        checkSendSMS = false
        isEnabled(binding.authPhoneVerifyButton)
        binding.authPhoneVerifyButton.text = context.resources.getText(R.string.reSend)
    }

    /**
     * Check SMS
     */
    private fun checkVerifyCode() = runBlocking {
        val mapParam: MutableMap<String, String> = hashMapOf(
            "phone" to phoneNumber!!,
            "code" to authNumber!!
        )

        setLoadingBar(true)

        RetrofitServerController.executeRequest(
            RetrofitServerController.HTTPMethod.GET,
            RetrofitServerController.EndPoint.GET_CHECK_SMS.endPoint,
            null,
            mapParam
        )?.let { result ->
            Log.i("checkSMSCode Response", result)

            setLoadingBar(false)

            when {
                checkError(result) || checkIOError(result) || checkTimeOut(result) -> MyToast.showToast(context, context.resources.getString(R.string.serverError))
                else -> verifyCodeEvent(result)
            }
        }
    }



    private fun verifyCodeEvent(result: String) {
        if (result.contains("true")) {

            binding.authPhoneOkButton.isEnabled = false

            authOnSuccess?.onSuccessCallback(phoneNumber!!)
            cancelOnSuccess?.onSuccessCallback(phoneNumber!!)

            dismiss()

        } else MyToast.showToast(context, context.resources.getString(R.string.confirmVerification))
    }



    private fun isEnabled(button: Button) {
        button.isEnabled = !button.isEnabled
    }



    /**
     * Set RecyclerView Visibility
     */
    private fun setListVisibility() {
        if (binding.countriesRecyclerView.visibility == View.VISIBLE) {
            binding.countriesRecyclerView.visibility = View.GONE
        } else binding.countriesRecyclerView.visibility = View.VISIBLE
    }


    /**
     * Set Country List
     */
    private fun setCountyList() {
        val phoneNumberUtil = PhoneNumberUtil.getInstance()

        countryMap = mutableMapOf()

        val countriesList = phoneNumberUtil.supportedRegions.map { regionCode ->
            val countryCode = phoneNumberUtil.getCountryCodeForRegion(regionCode)
            phoneNumberKit.getFlagIcon(regionCode.lowercase())?.let { icon ->
                countryMap!![regionCode] = countryCode
                CountryInfo(icon, regionCode, countryCode)
            }
        }

        binding.countriesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.countriesRecyclerView.adapter = CountriesAdapter(context, countriesList,this)
    }


    override fun onCountryClick(countryName: String, countryCode: Int) {
        binding.authPhoneIcon.setImageDrawable(phoneNumberKit.getFlagIcon(countryName.lowercase()))
        binding.authPhoneNationalButton.text = countryName
        binding.authPhoneEdittext.hint = "(+$countryCode)"

        this.countryCode = countryCode

        setListVisibility()
    }



    /**
     * Set UI
     */
    private fun setDialogSize() {
        // Full Screen
        window?.let { window ->
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(window.attributes)
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            window.attributes = layoutParams
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setGravity(Gravity.CENTER)
        }
    }

    private fun setLoadingBar(flag: Boolean) {
        if (flag) binding.authPhoneProgressBar.visibility = View.VISIBLE
        else binding.authPhoneProgressBar.visibility = View.GONE
    }
}