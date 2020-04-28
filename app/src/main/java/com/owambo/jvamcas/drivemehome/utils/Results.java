package com.owambo.jvamcas.drivemehome.utils;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.owambo.jvamcas.drivemehome.data.repo.UserRepo;

import static com.google.firebase.firestore.FirebaseFirestoreException.Code.ALREADY_EXISTS;
import static com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED;
import static com.google.firebase.firestore.FirebaseFirestoreException.Code.UNKNOWN;

public class Results<T> {
    private T msg;

    private Results(T error) {
        msg = error;
    }

    public T getMsg() {
        return msg;
    }


    public static final class Success extends Results<String> {

        public Success(String msg) {
            super(msg);
        }

        @NonNull
        @Override
        public String toString() {
            return getMsg();
        }
    }

    public static final class Error extends Results<Exception> {

        private ERR_CODE errorCode;

        public enum ERR_CODE {
            AUTH_CANCELLED, NETWORK_ERROR, PERMISSION_DENIED, UNKNOWN_ERROR, ENTITY_EXISTS_ERROR
        }

        public Error(Exception error) {
            super(error);
            parseError();
        }

        public ERR_CODE getErrorCode() {
            return errorCode;
        }

        private void parseError() {
            Exception error = getMsg();
            if (error instanceof UserRepo.AuthCancelException)
                errorCode = ERR_CODE.AUTH_CANCELLED;
            else if (error instanceof FirebaseFirestoreException) {
                FirebaseFirestoreException e = (FirebaseFirestoreException) error;
                if (e.getCode() == PERMISSION_DENIED)
                    errorCode = ERR_CODE.PERMISSION_DENIED;
                else if (e.getCode() == ALREADY_EXISTS)
                    errorCode = ERR_CODE.ENTITY_EXISTS_ERROR;
                else if (e.getCode() == UNKNOWN)
                    errorCode = ERR_CODE.UNKNOWN_ERROR;
            } else if (error instanceof FirebaseNetworkException)
                errorCode = ERR_CODE.NETWORK_ERROR;
            else errorCode = ERR_CODE.UNKNOWN_ERROR;
        }
    }
}
