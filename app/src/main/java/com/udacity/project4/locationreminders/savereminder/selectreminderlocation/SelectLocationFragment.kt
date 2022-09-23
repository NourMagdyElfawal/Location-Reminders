package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
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

class SelectLocationFragment : BaseFragment(),OnMapReadyCallback {

    private  var map: GoogleMap? = null
    private val REQUEST_LOCATION_PERMISSION = 1
    private val zoomLevel=15f
    private val latitude = 37.422160
    private val longitude = -122.084270
    private val homeLatLng = LatLng(latitude, longitude)
    private lateinit var lastKnownLocation: Location
    private lateinit var poiMarker: Marker



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


//        checkDeviceLocationSettingsAndStartGeofence()

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment!!.getMapAsync(this)
//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


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
        System.err.println("OnMapReady start")

        googleMap.addMarker(MarkerOptions().position(homeLatLng).title("marker"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng,zoomLevel))

        setMapLongClick(googleMap)
        setPoiClick(googleMap)
        setMapStyle(googleMap)
//        enableMyLocation(googleMap)

    }
//    private fun isPermissionGranted() : Boolean {
//        return ContextCompat.checkSelfPermission(
//            requireContext(),
//            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun enableMyLocation(map: GoogleMap) {
//        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
//        if (isPermissionGranted()) {
//            Log.d("TAG", "isPermissionGranted")
//
//            if (ActivityCompat.checkSelfPermission(
//                    context!!,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                    context!!,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return
//            }
//            map.setMyLocationEnabled(true)
//            val locationResult = fusedLocationProviderClient.lastLocation
//            locationResult.addOnCompleteListener(requireActivity()) { task ->
//                if (task.isSuccessful) {
//                    // Set the map's camera position to the current location of the device.
//                    if (task.result != null) {
//                        lastKnownLocation = task.result!!
//                        map.moveCamera(
//                            CameraUpdateFactory.newLatLngZoom(
//                                LatLng(
//                                    lastKnownLocation!!.latitude,
//                                    lastKnownLocation!!.longitude
//                                ),
//                                zoomLevel.toFloat()
//                            )
//                        )
//                    }
//                } else {
//                    Log.d("TAG", "Current location is null. Using defaults.")
//                    Log.e("TAG", "Exception: %s", task.exception)
//                    map.moveCamera(
//                        CameraUpdateFactory
//                            .newLatLngZoom(homeLatLng, zoomLevel.toFloat())
//                    )
//                    map.uiSettings?.isMyLocationButtonEnabled = false
//                }
//            }
//
//        } else {
//            Log.d("TAG", "isPermissionNotGranted")
//            ActivityCompat.requestPermissions(
//                activity!!,
//                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
//                REQUEST_LOCATION_PERMISSION
//            )
//        }
//    }
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray) {
//        Log.d("TAG", "onRequestPermissionResult")
//
//        // Check if location permissions are granted and if so enable the
//        // location data layer.
//        if (requestCode == REQUEST_LOCATION_PERMISSION) {
//            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
////        checkDeviceLocationSettingsAndStartGeofence()
//                Log.d("TAG","PERMISSION_GRANTED")
//                map?.let { enableMyLocation(it) }
//            }else{
//                Log.d("TAG","PERMISSION_DENIED")
//
//            }
//        }
//    }


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


//    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
//        val locationRequest = LocationRequest.create().apply {
//            priority = LocationRequest.PRIORITY_LOW_POWER
//        }
//        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
//        val settingsClient = LocationServices.getSettingsClient(context!!)
//        val locationSettingsResponseTask =
//            settingsClient.checkLocationSettings(builder.build())
//        locationSettingsResponseTask.addOnFailureListener { exception ->
//            if (exception is ResolvableApiException && resolve){
//                try {
//                    exception.startResolutionForResult(
//                        activity!!,
//                        REQUEST_TURN_DEVICE_LOCATION_ON)
//                    Log.d("TAG", "startResolutionForResult")
//                } catch (sendEx: IntentSender.SendIntentException) {
//                    Log.d("TAG", "Error getting location settings resolution: " + sendEx.message)
//                }
//            } else {
//                Log.d("TAG", "Snackbar")
//                Snackbar.make(
//                    binding.selectLocationFragment,
//                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
//                ).setAction(android.R.string.ok) {
//                    checkDeviceLocationSettingsAndStartGeofence()
//                }.show()
//            }
//        }
//        locationSettingsResponseTask.addOnCompleteListener {
//            if ( it.isSuccessful ) {
//                map?.let { it1 -> enableMyLocation(it1) }
//                Log.d("TAG","Accept")
//            }
//            if(it.isCanceled){
//                Log.d("TAG","isCanceled")
//                Snackbar.make(
//                    binding.selectLocationFragment,
//                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
//                ).setAction(android.R.string.ok) {
//                    checkDeviceLocationSettingsAndStartGeofence()
//                }.show()
//
//            }
//        }
//    }

}
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
