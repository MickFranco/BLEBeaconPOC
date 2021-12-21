package com.mrzhevskyi.beaconpoc

import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.fragment.app.Fragment

    public fun Fragment.showToast(text: String){
        Toast.makeText(this.requireContext(),text,LENGTH_LONG).show()
    }