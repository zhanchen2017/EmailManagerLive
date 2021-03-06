package com.example.emailmanagerlive.data.source.local;

import android.util.Log;

import com.example.emailmanagerlive.data.Account;
import com.example.emailmanagerlive.data.AccountDao;
import com.example.emailmanagerlive.data.source.AccountDataSource;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

public class AccountLocalDataSource implements AccountDataSource {

    private static AccountLocalDataSource INSTANCE;

    private AccountDao dao;

    private AccountLocalDataSource(AccountDao dao) {
        this.dao = dao;
    }

    public static AccountLocalDataSource getInstance(AccountDao dao) {
        if (INSTANCE == null) {
            INSTANCE = new AccountLocalDataSource(dao);
        }
        return INSTANCE;
    }

    @Override
    public void add(Account account) {
        //清除当前账号状态
        QueryBuilder<Account> queryBuilder = dao.queryBuilder().where(AccountDao.Properties.IsCur.eq("true"));
        List<Account> list = queryBuilder.list();
        if (list != null && list.size() > 0) {
            for (Account accountDetail : list) {
                accountDetail.setCur(false);
            }
            dao.updateInTx(list);
        }
        dao.insert(account);
    }

    @Override
    public void update(Account account) {
        dao.update(account);
    }

    @Override
    public void delete(Account account) {
        dao.delete(account);
    }

    @Override
    public void deleteAll() {
        dao.deleteAll();
    }

    @Override
    public void getAccounts(AccountsCallBack callBack) {
        List<Account> accounts = dao.loadAll();
        if (accounts != null && accounts.size() > 0) {
            callBack.onAccountsLoaded(accounts);
        } else {
            callBack.onDataNotAvailable();
        }
    }

    @Override
    public void getAccount(AccountCallBack callBack) {
        QueryBuilder<Account> qb = dao.queryBuilder().where(AccountDao.Properties.IsCur.eq("true"));
        List<Account> data = qb.list();
        if (data != null && data.size() > 0) {
            Account account = data.get(0);
            callBack.onAccountLoaded(data.get(0));
            Log.i("mango", "account:" + account.getAccount() + "    isCurrent:" + account.isCur() + "  ReceiveProtocol:"
                    + account.getConfig().getReceiveProtocol() + "  ReceiveHost:" + account.getConfig().getReceiveHostValue());
        } else {
            callBack.onDataNotAvailable();
        }
    }

    public void setCurAccount(Account account) {
        List<Account> accounts = dao.loadAll();
        for (int i = 0; i < accounts.size(); i++) {
            Account _account = accounts.get(i);
            if (_account.equals(account)) {
                _account.setCur(true);
            } else {
                _account.setCur(false);
            }
        }
        dao.updateInTx(accounts);
    }
}
