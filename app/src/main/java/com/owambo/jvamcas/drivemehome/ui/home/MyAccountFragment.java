package com.owambo.jvamcas.drivemehome.ui.home;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.owambo.jvamcas.drivemehome.R;
import com.owambo.jvamcas.drivemehome.databinding.FragmentMyAccountBinding;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyAccountFragment extends AbstractHomeFragmentHelper {

    private FragmentMyAccountBinding accountBinding;

    public MyAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater,container,savedInstanceState);
        accountBinding = FragmentMyAccountBinding.inflate(inflater,container,false);

        user_vModel.getUser().observe(getViewLifecycleOwner(),user -> {
            accountBinding.setMUser(user);
        });


        return accountBinding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.fragment_my_account, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem mItem) {
        switch (mItem.getItemId()) {
            case R.id.update_my_account:
                navController.navigate(R.id.action_global_updateUserProfileFragment);
                break;
        }
        return super.onOptionsItemSelected(mItem);
    }
}
