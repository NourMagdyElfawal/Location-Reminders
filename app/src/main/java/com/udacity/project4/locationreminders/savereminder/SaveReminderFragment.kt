package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.BuildConfig
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
const val ACTION_GEOFENCE_EVENT = "SaveReminderFragment.project4.action.ACTION_GEOFENCE_EVENT"
private const val GEOFENCE_RADIUS_IN_METERS=100f
val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)


class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private val runningROrLater = Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.Q
    private lateinit var reminderDataItem: ReminderDataItem
    private lateinit var geofencingClient: GeofencingClient

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            checkPermissionsAndStartGeofencing()
            reminderDataItem=ReminderDataItem(
                title,
                description,
                location,
                latitude,
                longitude
            )
        }
    }

    private fun checkPermissionsAndStartGeofencing() {
    if (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        //fine location is granted
        if (runningROrLater) {
//                ask for background permission
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Background Location Permission is granted so do your work here
                checkDeviceLocationSettingsAndStartGeofence()
            } else {
                // Ask for Background Location Permission
                askPermissionForBackgroundUsage()

            }
        }

    } else {
        // Fine Location Permission is not granted so ask for permission
        askForFineLocationPermission()
    }
}

    private fun askForFineLocationPermission() {
        fineLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    val fineLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission())
    { isGranted ->
        if (isGranted) {
            // Do if the permission is granted
            Log.d("TAG", "ACCESS_FINE_LOCATION isGranted")
            if(runningROrLater) {
                checkPermissionsAndStartGeofencing()
            }else{
                checkDeviceLocationSettingsAndStartGeofence()
            }

        } else {
            // Do otherwise
            Log.d("TAG", "ACCESS_FINE_LOCATION isNotGranted")
            Snackbar.make(
                binding.saveReminderFragment,
                R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok) {
                askForFineLocationPermission()
            }.show()


        }
    }
            private fun askPermissionForBackgroundUsage() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }

            }

    val backgroundPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission())
    { isGranted ->
    if (isGranted) {
        // Do if the permission is granted
        Log.d("TAG", "ACCESS_BACKGROUND_LOCATION isGranted")
        checkDeviceLocationSettingsAndStartGeofence()

    } else {
        // Do otherwise
        Log.d("TAG", "ACCESS_BACKGROUND_LOCATION isNotGranted")
         Snackbar.make(
                binding.saveReminderFragment,
                R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok) {
                askPermissionForBackgroundUsage()
            }.show()
        }
    }


    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            Log.d("TAG","addOnFailureListener")
            if (exception is ResolvableApiException && resolve){
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON, null, 0, 0, 0, null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("TAG", "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.saveReminderFragment,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
                Log.d("TAG", "location_required_error")

            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                Log.d("TAG","addOnCompleteListener")
                addGeofence()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }
    @SuppressLint("MissingPermission")
    private fun addGeofence() {
        if (_viewModel.validateAndSaveReminder(reminderDataItem)) {
            val geofence = Geofence.Builder()
                .setRequestId(reminderDataItem.id)
                .setCircularRegion(
                    reminderDataItem.latitude!!,
                    reminderDataItem.longitude!!,
                    GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
                .addGeofence(geofence)
                .build()
                    geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                        addOnSuccessListener {
                            Log.e("TAG", "Add geofence")
                        }
                        addOnFailureListener {
                            // Failed to add geofences.
                            Toast.makeText(ApplicationProvider.getApplicationContext(), R.string.geofences_not_added,
                                Toast.LENGTH_SHORT
                            ).show()
                            if ((it.message != null)) {
                                Log.e("TAG", it.message.toString())
                            }
                        }
                    }

        }
    }


    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "HuntMainActivity.treasureHunt.action.ACTION_GEOFENCE_EVENT"
    }

    override fun onDestroy() {
        super.onDestroy()

        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}

