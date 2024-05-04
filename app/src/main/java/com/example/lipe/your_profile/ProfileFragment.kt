import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.lipe.R
import com.example.lipe.chats_and_groups.ChatsAndGroupsFragment
import com.example.lipe.databinding.FragmentProfileBinding
import com.example.lipe.viewModels.ProfileVM
import com.example.lipe.your_profile.cur_events.CurEventsInProfileFragment
import com.example.lipe.your_profile.cur_events.YourEventsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference
    private var originalBackground: Drawable? = null

    private val profileVM: ProfileVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        originalBackground = binding.btnCurEvent.background
        switchTabs(0)

        binding.btnYourEvents.setBackgroundResource(0)
        binding.btnPastEvent.setBackgroundResource(0)
        binding.btnCurEvent.background = originalBackground

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
                        "${userData.firstName} ${userData.lastName}" + " ⓘ",
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

            theme.setOnClickListener {
                selectImageTheme.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            avatar.setOnClickListener {
                selectImageAvatar.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            btnCurEvent.setOnClickListener {
                switchTabs(0)
                btnYourEvents.setBackgroundResource(0)
                btnPastEvent.setBackgroundResource(0)
                btnCurEvent.background = originalBackground
            }
            btnPastEvent.setOnClickListener {
                switchTabs(1)
                btnYourEvents.setBackgroundResource(0)
                btnCurEvent.setBackgroundResource(0)
                btnPastEvent.background = originalBackground
            }
            btnYourEvents.setOnClickListener {
                switchTabs(2)
                btnCurEvent.setBackgroundResource(0)
                btnPastEvent.setBackgroundResource(0)
                btnYourEvents.background = originalBackground
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
                            Picasso.get().load(url).into(binding.theme)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("INFOG", "ProfileErrTheme")
                    }
            }
        } else if(type == "avatar") {
            val storageRef = FirebaseStorage.getInstance().getReference("avatar")
            imageUri.let { uri ->
                val uid: String = auth.currentUser!!.uid
                val imageRef = storageRef.child(uid)
                imageRef.putFile(uri)
                    .addOnSuccessListener { task ->
                        task.storage.downloadUrl.addOnSuccessListener { url ->
                            Picasso.get().load(url).into(binding.avatar)
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
                val name: String = dataSnapshot.child("firstName").value.toString()
                val lastName: String = dataSnapshot.child("lastName").value.toString()
                val ratingAmount: Int = dataSnapshot.child("rating").value.toString().toInt()
                val friendsAmount: Int = dataSnapshot.child("friends_amount").value.toString().toInt()
                val eventsAmount: Int = dataSnapshot.child("events_amount").value.toString().toInt()

                callback(
                    UserData(
                        name,
                        lastName,
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

        tokenTaskAvatar.addOnSuccessListener { uri ->
            val imageUrl = uri.toString()
            Picasso.get().load(imageUrl).rotate(90f).into(binding.avatar)
            tokenTaskTheme.addOnSuccessListener { uri ->
                val imageThemeUrl = uri.toString()
                Picasso.get().load(imageThemeUrl).rotate(90f).into(binding.theme)
                callback(true)
            }
        }.addOnFailureListener {
            callback(false)
        }
    }

    private fun switchTabs(position: Int) {
        val fragment = when (position) {
            0 -> CurEventsInProfileFragment()
            1 -> ChatsAndGroupsFragment()
            2 -> YourEventsFragment()
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
    val firstName: String,
    val lastName: String,
    val ratingPoints: Int,
    val friendsAmount: Int,
    val eventsAmount: Int,
)
