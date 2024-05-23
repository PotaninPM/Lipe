import android.content.Context
import android.graphics.Bitmap
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.request.ImageRequest
import com.example.lipe.R
import com.example.lipe.all_profiles.change_info_sheet.ChangeInfoBottomSheet
import com.example.lipe.databinding.FragmentProfileBinding
import com.example.lipe.viewModels.ProfileVM
import com.example.lipe.all_profiles.cur_events.CurEventsInProfileFragment
import com.example.lipe.all_profiles.cur_events.YourEventsFragment
import com.example.lipe.all_profiles.friends.FriendsBottomSheet
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference
    private var originalBackground: Drawable? = null

    private val profileVM: ProfileVM by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        switchTabs(0)

        setupTabLayout()

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
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

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
                if(it) {
                    loadingProgressBar.visibility = View.GONE
                    allProfile.visibility = View.VISIBLE
                }
            }
        }

        return view
    }

    private fun setupTabLayout() {
        val tabLayout = binding.tabLayout
        tabLayout.addTab(tabLayout.newTab().setText(R.string.cur_events))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.your_events))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> switchTabs(0)
                    1 -> switchTabs(1)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
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
                val sharedPrefUser = activity?.getSharedPreferences("userRef", Context.MODE_PRIVATE)
                val username = sharedPrefUser?.getString("username", null)
                if(username == null) {
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
                                    dataSnapshot.child("friends_amount").value.toString().toInt()
                                val eventsAmount: Int =
                                    dataSnapshot.child("events_amount").value.toString().toInt()

                                val editor = sharedPrefUser?.edit()

                                editor?.apply {
                                    putString("username", username)
                                    putString("name", name)
                                    putString("desc", desc)
                                    putInt("rating", ratingAmount)
                                    putInt("friends", friendsAmount)
                                    putInt("events", eventsAmount)
                                    putString("desc", desc)
                                    apply()
                                }

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
                    val username = sharedPrefUser.getString("username", null)
                    val desc = sharedPrefUser.getString("desc", null)
                    val name = sharedPrefUser.getString("name", null)
                    val rating = sharedPrefUser.getInt("rating", -1)
                    val friends = sharedPrefUser.getInt("friends", -1)
                    val events = sharedPrefUser.getInt("events", -1)
                    callback(
                        UserData(
                            username!!,
                            rating,
                            friends,
                            events,
                            desc!!,
                            name!!
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("INFOG", e.message.toString())
        }
    }

    private fun setProfilePhotos(callback: (Boolean) -> Unit) {
        val photoRef = storageRef.child("avatars/${auth.currentUser!!.uid}")
        val themeRef = storageRef.child("user_theme/${auth.currentUser!!.uid}")

        val tokenTaskAvatar = photoRef.downloadUrl
        val tokenTaskTheme = themeRef.downloadUrl

        tokenTaskAvatar.addOnSuccessListener { url_avatar ->
            profileVM.setPhoto(url_avatar.toString())
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
            0 -> CurEventsInProfileFragment(auth.currentUser!!.uid)
            1 -> YourEventsFragment(auth.currentUser!!.uid)
            else -> null
        }

        fragment?.let {
            childFragmentManager.beginTransaction()
                .replace(R.id.switcher, it)
                .commit()
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
