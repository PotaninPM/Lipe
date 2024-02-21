package com.example.lipe

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import com.example.lipe.databinding.FragmentCreateEntEventBinding
import com.example.lipe.databinding.FragmentCreateEventBinding
import java.util.Calendar

class CreateEntEventFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private lateinit var spinner: Spinner

    private var _binding: FragmentCreateEntEventBinding? = null
    private val binding get() = _binding!!

    private var items: List<SpinnerItem> = listOf(
        SpinnerItem("Выберите тип развлечения", R.drawable.light_bulb),
        SpinnerItem("Баскетбол", R.drawable.img_basketballimg),
        SpinnerItem("Воллейбол", R.drawable.volleyball_2),
        SpinnerItem("Футбол", R.drawable.football),
        SpinnerItem("Рэгби", R.drawable.rugby_ball),
        SpinnerItem("Воркаут", R.drawable.weights),
        SpinnerItem("Большой тенис", R.drawable.tennis),
        SpinnerItem("Бадминтон", R.drawable.shuttlecock),
        SpinnerItem("Пинпонг", R.drawable.table_tennis),
        SpinnerItem("Гимнастика", R.drawable.gymnastic_rings),
        SpinnerItem("Фехтование", R.drawable.fencing),
        SpinnerItem("Бег", R.drawable.running_shoe),
        SpinnerItem("Кёрлинг", R.drawable.curling),
        SpinnerItem("Хоккей", R.drawable.ice_hockey),
        SpinnerItem("Катание на коньках", R.drawable.ice_skate),
        SpinnerItem("Лыжная ходьба", R.drawable.skiing_1),
        SpinnerItem("Горные лыжи", R.drawable.skiing),
        SpinnerItem("Сноуборд", R.drawable.snowboarding),
        SpinnerItem("Настольные игры", R.drawable.board_game),
        SpinnerItem("Мобильные игры", R.drawable.mobile_game),
        SpinnerItem("Шахматы", R.drawable.chess_2),
        SpinnerItem("Программирование", R.drawable.programming),
        SpinnerItem("Ничего", R.drawable.remove),
    )

    var year = 0
    var month = 0
    var day = 0
    var hour = 0
    var minute = 0

    var savedYear = 0
    var savedMonth = 0
    var savedDay = 0
    var savedHour = 0
    var savedMinute = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateEntEventBinding.inflate(inflater, container, false)

        setDesignToFields()

        spinner = binding.spinner1

        val adapter = CustomAdapter(requireContext(), items)

        spinner.adapter = adapter
        selectDate()

        val view = binding.root
        return view
    }

    private fun setDesignToFields() {
        binding.etNameinputLay.boxStrokeColor = Color.BLUE
        binding.etDescInputLay.boxStrokeColor = Color.BLUE
        binding.etMaxInputLay.boxStrokeColor = Color.BLUE
    }

    data class SpinnerItem(val name: String, val imageResourceId: Int)

    private class CustomAdapter(context: Context, private val items: List<SpinnerItem>) : ArrayAdapter<SpinnerItem>(context, R.layout.spinner_one_chose, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_one_chose, parent, false)

            val imageView = view.findViewById<ImageView>(R.id.imageView)
            val textView = view.findViewById<TextView>(R.id.textView)


            val item = getItem(position)
            textView.text = item?.name
            imageView.setImageResource(item?.imageResourceId ?: 0)

            return view
        }
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getView(position, convertView, parent)
        }
    }

    private fun getDateTime() {
        val calendar = Calendar.getInstance()
        day = calendar.get(Calendar.DAY_OF_MONTH)
        month = calendar.get(Calendar.MONTH)
        year = calendar.get(Calendar.YEAR)
        day = calendar.get(Calendar.DAY_OF_MONTH)

        hour = calendar.get(Calendar.HOUR)
        minute = calendar.get(Calendar.MINUTE)
    }

    private fun selectDate() {
        binding.setDateLay.setOnClickListener {
            getDateTime()
            DatePickerDialog(requireContext(), this, year, month, day).show()
        }
    }
    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
        savedYear = year
        savedMonth = month
        savedDay = day

        getDateTime()
        TimePickerDialog(requireContext(), this, hour, minute, true).show()
    }

    override fun onTimeSet(p0: TimePicker?, hour: Int, minute: Int) {
        savedHour = hour
        savedMinute = minute
        binding.setDateLay.setBackgroundResource(R.drawable.choose_date_success)
        binding.setDate.setText("$savedYear.$savedMonth.$savedDay в $savedHour:$savedMinute")
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}
