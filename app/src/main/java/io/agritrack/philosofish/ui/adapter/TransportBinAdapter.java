package io.agritrack.philosofish.ui.adapter;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.common.TesseractOCR;

public class TransportBinAdapter extends RecyclerView.Adapter<TransportBinAdapter.MyViewHolder> {

    public static final int REQUEST_IMAGE1_CAPTURE = 1;
    private static final String errorFileCreate = "Error file create!";
    private static final String errorConvert = "Error convert!";
    private final Context context;
    private final LayoutInflater mLayoutInflater;
    protected String mCurrentPhotoPath;
    int PERMISSION_ALL = 1;
    boolean flagPermissions = false;
    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
    };
    private ProgressDialog mProgressDialog;
    private List<TransportBinItem> mList;
    private int selectedPos = RecyclerView.NO_POSITION;
    private int previousSelectedPos = -1;
    private String selectedValue = null;
    private String selectedLabel = null;
    private TesseractOCR mTessOCR;
    private Uri photoURI1;
    private Uri oldPhotoURI;

    private AdapterCallback callback;

    public TransportBinAdapter(Context context, ArrayList<TransportBinItem> values, AdapterCallback callback) {
        super();
        this.context = context;
        this.mList = values;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.callback = callback;

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        if (!flagPermissions) {
            checkPermissions();
        }
        String language = "eng";
        mTessOCR = new TesseractOCR(context, language);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<TransportBinItem> getValues() {
        return mList;
    }

    public void setValues(List<TransportBinItem> values) {
        this.mList = values;
    }

    public void addUniqueItem(TransportBinItem val) {
        if (this.mList.stream().noneMatch(x -> x.epc.equals(val.epc))) {
            this.mList.add(val);
        }
    }

    public String getSelectedValue() {
        return this.selectedValue;
    }

    public String getSelectedLabel() {
        return this.selectedLabel;
    }

    public void clearSelectedValue() {
        selectedPos = RecyclerView.NO_POSITION;
        this.selectedValue = null;
    }

    public void addItem(TransportBinItem val) {
        this.mList.add(val);
    }

    public void removeItem(String epc) {
        Optional<TransportBinItem> binFound = this.mList.stream().filter(x -> x.epc.equals(epc)).findFirst();
        if (binFound.isPresent()) {
            this.mList.remove(binFound.get());
        }
    }

    @NonNull
    @Override
    public TransportBinAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.transport_bin_adapter, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (mList.size() <= holder.getAdapterPosition()) {
            return;
        }
        TransportBinItem currBin = mList.get(holder.getAdapterPosition());
        String tag = currBin.epc.length() > 10 ? currBin.epc.substring(currBin.epc.length() - 10) : currBin.epc;
        holder.tvRfid.setText(tag);

        holder.etClip1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (holder.etClip1.getText() != null && holder.getAdapterPosition() == position) {
                        currBin.clip1 = !Strings.isEmptyOrWhitespace(holder.etClip1.getText().toString()) ? holder.etClip1.getText().toString() : null;
                    }
                    //Clear focus here from edittext
                    holder.etClip1.clearFocus();
                }
                return false;
            }
        });

        if (currBin.clip1 != null) {
            holder.etClip1.setText(String.valueOf(currBin.clip1));
        } else {
            holder.etClip1.setText("");
        }

        holder.etClip2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (holder.etClip2.getText() != null && holder.getAdapterPosition() == position) {
                        currBin.clip2 = !Strings.isEmptyOrWhitespace(holder.etClip2.getText().toString()) ? holder.etClip2.getText().toString() : null;
                    }
                    //Clear focus here from edittext
                    holder.etClip2.clearFocus();
                }
                return false;
            }
        });

        if (currBin.clip2 != null) {
            holder.etClip2.setText(String.valueOf(currBin.clip2));
        } else {
            holder.etClip2.setText("");
        }

        holder.itemView.setBackgroundColor(selectedPos == holder.getAdapterPosition() ? Color.GRAY : R.color.agri_blue);
        holder.tvItemSNo.setText(position + 1 + ".");
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    void checkPermissions() {
        if (!hasPermissions(context, PERMISSIONS)) {
            ((Activity) context).requestPermissions(PERMISSIONS,
                    PERMISSION_ALL);
            flagPermissions = false;
        }
        flagPermissions = true;
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("MMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Handle the result inside the adapter
    public void handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE1_CAPTURE: {
                if (resultCode == RESULT_OK) {
                    Bitmap bmp = null;
                    try {
                        InputStream is = context.getContentResolver().openInputStream(photoURI1);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        bmp = BitmapFactory.decodeStream(is, null, options);

                    } catch (Exception ex) {
                        Log.i(getClass().getSimpleName(), ex.getMessage());
                        Toast.makeText(context, errorConvert, Toast.LENGTH_SHORT).show();
                    }

//                    oc.setImageBitmap(bmp);
                    doOCR(bmp);

                    OutputStream os;
                    try {
                        os = new FileOutputStream(photoURI1.getPath());
                        if (bmp != null) {
                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, os);
                        }
                        os.flush();
                        os.close();
                    } catch (Exception ex) {
                        Log.e(getClass().getSimpleName(), ex.getMessage());
                        Toast.makeText(context, errorFileCreate, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    {
                        photoURI1 = oldPhotoURI;
//                        firstImage.setImageURI(photoURI1);
                    }
                }
            }
        }
    }

    private void doOCR(final Bitmap bitmap) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(context, "Processing",
                    "Doing OCR...", true);
        } else {
            mProgressDialog.show();
        }
        new Thread(new Runnable() {
            public void run() {
                final String srcText = mTessOCR.getOCRResult(bitmap);
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (srcText != null && !srcText.equals("")) {
//                            .setText(srcText);
                        }
                        mProgressDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    public interface AdapterCallback {
        void onResultHandled(String result);
    }

    public static class TransportBinItem {
        public String epc;
        public String clip1;
        public String clip2;

        public TransportBinItem() {
        }

        public TransportBinItem(String rfid) {
            this.epc = rfid;
        }

        public TransportBinItem(CharSequence x) {
            this.epc = x.toString();
        }

        public TransportBinItem(String epc, String clip1, String clip2) {
            this.epc = epc;
            this.clip1 = clip1;
            this.clip2 = clip2;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView tvRfid, tvItemSNo;
        private final EditText etClip1, etClip2;
        private final ImageButton ibOcr;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRfid = itemView.findViewById(R.id.tvRfid);
            tvItemSNo = itemView.findViewById(R.id.tvRecyclerItemSNo);
            ibOcr = itemView.findViewById(R.id.ibOcr);
            etClip1 = itemView.findViewById(R.id.etClip1);
            etClip2 = itemView.findViewById(R.id.etClip2);
            etClip1.setSelectAllOnFocus(true);
            etClip2.setSelectAllOnFocus(true);

            ibOcr.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Below line is just like a safety check, because sometimes holder could be null,
            // in that case, getAdapterPosition() will return RecyclerView.NO_POSITION
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

//            if (selectedPos == getAdapterPosition()) {
//                selectedPos = RecyclerView.NO_POSITION;
//                selectedValue = null;
//                selectedLabel = null;
//                notifyDataSetChanged();
//                return;
//            }

            // Updating old as well as new positions
//            notifyItemChanged(selectedPos);
//            selectedPos = getAdapterPosition();
//            selectedValue = mList.get(selectedPos).epc;
//            selectedLabel = selectedValue.length()>10? selectedValue.substring(selectedValue.length()-10) : selectedValue;
//            notifyItemChanged(selectedPos);

            if (!flagPermissions) {
                checkPermissions();
                return;
            }
            //prepare intent
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Toast.makeText(context, errorFileCreate, Toast.LENGTH_SHORT).show();
                    Log.i("File error", ex.toString());
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    oldPhotoURI = photoURI1;
                    photoURI1 = Uri.fromFile(photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI1);
                    if (context instanceof Activity) {
                        ((Activity) context).startActivityForResult(takePictureIntent, REQUEST_IMAGE1_CAPTURE);
                    }
                }
            }

            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (etClip1.getText() == null) {
                etClip1.requestFocus();
                imm.showSoftInput(etClip1, InputMethodManager.SHOW_IMPLICIT);
            } else if (etClip2.getText() == null) {
                etClip2.requestFocus();
                imm.showSoftInput(etClip2, InputMethodManager.SHOW_IMPLICIT);
            }

//            BinLoadDialog binDialog = new BinLoadDialog(context, selectedLabel);
//            binDialog.showDialog();


            // Do your another stuff for your onClick
        }
    }
}
