package com.qtfreet.musicuu.ui.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.blankj.utilcode.util.StringUtils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.qtfreet.musicuu.R;
import com.qtfreet.musicuu.model.Constant.Constants;
import com.qtfreet.musicuu.utils.SPUtils;

import java.io.File;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by qtfreet on 2016/3/20.
 */
public class DownloadService extends Service {
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }
        final String path = Environment.getExternalStorageDirectory() + "/" + SPUtils.get(Constants.MUSICUU_PREF, this, Constants.SAVE_PATH, "musicuu");
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        final String url = intent.getStringExtra(Constants.URL);
        final String name = intent.getStringExtra(Constants.NAME);
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(name)) {
            return START_NOT_STICKY;
        }
        final String localName;
        if (url.contains(".mp3")) {
            localName = name + ".mp3";
        } else if (url.contains(".flac")) {
            localName = name + ".flac";
        } else if (url.contains(".ape")) {
            localName = name + ".ape";
        } else if (url.contains(".mp4")) {
            localName = name + ".mp4";
        } else {
            localName = name + ".mp3";
        }
        final File file = new File(path + "/" + localName);
        if (file.exists()) {
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this);
            if (Build.VERSION.SDK_INT > 19) {
                sweetAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
            } else {
                sweetAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
            sweetAlertDialog.setTitleText("提示").setContentText("文件已存在，是否需要重新下载？").setConfirmText("是").setCancelText("否");
            sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    file.delete();
                    download(url, localName, path);
                    sweetAlertDialog.dismissWithAnimation();
                }
            });
            sweetAlertDialog.show();

        } else {
            download(url, localName, path);
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void download(String url, String name, String path) {
        if (url.isEmpty()) {
            Toast.makeText(this, "未获取到下载链接", Toast.LENGTH_SHORT).show();
            return;

        }
        Log.e("qtfreet0000", "开始下载  url="+url);
        FileDownloader.getImpl().create(url)
                .setPath(path + "/" + name)
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                        //     Toast.makeText(DownloadService.this, "正在下载中", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        String name = task.getFilename();
                        updateProgress((int) (soFarBytes * 100.0 / totalBytes), task.getId(), name);

                    }

                    @Override
                    protected void blockComplete(BaseDownloadTask task) {
                    }

                    @Override
                    protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        updateProgress(100, task.getId(), task.getFilename());
                        mNotifyManager.cancel(task.getId());
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                    }
                }).start();

    }


    private void updateProgress(int progress, int id, String name) {
        mBuilder.setContentTitle(name).setSmallIcon(getApplicationInfo().icon);
        mBuilder.setContentText(this.getString(R.string.download_progress, progress)).setProgress(100, progress, false);
        PendingIntent pendingintent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pendingintent);
        mNotifyManager.notify(id, mBuilder.build());
    }

}