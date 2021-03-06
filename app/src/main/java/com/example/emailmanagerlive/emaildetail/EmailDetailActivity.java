package com.example.emailmanagerlive.emaildetail;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.emailmanagerlive.EmailApplication;
import com.example.emailmanagerlive.Event;
import com.example.emailmanagerlive.R;
import com.example.emailmanagerlive.data.Email;
import com.example.emailmanagerlive.data.EmailParams;
import com.example.emailmanagerlive.data.source.EmailRepository;
import com.example.emailmanagerlive.databinding.ActivityEmailDetailBinding;
import com.example.emailmanagerlive.emaildetail.adapter.AttachmentListAdapter;
import com.example.emailmanagerlive.send.SendEmailActivity;
import com.example.multifile.ui.EMDecoration;
import com.google.android.material.snackbar.Snackbar;

public class EmailDetailActivity extends AppCompatActivity implements EmailDetailNavigator {

    private ActivityEmailDetailBinding binding;
    private EmailViewModel viewModel;
    private AttachmentListAdapter listAdapter;
    private EmailParams mEmailParams;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_email_detail);
        binding.rvAttachment.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAttachment.addItemDecoration(new EMDecoration(this, EMDecoration.VERTICAL_LIST,
                R.drawable.list_divider, 0));
        mEmailParams = getIntent().getParcelableExtra("params");
        setupToolbar();
        setupAdapter();
        setupViewModel();
        setupSnackBar();
        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mEmailParams.getType() == EmailParams.Type.DRAFTS)
            getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            Email email = viewModel.getEmail();
            if (email != null) {
                mEmailParams.setFunction(EmailParams.Function.EDIT);
                SendEmailActivity.start2SendEmailActivity(this, mEmailParams, email);
            } else {
                Snackbar.make(binding.getRoot(), "请等待加载完成后操作", Snackbar.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (checkPermission(this, permissions)) {
            listAdapter.realDownloadOrOpen();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("提示信息")
                    .setMessage("缺少必要权限，会造成app部分功能无法使用。如若需要，请单击【确定】按钮前往设置中心进行权限授权。")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startAppSettings();
                        }
                    }).show();
        }
    }

    @Override
    public void onEmailDeleted() {
        finish();
    }

    private void initData() {
        viewModel.getEmail(mEmailParams);
    }

    private void setupSnackBar() {
        viewModel.getSnackBarText().observe(this, new Observer<Event<String>>() {
            @Override
            public void onChanged(Event<String> stringEvent) {
                String msg = stringEvent.getContentIfNotHandled();
                if (!TextUtils.isEmpty(msg))
                    Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void setupViewModel() {
        viewModel = new EmailViewModel(EmailRepository.provideRepository(), EmailApplication.getAccount(), this);
        viewModel.setNavigator(this);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
    }

    private void setupAdapter() {
        listAdapter = new AttachmentListAdapter(this, mEmailParams, this);
        binding.rvAttachment.setAdapter(listAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean checkPermission(Context context, String[] permissions) {
        boolean flag = true;
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                flag = !flag;
                break;
            }
        }
        return flag;
    }

    /**
     * 跳到app权限设置界面
     */
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    public static void start2EmailDetailActivity(Context context, long id, int type) {
        context.startActivity(new Intent(context, EmailDetailActivity.class)
                .putExtra("id", id)
                .putExtra("type", type));
    }

    public static void start2EmailDetailActivity(Context context, EmailParams params) {
        context.startActivity(new Intent(context, EmailDetailActivity.class)
                .putExtra("params", params));
    }
}
