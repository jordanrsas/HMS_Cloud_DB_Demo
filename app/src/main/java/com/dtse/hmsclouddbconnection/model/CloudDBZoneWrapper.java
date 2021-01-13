package com.dtse.hmsclouddbconnection.model;

import android.content.Context;
import android.util.Log;

import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.ListenerHandler;
import com.huawei.agconnect.cloud.database.OnSnapshotListener;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class CloudDBZoneWrapper {
    private static final String TAG = "CloudDBZoneWrapper";

    private AGConnectCloudDB mCloudDB;

    private CloudDBZone mCloudDBZone;

    private CloudDBZoneConfig mConfig;

    private ListenerHandler mRegister;


    private UiCallback mUiCallback = UiCallback.DEFAULT;

    /**
     * Monitor data change from database. Update book info list if data have changed
     */
    private OnSnapshotListener<users> mSnapshotListener = (cloudDBZoneSnapshot, e) -> {
        if (e != null) {
            Log.w(TAG, "onSnapshot: " + e.getMessage());
            return;
        }

        CloudDBZoneObjectList<users> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();
        List<users> usersList = new ArrayList<>();
        try {
            if (snapshotObjects != null) {
                while (snapshotObjects.hasNext()) {
                    users userInfo = snapshotObjects.next();
                    usersList.add(userInfo);
                    //updateUsersIndex(userInfo);
                }
                Log.i(TAG, "usuarios en lista: " + usersList.toString());
            }
        } catch (AGConnectCloudDBException snapshotException) {
            Log.w(TAG, "onSnapshot:(getObject) " + snapshotException.getMessage());
        } finally {
            cloudDBZoneSnapshot.release();
        }
    };

    public CloudDBZoneWrapper() {
        mCloudDB = AGConnectCloudDB.getInstance();
    }

    /**
     * Init AGConnectCloudDB in Application
     *
     * @param context application context
     */
    public static void initAGConnectCloudDB(Context context) {
        AGConnectCloudDB.initialize(context);
    }

    /**
     * Call AGConnectCloudDB.openCloudDBZone to open a cloudDBZone.
     * We set it with cloud cache mode, and data can be store in local storage
     */
    public void openCloudDBZoneV2() {
        mConfig = new CloudDBZoneConfig("first",
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        Task<CloudDBZone> openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true);
        openDBZoneTask.addOnSuccessListener(cloudDBZone -> {
            Log.w(TAG, "open clouddbzone success");
            mCloudDBZone = cloudDBZone;
            // Add subscription after opening cloudDBZone success
            //addSubscription();
        }).addOnFailureListener(e -> {
            Log.w(TAG, "open clouddbzone failed for " + e.getMessage());
        });
    }

    /**
     * Call AGConnectCloudDB.closeCloudDBZone
     */
    public void closeCloudDBZone() {
        try {
            //mRegister.remove();
            mCloudDB.closeCloudDBZone(mCloudDBZone);
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "closeCloudDBZone: " + e.getMessage());
        }
    }

    /**
     * Add a callback to update users info list
     *
     * @param uiCallback callback to update book list
     */
    public void addCallBacks(UiCallback uiCallback) {
        mUiCallback = uiCallback;
    }


    /**
     * Add mSnapshotListener to monitor data changes from storage
     */
    public void addSubscription() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        try {
            CloudDBZoneQuery<users> snapshotQuery = CloudDBZoneQuery.where(users.class)
                    .notEqualTo("name", "");

            mRegister = mCloudDBZone.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, mSnapshotListener);
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "subscribeSnapshot: " + e.getMessage());
        }
    }

    /**
     * Call AGConnectCloudDB.createObjectType to init schema
     */
    public void createObjectType() {
        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "createObjectType: " + e.getMessage());
        }
    }

    /**
     * Query all users in storage from cloud side with CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
     */
    public void queryAllUsers() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<CloudDBZoneSnapshot<users>> queryTask = mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(users.class),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        ).addOnSuccessListener(this::processQueryResult).addOnFailureListener(e -> {
            Log.e(TAG, "Query user list from cloud failed");
        });
    }

    private void processQueryResult(CloudDBZoneSnapshot<users> snapshot) {
        CloudDBZoneObjectList<users> bookInfoCursor = snapshot.getSnapshotObjects();
        List<users> userInfoList = new ArrayList<>();
        try {
            while (bookInfoCursor.hasNext()) {
                users userInfo = bookInfoCursor.next();
                userInfoList.add(userInfo);
            }
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "processQueryResult: " + e.getMessage());
        } finally {
            snapshot.release();
        }
        mUiCallback.onAddOrQuery(userInfoList);
    }

    /**
     * Upsert user
     *
     * @param user users added or modified from local
     */
    public void upsertUserInfos(users user) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(user);
        upsertTask
                .addOnSuccessListener(cloudDBZoneResult -> {
                    Log.w(TAG, "upsert " + cloudDBZoneResult + " records");
                    mUiCallback.onUpsert();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Insert book info failed" + e.getMessage());
                });
    }

    /**
     * Delete user
     *
     * @param userInfoList users selected by user
     */
    public void deleteUserInfo(List<users> userInfoList) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        Task<Integer> deleteTask = mCloudDBZone.executeDelete(userInfoList);
        if (deleteTask.getException() != null) {
            Log.e(TAG, "Delete user info failed");
            return;
        }
        mUiCallback.onDelete();
    }

    public void queryUsersToDelete(List<String> userInfoList) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        List<users> usersToDeleteList = new ArrayList<>();

        for (String id : userInfoList) {
            CloudDBZoneQuery<users> query = CloudDBZoneQuery.where(users.class);
            query.equalTo("uid", id);

            Task<CloudDBZoneSnapshot<users>> queryTask = mCloudDBZone.executeQuery(query,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
            queryTask.addOnSuccessListener(snapshot -> {
                processQueryResult(snapshot);
            }).addOnFailureListener(e -> {
                Log.w(TAG, "processQueryResult: " + e.getMessage());
            });
        }
    }

    public interface UiCallback {
        UiCallback DEFAULT = new UiCallback() {
            @Override
            public void onAddOrQuery(List<users> userInfoList) {
                Log.w(TAG, "Using default onAddOrQuery");
            }

            @Override
            public void onDelete() {
                Log.w(TAG, "Using default onDelete");
            }

            @Override
            public void onUpsert() {
                Log.w(TAG, "Using default onUpsert");
            }
        };

        void onAddOrQuery(List<users> userInfoList);

        void onUpsert();

        void onDelete();
    }
}
