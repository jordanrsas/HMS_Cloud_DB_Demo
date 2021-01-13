package com.dtse.hmsclouddbconnection.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dtse.hmsclouddbconnection.R;
import com.dtse.hmsclouddbconnection.model.CloudDBZoneWrapper;
import com.dtse.hmsclouddbconnection.model.users;

import java.util.Arrays;
import java.util.List;

public class DbFragment extends Fragment implements
        CloudDBZoneWrapper.UiCallback {
    private CloudDBZoneWrapper mCloudDBZoneWrapper;
    private Handler mHandler = null;

    private EditText userNameEdit;
    private EditText lastNameEdit;
    private EditText nickNameEdit;
    private EditText avatarEdit;
    private TextView usersTextView;
    private EditText userIdToDeletEdit;

    private Boolean isDeleteAction = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
        mHandler = new Handler(Looper.getMainLooper());

        mHandler.postDelayed(() -> {
            mCloudDBZoneWrapper.addCallBacks(this);
            mCloudDBZoneWrapper.createObjectType();
            mCloudDBZoneWrapper.openCloudDBZoneV2();
        }, 500);


        //mHandler.post(mCloudDBZoneWrapper::queryAllUsers);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_db, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userNameEdit = view.findViewById(R.id.nameEditText);
        lastNameEdit = view.findViewById(R.id.lastNameEditText);
        nickNameEdit = view.findViewById(R.id.nickNameEditText);
        avatarEdit = view.findViewById(R.id.avatarEditText);
        usersTextView = view.findViewById(R.id.usersListView);
        userIdToDeletEdit = view.findViewById(R.id.uidToDeleteEdit);

        view.findViewById(R.id.queryAllUsers).setOnClickListener(viewBtn -> {
            mHandler.post(mCloudDBZoneWrapper::queryAllUsers);
        });

        view.findViewById(R.id.saveUserButton).setOnClickListener(viewBtn -> {
            upsertUser();
        });

        view.findViewById(R.id.deletUserButton).setOnClickListener(viewBtn -> {
            deleteUser();
        });
    }

    private void upsertUser() {
        String name = userNameEdit.getText().toString();
        String lastName = lastNameEdit.getText().toString();
        String nickName = nickNameEdit.getText().toString();
        String avatar = avatarEdit.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(lastName)) {
            Toast.makeText(getContext(), "Name and Last Name are required.", Toast.LENGTH_LONG).show();
            return;
        }
        String uid = name + "_" + lastName;

        users userInfo = new users();
        userInfo.setUid(uid);
        userInfo.setName(name);
        userInfo.setLast(lastName);
        userInfo.setNick(nickName);
        userInfo.setAvatar(avatar);

        mHandler.post(() -> mCloudDBZoneWrapper.upsertUserInfos(userInfo));
    }

    private void deleteUser() {
        String userId = userIdToDeletEdit.getText().toString();
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(getContext(), "Set User Uid comma separated values to delete", Toast.LENGTH_LONG).show();
            return;
        }

        List<String> usersList = Arrays.asList(userId.split(","));
        isDeleteAction = true;
        mHandler.post(() -> mCloudDBZoneWrapper.queryUsersToDelete(usersList));
    }

    @Override
    public void onDestroy() {
        mHandler.post(mCloudDBZoneWrapper::closeCloudDBZone);
        super.onDestroy();
    }

    @Override
    public void onAddOrQuery(List<users> userInfoList) {
        if (isDeleteAction) {
            mHandler.post(() -> mCloudDBZoneWrapper.deleteUserInfo(userInfoList));
            isDeleteAction = false;
        } else {
            usersTextView.setText("");
            StringBuilder users = new StringBuilder();
            for (users user : userInfoList) {
                users.append(user.getName()).append(", ");
            }
            usersTextView.setText(users.toString());
        }
    }

    @Override
    public void onDelete() {

    }

    @Override
    public void onUpsert() {
    }
}
