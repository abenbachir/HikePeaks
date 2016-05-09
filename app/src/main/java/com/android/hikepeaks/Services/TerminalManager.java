package com.android.hikepeaks.Services;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.android.hikepeaks.Models.Account;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class TerminalManager {

    protected static TerminalManager INSTANCE = null;
    protected final String dirname = "HikePeaks";
    protected File appDir;
    protected File mediaDir;
    protected Context context = null;

    protected TerminalManager() {
        appDir = new File(Environment.getExternalStorageDirectory() + "/" + dirname);
        if (!appDir.exists())
            appDir.mkdirs();

        mediaDir = new File(appDir + "/" + "Media");
        if (!mediaDir.exists())
            mediaDir.mkdirs();
    }

    public static TerminalManager getInstance() {
        if (INSTANCE == null) {
            synchronized (TerminalManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TerminalManager();
                }
            }
        }
        return INSTANCE;
    }

    public Context getContext() {
        return context;
    }

    public TerminalManager setContext(Context context) {
        this.context = context;
        return this;
    }

    public File getAppDir() {
        return appDir;
    }

    public File getMediaDir() {
        return mediaDir;
    }

    public boolean isLocalPath(String path) {
        return path.startsWith(appDir.getParent());
    }

    public boolean existMedia(String url) {
        String filename = url.substring(url.lastIndexOf('/') + 1);
        File mediaFile = new File(mediaDir + "/" + filename);
        return mediaFile.exists();
    }

    public boolean existAccount(String email) {
        File userFile = new File(appDir + "/" + email + ".xml");
        return userFile.exists();
    }

    public File getAccountFile(String email) {
        return new File(appDir + "/" + email + ".xml");
    }

    public File getMediaFile(String filename) {
        return new File(mediaDir + "/" + filename);
    }

    public File getFile(String path) {
        return new File(appDir + "/" + path);
    }

    public String saveMedia(Bitmap image, String url) {
        String filename = url.substring(url.lastIndexOf('/') + 1);
        File mediaFile = new File(mediaDir + "/" + filename);

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(mediaFile);
            // Use the compress method on the BitMap object to write image to the OutputStream
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            fos.close();
        }
        return mediaFile.getPath();
    }

    public Bitmap loadMedia(String url) {
        return loadMedia(url, false);
    }

    public Bitmap loadMedia(String url, boolean removeCache) {
        if (url == null)
            return null;

        if (url.contains("?"))
            url = url.substring(0, url.lastIndexOf('?'));

        String filename = url.substring(url.lastIndexOf('/') + 1);
        File mediaFile = new File(mediaDir + "/" + filename);
        if (!mediaFile.exists()) {
            try {
                InputStream in = new java.net.URL(url).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                this.saveMedia(bitmap, url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(mediaFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(removeCache)
            mediaFile.delete();

        return bitmap;
    }

    public boolean existActiveAccount() {
        File userFile = new File(appDir + "/" + "activeAccount.xml");
        return userFile.exists();
    }

    public String updateActiveAccount(Account account) {
        // save active account in two files
        File activeAccountFile = new File(appDir + "/" + "activeAccount.xml");
        try {
            Serializer serializer = new Persister();
            serializer.write(account, activeAccountFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activeAccountFile.getPath();
    }

    public Account getActiveAccount() {
        if (!this.existActiveAccount())
            return null;
        File accountFile = new File(appDir + "/" + "activeAccount.xml");
        Account account = null;
        try {
            Serializer serializer = new Persister();
            account = serializer.read(Account.class, accountFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return account;
    }

}
