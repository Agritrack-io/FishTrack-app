package io.agritrack.hotel.ui.sync;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.common.FileUtils;
import io.agritrack.data.db.MobileDB;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.hotel.ui.HotelHomeActivity;
import io.agritrack.ui.login.api.UploadingApi;
import io.agritrack.ui.service.LocalPreferences;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HotelSynchronizeActivity extends AppCompatActivity { //implements AdapterView.OnItemClickListener{

    private final UploadingApi upldSvc = APIServiceGenerator.createAPI(UploadingApi.class);
    private MobileDB db;
    private ListView lvFilesToSync;
    private File[] files;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    private long filesLength = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_synchronize);

        // get main controls references
        this.lvFilesToSync = findViewById(R.id.lvFilesToSync);

        /*// define if single or multiple choice mode will be used to display the checkboxes.
        this.lvFilesToSync.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);*/

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderSyncFiles);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        ArrayAdapter<String> fileNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getFileNamesList()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextSize(16);
                return view;
            }
        };

        this.lvFilesToSync.setAdapter(fileNameAdapter);

        ivSupport = findViewById(R.id.ivSupport);
        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(HotelSynchronizeActivity.this);
            supportDialog.showDialog();
        });

        // create Footer
        configFooter();
    }

    // Define 'back' / 'next' Buttons functionality
    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(v), Toast.LENGTH_LONG);
            } else {
                boolean proceed = syncAllFiles();
                if (proceed) {
                    CToast(getApplicationContext(), render("Files uploaded successfully!!!"), Toast.LENGTH_LONG);
                    Intent i = new Intent(getApplicationContext(), HotelHomeActivity.class);
                    startActivity(i);
                } else {
                    CToast(getApplicationContext(), render("Some files couldn't upload!!! Please retry!"), Toast.LENGTH_LONG);
                }
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), HotelHomeActivity.class);
            startActivity(i);
        });
    }

    private List<String> getFileNamesList() {
        File documentsFolder = new File(HotelSynchronizeActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
        files = documentsFolder.listFiles();
        filesLength = files.length;
        List<String> fileNames = Arrays.stream(files).map(x -> x.getName()).collect(Collectors.toList());

        return fileNames;
    }

    private boolean syncAllFiles() {
        try {
            String token = LocalPreferences.getToken();

            for (File file : files) {
                // create RequestBody instance from file
                RequestBody requestFile = RequestBody.create(file, MediaType.parse("application/json"));

                // MultipartBody.Part is used to send also the actual file name
                MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

                Call<ResponseBody> uploadJsonFileAsyncCall = upldSvc.uploadHotelInventory(filePart, "Bearer " + token);
                uploadJsonFileAsyncCall.enqueue(new HotelSynchronizeActivity.InventoryFileUploadCallBack());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        } finally {

        }
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (this.filesLength == 0) {
            sb.append("No files for sync available.");
        }

        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public class InventoryFileUploadCallBack implements Callback<ResponseBody> {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            boolean res = FileUtils.deleteInventoryFile(HotelSynchronizeActivity.this, "Inventory.ALL.20220215140236.json");
            if (res) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("File was uploaded successfully!!!"), Toast.LENGTH_LONG));
            } else {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Failed to remove file from local folder!!!"), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            //runOnUiThread(() -> CToast(getApplicationContext(), render("File upload failure!!! Please sync files from main menu."), Toast.LENGTH_LONG));
        }
    }
}