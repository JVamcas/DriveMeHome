package com.owambo.jvamcas.drivemehome.ui.driverpair;

import android.os.AsyncTask;


import androidx.annotation.NonNull;

import com.appspot.drivemehome_86841.drivemehomeapi.Drivemehomeapi;
import com.appspot.drivemehome_86841.drivemehomeapi.model.Charge;
import com.appspot.drivemehome_86841.drivemehomeapi.model.ClientMessage;
import com.appspot.drivemehome_86841.drivemehomeapi.model.Person;
import com.appspot.drivemehome_86841.drivemehomeapi.model.Spot;
import com.appspot.drivemehome_86841.drivemehomeapi.model.Trip;
import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.owambo.jvamcas.drivemehome.R;
import com.owambo.jvamcas.drivemehome.data.model.MessageType;
import com.owambo.jvamcas.drivemehome.data.model.User;
import com.owambo.jvamcas.drivemehome.utils.ParseUtil;
import com.owambo.jvamcas.drivemehome.utils.Results;
import com.owambo.jvamcas.drivemehome.utils.callback.Callback;
import com.owambo.jvamcas.drivemehome.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.owambo.jvamcas.drivemehome.data.model.MessageType.FIND_NEAREST_DRIVERS;
import static com.owambo.jvamcas.drivemehome.data.model.MessageType.NOTIFY_CLIENT_DRIVE_REQUEST_REJECTED;
import static com.owambo.jvamcas.drivemehome.data.model.MessageType.NOTIFY_DRIVERS_TRIP_CANCELLED;
import static com.owambo.jvamcas.drivemehome.data.model.MessageType.NOTIFY_DRIVER_REMOVED;
import static com.owambo.jvamcas.drivemehome.data.model.MessageType.NOTIFY_DRIVER_SELECTED;
import static com.owambo.jvamcas.drivemehome.data.model.MessageType.SEND_DRIVER_CHARGE_TO_CLIENT;
import static com.owambo.jvamcas.drivemehome.utils.Const.SERVER_ERR;
import static com.owambo.jvamcas.drivemehome.utils.Const.TRIP_ID;

/***
 * Responsible for managing a Trip instance
 * Handles communications with the server: Protocols {FCM and REST}
 *
 * How it works:
 * 1. Client creates new trip instance and request for nearest drivers from server [Param => Trip] [Protocol => REST]
 * 2. Client Receive trip charges from drivers [Param => Trip] [Protocol => FCM]
 * 3. Client request server to notify server selected driver for this trip [Param => Trip] [Protocol => REST]
 * 4. Client receive confirmation from server that driver has departed [Param => Abstract data] [Protocol => FCM]
 */
public class TripManager extends FirebaseMessagingService {

    public static String DEVICE_ID;
    private Trip mTrip;
    private UserViewModel user_vModel;
    private static Drivemehomeapi tripService;
    private static HomeFragment pairFragment;

    public TripManager() {
    }

    public TripManager(UserViewModel user_vModel, HomeFragment pairFragment) {
        TripManager.pairFragment = pairFragment;
        this.user_vModel = user_vModel;
        if (tripService == null) {
            Drivemehomeapi.Builder builder = new Drivemehomeapi.Builder(
                    new NetHttpTransport(), new GsonFactory(), null)
                    .setRootUrl("https://drivemehome-86841.appspot.com/_ah/api/");
            tripService = builder.build();

            requestCurrentDeviceID((deviceId, mResult) -> {
                if (deviceId != null) updateUserDeviceId(deviceId);
            });
        }
    }

    /**
     * Create new trip instance and request server for nearest drivers
     *
     * @param startLocation client's start location
     * @param destinationLocation client's destination
     */
    public void startNewTrip(Spot startLocation, Spot destinationLocation) {
        mTrip = new Trip()
                .setId(ParseUtil.randomUID())
                .setStart(startLocation)
                .setDestination(destinationLocation);

        if (DEVICE_ID == null)
            requestCurrentDeviceID((deviceId, mResult) -> {
                if (mResult instanceof Results.Success && deviceId != null) {
                    DEVICE_ID = deviceId;
                    mTrip.setDeviceId(DEVICE_ID);
                    user_vModel.newTrip(mTrip);

                    updateUserDeviceId(deviceId);
                    requestRoutePath();
                    requestNearestDrivers();
                } else pairFragment.showToast(SERVER_ERR);
            });
        else {
            user_vModel.newTrip(mTrip);
            //request server for drivers nearest to the VO
            requestRoutePath();
            requestNearestDrivers();
        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        DEVICE_ID = token;
        updateUserDeviceId(token);
    }

    private void requestCurrentDeviceID(Callback<String> callback) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null)
                        callback.onTaskComplete(task.getResult().getToken(), new Results.Success(""));

                    else callback.onTaskComplete(null, new Results.Error(task.getException()));
                });
    }

    private void updateUserDeviceId(String deviceId) {
        User user = user_vModel.getUser().getValue();
        if (user != null) {
            user.setDeviceId(deviceId);
            user_vModel.updateUserProfile(user);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> message = remoteMessage.getData();
            ClientMessage clientMessage = ParseUtil.fromMap(message,ClientMessage.class);
            MessageType messageType = MessageType.valueOf(clientMessage.getMessageType());

            if (existValidTrip()) {
                switch (messageType) {

                    //Multiple calls from different drivers might cause concurrency issue
                    //Msg from server containing driver's charge for this trip
                    case DRIVER_TRIP_CHARGE:
                        Charge charge = ParseUtil.fromMap(message, Charge.class);
                        addDriverCharge(charge);
                        break;

                    //Msg from server that client is requesting service for this driver
                    case DRIVER_REQUEST://<--FROM SERVER TO DRIVER-->
                        synchronized (this) {//could receive multiple drive requests simultaneously
                            Trip mTrip = ParseUtil.fromMap(remoteMessage.getData(), Trip.class);
                            user_vModel.addDriverRequest(mTrip);
                            pairFragment.showToast("Received trip request from " + mTrip.getDeviceId());
                        }
                        break;

                    //Msg from the server that driver has rejected the client request
                    //Driver might/not have been allocated to this trip
                    case DRIVE_REQUEST_REJECTED:
                        mTrip.setAllocatedDriver(null);
                        user_vModel.updateTripDetails(mTrip);
                        break;

                    //Msg from server that client has removed this driver from trip
                    case DRIVER_REMOVED_FROM_TRIP:

                        break;

                    //Msg from server that client have cancelled the trip
                    case TRIP_CANCELLED:
                        String tripId = message.get(TRIP_ID);
                        user_vModel.removeDriverRequest(tripId);
                        break;
                }
            }
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        pairFragment.showToast("Messages deleted!");
    }

    /**
     * Client will pre-select driver
     * Server to inform driver he's been chosen
     * On success: driver is allocated, else not
     *
     * @param chosenDriver the driver to be allocated
     */
    public void allocateSelectedDriver(Person chosenDriver) {
        pairFragment.showProgressBar("Notifying driver...");
        mTrip.setAllocatedDriver(chosenDriver);
        ClientMessage clientMessage = clientMessageInstance(NOTIFY_DRIVER_SELECTED, mTrip);

        ToServerTask allocateDriver = new ToServerTask((serverMessage, mResult) -> {
            pairFragment.endProgressBar();
            if (mResult instanceof Results.Success)
                pairFragment.showToast("Your driver " + chosenDriver.getName() + " is on the way");
            else {
                pairFragment.showToast(SERVER_ERR);
                mTrip.setAllocatedDriver(null);
            }
            user_vModel.updateTripDetails(mTrip);

        });
        allocateDriver.execute(clientMessage);
    }

    private void addDriverCharge(Charge mCharge) {
        synchronized (this) {//multiple calls to onMessageRecieved might cause concurrency issues
            //ensure that charge received is for the current trip
            if (existValidTrip() && mCharge.getTripId().equals(mTrip.getId())) {
                List<Charge> driverCharges = mTrip.getNearByDriverChargeList() == null ?
                        new ArrayList<>() : mTrip.getNearByDriverChargeList();

                driverCharges.add(mCharge);
                mTrip.setNearByDriverChargeList(driverCharges);
                user_vModel.updateTripDetails(mTrip);
            }
        }
    }

    /***
     * Request for nearest drivers from server: REST
     */
    public void requestNearestDrivers() {
        //Query for nearest drivers from server==> REST
        ClientMessage clientMessage = clientMessageInstance(FIND_NEAREST_DRIVERS, mTrip);
        pairFragment.showProgressBar("Searching for nearest drivers...");

        ToServerTask driversTask = new ToServerTask((serverMessage, mResult) -> {
            if (mResult instanceof Results.Success) {
                if (serverMessage.getPayload() == null)
                    pairFragment.showToast("Sorry: Could not find drivers nearby.");
                else {
                    Trip mTrip = ParseUtil.fromMap(serverMessage.getPayload(),Trip.class);
                    List<Person>nearbyDrivers = mTrip.getNearByDriverList();
                    mTrip.setNearByDriverList(nearbyDrivers);
                    user_vModel.updateTripDetails(mTrip);
                }
            } else pairFragment.showToast(SERVER_ERR);
            pairFragment.endProgressBar();
        });
        driversTask.execute(clientMessage);
    }

    /***
     * Make async request for route polylines from google Directions API
     */
    public void requestRoutePath() {
        String api_key = pairFragment.requireActivity().getResources().getString(R.string.google_map_api);
        LatLng origin = new LatLng(mTrip.getStart().getLatitude(), mTrip.getStart().getLongitude());
        LatLng end = new LatLng(mTrip.getDestination().getLatitude(), mTrip.getDestination().getLongitude());

        new GoogleMapRoute(api_key, origin, end).
                path((routePoints, mResult) -> {
                    List<Spot> locationList = null;

                    if (routePoints != null) {
                        locationList = new ArrayList<>();
                        for (LatLng latLng : routePoints) {
                            locationList.add(new Spot()
                                    .setLatitude(latLng.latitude)
                                    .setLongitude(latLng.longitude));
                        }
                    }
                    user_vModel.updateTripDetails(mTrip.setRoutePoints(locationList));
                });
    }

    public void cancelCurrentTrip() {
        if (existValidTrip()) {
            // Notify all nearby drivers via server that current trip has been cancelled by the user
            ClientMessage clientMessage = clientMessageInstance(NOTIFY_DRIVERS_TRIP_CANCELLED, mTrip);

            ToServerTask tripCancelled = new ToServerTask((serverMessage, mResult) -> {
                if (mResult instanceof Results.Success) {
                    mTrip = null;
                    user_vModel.updateTripDetails(null);
                    pairFragment.moveToCurrentLocation();
                    pairFragment.showToast("Trip has been cancelled!");
                } else {
                    pairFragment.showToast(SERVER_ERR);
                }
            });
            tripCancelled.execute(clientMessage);
        }
    }

    public boolean existValidTrip() {
        return mTrip != null;
    }

    public Trip getCurrentTrip() {
        return mTrip;
    }

    public Person getNearbyDriver(String driverId) {
        if (mTrip.getNearByDriverList() != null) {
            for (Person driver : mTrip.getNearByDriverList()) {
                if (driver.getId().equals(driverId))
                    return driver;
            }
        }
        return null;
    }

    public String getNearbyDriverCharge(String driverId) {
        if (mTrip.getNearByDriverChargeList() != null) {
            for (Charge charge : mTrip.getNearByDriverChargeList()) {
                if (charge.getClientDeviceId().equals(driverId))
                    return ParseUtil.moneyString("NAD", charge.getAmount());
            }
        }
        return ParseUtil.moneyString("NAD", 0.0);
    }

    /***
     * Remove driver allocated to this trip
     * Server to inform driver that he's been removed
     * On success, driver is removed else not
     */
    public void removeDriverFromTrip() {
        ClientMessage clientMessage = clientMessageInstance(NOTIFY_DRIVER_REMOVED, mTrip);

        ToServerTask allocatedDriverTask = new ToServerTask((serverMessage, mResult) -> {
            if (mResult instanceof Results.Success) {
                mTrip.setAllocatedDriver(null);
                user_vModel.updateTripDetails(mTrip);
                pairFragment.showToast("Driver removed!");
            } else pairFragment.showToast(SERVER_ERR);
        });
        allocatedDriverTask.execute(clientMessage);
    }

    public void updateTripCharge(Trip mTrip, String newCharge) {
        User user = user_vModel.getUser().getValue();
        if (user != null) {
            Charge mCharge = new Charge()
                    .setTripId(mTrip.getId())
                    .setClientDeviceId(mTrip.getDeviceId())
                    .setDriverDeviceId(user.getDeviceId())
                    .setAmount(ParseUtil.moneyDigit(newCharge));

            List<Charge> chargeList = new ArrayList<>();
            chargeList.add(mCharge);

            Trip dummyTrip = new Trip()
                    .setNearByDriverChargeList(chargeList);

            ClientMessage clientMessage = clientMessageInstance(
                    SEND_DRIVER_CHARGE_TO_CLIENT, dummyTrip);

            ToServerTask tripCharge = new ToServerTask((serverMessage, mResult) -> {
                if (mResult instanceof Results.Success)
                    pairFragment.showToast("Trip cost updated.");
                else pairFragment.showToast(SERVER_ERR);
            });
            tripCharge.execute(clientMessage);
        }
    }
    /***
     * Asynchronously sends messages to the server
     */
    public static class ToServerTask extends AsyncTask<ClientMessage, Void, ClientMessage> {
        Callback<ClientMessage> messageCallback;

        ToServerTask(Callback<ClientMessage> messageCallback) {
            this.messageCallback = messageCallback;
        }

        @Override
        protected ClientMessage doInBackground(ClientMessage... clientMessages) {
            ClientMessage toServer = clientMessages[0];
            MessageType messageType = MessageType.valueOf(toServer.getMessageType());

            Trip mTrip = ParseUtil.fromMap(toServer.getPayload(),Trip.class);
            Charge mCharge = ParseUtil.fromMap(toServer.getPayload(), Charge.class);

            try {
                switch (messageType) {
                    case FIND_NEAREST_DRIVERS:
                        return tripService.findNearestDrivers(mTrip).execute();

                    case SEND_DRIVER_CHARGE_TO_CLIENT:
                        return tripService.sendDriverTripCharge(mCharge).execute();

                    case NOTIFY_DRIVER_SELECTED:
                        return tripService.notifyDriverSelected(mTrip).execute();

                    case NOTIFY_DRIVER_REMOVED:
                        return tripService.notifyDriverRemoved(mTrip).execute();

                    case NOTIFY_CLIENT_DRIVE_REQUEST_REJECTED:
                        return tripService.notifyClientDriverRequestRejected(mTrip).execute();

                    case NOTIFY_DRIVERS_TRIP_CANCELLED:
                        return tripService.notifyDriversTripCancelled(mTrip).execute();
                }
            }
            catch (Exception e){
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(ClientMessage clientMessage) {
            super.onPostExecute(clientMessage);
            Results mResults = clientMessage == null ? new Results.Error(new Exception()) : new Results.Success("");
            messageCallback.onTaskComplete(clientMessage, mResults);
        }
    }

    public void driverRejectClientRequest(Trip mTrip) {
        ClientMessage clientMessage = clientMessageInstance(NOTIFY_CLIENT_DRIVE_REQUEST_REJECTED, mTrip);

        ToServerTask request = new ToServerTask((serverMessage, mResult) -> {
            if (mResult instanceof Results.Success)
                user_vModel.removeDriverRequest(mTrip.getId());
            else
                pairFragment.showToast("Err: Cannot reject client request now.");
        });
        request.execute(clientMessage);
    }

    @NonNull
    private <K> ClientMessage clientMessageInstance(MessageType messageType, K obj) {
        return new ClientMessage()
                .setMessageType(messageType.name())
                .setPayload(ParseUtil.toMap(obj));
    }
}
