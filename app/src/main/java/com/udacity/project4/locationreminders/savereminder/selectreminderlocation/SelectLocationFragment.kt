package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.telephony.CarrierConfigManager
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*
private const val REQUEST_TURN_DEVICE_LOCATION_IS_ON = 29

class SelectLocationFragment : BaseFragment(),OnMapReadyCallback {

    private  var map: GoogleMap? = null
    private val REQUEST_LOCATION_PERMISSION = 1
    private val zoomLevel=15f
    private val latitude = 37.422160
    private val longitude = -122.084270
    private val homeLatLng = LatLng(latitude, longitude)
    private lateinit var lastKnownLocation: Location
    private lateinit var poiMarker: Marker
    private val runningROrLater = Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.R



    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this



        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment!!.getMapAsync(this)
//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected
//        checkPermissionsAndStartGeofencing()
//        TODO: call this function after the user confirms on the selected location
        binding.saveButton.setOnClickListener { onLocationSelected() }


        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        if (this::poiMarker.isInitialized) {
            _viewModel.latitude.value = poiMarker.position.latitude
            _viewModel.longitude.value = poiMarker.position.longitude
            _viewModel.reminderSelectedLocationStr.value = poiMarker.title
            findNavController().popBackStack()
        }else{
            Toast.makeText(context,"no location selected",Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map!!.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map!!.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map!!.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map!!.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        System.err.println("OnMapReady start")

        googleMap.addMarker(MarkerOptions().position(homeLatLng).title("marker"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng,zoomLevel))

        setMapLongClick(googleMap)
        setPoiClick(googleMap)
        setMapStyle(googleMap)
//        enableMyLocation(googleMap)
        checkPermissionsAndStartGeofencing()


    }
    @SuppressLint("MissingPermission")
    private fun enableMyLocation(map: GoogleMap) {

        if ((ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            map.isMyLocationEnabled = true
            val locationResult =
                LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation
            locationResult.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Set the map's camera position to the current location of the device.
                    if (task.result != null) {
                        lastKnownLocation = task.result!!
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude
                                ),
                                zoomLevel.toFloat()
                            )
                        )
                    }
                } else {
                    Log.d("TAG", "Current location is null. Using defaults.")
                    Log.e("TAG", "Exception: %s", task.exception)
                    map.moveCamera(
                        CameraUpdateFactory
                            .newLatLngZoom(homeLatLng, zoomLevel.toFloat())
                    )
                    map.uiSettings?.isMyLocationButtonEnabled = false
                }
            }
        }
    }

    //to show the place details
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
             poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()
        }

    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            map.clear()
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            poiMarker=map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

            poiMarker.showInfoWindow()

        }

    }
//create style
    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e("TAG", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("TAG", "Can't find style. Error: ", e)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.e("TAG", "onStart")


    }

    override fun onResume() {
        super.onResume()
        Log.e("TAG", "onResume")
//        checkDeviceLocationSettingsAndStartGeofence()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("TAG", "onDestroy")

    }


    @SuppressLint("MissingPermission")
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
                Log.d("TAG", "Snackbar")
                Snackbar.make(
                    binding.selectLocationFragment,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                Log.d("TAG","Accept")
                    map!!.isMyLocationEnabled = true
                    val locationResult = LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation
                    locationResult.addOnCompleteListener(requireActivity()) { location ->
                        if (location.isSuccessful) {
                            Log.d("TAG","locationResult.isSuccessful")

                            // Set the map's camera position to the current location of the device.
                            if (location.result != null) {
                                lastKnownLocation = location.result
                                Log.d("TAG", "task.result")
                                map!!.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            lastKnownLocation!!.latitude,
                                            lastKnownLocation!!.longitude
                                        ),
                                        zoomLevel.toFloat()
                                    )
                                )

                        } else {
                            Log.d("TAG", "Current location is null. Using defaults.")
                            Log.e("TAG", "Exception: %s", location.exception)
                            map!!.moveCamera(
                                CameraUpdateFactory
                                    .newLatLngZoom(homeLatLng, zoomLevel.toFloat())
                            )
                            map!!.uiSettings?.isMyLocationButtonEnabled = false
                        }
                    }
                }



            }
            if(it.isCanceled){
                Log.d("TAG","isCanceled")
                Snackbar.make(
                    binding.selectLocationFragment,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()

            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_IS_ON) {
            checkDeviceLocationSettingsAndStartGeofence(false)
        }else{
            super.onActivityResult(requestCode, resultCode, data)
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
                map?.let { checkPermissionsAndStartGeofencing() }
            }else{
                checkDeviceLocationSettingsAndStartGeofence()
            }

        } else {
            // Do otherwise
            Log.d("TAG", "ACCESS_FINE_LOCATION isNotGranted")
            Snackbar.make(
                binding.selectLocationFragment,
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
                binding.selectLocationFragment,
                R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok) {
                askPermissionForBackgroundUsage()
            }.show()
        }
    }

}
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
