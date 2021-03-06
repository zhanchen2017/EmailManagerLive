package com.example.emailmanagerlive.account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;

import com.example.emailmanagerlive.Event;
import com.example.emailmanagerlive.MainActivity;
import com.example.emailmanagerlive.R;
import com.example.emailmanagerlive.data.source.AccountRepository;
import com.example.emailmanagerlive.databinding.ActivityVerifyBinding;
import com.google.android.material.snackbar.Snackbar;

public class VerifyActivity extends AppCompatActivity implements VerifyNavigator {

    private VerifyModel mViewModel;
    private ActivityVerifyBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_verify);
        mViewModel = new VerifyModel(AccountRepository.provideRepository(), getIntent().getLongExtra("category", 1));
        mBinding.setViewModel(mViewModel);
        setupSnackBar();
        subscribeToNavigationChanges();
    }

    @Override
    public void onAccountVerify() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void subscribeToNavigationChanges() {
        mViewModel.getVerified().observe(this, new Observer<Event<Object>>() {
            @Override
            public void onChanged(Event<Object> objectEvent) {
                if (objectEvent.getContentIfNotHandled() != null) {
                    VerifyActivity.this.onAccountVerify();
                }
            }
        });
    }

    private void setupSnackBar() {
        mViewModel.getSnackBarText().observe(this, new Observer<Event<String>>() {
            @Override
            public void onChanged(Event<String> stringEvent) {
                String msg = stringEvent.getContentIfNotHandled();
                if (!TextUtils.isEmpty(msg)) {
                    Snackbar.make(mBinding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void start2VerifyActivity(Context context, long categoryId) {
        context.startActivity(new Intent(context, VerifyActivity.class)
                .putExtra("category", categoryId));
    }
}
