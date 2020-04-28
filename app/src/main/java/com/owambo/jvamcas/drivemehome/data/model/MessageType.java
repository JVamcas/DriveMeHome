package com.owambo.jvamcas.drivemehome.data.model;

/***
 * Constants indicating the type of message in communication between client and server
 */
public enum MessageType {
    SEND_DRIVER_CHARGE_TO_CLIENT,
    DRIVER_REQUEST,
    NOTIFY_CLIENT_DRIVE_REQUEST_REJECTED,
    NOTIFY_DRIVERS_TRIP_CANCELLED,
    TRIP_CANCELLED,
    FIND_NEAREST_DRIVERS,
    DRIVER_TRIP_CHARGE,
    DRIVE_REQUEST_REJECTED,
    NOTIFY_DRIVER_REMOVED,
    DRIVER_REMOVED_FROM_TRIP,
    NOTIFY_DRIVER_SELECTED,
    DRIVER_SELECTED,
    MESSAGE_TYPE,
}
