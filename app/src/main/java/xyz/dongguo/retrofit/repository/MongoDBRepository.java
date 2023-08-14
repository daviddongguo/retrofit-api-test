package xyz.dongguo.retrofit.repository;

import io.realm.mongodb.sync.SyncConfiguration;
import io.realm.mongodb.App;

public interface MongoDBRepository {
    public SyncConfiguration configureRealmProfile();
    public App getApp();
    public void setApp(App app);
}
