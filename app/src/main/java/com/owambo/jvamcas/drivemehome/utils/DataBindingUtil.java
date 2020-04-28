package com.owambo.jvamcas.drivemehome.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.IdRes;
import androidx.databinding.BindingAdapter;

import com.google.android.material.button.MaterialButton;
import com.owambo.jvamcas.drivemehome.utils.transform.CircleTransformation;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public class DataBindingUtil {

    @BindingAdapter(value = {"android:errorMsg"})
    public static void emptyEdit(EditText mEditText, String errorMsg) {
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence value, int i, int i1, int i2) {
                mEditText.setSelection(value.length());
                if (value.toString().trim().isEmpty())
                    mEditText.setError(errorMsg);
                else mEditText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    @BindingAdapter(value = {"currency", "errorMsg"})
    public static void validateTripCharge(EditText mEditText, String currency, String errorMsg) {
        mEditText.addTextChangedListener(new TextWatcher() {
            String oldValue = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                oldValue = charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String newValue = charSequence.toString();
                if (!newValue.equals(oldValue)) {//avoid recursive call
                    if (!isValidPrice(newValue))
                        mEditText.setError(errorMsg);

                    else {
                        mEditText.setError(null);
                        //ensure money value is prepended with a currency
                        String costValue = currency + " " + newValue.replaceAll("[^.0123456789]", "");
                        mEditText.setText(costValue);
                    }
                }mEditText.setSelection(newValue.length());
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /***
     * Load image from provided url, transform it and set it into the imageview
     * @param mView the image view
     * @param default_icon default icon in case of error or when url is null
     * @param photoUrl the url
     * @param size the required size of the image
     */
    @BindingAdapter(value = {"default_icon", "photoUrl", "size", "viewId"})
    public static void icon(ImageView mView, @IdRes int default_icon, String photoUrl, int size, String viewId) {

        String absPath = null;
        if (photoUrl != null) {//check if picture for this user exist on device storage
            File dir = new File(mView.getContext().getExternalFilesDir(null), Const.IMAGE_ROOT_PATH);
            FilenameFilter filter = ((file1, fileName) ->
                    fileName.replaceAll(".*_(\\w+)_.*", "$1").equals(viewId));
            String[] result = dir.list(filter);
            absPath = result.length == 0 ? photoUrl ://view image does not exist in storage, use photoUrl
                    "file:" + dir.getAbsolutePath() + "/" + result[0];//view image exist in storage, use that image
        }
        ImageUtil.requestCreator(new CircleTransformation(), absPath, size, default_icon).into(mView);
    }

    @BindingAdapter(value = {"username", "email", "cellphone"})
    public static void validateUser(MaterialButton button, String username, String email, String cellphone) {
        button.setEnabled(username != null && !username.trim().isEmpty() && isValidEmail(email) && isValidMobile(cellphone));
    }

    private static boolean isValidEmail(String email) {
        String EMAIL_STRING = "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        return Pattern.compile(EMAIL_STRING).matcher(email).matches();
    }

    private static boolean isValidMobile(String phone) {
        if (phone != null && !Pattern.matches("\\+?[a-zA-Z]+", phone)) {
            return phone.length() >= 7 && phone.length() <= 13;
        }
        return false;
    }

    private static boolean isValidPrice(String price) {
        String priceValue = price.replaceAll("[^.0123456789]", "");
        String[] priceArray = priceValue.split("\\.");
        return !priceValue.isEmpty() && (priceArray.length != 2 || priceArray[1].length() <= 2);
    }
}
