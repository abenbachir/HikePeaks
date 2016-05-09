package com.android.hikepeaks.Services;

import com.android.hikepeaks.Models.Account;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class AccountManager {

    protected static AccountManager INSTANCE = null;
    private Account account = null;
    private GoogleApiClient googleApiClient = null;

    protected AccountManager() {

    }

    public static AccountManager getInstance() {
        if (INSTANCE == null) {
            synchronized (AccountManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AccountManager();
                }
            }
        }
        return INSTANCE;
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public boolean isConnected() {
        return googleApiClient != null && account != null && googleApiClient.isConnected();
    }

    public void disconnect() {
        if (googleApiClient == null)
            return;
        Plus.AccountApi.clearDefaultAccount(googleApiClient);
        googleApiClient.disconnect();
        googleApiClient = null;
    }

    public void updateAccount() {
        Person info = getAccountInfoFromGoogleApi();
        if (info == null)
            return;
        this.account = new Account();
        account.setDisplayName(info.getDisplayName());
        String pictureUrl = info.getImage().getUrl();
        pictureUrl = pictureUrl.substring(0, pictureUrl.length() - 2) + 100;
        account.setPictureUrl(pictureUrl);
        account.setEmail(getPersonEmail());
    }

    public String getPersonEmail() {
        String email = "";
        if (this.googleApiClient != null)
            email = Plus.AccountApi.getAccountName(this.googleApiClient);
        return email;
    }

    protected Person getAccountInfoFromGoogleApi() {

        try {
            if (Plus.PeopleApi.getCurrentPerson(this.googleApiClient) != null) {
                return Plus.PeopleApi.getCurrentPerson(this.googleApiClient);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
