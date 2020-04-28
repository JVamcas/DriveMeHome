package com.owambo.jvamcas.drivemehome.data.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.firebase.auth.FirebaseUser;
import com.owambo.jvamcas.drivemehome.BR;

public class User extends BaseObservable {

    private String id;
    private String deviceId;//id of device currently being used by this user
    private String name;
    private String cellphone;
    private String email;
    private String licenseCode;
    private String licenseNumber;
    private String userIconUrl;
    private double latitude = -33.8523341;
    private double longitude = 151.2106085;
    private boolean available = true;//whether a driver is available for request or not

    //    private Location location;

    public User() {
    }


    private String vehicleRegistrationNumber;
    private String vehicleModel;

    public User(FirebaseUser mUser) {
        name = mUser.getDisplayName();
        id = mUser.getUid();
        userIconUrl = mUser.getPhotoUrl() == null ? null : mUser.getPhotoUrl().toString();
        cellphone = mUser.getPhoneNumber();
        email = mUser.getEmail();
    }

    @Bindable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (this.id == null || !this.id.equals(id)) {
            this.id = id;
            notifyPropertyChanged(BR.id);
        }
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (this.name == null || !this.name.equals(name)) {
            this.name = name;
            notifyPropertyChanged(BR.name);
        }
    }

    @Bindable
    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        if (this.cellphone == null || !this.cellphone.equals(cellphone)) {
            this.cellphone = cellphone;
            notifyPropertyChanged(BR.cellphone);
        }
    }

    @Bindable
    public String getLicenseCode() {
        return licenseCode;
    }

    public void setLicenseCode(String licenseCode) {
        if (this.licenseCode == null || !this.licenseCode.equals(licenseCode)) {
            this.licenseCode = licenseCode;
            notifyPropertyChanged(BR.licenseCode);
        }
    }


    @Bindable
    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        if (this.licenseNumber == null || !this.licenseNumber.equals(licenseNumber)) {
            this.licenseNumber = licenseNumber;
            notifyPropertyChanged(BR.licenseNumber);
        }
    }

    @Bindable
    public String getUserIconUrl() {
        return userIconUrl;
    }

    public void setUserIconUrl(String userIconUrl) {
        if (this.userIconUrl == null || !this.userIconUrl.equals(userIconUrl)) {
            this.userIconUrl = userIconUrl;
            notifyPropertyChanged(BR.userIconUrl);
        }
    }

    @Bindable
    public String getVehicleRegistrationNumber() {
        return vehicleRegistrationNumber;
    }

    public void setVehicleRegistrationNumber(String vehicleRegistrationNumber) {
        if (this.vehicleRegistrationNumber == null || !this.vehicleRegistrationNumber.equals(vehicleRegistrationNumber)) {
            this.vehicleRegistrationNumber = vehicleRegistrationNumber;
            notifyPropertyChanged(BR.vehicleRegistrationNumber);
        }
    }

    @Bindable
    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        if (this.vehicleModel == null || !this.vehicleModel.equals(vehicleModel)) {
            this.vehicleModel = vehicleModel;
            notifyPropertyChanged(BR.vehicleModel);
        }
    }

    @Bindable
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (this.email == null || !this.email.equals(email)) {
            this.email = email;
            notifyPropertyChanged(BR.email);
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
