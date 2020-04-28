package com.owambo.jvamcas.drivemehome.data.repo;

import android.content.Intent;

import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.owambo.jvamcas.drivemehome.data.model.User;
import com.owambo.jvamcas.drivemehome.utils.Const;
import com.owambo.jvamcas.drivemehome.utils.ParseUtil;
import com.owambo.jvamcas.drivemehome.utils.Results;
import com.owambo.jvamcas.drivemehome.utils.callback.Callback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public final class UserRepo {

    private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
    private final FirebaseStorage STORE = FirebaseStorage.getInstance();
    private static String USER_PATH;
    private String userId;

    public UserRepo() {
    }

    public void authenticate(int requestCode, int resultCode, Intent data, Callback<User> callback) {
        if (requestCode == Const.RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                User newUser = new User(FirebaseAuth.getInstance().getCurrentUser());
                setUserId(newUser.getId());
                loadUserProfile((oldUser, mResult) -> {
                    if (mResult instanceof Results.Success) {
                        if (oldUser != null)//user exist
                            callback.onTaskComplete(oldUser, new Results.Success(""));
                        else
                            createNewUser(newUser, callback);
                    } else
                        callback.onTaskComplete(oldUser, mResult);//error happened while fetching old user
                });
                callback.onTaskComplete(null, new Results.Success("Welcome to Stokka!"));
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                if (response == null)
                    callback.onTaskComplete(null, new Results.Error(new AuthCancelException()));
                else callback.onTaskComplete(null, new Results.Error(response.getError()));
            }
        }
    }

    private void createNewUser(User mUser, Callback<User> callBack) {
        DB.document(USER_PATH)
                .set(mUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        callBack.onTaskComplete(mUser, new Results.Success(""));
                    else callBack.onTaskComplete(mUser, new Results.Error(task.getException()));
                });
    }

    public void updateUserProfile(User mUser, Callback<User> callback) {
        //TODO to be fixed later
        DB.document(USER_PATH)
                .update(ParseUtil.toMap(mUser))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        callback.onTaskComplete(mUser, new Results.Success(""));
                    else callback.onTaskComplete(null, new Results.Error(task.getException()));
                });
    }

    public void loadUserProfile(Callback<User> callback) {
        DB.document(USER_PATH).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User mUser = null;
                        DocumentSnapshot shot = task.getResult();

                        if (shot != null)
                            mUser = shot.toObject(User.class);
                        callback.onTaskComplete(mUser, new Results.Success(""));
                    } else callback.onTaskComplete(null, new Results.Error(task.getException()));
                });
    }

    public void uploadImage(String imagePath, User mUser, Callback<User> callback) {
        String destination = ParseUtil.path(Const.IMAGE_ROOT_PATH, getFirebaseUser().getUid());
        StorageReference ref = STORE.getReference(destination);
        try {
            FileInputStream stream = new FileInputStream(new File(imagePath));
            ref.putStream(stream).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ref.getDownloadUrl().addOnCompleteListener(urlTask -> {
                        if (urlTask.isSuccessful() && urlTask.getResult() != null) {
                            String url = urlTask.getResult().toString();
                            mUser.setUserIconUrl(url);
                            updateUserProfile(mUser, callback);
                        } else
                            callback.onTaskComplete(null, new Results.Error(urlTask.getException()));
                    });
                } else
                    callback.onTaskComplete(null, new Results.Error(task.getException()));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadPictureToDevice(String destinationPath, Callback<Void> callback) {
        String sourcePath = ParseUtil.path(Const.IMAGE_ROOT_PATH, getFirebaseUser().getUid());
        StorageReference ref = STORE.getReference(sourcePath);
        File mFile = new File(destinationPath);

        ref.getFile(mFile).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                callback.onTaskComplete(null, new Results.Success(""));
            else callback.onTaskComplete(null, new Results.Error(task.getException()));
        });
    }


    public void setUserId(String id) {
        userId = id;
        USER_PATH = ParseUtil.path(Const.USERS, userId);
    }

    public final class AuthCancelException extends Exception {
    }

    public final class UpLoadCancelException extends Exception {
    }

    public FirebaseUser getFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public boolean isLoggedIn() {
        return getFirebaseUser() != null;
    }
}
