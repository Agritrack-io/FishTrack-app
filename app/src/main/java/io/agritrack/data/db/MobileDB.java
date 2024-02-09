package io.agritrack.data.db;

import android.content.Context;
import net.sqlcipher.database.SQLiteDatabase;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import net.sqlcipher.database.SupportFactory;

import io.agritrack.data.converter.ConsumableTypeConverter;
import io.agritrack.data.converter.DateConverter;
import io.agritrack.data.converter.LongListConverter;
import io.agritrack.data.converter.StringListConverter;
import io.agritrack.data.converter.StringSetConverter;
import io.agritrack.data.converter.TxStatusEnumConverter;
import io.agritrack.data.converter.UUIDConverter;
import io.agritrack.data.dao.AppUserDAO;
import io.agritrack.data.dao.BinInfoDAO;
import io.agritrack.data.dao.CageDetailsDAO;
import io.agritrack.data.dao.EncodingSchemeDAO;
import io.agritrack.data.dao.FishingRequestDAO;
import io.agritrack.data.dao.SiteDAO;
import io.agritrack.data.dao.common.CustomerDAO;
import io.agritrack.data.dao.common.EmployeeDAO;
import io.agritrack.data.dao.common.SpeciesDAO;
import io.agritrack.data.dao.common.SupplierDAO;
import io.agritrack.data.dao.iotlogger.IotLoggerDAO;
import io.agritrack.data.dao.iotlogger.MeasurementsDAO;
import io.agritrack.data.dao.iotlogger.ReaderDAO;
import io.agritrack.data.dao.iotlogger.TemperatureDataDAO;
import io.agritrack.data.dao.tx.AssetTransactionDAO;
import io.agritrack.data.dao.tx.AssetTxItemDAO;
import io.agritrack.data.dao.tx.CorrelationTransactionDAO;
import io.agritrack.data.dao.tx.FishingTransactionDAO;
import io.agritrack.data.dao.tx.PostPackageQualityTransactionDAO;
import io.agritrack.data.dao.tx.ProcessingTransactionDAO;
import io.agritrack.data.dao.tx.QualityTransactionDAO;
import io.agritrack.data.dao.tx.TransportTransactionDAO;
import io.agritrack.data.dao.wh.AssetDAO;
import io.agritrack.data.dao.wh.FoodSkuDAO;
import io.agritrack.data.dao.wh.RFIDInventoryDAO;
import io.agritrack.data.dao.wh.RFIDInventoryItemDAO;
import io.agritrack.data.model.AppUser;
import io.agritrack.data.model.BinInfo;
import io.agritrack.data.model.CageDetails;
import io.agritrack.data.model.EncodingSchemeEntity;
import io.agritrack.data.model.FishingRequest;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.common.Customer;
import io.agritrack.data.model.common.Employee;
import io.agritrack.data.model.common.IotLogger;
import io.agritrack.data.model.common.Measurement;
import io.agritrack.data.model.common.Reader;
import io.agritrack.data.model.common.Species;
import io.agritrack.data.model.common.Supplier;
import io.agritrack.data.model.common.TemperatureData;
import io.agritrack.data.model.tx.AssetTransaction;
import io.agritrack.data.model.tx.AssetTxItem;
import io.agritrack.data.model.tx.CorrelationTransaction;
import io.agritrack.data.model.tx.FishingTransaction;
import io.agritrack.data.model.tx.PostPackageQualityTransaction;
import io.agritrack.data.model.tx.ProcessingTransaction;
import io.agritrack.data.model.tx.QualityTransaction;
import io.agritrack.data.model.tx.TransportTransaction;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.data.model.wh.FoodSku;
import io.agritrack.data.model.wh.RFIDInventory;
import io.agritrack.data.model.wh.RFIDInventoryItem;

@Database(entities = {AppUser.class, Site.class, Asset.class, FoodSku.class, Supplier.class,
        FishingRequest.class, EncodingSchemeEntity.class, CageDetails.class, BinInfo.class,
        Employee.class, Species.class, Reader.class, IotLogger.class, FishingTransaction.class,
        TransportTransaction.class, ProcessingTransaction.class, QualityTransaction.class,
        PostPackageQualityTransaction.class, AssetTransaction.class, AssetTxItem.class, CorrelationTransaction.class,
        RFIDInventory.class, RFIDInventoryItem.class, Customer.class, Measurement.class, TemperatureData.class},
        version = 7, exportSchema = false)

@TypeConverters({TxStatusEnumConverter.class, DateConverter.class, LongListConverter.class,
        StringSetConverter.class, StringListConverter.class, ConsumableTypeConverter.class, UUIDConverter.class})
public abstract class MobileDB extends RoomDatabase {
    private static final Object sLock = new Object();
    private static MobileDB INSTANCE;

    public static MobileDB getInstance(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                SQLiteDatabase.loadLibs(context);
                final byte[] passphrase = "{password}".getBytes();
                final SupportFactory factory = new SupportFactory(passphrase);
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        MobileDB.class, "AGRIFISH_local.db")
                        .openHelperFactory(factory)
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

    public abstract FoodSkuDAO foodSkuDAO();

    public abstract EncodingSchemeDAO encodingSchemeDAO();

    public abstract SupplierDAO supplierDAO();

    public abstract CustomerDAO customerDAO();

    public abstract CageDetailsDAO cageDetailsDAO();

    public abstract BinInfoDAO binInfoDAO();

    public abstract FishingRequestDAO fishingRequestsDAO();

    public abstract FishingTransactionDAO fishingTransactionDAO();

    public abstract TransportTransactionDAO transportTransactionDAO();

    public abstract ProcessingTransactionDAO processingTransactionDAO();

    public abstract QualityTransactionDAO qualityTransactionDAO();

    public abstract PostPackageQualityTransactionDAO postPackageQualityTransactionDAO();

    public abstract AssetTransactionDAO assetTransactionDAO();

    public abstract AssetTxItemDAO assetTxItemDAO();

    public abstract CorrelationTransactionDAO correlationTransactionDAO();

    public abstract EmployeeDAO employeeDAO();

    public abstract SpeciesDAO speciesDAO();

    public abstract IotLoggerDAO iotLoggerDAO();

    public abstract RFIDInventoryDAO rFIDInventoryDAO();

    public abstract RFIDInventoryItemDAO rFIDInventoryItemDAO();

    public abstract MeasurementsDAO measurementsDAO();

    public abstract TemperatureDataDAO temperatureDataDAO();
}

