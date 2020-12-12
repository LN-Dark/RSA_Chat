package com.luanegra.rsachat.fragments

import android.R.attr
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.luanegra.rsachat.R
import com.luanegra.rsachat.modelclasses.Users
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.launch
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.MediaFile
import pl.aprilapps.easyphotopicker.MediaSource
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.collections.HashMap


class SettingsFragment : Fragment() {
    private var refUsers: DatabaseReference? = null
    var firebaseUser: FirebaseUser?= null
    private var RequestCode: Int = 1
    private var imageUri: Uri? = null
    private var storageProfileRef: StorageReference? = null
    private var storageCoverRef: StorageReference? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_settings, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid)
        storageProfileRef = FirebaseStorage.getInstance().reference.child("profileImages")
        storageCoverRef = FirebaseStorage.getInstance().reference.child("coverImages")
        val cover_settings: ImageView = view.findViewById(R.id.cover_settings)
        val profileimage_settings: de.hdodenhof.circleimageview.CircleImageView = view.findViewById(
            R.id.profileimage_settings
        )
        val username: TextInputEditText = view.findViewById(R.id.username_settings)
        val facebook_settings: ImageView = view.findViewById(R.id.facebook_settings)
        val instagram_settings: ImageView = view.findViewById(R.id.instagram_settings)
        val website_settings: ImageView = view.findViewById(R.id.website_settings)
        val aboutme_settings: TextInputEditText = view.findViewById(R.id.aboutme_settings)
        val check_notifications_settings: SwitchMaterial = view.findViewById(R.id.check_notifications_settings)
        refUsers!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val newuser: Users? = snapshot.getValue(Users::class.java)
                    if (context != null) {
                        cover_settings.load(newuser!!.getcover())
                        profileimage_settings.load(newuser.getprofile())
                        username.setText(newuser.getusername())
                        aboutme_settings.setText(newuser.getaboutMe())
                        facebook_settings.setOnClickListener {
                            setSocial(0)
                        }
                        instagram_settings.setOnClickListener {
                            setSocial(1)
                        }
                        website_settings.setOnClickListener {
                            setSocial(2)
                        }
                        check_notifications_settings.isChecked = newuser.getnotificationsShow()
                        check_notifications_settings.setOnClickListener {
                            notificationUpdate(check_notifications_settings.isChecked)
                        }
                        username.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {

                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                                saveName(s.toString())
                                username.setSelection(username.text!!.length)
                            }

                            override fun afterTextChanged(s: Editable?) {
                                saveName(s.toString())
                                username.setSelection(username.text!!.length)
                            }

                        })

                        aboutme_settings.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {

                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                                saveAboutme(s.toString())
                                aboutme_settings.setSelection(aboutme_settings.text!!.length)
                            }

                            override fun afterTextChanged(s: Editable?) {
                                saveAboutme(s.toString())
                                aboutme_settings.setSelection(aboutme_settings.text!!.length)
                            }

                        })
                        cover_settings.setOnClickListener {
                            RequestCode = 2
                            pickimage()
                        }
                        profileimage_settings.setOnClickListener {
                            RequestCode = 1
                            pickimage()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        return view
    }

    fun notificationUpdate(isShow: Boolean){
            val map = HashMap<String, Any>()
            map["notificationsShow"] = isShow
            refUsers!!.updateChildren(map)
    }

    private fun saveName(newUserName: String){
        if(newUserName.isNotEmpty()){
            val map = HashMap<String, Any>()
            map["username"] = newUserName
            map["search"] = newUserName.toLowerCase(Locale.ROOT)
            refUsers!!.updateChildren(map)
        }
    }

    private fun saveAboutme(newAboutme: String){
        if(newAboutme.isNotEmpty()){
            val map = HashMap<String, Any>()
            map["aboutMe"] = newAboutme
            refUsers!!.updateChildren(map)
        }
    }

    private fun setSocial(type: Int) {
        //0 facebook
        //1 intagram
        //2 website
        val builder: AlertDialog.Builder = AlertDialog.Builder(
            requireContext(),
            R.style.ThemeOverlay_MaterialComponents_Dialog
        )
        builder.setIcon(R.mipmap.ic_launcher)
        val map = HashMap<String, Any>()
        val edittext = EditText(context)
        if(type == 0){
            builder.setTitle(getString(R.string.writeusername))
            edittext.hint = "e.g tonycastanheira123"
        }else if(type == 1){
            builder.setTitle(getString(R.string.writeusername))
            edittext.hint = "e.g tonycastanheira123"
        }else if(type == 2){

            builder.setTitle(getString(R.string.writeurl))
            edittext.hint = "e.g www.google.pt"
        }
        builder.setView(edittext)
        builder.setPositiveButton(getString(R.string.create)) { dialog, which ->
            if (edittext.text.toString() == "") {
                Toast.makeText(context, getString(R.string.writesomething), Toast.LENGTH_LONG)
                    .show()
            } else {
                val str: String = edittext.text.toString()
                if (type == 0) {
                    map["facebook"] = "https://m.facebook.com/$str"
                } else if (type == 1) {
                    map["instagram"] = "https://m.instagram.com/$str"
                } else if (type == 2) {
                    map["website"] = "https://$str"
                }
                refUsers!!.updateChildren(map).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, getString(R.string.updated), Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, which ->
            dialog.cancel()
        }
        builder.show()
    }

    val intent = Intent()
    var easyImage: EasyImage? = null
    private fun pickimage(){


        intent.putExtra("resultAUTH", "true")

        requireActivity().intent.putExtra("resultAUTH", "true")

         easyImage = EasyImage.Builder(requireContext())
            .setCopyImagesToPublicGalleryFolder(false)
            .setFolderName(getString(R.string.selectimage))
            .allowMultiple(false)
            .build()

        easyImage!!.openChooser(this)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(RequestCode == 1 && resultCode == Activity.RESULT_OK && data!!.data != null){
            easyImage!!.handleActivityResult(
                requestCode,
                resultCode,
                data,
                requireActivity(),
                object : DefaultCallback() {
                    override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                        //imageUri = imageFiles.get(0).file
                        uploadImage(1, imageFiles.get(0).file)
                    }

                    override fun onImagePickerError(error: Throwable, source: MediaSource) {
                        //Some error handling
                        error.printStackTrace()
                    }

                    override fun onCanceled(source: MediaSource) {
                        //Not necessary to remove any files manually anymore
                    }
                })
        }else if(RequestCode == 2 && resultCode == Activity.RESULT_OK && data!!.data != null){
            easyImage!!.handleActivityResult(
                requestCode,
                resultCode,
                data,
                requireActivity(),
                object : DefaultCallback() {
                    override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                        //imageUri = imageFiles.get(0).file
                        uploadImage(2, imageFiles.get(0).file)
                    }

                    override fun onImagePickerError(error: Throwable, source: MediaSource) {
                        //Some error handling
                        error.printStackTrace()
                    }

                    override fun onCanceled(source: MediaSource) {
                        //Not necessary to remove any files manually anymore
                    }
                })
        }
    }

    private fun uploadImage(type: Int, filetoup: File){
        val loadingBar = ProgressDialog(context)
        if(type == 1){
            val fileRef = storageProfileRef!!.child(
                System.currentTimeMillis().toString() + ".jpg"
            )
            lifecycleScope.launch {

                val compressedImageFile = Compressor.compress(requireView().context, filetoup){
                    quality(25)
                    format(Bitmap.CompressFormat.JPEG)
                }
                if(compressedImageFile.length() < 30000000){
                    loadingBar.setMessage(getString(R.string.pleasewait))
                    loadingBar.setCancelable(false)
                    loadingBar.show()
                    val uploadTask2: UploadTask = fileRef.putBytes(compressedImageFile.readBytes())
                    uploadTask2.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                        if (task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        return@Continuation fileRef.downloadUrl
                    }).addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            val downloadUrl = task.result
                            val map = HashMap<String, Any>()
                            map["profile"] = downloadUrl.toString()
                            refUsers!!.updateChildren(map)
                            loadingBar.dismiss()

                        }
                    }
                }else{
                    Toast.makeText(context, getString(R.string.imagetoobig), Toast.LENGTH_LONG).show()
                }
            }


        }else if(type == 2){
            val fileRef = storageCoverRef!!.child(System.currentTimeMillis().toString() + ".jpg")
            lifecycleScope.launch {
                val compressedImageFile = Compressor.compress(requireView().context, filetoup){
                    quality(25)
                    format(Bitmap.CompressFormat.JPEG)
                }
                if(compressedImageFile.length() < 30000000){
                    loadingBar.setMessage(getString(R.string.pleasewait))
                    loadingBar.setCancelable(false)
                    loadingBar.show()
                    val uploadTask2: UploadTask = fileRef.putBytes(compressedImageFile.readBytes())
                    uploadTask2.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                        if (task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        return@Continuation fileRef.downloadUrl
                    }).addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            val downloadUrl = task.result
                            val map = HashMap<String, Any>()
                            map["profile"] = downloadUrl.toString()
                            refUsers!!.updateChildren(map)
                            loadingBar.dismiss()
                        }
                    }
                }else{
                    Toast.makeText(context, getString(R.string.imagetoobig), Toast.LENGTH_LONG).show()
                }
            }
        }
    }




}