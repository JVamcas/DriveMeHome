package com.owambo.jvamcas.drivemehome.ui.driverpair;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.appspot.drivemehome_86841.drivemehomeapi.model.Person;
import com.owambo.jvamcas.drivemehome.data.model.User;
import com.owambo.jvamcas.drivemehome.databinding.FragmentCurrentTripBinding;
import com.owambo.jvamcas.drivemehome.ui.home.AbstractHomeFragmentHelper;
import com.owambo.jvamcas.drivemehome.utils.ParseUtil;


/**
 * A simple {@link Fragment} subclass.
 */
public class CurrentTripFragment extends AbstractHomeFragmentHelper {


    public CurrentTripFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TripManager tripManager = HomeFragment.tripManager;

        // Inflate the layout for this fragment
        FragmentCurrentTripBinding tripDetailsBinding = FragmentCurrentTripBinding.inflate(inflater, container, false);

        user_vModel.getCurrentTrip().observe(getViewLifecycleOwner(), mTrip -> {
            if (mTrip != null) {
                if (mTrip.getAllocatedDriver() != null) {
                    tripDetailsBinding.allocatedDriver.getRoot().setVisibility(View.VISIBLE);
                    tripDetailsBinding.noDriverAllocated.setVisibility(View.GONE);

                    Person allocatedDriver = mTrip.getAllocatedDriver();
                    String tripCharge = allocatedDriver == null ? null : tripManager.getNearbyDriverCharge(allocatedDriver.getId());
                    tripDetailsBinding.setMUser(ParseUtil.parseUser(allocatedDriver, User.class));
                    tripDetailsBinding.setTripCharge(tripCharge);
                } else {
                    tripDetailsBinding.allocatedDriver.getRoot().setVisibility(View.GONE);
                    tripDetailsBinding.noDriverAllocated.setVisibility(View.VISIBLE);
                }
            }tripDetailsBinding.setMTrip(tripManager.getCurrentTrip());
        });

        //trip cancelled by the client
        tripDetailsBinding.cancelTrip.setOnClickListener(view -> tripManager.cancelCurrentTrip());

        //client removed driver from this trip
        tripDetailsBinding.allocatedDriver.actionBtn.setOnClickListener(view -> tripManager.removeDriverFromTrip());

        return tripDetailsBinding.getRoot();
    }
}
