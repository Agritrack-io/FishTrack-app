package io.agritrack.fishtrack.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import io.agritrack.fishtrack.data.dao.AppUserDAO;
import io.agritrack.fishtrack.data.dao.SiteDAO;
import io.agritrack.fishtrack.data.model.AppUser;
import io.agritrack.fishtrack.data.model.Site;


@Database(entities = {Site.class, AppUser.class}, version = 31)
@TypeConverters({DateConverter.class, LongListConverter.class})
public abstract class MobileDB extends RoomDatabase {
    private static final Object sLock = new Object();
    private static MobileDB INSTANCE;

    public static MobileDB getInstance(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        MobileDB.class, "AGRIFISH_local.db")
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build();
            }
            return INSTANCE;
        }
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    public abstract SiteDAO siteDAO();

    public abstract AppUserDAO userDAO();

//    public abstract PlantDAO plantDAO();
//
//    public abstract DistributorDAO distributorDAO();
//
//    public abstract DriverDAO driverDAO();
//
//    public abstract TruckDAO truckDAO();
//
//    public abstract RouteDAO routeDAO();
//
//    public abstract TankDAO tankDAO();
//
//    public abstract ProducerDAO producerDAO();
//
//    public abstract TransactionDAO transactionDAO();
}

