package io.agritrack.philosofish.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

import io.agritrack.philosofish.data.converter.ConsumableTypeConverter;
import io.agritrack.philosofish.data.converter.DateConverter;
import io.agritrack.philosofish.data.converter.LongListConverter;
import io.agritrack.philosofish.data.converter.SortingSampleConverter;
import io.agritrack.philosofish.data.converter.StringListConverter;
import io.agritrack.philosofish.data.converter.StringSetConverter;
import io.agritrack.philosofish.data.converter.TimeConverter;
import io.agritrack.philosofish.data.converter.TxStatusEnumConverter;
import io.agritrack.philosofish.data.converter.UUIDConverter;
import io.agritrack.philosofish.data.dao.AppUserDAO;
import io.agritrack.philosofish.data.dao.BinInfoDAO;
import io.agritrack.philosofish.data.dao.CageDetailsDAO;
import io.agritrack.philosofish.data.dao.EncodingSchemeDAO;
import io.agritrack.philosofish.data.dao.FishingRequestDAO;
import io.agritrack.philosofish.data.dao.SiteDAO;
import io.agritrack.philosofish.data.dao.common.CustomerDAO;
import io.agritrack.philosofish.data.dao.common.EmployeeDAO;
import io.agritrack.philosofish.data.dao.common.SpeciesDAO;
import io.agritrack.philosofish.data.dao.common.SupplierDAO;
import io.agritrack.philosofish.data.dao.iotlogger.IotLoggerDAO;
import io.agritrack.philosofish.data.dao.iotlogger.MeasurementsDAO;
import io.agritrack.philosofish.data.dao.iotlogger.TemperatureDataDAO;
import io.agritrack.philosofish.data.dao.tx.AssetTransactionDAO;
import io.agritrack.philosofish.data.dao.tx.AssetTxItemDAO;
import io.agritrack.philosofish.data.dao.tx.CorrelationTransactionDAO;
import io.agritrack.philosofish.data.dao.tx.FinalQualityTxDAO;
import io.agritrack.philosofish.data.dao.tx.FishingTransactionDAO;
import io.agritrack.philosofish.data.dao.tx.PackageQualityTxDAO;
import io.agritrack.philosofish.data.dao.tx.PostPackageQualityTransactionDAO;
import io.agritrack.philosofish.data.dao.tx.ProcessingTransactionDAO;
import io.agritrack.philosofish.data.dao.tx.QualityTransactionDAO;
import io.agritrack.philosofish.data.dao.tx.ReceiptQualityTxDAO;
import io.agritrack.philosofish.data.dao.tx.TransportTransactionDAO;
import io.agritrack.philosofish.data.dao.wh.AssetDAO;
import io.agritrack.philosofish.data.dao.wh.FoodSkuDAO;
import io.agritrack.philosofish.data.dao.wh.RFIDInventoryDAO;
import io.agritrack.philosofish.data.dao.wh.RFIDInventoryItemDAO;
import io.agritrack.philosofish.data.model.AppUser;
import io.agritrack.philosofish.data.model.BinInfo;
import io.agritrack.philosofish.data.model.CageDetails;
import io.agritrack.philosofish.data.model.EncodingSchemeEntity;
import io.agritrack.philosofish.data.model.FishingRequest;
import io.agritrack.philosofish.data.model.Site;
import io.agritrack.philosofish.data.model.common.Customer;
import io.agritrack.philosofish.data.model.common.Employee;
import io.agritrack.philosofish.data.model.common.IotLogger;
import io.agritrack.philosofish.data.model.common.Measurement;
import io.agritrack.philosofish.data.model.common.Reader;
import io.agritrack.philosofish.data.model.common.Species;
import io.agritrack.philosofish.data.model.common.Supplier;
import io.agritrack.philosofish.data.model.common.TemperatureData;
import io.agritrack.philosofish.data.model.tx.AssetTransaction;
import io.agritrack.philosofish.data.model.tx.AssetTxItem;
import io.agritrack.philosofish.data.model.tx.CorrelationTransaction;
import io.agritrack.philosofish.data.model.tx.FinalQualityTransaction;
import io.agritrack.philosofish.data.model.tx.FishingTransaction;
import io.agritrack.philosofish.data.model.tx.PackageQualityTransaction;
import io.agritrack.philosofish.data.model.tx.PostPackageQualityTransaction;
import io.agritrack.philosofish.data.model.tx.ProcessingTransaction;
import io.agritrack.philosofish.data.model.tx.QualityTransaction;
import io.agritrack.philosofish.data.model.tx.ReceiptQualityTransaction;
import io.agritrack.philosofish.data.model.tx.TransportTransaction;
import io.agritrack.philosofish.data.model.wh.Asset;
import io.agritrack.philosofish.data.model.wh.FoodSku;
import io.agritrack.philosofish.data.model.wh.RFIDInventory;
import io.agritrack.philosofish.data.model.wh.RFIDInventoryItem;

@Database(entities = {AppUser.class, Site.class, Asset.class, FoodSku.class, Supplier.class,
        FishingRequest.class, EncodingSchemeEntity.class, CageDetails.class, BinInfo.class,
        Employee.class, Species.class, Reader.class, IotLogger.class, FishingTransaction.class,
        TransportTransaction.class, ProcessingTransaction.class, QualityTransaction.class, ReceiptQualityTransaction.class,
        PackageQualityTransaction.class, PostPackageQualityTransaction.class, AssetTransaction.class, AssetTxItem.class, CorrelationTransaction.class,
        RFIDInventory.class, RFIDInventoryItem.class, Customer.class, Measurement.class, TemperatureData.class, FinalQualityTransaction.class},
        version = 13, exportSchema = false)

@TypeConverters({TxStatusEnumConverter.class, DateConverter.class, LongListConverter.class,
        StringSetConverter.class, StringListConverter.class, ConsumableTypeConverter.class, UUIDConverter.class, SortingSampleConverter.class, TimeConverter.class})
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

    public abstract ReceiptQualityTxDAO receiptQualityTransactionDAO();

    public abstract PackageQualityTxDAO packageQualityTransactionDAO();


    public abstract FinalQualityTxDAO finalQualityTransactionDAO();

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

