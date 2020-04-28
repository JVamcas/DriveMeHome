package com.owambo.jvamcas.drivemehome.viewmodel;

import android.app.Application;
import android.content.Intent;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.appspot.drivemehome_86841.drivemehomeapi.model.Trip;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.owambo.jvamcas.drivemehome.data.model.User;
import com.owambo.jvamcas.drivemehome.data.repo.UserRepo;
import com.owambo.jvamcas.drivemehome.utils.Results;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserViewModel extends AndroidViewModel {

    public enum AuthState {
        AUTHENTICATED,
        UNAUTHENTICATED,
        AUTH_CANCELLED,
        NETWORK_ERROR,
        UNKNOWN_ERROR
    }

    private MutableLiveData<User> user = new MutableLiveData<>();
    private MutableLiveData<AuthState> authState = new MutableLiveData<>();
    private MutableLiveData<Results> op_results = new MutableLiveData<>();
    private MutableLiveData<Trip> currentTrip = new MutableLiveData<>();
    private final MutableLiveData<List<Trip>> driverRequests = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> signInProviderID = new MutableLiveData<>();

    private UserRepo mUserRepo;
    private Application mApplication;

    public UserViewModel(Application mApplication) {
        super(mApplication);
        this.mApplication = mApplication;
        mUserRepo = new UserRepo();

        if (mUserRepo.isLoggedIn()) {//sometimes userviewmodel is null but user is logged in
            authState.setValue(AuthState.AUTHENTICATED);
            mUserRepo.setUserId(FirebaseAuth.getInstance().getUid());
            loadUserProfile();
        } else authState.setValue(AuthState.UNAUTHENTICATED);
    }

    public void authenticate(int requestCode, int resultCode, Intent data) {
        mUserRepo.authenticate(requestCode, resultCode, data, (user, mResult) -> {
            if (mResult instanceof Results.Success) {
                authState.setValue(AuthState.AUTHENTICATED);
                this.user.setValue(user);

            } else {
                Results.Error error = (Results.Error) mResult;
                switch (error.getErrorCode()) {
                    case AUTH_CANCELLED:
                        authState.setValue(AuthState.AUTH_CANCELLED);
                        break;
                    case UNKNOWN_ERROR:
                        authState.setValue(AuthState.UNKNOWN_ERROR);
                        break;
                    case NETWORK_ERROR:
                        authState.setValue(AuthState.NETWORK_ERROR);
                        break;
                }
            }
        });
    }

    public void uploadImage(String imagePath) {
        mUserRepo.uploadImage(imagePath, user.getValue(), (mUser, mResult) -> {
            if (mResult instanceof Results.Success)
                user.setValue(mUser);
            op_results.setValue(mResult);
        });
    }


    private void loadUserProfile() {
        mUserRepo.loadUserProfile((user, mResult) -> {
            if (mResult instanceof Results.Success) {
                this.user.setValue(user);
                signInProviderID.setValue(mUserRepo.getFirebaseUser().getProviderId());

            }
        });
    }

    public void downloadImage(String destination) {
        mUserRepo.downloadPictureToDevice(destination, (value, mResult) -> {
            if (mResult instanceof Results.Success)
                user.setValue(user.getValue());//force view refresh
        });
    }

    public void logout() {
        AuthUI.getInstance()
                .signOut(mApplication)
                .addOnSuccessListener(task -> {
                    user.setValue(null);
                    authState.setValue(AuthState.UNAUTHENTICATED);
                    signInProviderID.setValue(null);
                });
    }

    public void updateTripDetails(Trip trip) {
        currentTrip.setValue(trip);
    }

    public void updateUserProfile(User mUser) {
        mUserRepo.updateUserProfile(mUser, (user, mResult) ->
        {
            if (mResult instanceof Results.Success)
                this.user.setValue(user);
            op_results.setValue(mResult);
        });
    }

    public void updateUserLocation(LatLng mL) {
        User mUser = user.getValue();
        assert mUser != null;
        mUser.setLatitude(mL.latitude);
        mUser.setLongitude(mL.longitude);
        updateUserProfile(mUser);
    }

    public synchronized void addDriverRequest(Trip mTrip) {
        List<Trip> tripList = driverRequests.getValue();
        tripList.add(mTrip);
        driverRequests.setValue(tripList);

    }

    public synchronized void removeDriverRequest(String tripId) {
        Iterator<Trip> iterator = driverRequests.getValue().iterator();

        while (iterator.hasNext()) {
            Trip currentTrip = iterator.next();
            if (currentTrip.getId().equals(tripId))
                iterator.remove();
        }
        driverRequests.setValue(driverRequests.getValue());
    }

    public LiveData<Trip> getCurrentTrip() {
        return currentTrip;
    }

    public void newTrip(Trip trip) {
        currentTrip.setValue(trip);
    }

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<AuthState> getAuthState() {
        return authState;
    }

    public LiveData<Results> getOpResults() {
        return op_results;
    }

    public synchronized LiveData<List<Trip>> getDriverRequests() {
        return driverRequests;
    }


    public void resetOpResults(LifecycleOwner owner) {
        op_results.removeObservers(owner);
        op_results.setValue(null);
    }

    public void cancelAuth(LifecycleOwner owner) {
        authState.removeObservers(owner);
        authState.setValue(AuthState.UNAUTHENTICATED);
    }
}
