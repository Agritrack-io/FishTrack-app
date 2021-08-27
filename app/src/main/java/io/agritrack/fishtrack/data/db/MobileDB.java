package io.agritrack.fishtrack.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import io.agritrack.fishtrack.data.converter.DateConverter;
import io.agritrack.fishtrack.data.converter.LongListConverter;
import io.agritrack.fishtrack.data.converter.StringListConverter;
import io.agritrack.fishtrack.data.dao.AppUserDAO;
import io.agritrack.fishtrack.data.dao.CageDetailsDAO;
import io.agritrack.fishtrack.data.dao.SiteDAO;
import io.agritrack.fishtrack.data.dao.common.EmployeeDAO;
import io.agritrack.fishtrack.data.dao.common.FishSpeciesDAO;
import io.agritrack.fishtrack.data.dao.common.ReaderDAO;
import io.agritrack.fishtrack.data.dao.common.SupplierDAO;
import io.agritrack.fishtrack.data.dao.tx.FishingTransactionDAO;
import io.agritrack.fishtrack.data.dao.tx.HarvestTransactionDAO;
import io.agritrack.fishtrack.data.dao.tx.ProcessingTransactionDAO;
import io.agritrack.fishtrack.data.dao.tx.RepairTransactionDAO;
import io.agritrack.fishtrack.data.dao.tx.TransportTransactionDAO;
import io.agritrack.fishtrack.data.dao.tx.WHIncomingDAO;
import io.agritrack.fishtrack.data.dao.wh.AssetDAO;
import io.agritrack.fishtrack.data.model.AppUser;
import io.agritrack.fishtrack.data.model.CageDetails;
import io.agritrack.fishtrack.data.model.Site;
import io.agritrack.fishtrack.data.model.common.Employee;
import io.agritrack.fishtrack.data.model.common.FishSpecies;
import io.agritrack.fishtrack.data.model.common.Reader;
import io.agritrack.fishtrack.data.model.common.Supplier;
import io.agritrack.fishtrack.data.model.tx.FishingTransaction;
import io.agritrack.fishtrack.data.model.tx.HarvestTransaction;
import io.agritrack.fishtrack.data.model.tx.IncomingWHTransaction;
import io.agritrack.fishtrack.data.model.tx.ProcessingTransaction;
import io.agritrack.fishtrack.data.model.tx.RepairTransaction;
import io.agritrack.fishtrack.data.model.tx.TransportTransaction;
import io.agritrack.fishtrack.data.model.wh.Asset;

@Database(entities = {AppUser.class, Site.class, Asset.class, Supplier.class,
        CageDetails.class, Employee.class, FishSpecies.class, Reader.class,
        FishingTransaction.class, TransportTransaction.class, ProcessingTransaction.class,
        IncomingWHTransaction.class, RepairTransaction.class, HarvestTransaction.class},
        version = 15, exportSchema = false)
@TypeConverters({DateConverter.class, LongListConverter.class, StringListConverter.class})
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

    public abstract AssetDAO assetDAO();

    public abstract SupplierDAO supplierDAO();

    public abstract CageDetailsDAO cageDetailsDAO();

    public abstract FishingTransactionDAO fishingTransactionDAO();

    public abstract TransportTransactionDAO transportTransactionDAO();

    public abstract ProcessingTransactionDAO processingTransactionDAO();

    public abstract HarvestTransactionDAO harvestTransactionDAO();

    public abstract WHIncomingDAO whIncomingTransactionDAO();

    public abstract RepairTransactionDAO repairTransactionDAO();

    public abstract EmployeeDAO employeeDAO();

    public abstract FishSpeciesDAO speciesDAO();

    public abstract ReaderDAO readerDAO();
}

