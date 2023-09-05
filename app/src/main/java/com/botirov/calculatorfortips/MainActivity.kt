package com.botirov.calculatorfortips

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.botirov.calculatorfortips.R
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private var billTotal = "0"
    private var tipPercent = 15.0
    private var splitNumber = 1.0

    private lateinit var billTotalTextView: TextView
    private lateinit var tipPercentTextView: TextView
    private lateinit var tipTotalTextView: TextView
    private lateinit var splitNumberTextView: TextView
    private lateinit var splitTotalTextView: TextView
    private var isTipBeingEdited = false

    private enum class State {
        BILL, TIP, SPLIT
    }

    private var currentState = State.BILL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        billTotalTextView = findViewById(R.id.billTotalValueTextView)
        tipPercentTextView = findViewById(R.id.tipValueTextView)
        tipTotalTextView = findViewById(R.id.tipTotalValueTextView)
        splitNumberTextView = findViewById(R.id.splitValueTextView)
        splitTotalTextView = findViewById(R.id.splitTotalValueTextView)

        billTotalTextView.setOnClickListener { currentState = State.BILL }
        tipPercentTextView.setOnClickListener { showTipPercentageDialog()}
        splitNumberTextView.setOnClickListener { showSplitNumberDialog() }

        // Setup the number buttons
        for (i in 0..9) {
            val btnId = resources.getIdentifier("button$i", "id", packageName)
            findViewById<Button>(btnId).setOnClickListener { onNumberClicked(it) }
        }

        // Setup AC button
        findViewById<Button>(R.id.acButton).setOnClickListener { onAcClicked() }

        // Setup Delete button
        findViewById<Button>(R.id.deleteButton).setOnClickListener { onDeleteClicked() }

        updateUI()
    }

    private fun onNumberClicked(view: View) {
        if (view is Button) {
            val number = view.text.toString()
            when (currentState) {
                State.BILL -> billTotal = if (billTotal == "0") number else billTotal + number
                State.TIP -> {
                    if (!isTipBeingEdited) {
                        tipPercent = 0.0
                        isTipBeingEdited = true
                    }
                    tipPercent = (tipPercent * 10 + number.toDouble()).coerceIn(0.0, 100.0)
                }
                State.SPLIT -> splitNumber = number.toDouble()
            }
            updateUI()
        }
    }

    private fun onAcClicked() {
        billTotal = "0"
        tipPercent = 15.0
        splitNumber = 1.0
        currentState = State.BILL
        isTipBeingEdited = false
        updateUI()
    }

    private fun onDeleteClicked() {
        when (currentState) {
            State.BILL -> billTotal = billTotal.dropLast(1).takeIf { it.isNotEmpty() } ?: "0"
            State.TIP -> {
                tipPercent = (tipPercent / 10).toInt().toDouble()
                isTipBeingEdited = tipPercent != 0.0
            }
            State.SPLIT -> splitNumber = 1.0
            }
        updateUI()
    }

    private fun showTipPercentageDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tip_percentage, null)
        val editText = dialogView.findViewById<EditText>(R.id.tipPercentageEditText)
        editText.setText(tipPercent.toString())

        AlertDialog.Builder(this)
            .setTitle("Set Tip Percentage")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                tipPercent = editText.text.toString().toDoubleOrNull() ?: tipPercent
                isTipBeingEdited = true
                updateUI()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSplitNumberDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_split_number, null)
        val splitNumberEditText = view.findViewById<EditText>(R.id.splitNumberEditText)

        AlertDialog.Builder(this)
            .setTitle("Set number of people you want to split the bill with!")
            .setView(view)
            .setPositiveButton("OK") { _, _ ->
                val inputNumber = splitNumberEditText.text.toString().toDoubleOrNull()
                if (inputNumber != null && inputNumber > 0) {
                    splitNumber = inputNumber
                    updateUI()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateUI() {
        val decimalFormat = DecimalFormat("0.00")
        billTotalTextView.text = "$${decimalFormat.format(billTotal.toDouble())}"

        // Check if the tip percent is an integer value, and format it accordingly
        tipPercentTextView.text = if (tipPercent % 1 == 0.0) {
            "${tipPercent.toInt()}%"
        } else {
            "${decimalFormat.format(tipPercent)}%"
        }

        val tipTotal = billTotal.toDouble() * tipPercent / 100
        tipTotalTextView.text = "$${decimalFormat.format(tipTotal)}"
        splitNumberTextView.text = "${splitNumber.toInt()} person(s)"
        val splitTotal = (billTotal.toDouble() + tipTotal) / splitNumber
        splitTotalTextView.text = "$${decimalFormat.format(splitTotal)}"
    }

}
