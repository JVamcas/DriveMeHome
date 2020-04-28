package com.owambo.jvamcas.drivemehome.ui.home;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.owambo.jvamcas.drivemehome.R;
import com.owambo.jvamcas.drivemehome.ui.AbstractAppFragment;
import com.owambo.jvamcas.drivemehome.utils.Results;

public abstract class AbstractHomeFragmentHelper extends AbstractAppFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstance) {
        super.onCreateView(inflater,parent,savedInstance);
        return null;
    }

    public void parseOpResults(Results results) {

        if (results instanceof Results.Error) {
            Results.Error.ERR_CODE errCode = ((Results.Error) results).getErrorCode();
            Resources res = requireActivity().getResources();

            switch (errCode) {
                case NETWORK_ERROR:
                    showToast(res.getString(R.string.no_internet));
                    break;
                case PERMISSION_DENIED:
                    showToast(res.getString(R.string.permission_denied));
                    break;
                case UNKNOWN_ERROR:
                    showToast(res.getString(R.string.unknown_error));
                    break;
            }
        }
    }
}
