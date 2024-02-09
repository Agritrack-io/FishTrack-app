package io.agritrack.settings;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;

import com.google.android.material.tabs.TabLayout;

import io.agritrack.kefalonia.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.common.DeviceUtils;
import io.agritrack.kefalonia.databinding.ActivitySettingsBinding;
import io.agritrack.fragment.ApplicationSettingsFragment;
import io.agritrack.fragment.LicenseSettingsFragment;
import io.agritrack.ui.adapter.ViewPagerAdapter;
import io.agritrack.ui.login.LoginActivity;
import io.agritrack.ui.viewmodel.ConfigViewModel;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_READ_PHONE_STATE = 777;
    private Spinner spConfigSource;
    private ImageView ivSave, ivBack, ivRefreshSettings;
    private EditText etBackendURL;
    private TabLayout tabLayout;
    private ViewPager2 vpFragmentContainer;
    private ConfigViewModel appSettingsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // #1. set content view
        ActivitySettingsBinding appSettingBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        // #1.1. if line goes before #1, all Controls will be assigned to null!!!
        assignCtrlVars();

        // show the current backend URL.
        etBackendURL.setText(APIServiceGenerator.getAgrisenseUrl());

        // #2. create viewModel and bind it to the layout
        appSettingsViewModel = new ConfigViewModel(this);
        try {
            appSettingBinding.setAppSettings(appSettingsViewModel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // #3. set persistence type selector
        ArrayAdapter persistenceTypeAdapter = new ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, appSettingsViewModel.getPersistenceTypeList());
        spConfigSource.setAdapter(persistenceTypeAdapter);

        // #4. configure custom ViewPagerAdapter
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPagerAdapter.addFragment(new LicenseSettingsFragment(appSettingsViewModel));
        viewPagerAdapter.addFragment(new ApplicationSettingsFragment(appSettingsViewModel));

        // #5. configure ViewPager2 & TabLayout
        vpFragmentContainer.setAdapter(viewPagerAdapter);
        vpFragmentContainer.registerOnPageChangeCallback(new OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                vpFragmentContainer.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // #6. Configure refresh button
        ivRefreshSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appSettingsViewModel.loadConfig();
            }
        });

        // #6. Config navigation buttons
        configFooter();

        // #7. Request permission for PHONE_ID
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            DeviceUtils.getSerialNumber(getApplicationContext());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // hide android nav bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void configFooter() {

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });

        ivSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Try to save changes and return to main menu if successful
                if (appSettingsViewModel.saveConfig()) {
                    // go back to main menu
                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(i);
                }
            }
        });
    }

    private void assignCtrlVars() {
        this.etBackendURL = findViewById(R.id.etBackendURL);
        this.spConfigSource = findViewById(R.id.spConfigSource);
        this.tabLayout = findViewById(R.id.tabLayout);
        this.vpFragmentContainer = findViewById(R.id.viewPager2);
        this.ivSave = findViewById(R.id.ivSave);
        this.ivBack = findViewById(R.id.ivBack);
        this.ivRefreshSettings = findViewById(R.id.ivRefreshSettings);
    }
}