package com.owambo.jvamcas.drivemehome.ui.driverpair;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.appspot.drivemehome_86841.drivemehomeapi.model.Trip;
import com.owambo.jvamcas.drivemehome.databinding.DriveRequestBinding;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestAdapter extends RecyclerView.Adapter<DriverRequestAdapter.ViewHolder> {

    private List<Trip> tripList = new ArrayList<>();
    private DriveRequestBinding driveRequestBinding;
    private TripManager tripManager = HomeFragment.tripManager;


    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        driveRequestBinding = DriveRequestBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(driveRequestBinding.getRoot());
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {
        holder.mTrip = tripList.get(position);
        driveRequestBinding.setMTrip(holder.mTrip);
        String allocatedDriverId = holder.mTrip.getAllocatedDriver().getId();
        String tripCharge = tripManager.getNearbyDriverCharge(allocatedDriverId);
        driveRequestBinding.setTripCharge(tripCharge);
        driveRequestBinding.setErrorMsg("Invalid value.");

        driveRequestBinding.driverRejectClientRequest.setOnClickListener(view -> {
            tripManager.driverRejectClientRequest(holder.mTrip);
        });

        driveRequestBinding.updateTripCharge.setOnClickListener(view -> {
            String chargeString = String.valueOf(driveRequestBinding.tripCharge.getText());
            tripManager.updateTripCharge(holder.mTrip, chargeString);
        });
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        Trip mTrip;
        ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public void setTripList(List<Trip> tripList) {
        this.tripList = tripList;
    }
}
