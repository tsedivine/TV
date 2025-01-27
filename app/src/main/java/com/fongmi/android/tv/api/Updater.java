package com.fongmi.android.tv.api;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.BuildConfig;
import com.fongmi.android.tv.Github;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.DialogUpdateBinding;
import com.fongmi.android.tv.net.Download;
import com.fongmi.android.tv.net.OkHttp;
import com.fongmi.android.tv.utils.FileUtil;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.Prefers;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONObject;

import java.io.File;
import java.util.Locale;

public class Updater implements Download.Callback {

    private DialogUpdateBinding binding;
    private AlertDialog dialog;
    private String branch;
    private boolean force;

    private static class Loader {
        static volatile Updater INSTANCE = new Updater();
    }

    public static Updater get() {
        return Loader.INSTANCE;
    }

    private File getFile() {
       
    }

    private String getJson() {
        
    }

    private String getApk() {
       
    }

    private Updater() {
        
    }

    public Updater reset() {
       
        return this;
    }

    public Updater force() {
        Notify.show(R.string.update_check);
        this.force = true;
        return this;
    }

    public Updater branch(String branch) {
        this.branch = branch;
        return this;
    }

    public void start(Activity activity) {
        App.execute(() -> doInBackground(activity));
    }

    private boolean need(int code, String name) {
        return (branch.equals(Github.DEV) ? !name.equals(BuildConfig.VERSION_NAME) : code > BuildConfig.VERSION_CODE) && Prefers.getUpdate();
    }

    private void doInBackground(Activity activity) {
        try {
            JSONObject object = new JSONObject(OkHttp.newCall(getJson()).execute().body().string());
            String name = object.optString("name");
            String desc = object.optString("desc");
            int code = object.optInt("code");
            if (need(code, name) || force) App.post(() -> show(activity, name, desc));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void show(Activity activity, String version, String desc) {
        binding = DialogUpdateBinding.inflate(LayoutInflater.from(activity));
        binding.version.setText(ResUtil.getString(R.string.update_version, version));
        binding.confirm.setOnClickListener(this::confirm);
        binding.cancel.setOnClickListener(this::cancel);
        binding.desc.setText(desc);
        create(activity).show();
    }

    private AlertDialog create(Activity activity) {
        if (dialog != null) dialog.dismiss();
        return dialog = new MaterialAlertDialogBuilder(activity).setView(binding.getRoot()).setCancelable(false).create();
    }

    private void dismiss() {
        if (dialog != null) dialog.dismiss();
        this.branch = Github.RELEASE;
        this.force = false;
    }

    private void cancel(View view) {
        Prefers.putUpdate(false);
        dismiss();
    }

    private void confirm(View view) {
        binding.confirm.setEnabled(false);
        Download.create(getApk(), getFile(), this).start();
    }

    @Override
    public void progress(int progress) {
        binding.confirm.setText(String.format(Locale.getDefault(), "%1$d%%", progress));
    }

    @Override
    public void error(String message) {
        Notify.show(message);
        dismiss();
    }

    @Override
    public void success(File file) {
        FileUtil.openFile(getFile());
        dismiss();
    }
}
