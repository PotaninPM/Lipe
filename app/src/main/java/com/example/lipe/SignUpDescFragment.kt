package com.example.lipe

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Camera
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.CameraDevice
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.lipe.databinding.FragmentSignUpDescBinding
import com.example.lipe.databinding.FragmentStartDescFirstBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpDescFragment : Fragment() {

    private lateinit var spinner: Spinner

    private lateinit var dbRef: DatabaseReference

    private var _binding: FragmentSignUpDescBinding? = null
    private val binding get() = _binding!!

    private var items: List<SpinnerItem> = listOf(
        SpinnerItem("Ваши увлечения", R.drawable.light_bulb),
        //summer act
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
        //winter act
        SpinnerItem("Кёрлинг", R.drawable.curling),
        SpinnerItem("Хоккей", R.drawable.ice_hockey),
        SpinnerItem("Катание на коньках", R.drawable.ice_skate),
        SpinnerItem("Лыжная ходьба", R.drawable.skiing_1),
        SpinnerItem("Горные лыжи", R.drawable.skiing),
        SpinnerItem("Сноуборд", R.drawable.snowboarding),
        //home act
        SpinnerItem("Настольные игры", R.drawable.board_game),
        SpinnerItem("Мобильные игры", R.drawable.mobile_game),
        SpinnerItem("Шахматы", R.drawable.chess_2),
        //program
        SpinnerItem("Программирование", R.drawable.programming),
        //noth
        SpinnerItem("Ничего", R.drawable.remove),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpDescBinding.inflate(inflater, container, false)

        spinner = binding.spinner1

        val adapter = CustomAdapter(requireContext(), items)

        spinner.adapter = adapter

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

//        dbRef = FirebaseDatabase.getInstance().getReference("users")
//
//        binding.btnSignUp.setOnClickListener {
//            var desc: String = binding.descText.text.toString().trim()
//
//            if(desc.isNotEmpty()) {
//
//                val bundle = Bundle()
//                bundle.putString("SignUpNew", "success_reg")
//
//                view.findNavController().navigate(R.id.action_signUpDescFragment_to_mapsFragment, bundle)
//            } else {
//                checkForEmpty(desc)
//            }
//
//        }

//        binding.uploadPhoto.setOnClickListener {
//
//        }
    }

    fun checkForEmpty(desc: String) {
        if(desc.isEmpty()) {
            Toast.makeText(requireContext(), "Введите описание!", Toast.LENGTH_LONG).show()
        }
    }

    private fun setError(er: String, field: TextInputEditText) {
        var textError: TextInputEditText = field
        textError.error=er
    }

    data class SpinnerItem(val name: String, val imageResourceId: Int)

    private class CustomAdapter(context: Context, private val items: List<SpinnerItem>) : ArrayAdapter<SpinnerItem>(context, R.layout.spinner_multi_chose, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_multi_chose, parent, false)

            val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
            val imageView = view.findViewById<ImageView>(R.id.imageView)
            val textView = view.findViewById<TextView>(R.id.textView)

            if (position == 0) {
                checkBox.visibility = View.GONE
            } else {
                checkBox.visibility = View.VISIBLE
            }

            val item = getItem(position)
            textView.text = item?.name
            imageView.setImageResource(item?.imageResourceId ?: 0)

            return view
        }
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getView(position, convertView, parent)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
