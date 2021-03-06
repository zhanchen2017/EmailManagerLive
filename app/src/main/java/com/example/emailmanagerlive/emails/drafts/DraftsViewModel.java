package com.example.emailmanagerlive.emails.drafts;

import android.util.Log;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.emailmanagerlive.EmailApplication;
import com.example.emailmanagerlive.data.Account;
import com.example.emailmanagerlive.data.Email;
import com.example.emailmanagerlive.data.source.EmailDataSource;
import com.example.emailmanagerlive.data.source.EmailRepository;
import com.example.emailmanagerlive.emails.EmailsViewModel;
import com.example.emailmanagerlive.utils.ThreadPoolFactory;

import java.util.Collections;
import java.util.List;

public class DraftsViewModel implements EmailsViewModel, EmailDataSource.GetEmailsCallBack {
    public final MutableLiveData<List<Email>> items = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mDataLoading = new MutableLiveData<>();
    public final LiveData<Boolean> mEmpty = Transformations.map(items,
            new Function<List<Email>, Boolean>() {
                @Override
                public Boolean apply(List<Email> input) {
                    return input.isEmpty();

                }
            });

    private EmailRepository mRepository;
    private Account mAccount;

    public DraftsViewModel(EmailRepository mRepository) {
        this.mRepository = mRepository;
    }

    @Override
    public void refresh() {
        loadDrafts(EmailApplication.getAccount());
    }

    @Override
    public void onEmailsLoaded(List<Email> emails) {
        Log.i("mango","emails_count:"+emails.size());
        Collections.reverse(emails);
        items.postValue(emails);
        mDataLoading.postValue(false);
    }

    @Override
    public void onDataNotAvailable() {
        mDataLoading.postValue(false);
    }

    public void loadDrafts(Account account) {
        this.mAccount = account;
        mDataLoading.setValue(true);
        ThreadPoolFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                mRepository.loadDrafts(mAccount, DraftsViewModel.this);
            }
        });
    }

    public LiveData<Boolean> isDataLoading() {
        return mDataLoading;
    }
}
