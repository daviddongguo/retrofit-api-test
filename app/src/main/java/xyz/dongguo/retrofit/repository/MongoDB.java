package xyz.dongguo.retrofit.repository;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.sync.SyncConfiguration;
import android.os.Bundle;

public class MongoDB implements MongoDBRepository{
    private final String appID = getResources().getString(R.string.);
    private App app = new App(new AppConfiguration.Builder(appID)
            .build());
    @Override
    public SyncConfiguration configureRealmProfile() {
        return null;
    }

    @Override
    public App getApp() {
        return null;
    }

    @Override
    public void setApp(App app) {

    }
}
