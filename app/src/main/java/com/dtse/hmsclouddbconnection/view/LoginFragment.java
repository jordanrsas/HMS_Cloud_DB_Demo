package com.dtse.hmsclouddbconnection.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dtse.hmsclouddbconnection.R;
import com.dtse.hmsclouddbconnection.utils.HmsAuth;

public class LoginFragment extends Fragment {
    private HmsAuth hmsAuthCallback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            hmsAuthCallback = (HmsAuth) context;
        }catch (ClassCastException ex){
            throw new ClassCastException(ex.getMessage() + " must implement HmsAuth interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button loginBtn = view.findViewById(R.id.buttonHuaweiLogIn);
        loginBtn.setOnClickListener(viewBtn ->{
            hmsAuthCallback.signIn();
        });
    }
}
