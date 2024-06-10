package com.example.lipe.reports.report_dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.lipe.R
import com.example.lipe.Report
import com.example.lipe.databinding.FragmentEventReportBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase
import java.time.Instant

class EventReportFragment(eventUid: String, personUid: String) : DialogFragment() {

    private val eventUid = eventUid
    private val personUid = personUid

    private var generalReason: String = ""

    private lateinit var binding: FragmentEventReportBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventReportBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentEventReportBinding.inflate(LayoutInflater.from(requireContext()))

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
                Log.d("INFOG", "Report Reason: $reportReason")
                Log.d("INFOG", "General Reason: $generalReason")

                if (reportReason.isNotEmpty() && generalReason.isNotEmpty()) {
                    sendReport(generalReason, reportReason)
                } else {
                    if (reportReason.isEmpty()) {
                        binding.reportReasonFull.error = getString(R.string.enter_reason)
                    }
                    if (generalReason.isEmpty()) {
                        binding.reasonSpinnerLayout.error = getString(R.string.select_reason)
                    }
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
