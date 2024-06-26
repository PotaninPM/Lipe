
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import coil.Coil
import coil.load
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.all_profiles.EventsInProfileTabAdapter
import com.example.lipe.all_profiles.change_info_sheet.ChangeInfoBottomSheet
import com.example.lipe.databinding.FragmentProfileBinding
import com.example.lipe.viewModels.ProfileVM
import com.example.lipe.all_profiles.cur_events.CurEventsInProfileFragment
import com.example.lipe.all_profiles.cur_events.YourEventsFragment
import com.example.lipe.all_profiles.friends.FriendsBottomSheet
import com.example.lipe.notifications.EventData
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference
    private var originalBackground: Drawable? = null

    private lateinit var adapter: EventsInProfileTabAdapter

    private val profileVM: ProfileVM by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(isAdded) {

            if(auth.currentUser != null) {
                binding.apply {
                    theme.setOnClickListener {
                        selectImageTheme.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }

                    changeYourInfo.setOnClickListener {
                        val bottomSheet = ChangeInfoBottomSheet()
                        bottomSheet.show(childFragmentManager, "ChangeInfoBottomSheet")
                    }

                    avatar.setOnClickListener {
                        selectImageAvatar.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }

                    friendsAmountLay.setOnClickListener {
                        val friendBottomSheet = FriendsBottomSheet()
                        friendBottomSheet.show(childFragmentManager, "FriendsBottomSheet")
                    }

                    tabLayout.apply {

                        addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                            override fun onTabSelected(tab: TabLayout.Tab?) {
                                binding.viewPager.currentItem = tab!!.position
                            }

                            override fun onTabUnselected(p0: TabLayout.Tab?) {}

                            override fun onTabReselected(p0: TabLayout.Tab?) {}

                        })
                    }

                    viewPager.registerOnPageChangeCallback(object :
                        ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)

                            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
                        }
                    })
                }
            } else {
                auth.signOut()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root


        if(isAdded) {
            dbRef = FirebaseDatabase.getInstance().getReference("users")
            auth = FirebaseAuth.getInstance()
            storageRef = FirebaseStorage.getInstance().reference

            binding.apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = profileVM

                adapter = EventsInProfileTabAdapter(childFragmentManager, lifecycle, auth.currentUser!!.uid)
                binding.viewPager.adapter = adapter

                loadingProgressBar.visibility = View.VISIBLE
                allProfile.visibility = View.GONE

                findAccount { userData ->
                    if (userData != null) {
                        profileVM.setInfo(
                            userData.nickname,
                            userData.friendsAmount,
                            userData.eventsAmount,
                            userData.ratingPoints,
                            userData.desc,
                            userData.name
                        )
                        loadingProgressBar.visibility = View.GONE
                        allProfile.visibility = View.VISIBLE
                    }
                }

                setProfilePhotos {
                    if (it) {
                        loadingProgressBar.visibility = View.GONE
                        allProfile.visibility = View.VISIBLE
                    }
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
        try {
            if(isAdded) {
                    if(auth.currentUser != null) {
                        val dbRef_user =
                            FirebaseDatabase.getInstance()
                                .getReference("users/${auth.currentUser!!.uid}")
                        dbRef_user.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val desc = dataSnapshot.child("about_you").value.toString()
                                val name = dataSnapshot.child("firstAndLastName").value.toString()
                                val username: String =
                                    dataSnapshot.child("username").value.toString()
                                val ratingAmount: Int =
                                    dataSnapshot.child("points").value.toString().toInt()
                                val friendsAmount: Int =
                                    dataSnapshot.child("friends").childrenCount.toInt()
                                val eventsAmount: Int =
                                    dataSnapshot.child("events_amount").value.toString().toInt()

                                Log.d("INFOG1", eventsAmount.toString())

                                callback(
                                    UserData(
                                        username,
                                        ratingAmount,
                                        friendsAmount,
                                        eventsAmount,
                                        desc,
                                        name
                                    )
                                )
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e("FirebaseError", "Ошибка Firebase ${databaseError.message}")
                                callback(null)
                            }
                        })
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.not_auth), Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            Log.e("INFOG", e.message.toString())
        }
    }

    private fun setProfilePhotos(callback: (Boolean) -> Unit) {
        if(isAdded) {
            val photoRef = storageRef.child("avatars/${auth.currentUser!!.uid}")
            val themeRef = storageRef.child("user_theme/${auth.currentUser!!.uid}")

            val tokenTaskAvatar = photoRef.downloadUrl
            val tokenTaskTheme = themeRef.downloadUrl

            tokenTaskAvatar.addOnSuccessListener { url_avatar ->
                profileVM.setPhoto(url_avatar.toString())
                lifecycleScope.launch {
                    if(isAdded) {
                        val bitmap: Bitmap = withContext(Dispatchers.IO) {
                            Coil.imageLoader(requireContext()).execute(
                                ImageRequest.Builder(requireContext())
                                    .data(url_avatar)
                                    .build()
                            ).drawable?.toBitmap()!!
                        }
                        binding.avatar.setImageBitmap(bitmap)
                    }
                }
                tokenTaskTheme.addOnSuccessListener { url_theme ->
                    lifecycleScope.launch {
                        if (isAdded) {
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
                }
            }.addOnFailureListener {
                callback(false)
            }
        }
    }
}

data class UserData(
    val nickname: String,
    val ratingPoints: Int,
    val friendsAmount: Int,
    val eventsAmount: Int,
    val desc: String,
    val name: String
)
