package com.library.KTLibrary.dialog

import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.library.KTLibrary.R
import com.library.KTLibrary.databinding.DialogBasicActionBinding
import com.library.KTLibrary.databinding.DialogBasicBinding
import com.library.KTLibrary.databinding.DialogBasicCancelActionBinding
import com.library.KTLibrary.databinding.DialogBasicEditTextBinding
import com.library.KTLibrary.databinding.DialogEmergencyActionBinding

class BasicDialog(
    title: String? = null, body: String? = null,
    ok: String? = null, back: String? = null,
    hint: String? = null,
    height: Double? = null,
    weight: Int? = null,
    context: Context,
    type: DialogType
) : Dialog(context) {

    enum class DialogType {
        BASIC, ACTION, ACTION_CANCEL, EDIT_TEXT, EMERGENCY
    }

    private var emergencyBinding: DialogEmergencyActionBinding? = null
    private var editTextBinding: DialogBasicEditTextBinding? = null

    private var inputString: String? = null

    // Interface
    private var actionDialogListener: ActionDialogListener? = null
    private var actionCancelDialogListener: ActionCancelDialogListener? = null
    private var editTextDialogListener: EditTextDialogListener? = null

    init {
        val inflater = LayoutInflater.from(context)

        when(type) {
            DialogType.BASIC -> {
                val binding = DialogBasicBinding.inflate(inflater)
                setBasicDialogUI(title, body, binding)
            }

            DialogType.ACTION -> {
                val binding = DialogBasicActionBinding.inflate(inflater)
                setActionDialogUI(title, body, ok, binding)
            }

            DialogType.ACTION_CANCEL -> {
                val binding = DialogBasicCancelActionBinding.inflate(inflater)
                setCancelDialogUI(body, ok, back, binding)
            }

            DialogType.EDIT_TEXT -> {
                editTextBinding = DialogBasicEditTextBinding.inflate(inflater)
                setEditTextDialogUI(title, body, ok, back, hint)
            }

            DialogType.EMERGENCY -> {
                emergencyBinding = DialogEmergencyActionBinding.inflate(inflater)
                setEmergencyDialogUI(title, body, ok)
            }
        }

        setDialog(height, weight)
    }

    fun setActionButtonListener(listener: ActionDialogListener) {
        actionDialogListener = listener
    }

    fun setActionCancelButtonListener(listener: ActionCancelDialogListener) {
        actionCancelDialogListener = listener
    }

    fun setEditTextButtonListener(listener: EditTextDialogListener) {
        editTextDialogListener = listener
    }

    /**
     * Basic Dialog
     * */
    private fun setBasicDialogUI(title: String?, body: String?, binding: DialogBasicBinding) {
        binding.dialogBasicTitle.text = title ?: "Notification"
        binding.dialogBasicBody.text = body ?: "isEmpty"
        setContentView(binding.root)
    }

    /**
     * Action Dialog
     * */
    private fun setActionDialogUI(title: String?, body: String?, ok: String?, binding: DialogBasicActionBinding) {
        binding.dialogBasicTitle.text = title ?: "Notification"
        binding.dialogBasicBody.text = body ?: "isEmpty"
        binding.dialogBasicButton.text = ok ?: "OK"

        binding.dialogBasicButton.setOnClickListener { actionDialogListener?.onConfirm() }

        setContentView(binding.root)
    }

    /**
     * Cancel Dialog
     * */
    private fun setCancelDialogUI(body: String?, ok: String?, back: String?, binding: DialogBasicCancelActionBinding) {
        binding.dialogBasicBody.text = body ?: "isEmpty"
        binding.dialogBasicButton.text = ok ?: "OK"
        binding.dialogBasicCancelButton.text = back ?: "CANCEL"

        binding.dialogBasicButton.setOnClickListener { actionCancelDialogListener?.onConfirm() }
        binding.dialogBasicCancelButton.setOnClickListener { actionCancelDialogListener?.onCancel() }

        setContentView(binding.root)
    }

    /**
     * EditText Dialog
     * */
    private fun setEditTextDialogUI(title: String?, body: String?, ok: String?, back: String?, hint: String?) {
        editTextBinding?.let { binding ->
            binding.title.text = title ?: "Notification"
            binding.body.text = body ?: "isEmpty"
            binding.ok.text = ok ?: "OK"
            binding.cancel.text = back ?: "CANCEL"
            binding.editText.hint = hint ?: "isEmpty"

            // EditText
            binding.editText.requestFocus()
            binding.editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) { inputString = s.toString() }
            })
            binding.editText.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    editTextDialogListener?.onConfirm(inputString)
                    true
                } else false
            }
            // Button
            binding.ok.setOnClickListener { editTextDialogListener?.onConfirm(inputString) }
            binding.cancel.setOnClickListener { editTextDialogListener?.onCancel() }

            setContentView(binding.root)
        }
    }

    fun showKeyboard() {
        editTextBinding?.editText?.post {
            if(editTextBinding?.editText?.requestFocus() == true) {
                val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(editTextBinding?.editText, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    /**
     * Emergency Dialog
     * */
    private fun setEmergencyDialogUI(title: String?, body: String?, ok: String?) {
        emergencyBinding?.let { binding ->
            binding.dialogBasicTitle.text = title ?: "Notification"
            binding.dialogBasicBody.text = body ?: "isEmpty"
            binding.dialogBasicButton.text = ok ?: "OK"

            binding.dialogBasicButton.setOnClickListener { actionDialogListener?.onConfirm() }

            setContentView(binding.root)
        }
    }

    fun updateEmergencyButtonText(text: String) {
        emergencyBinding?.dialogBasicButton?.text = text
    }


    /**
     * Set Dialog
     * */
    private fun setDialog(dialogHeight: Double?, dialogWeight: Int?) {
        val window = this.window
        window?.setBackgroundDrawableResource(R.drawable.dialog_basic_round_background)
        window?.let {
            val setHeight = dialogHeight ?: 3.5
            val setWeight = dialogWeight ?: 40

            val displayMetrics = context.resources.displayMetrics
            val width = displayMetrics.widthPixels - dpToPx(context, setWeight) // 화면 너비
            val height = (displayMetrics.heightPixels / setHeight) // 화면 높이의 3분의 1

            it.setLayout(width, height.toInt())
            it.setGravity(Gravity.CENTER)
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    /**
     * Progress bar
     * */
    fun setVisibilityProgressBar(flag: Boolean) {
        if (flag) editTextBinding?.loadingBar?.visibility = View.VISIBLE
        else editTextBinding?.loadingBar?.visibility = View.INVISIBLE
    }
}