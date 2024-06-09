package com.example.lipe

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.lipe.databinding.FragmentEventReportBinding
import com.example.lipe.viewModels.AppVM
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

class EventReportFragment(eventUid: String, personUid: String) : DialogFragment() {

    private val eventUid = eventUid
    private val personUid = personUid

    private var generalReason: String = ""

    private lateinit var binding: FragmentEventReportBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventReportBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = FragmentEventReportBinding.inflate(LayoutInflater.from(context))

        val items = listOf(getString(R.string.propaganda_violence),
            getString(R.string.event_fake),
            getString(R.string.fraud), getString(R.string.selling_drugs), getString(R.string.other))
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
        val autoCompleteTextView = binding.reasonSpinner
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            generalReason = parent.getItemAtPosition(position).toString()
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.send_report))
            .setView(binding.root)
            .setPositiveButton(getString(R.string.send), null)
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {

            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val reportReason = binding.reportReason.text.toString().trim()
                Log.d("INFOG", reportReason)
                if (reportReason.isNotEmpty() && generalReason.isNotEmpty()) {
                    Log.d("INFOG", generalReason)
                    sendReport(generalReason, reportReason)
                    dismiss()
                } else {
                    binding.reportReasonFull.error = getString(R.string.enter_reason)
                    binding.reasonSpinnerLayout.error = getString(R.string.select_reason)
                }
            }
        }

        return dialog
    }
    private fun sendReport(reportReasonGeneral: String, reportFullReason: String) {

        val time = Instant.now().epochSecond.toString()

        val report = Report(personUid, reportReasonGeneral, reportFullReason, time, "in_queue")
        val dbRef = FirebaseDatabase.getInstance().getReference("reports/$eventUid")
        dbRef.child(personUid).setValue(report).addOnSuccessListener {
            dismiss()
        }
    }
}