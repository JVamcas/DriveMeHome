package com.owambo.jvamcas.drivemehome.ui.driverpair;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.owambo.jvamcas.drivemehome.databinding.FragmentDriverRequestBinding;
import com.owambo.jvamcas.drivemehome.ui.home.AbstractHomeFragmentHelper;

public class DriverRequestFragment extends AbstractHomeFragmentHelper {


    private FragmentDriverRequestBinding requestBinding;
    private DriverRequestAdapter mDriverRequestAdapter;
    public DriverRequestFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);

        requestBinding = FragmentDriverRequestBinding.inflate(inflater,container,false);

        handleRecyclerView();
        return requestBinding.getRoot();
    }

    private void handleRecyclerView() {
        RecyclerView recyclerView = requestBinding.driveRequestsList;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mDriverRequestAdapter = new DriverRequestAdapter();
        recyclerView.setAdapter(mDriverRequestAdapter);

        user_vModel.getDriverRequests().observe(getViewLifecycleOwner(),driverRequestList -> {
            mDriverRequestAdapter.setTripList(driverRequestList);
            mDriverRequestAdapter.notifyDataSetChanged();

            requestBinding.setDriverRequestCount(driverRequestList.size());
        });
    }
}
