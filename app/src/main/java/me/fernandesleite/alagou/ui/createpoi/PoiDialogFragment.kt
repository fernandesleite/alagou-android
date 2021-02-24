package me.fernandesleite.alagou.ui.createpoi

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import me.fernandesleite.alagou.R

class PoiDialogFragment : DialogFragment() {
    private lateinit var listener: PoiDialogListener

    interface PoiDialogListener {
        fun onDialogPositiveClick(dialogFragment: DialogFragment, nome: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater

        val view = inflater.inflate(R.layout.dialog_poi, null)
        builder.setView(view)

        builder.setPositiveButton(
            "Confirmar"
        ) { _, _ ->
            listener.onDialogPositiveClick(
                this,
                view.findViewById<EditText>(R.id.poi_nome).text.toString()
            )
        }
        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = targetFragment as PoiDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                (context.toString() +
                        " must implement PoiDialogListener")
            )
        }
    }
}