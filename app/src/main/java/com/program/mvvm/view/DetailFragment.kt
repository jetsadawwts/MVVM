package com.program.mvvm.view


import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.telephony.SmsManager
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

import com.program.mvvm.R
import com.program.mvvm.databinding.FragmentDetailBinding
import com.program.mvvm.databinding.SendSmsDialogBinding
import com.program.mvvm.model.DogBreed
import com.program.mvvm.model.DogPalette
import com.program.mvvm.model.SmsInfo
import com.program.mvvm.util.getProgressDrawable
import com.program.mvvm.util.loadImage
import com.program.mvvm.viewmodel.DetailViewModel
import kotlinx.android.synthetic.main.fragment_detail.*

/**
 * A simple [Fragment] subclass.
 */
@Suppress("NAME_SHADOWING")
class DetailFragment : Fragment() {

    private lateinit var viewModel : DetailViewModel
    private var dogUuid = 0

    private lateinit var dataBinding: FragmentDetailBinding
    private var sendSmsStarted = false
    private var currentDog: DogBreed? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        arguments?.let {
            dogUuid = DetailFragmentArgs.fromBundle(it).dogUuid
        }
        viewModel = ViewModelProviders.of(this).get(DetailViewModel::class.java)
        viewModel.fetch(dogUuid)

        observeViewModel()
    }

         private fun observeViewModel() {

             viewModel.dogLiveData.observe(this, Observer { dog ->
                 currentDog = dog
                     dog?.let {
                         dataBinding.dog = dog
                         it.imageUrl?.let { it ->
                             setupBackgroundColor(it)
                     }
                         // dogName.text = dog.dogBreed
//                         dogPurpose.text = dog.breedFor
//                         dogTemperament.text = dog.temperament
//                         dogLifespan.text = dog.lifeSpan
//                         context?.let {
//                             dogImage.loadImage(dog.imageUrl, getProgressDrawable(it))
//                         }
                     }
             })
    }

    @SuppressLint("CheckResult")
    private fun setupBackgroundColor(url: String) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object: CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {

                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Palette.from(resource)
                        .generate { palette ->
                            val intColor = palette?.lightMutedSwatch?.rgb ?: 0
                            val myPalette = DogPalette(intColor)
                            dataBinding.palette = myPalette
                        }
                }

            })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.detail_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)  {
            R.id.action_send_sms -> {
                view?.let {
                    sendSmsStarted = true
                    (activity as MainActivity).checkSmsPermission()
                }
            }
            R.id.action_share -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, "Check out this dog breed")
                intent.putExtra(Intent.EXTRA_TEXT, "${currentDog?.dogBreed} bred for ${currentDog?.breedFor}")
                intent.putExtra(Intent.EXTRA_STREAM, currentDog?.imageUrl)
                startActivity(Intent.createChooser(intent, "Share with"))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onPermissionResult(permissionGranted: Boolean) {
        if (sendSmsStarted && permissionGranted) {
            context?.let {
                    val smsInfo = SmsInfo("", "${currentDog?.dogBreed} bred for ${currentDog?.breedFor}","${currentDog?.imageUrl}")
                    val dialogBinding = DataBindingUtil.inflate<SendSmsDialogBinding>(LayoutInflater.from(it), R.layout.send_sms_dialog, null, false)

                    AlertDialog.Builder(it)
                        .setView(dialogBinding.root)
                        .setPositiveButton("Send SMS") {dialog, which ->
                                if(!dialogBinding.smsDescription.text.isNullOrEmpty()) {
                                    smsInfo.to = dialogBinding.smsDescription.text.toString()
                                    sendSms(smsInfo)
                                }
                        }
                        .setNegativeButton("Cacel") {dialog, which ->

                        }
                        .show()
                dialogBinding.smsInfo = smsInfo
            }
        }
    }

    private fun sendSms(smsInfo: SmsInfo) {
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 0, intent, 0)
        val sms = SmsManager.getDefault()
        sms.sendTextMessage(smsInfo.to, null, smsInfo.text, pi, null)

    }

}
