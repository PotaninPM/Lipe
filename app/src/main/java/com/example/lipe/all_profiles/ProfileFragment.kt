import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.GetPointsFragment
import com.example.lipe.R
import com.example.lipe.chats_and_groups.ChatsAndGroupsFragment
import com.example.lipe.databinding.FragmentProfileBinding
import com.example.lipe.viewModels.ProfileVM
import com.example.lipe.all_profiles.cur_events.CurEventsInProfileFragment
import com.example.lipe.all_profiles.cur_events.YourEventsFragment
import com.example.lipe.start_frs.StartFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference
    private var originalBackground: Drawable? = null

    private val profileVM: ProfileVM by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        switchTabs(0)

        binding.apply {
            theme.setOnClickListener {
                selectImageTheme.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            avatar.setOnClickListener {
                selectImageAvatar.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            binding.buttonCur.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0067cf"))

            binding.toggleButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
                val buttons = arrayOf(
                    R.id.buttonCur,
                    R.id.buttonMy
                )

                if (isChecked) {
                    group.findViewById<MaterialButton>(checkedId).backgroundTintList = ColorStateList.valueOf(
                        Color.parseColor("#0067cf"))
                    group.findViewById<MaterialButton>(checkedId).isChecked = true

                    buttons.forEach { buttonId ->
                        if (buttonId != checkedId) {
                            group.findViewById<MaterialButton>(buttonId).backgroundTintList = ColorStateList.valueOf(
                                Color.TRANSPARENT)
                            group.findViewById<MaterialButton>(buttonId).isChecked = false
                        }
                    }

                    when (checkedId) {
                        R.id.buttonCur -> {
                            switchTabs(0)
                        }
                        R.id.buttonMy -> {
                            switchTabs(1)
                        }
                    }
                }
            }
            val navView: NavigationView = binding.navView
            navView.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_edit_info -> {
                        childFragmentManager.beginTransaction()
                            .replace(R.id.drawer_layout, ChatsAndGroupsFragment())
                            .commit()
                    }
                    R.id.get_points -> {
                        childFragmentManager.beginTransaction()
                            .replace(R.id.drawer_layout, GetPointsFragment())
                            .commit()
                        Log.d("INFOG", "2")
                    }
                    R.id.nav_settings -> {
//                        childFragmentManager.beginTransaction()
//                            .replace(R.id.all_profile, ChatsAndGroupsFragment())
//                            .commit()
                        Log.d("INFOG", "3")
                    }
                    R.id.nav_logout -> {
                        auth.signOut()
//                        childFragmentManager.beginTransaction()
//                            .replace(R.id.all_profile, StartFragment())
//                            .commit()
                        Log.d("INFOG", "4")
                    }
                }
                drawerLayout.closeDrawer(GravityCompat.END)
                menuItem.isChecked = true
                true
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
//        originalBackground = binding.btnCurEvent.background

//        binding.btnYourEvents.setBackgroundResource(0)
//        binding.btnPastEvent.setBackgroundResource(0)
//        binding.btnCurEvent.background = originalBackground


        dbRef = FirebaseDatabase.getInstance().getReference("users")
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = profileVM

            loadingProgressBar.visibility = View.VISIBLE
            allProfile.visibility = View.GONE

            findAccount { userData ->
                if(userData != null) {
                    profileVM.setInfo(
                         userData.name + " ⓘ",
                        userData.friendsAmount,
                        userData.eventsAmount,
                        userData.ratingPoints
                    )
                    loadingProgressBar.visibility = View.GONE
                    allProfile.visibility = View.VISIBLE
                }
            }

            setProfilePhotos {
                if(it) {
                    loadingProgressBar.visibility = View.GONE
                    allProfile.visibility = View.VISIBLE
                }
            }
        }

        return view
    }

    val selectImageTheme =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                binding.theme.setImageURI(uri)
                uploadImage(uri, "theme")
            } else {
                Log.d("INFOG", "No media selected")
            }
        }
    val selectImageAvatar =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                binding.avatar.setImageURI(uri)
                uploadImage(uri, "avatar")
            } else {
                Log.d("INFOG", "No media selected")
            }
        }
    private fun uploadImage(imageUri: Uri, type: String) {
        if(type == "theme") {
            val storageRef = FirebaseStorage.getInstance().getReference("user_theme")
            imageUri.let { uri ->
                val uid: String = auth.currentUser!!.uid
                val imageRef = storageRef.child(uid)
                imageRef.putFile(uri)
                    .addOnSuccessListener { task ->
                        task.storage.downloadUrl.addOnSuccessListener { url ->
                            lifecycleScope.launch {
                                val bitmap: Bitmap = withContext(Dispatchers.IO) {
                                    Coil.imageLoader(requireContext()).execute(
                                        ImageRequest.Builder(requireContext())
                                            .data(url)
                                            .build()
                                    ).drawable?.toBitmap()!!
                                }
                                binding.theme.setImageBitmap(bitmap)
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("INFOG", "ProfileErrTheme")
                    }
            }
        } else if(type == "avatar") {
            val storageRef = FirebaseStorage.getInstance().getReference("avatars")
            imageUri.let { uri ->
                val uid: String = auth.currentUser!!.uid
                val imageRef = storageRef.child(uid)
                imageRef.putFile(uri)
                    .addOnSuccessListener { task ->
                        task.storage.downloadUrl.addOnSuccessListener { url ->
                            lifecycleScope.launch {
                                val bitmap: Bitmap = withContext(Dispatchers.IO) {
                                    Coil.imageLoader(requireContext()).execute(
                                        ImageRequest.Builder(requireContext())
                                            .data(url)
                                            .build()
                                    ).drawable?.toBitmap()!!
                                }
                                binding.avatar.setImageBitmap(bitmap)
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("INFOG", "ProfileErrTheme")
                    }
            }
        }
    }

    private fun findAccount(callback: (UserData?) -> Unit) {
        val dbRef_user =
            FirebaseDatabase.getInstance().getReference("users/${auth.currentUser!!.uid}")
        dbRef_user.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name: String = dataSnapshot.child("firstAndLastName").value.toString()
                val ratingAmount: Int = dataSnapshot.child("points").value.toString().toInt()
                val friendsAmount: Int = dataSnapshot.child("friends_amount").value.toString().toInt()
                val eventsAmount: Int = dataSnapshot.child("events_amount").value.toString().toInt()

                callback(
                    UserData(
                        name,
                        ratingAmount,
                        friendsAmount,
                        eventsAmount,
                    )
                )
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", "Ошибка Firebase ${databaseError.message}")
                callback(null)
            }
        })
    }

    private fun setProfilePhotos(callback: (Boolean) -> Unit) {
        val photoRef = storageRef.child("avatars/${auth.currentUser!!.uid}")
        val themeRef = storageRef.child("user_theme/${auth.currentUser!!.uid}")

        val tokenTaskAvatar = photoRef.downloadUrl
        val tokenTaskTheme = themeRef.downloadUrl

        tokenTaskAvatar.addOnSuccessListener { url_avatar ->
            lifecycleScope.launch {
                val bitmap: Bitmap = withContext(Dispatchers.IO) {
                    Coil.imageLoader(requireContext()).execute(
                        ImageRequest.Builder(requireContext())
                            .data(url_avatar)
                            .build()
                    ).drawable?.toBitmap()!!
                }
                binding.avatar.setImageBitmap(bitmap)
            }
            tokenTaskTheme.addOnSuccessListener { url_theme ->
                lifecycleScope.launch {
                    val bitmap: Bitmap = withContext(Dispatchers.IO) {
                        Coil.imageLoader(requireContext()).execute(
                            ImageRequest.Builder(requireContext())
                                .data(url_theme)
                                .build()
                        ).drawable?.toBitmap()!!
                    }
                    binding.theme.setImageBitmap(bitmap)
                }
                callback(true)
            }
        }.addOnFailureListener {
            callback(false)
        }
    }

    private fun switchTabs(position: Int) {
        val fragment = when (position) {
            //0 -> CurEventsInProfileFragment(auth.currentUser!!.uid)
            0 -> CurEventsInProfileFragment(auth.currentUser!!.uid)
            1 -> YourEventsFragment()
            else -> null
        }

        fragment?.let {
            childFragmentManager.beginTransaction()
                .replace(R.id.switcher, it)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class UserData(
    val name: String,
    val ratingPoints: Int,
    val friendsAmount: Int,
    val eventsAmount: Int,
)
