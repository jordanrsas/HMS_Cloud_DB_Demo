package com.dtse.hmsclouddbconnection;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.dtse.hmsclouddbconnection.utils.HmsAuth;
import com.huawei.agconnect.auth.AGConnectAuth;

public class MainActivity extends AppCompatActivity implements HmsAuth {

    NavController navController;

    private static final String TAG = "HMSCloudDBT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_container);
        navController = navHostFragment.getNavController();
    }

    /**
     * Anonymous Account
     * You can integrate the anonymous account authentication mode into your app,
     * so that your users can be identified by AppGallery Connect in guest mode.
     * <p>
     * Before You Start
     * Integrate the Auth Service SDK into your app. For details,
     * please refer to Integrating the Auth Service SDK.
     * https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-auth-web-getstarted-0000001053612703#EN-US_TOPIC_0000001072468607__section717172671510
     * <p>
     * Enable Auth Service for Anonymous Account in AppGallery Connect.
     * For details, please refer to Enabling Auth Service.
     * https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-auth-web-getstarted-0000001053612703#EN-US_TOPIC_0000001072468607__section136771946897
     */
    @Override
    public void signIn() {
        AGConnectAuth auth = AGConnectAuth.getInstance();
        auth.signInAnonymously().addOnSuccessListener(this, signInResult -> {
            Log.w(TAG, "addOnSuccessListener: " + signInResult.getUser().getDisplayName());
            navController.navigate(R.id.action_loginFragment_to_dbFragment);
        }).addOnFailureListener(this, e -> {
            Log.w(TAG, "sign in for agc failed: " + e.getMessage());
        });
    }

    @Override
    public void signOut() {

    }
}