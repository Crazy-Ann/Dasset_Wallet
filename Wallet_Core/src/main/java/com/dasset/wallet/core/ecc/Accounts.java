package com.dasset.wallet.core.ecc;

import com.google.common.collect.Lists;

import java.util.List;

public final class Accounts {

    private List<Account> accounts = Lists.newArrayList();

    public void add(Account account) {
        accounts.add(account);
    }

    public void addAll(List<Account> accounts) {
        this.accounts.addAll(accounts);
    }

    public Account get(int position) {
        return accounts.get(position);
    }

    public int size() {
        return accounts.size();
    }
}
