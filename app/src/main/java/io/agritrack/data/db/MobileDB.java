package io.agritrack.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import io.agritrack.data.converter.AssetTypeConverter;
import io.agritrack.data.converter.ConsumableTypeConverter;
import io.agritrack.data.converter.DateConverter;
import io.agritrack.data.converter.LongListConverter;
import io.agritrack.data.converter.StringListConverter;
import io.agritrack.data.converter.StringSetConverter;
import io.agritrack.data.converter.TxStatusEnumConverter;
import io.agritrack.data.dao.AppUserDAO;
import io.agritrack.data.dao.CageDetailsDAO;
import io.agritrack.data.dao.HarvestRequestDAO;
import io.agritrack.data.dao.SiteDAO;
import io.agritrack.data.dao.common.CustomerDAO;
import io.agritrack.data.dao.common.EmployeeDAO;
import io.agritrack.data.dao.common.FishSpeciesDAO;
import io.agritrack.data.dao.common.ReaderDAO;
import io.agritrack.data.dao.common.SupplierDAO;
import io.agritrack.data.dao.tx.AssetTransactionDAO;
import io.agritrack.data.dao.tx.ConsumableTransactionDAO;
import io.agritrack.data.dao.tx.CorrelationTransactionDAO;
import io.agritrack.data.dao.tx.FishingTransactionDAO;
import io.agritrack.data.dao.tx.ProcessingTransactionDAO;
import io.agritrack.data.dao.tx.RepairTransactionDAO;
import io.agritrack.data.dao.tx.TransportTransactionDAO;
import io.agritrack.data.dao.wh.AssetDAO;
import io.agritrack.data.dao.wh.CoInventoryDAO;
import io.agritrack.data.dao.wh.CoInventoryItemDAO;
import io.agritrack.data.dao.wh.RFIDInventoryDAO;
import io.agritrack.data.dao.wh.RFIDInventoryItemDAO;
import io.agritrack.data.model.AppUser;
import io.agritrack.data.model.CageDetails;
import io.agritrack.data.model.HarvestRequest;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.common.Customer;
import io.agritrack.data.model.common.Employee;
import io.agritrack.data.model.common.FishSpecies;
import io.agritrack.data.model.common.Reader;
import io.agritrack.data.model.common.Supplier;
import io.agritrack.data.model.tx.AssetTransaction;
import io.agritrack.data.model.tx.ConsumableTransaction;
import io.agritrack.data.model.tx.CorrelationTransaction;
import io.agritrack.data.model.tx.FishingTransaction;
import io.agritrack.data.model.tx.ProcessingTransaction;
import io.agritrack.data.model.tx.RepairTransaction;
import io.agritrack.data.model.tx.TransportTransaction;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.data.model.wh.CoInventory;
import io.agritrack.data.model.wh.CoInventoryItem;
import io.agritrack.data.model.wh.RFIDInventory;
import io.agritrack.data.model.wh.RFIDInventoryItem;

@Database(entities = {AppUser.class, Site.class, Asset.class, Supplier.class, HarvestRequest.class,
        CageDetails.class, Employee.class, FishSpecies.class, Reader.class,
        FishingTransaction.class, TransportTransaction.class, ProcessingTransaction.class,
        AssetTransaction.class, ConsumableTransaction.class, CorrelationTransaction.class, RepairTransaction.class,
        RFIDInventory.class, RFIDInventoryItem.class, CoInventory.class, CoInventoryItem.class, Customer.class},
        version = 14, exportSchema = false)
@TypeConverters({TxStatusEnumConverter.class, DateConverter.class, LongListConverter.class, StringSetConverter.class, StringListConverter.class, AssetTypeConverter.class, ConsumableTypeConverter.class})
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

    public abstract CustomerDAO customerDAO();

    public abstract CageDetailsDAO cageDetailsDAO();

    public abstract HarvestRequestDAO harvestRequestsDAO();

    public abstract FishingTransactionDAO fishingTransactionDAO();

    public abstract TransportTransactionDAO transportTransactionDAO();

    public abstract ProcessingTransactionDAO processingTransactionDAO();

    public abstract AssetTransactionDAO assetTransactionDAO();

    public abstract ConsumableTransactionDAO consumableTransactionDAO();

    public abstract CorrelationTransactionDAO correlationTransactionDAO();

    public abstract RepairTransactionDAO repairTransactionDAO();

    public abstract EmployeeDAO employeeDAO();

    public abstract FishSpeciesDAO speciesDAO();

    public abstract ReaderDAO readerDAO();

    public abstract RFIDInventoryDAO rFIDInventoryDAO();

    public abstract RFIDInventoryItemDAO rFIDInventoryItemDAO();

    public abstract CoInventoryDAO coInventoryDAO();

    public abstract CoInventoryItemDAO coInventoryItemDAO();
}

