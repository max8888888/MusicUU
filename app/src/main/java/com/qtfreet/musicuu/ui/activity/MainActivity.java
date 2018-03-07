package com.qtfreet.musicuu.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.autoupdate.IFlytekUpdate;
import com.iflytek.autoupdate.IFlytekUpdateListener;
import com.iflytek.autoupdate.UpdateConstants;
import com.iflytek.autoupdate.UpdateErrorCode;
import com.iflytek.autoupdate.UpdateInfo;
import com.iflytek.autoupdate.UpdateType;
import com.iflytek.sunflower.FlowerCollector;
import com.qtfreet.musicuu.R;
import com.qtfreet.musicuu.model.Constant.Constants;
import com.qtfreet.musicuu.model.OnHistoryItemClickListener;
import com.qtfreet.musicuu.ui.BaseActivity;
import com.qtfreet.musicuu.ui.adapter.HistoryAdapter;
import com.qtfreet.musicuu.ui.adapter.MySimpleDivider;
import com.qtfreet.musicuu.utils.FileUtils;
import com.qtfreet.musicuu.utils.SPUtils;
import com.qtfreet.musicuu.utils.Sp;
import com.zhy.m.permission.MPermissions;
import com.zhy.m.permission.PermissionDenied;
import com.zhy.m.permission.PermissionGrant;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import me.curzbin.library.BottomDialog;
import me.curzbin.library.Item;
import me.curzbin.library.OnItemClickListener;

public class MainActivity extends BaseActivity implements View.OnClickListener, View.OnKeyListener,
        View.OnTouchListener, OnHistoryItemClickListener {
    @Bind(R.id.ib_search_btn)
    ImageButton mSearchButton;
    @Bind(R.id.et_search_content)
    EditText mSearchEditText;
    @Bind(R.id.lv_search_history_layout)
    LinearLayout mSearchHistoryLayout;
    @Bind(R.id.lv_search_history_list)
    RecyclerView mSearchHistoryList;
    @Bind(R.id.lv_search_history_clear)
    TextView mSearchHistoryClear;
    @Bind(R.id.iv_title_icon)
    ImageView mSearchTitleIcon;
    @Bind(R.id.iv_title_text)
    TextView mSearchTitleText;

    private static final String HISTORY = "history";
    private static final int REQUECT_CODE_SDCARD = 2;

    HistoryAdapter mHistoryAdapter;
    ArrayList<String> mHistoryList = new ArrayList<String>();

    @Override
    public void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        MPermissions.requestPermissions(MainActivity.this, REQUECT_CODE_SDCARD, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        firstUse();
        ButterKnife.bind(this);
        mistype = "wy";
        setTitleName("歌曲搜索", false);
        floatingActionButton.setOnClickListener(this);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchSong();
            }
        });
        mSearchEditText.setOnKeyListener(this);
        mSearchEditText.setOnTouchListener(this);
        mSearchHistoryClear.setOnClickListener(this);
        //checkUpdate();
        initHistory();
    }

    private void initHistory() {
        mHistoryAdapter = new HistoryAdapter(mContext, mHistoryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mSearchHistoryList.setLayoutManager(layoutManager);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        mSearchHistoryList.setAdapter(mHistoryAdapter);
        mSearchHistoryList.addItemDecoration(new MySimpleDivider(mContext));
        mSearchHistoryList.setNestedScrollingEnabled(false);
        mHistoryAdapter.setOnHistoryClickListener(this);
    }


    private void firstUse() {
        boolean isFirst = (boolean) SPUtils.get(this, Constants.IS_FIRST_RUN, true);
        if (isFirst) {
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this);
            sweetAlertDialog.setTitleText(getString(R.string.start_info));
            sweetAlertDialog.setContentText(getString(R.string.description)).setConfirmText(getString(R.string.i_know));
            sweetAlertDialog.setCancelable(false);
            sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    SPUtils.put(MainActivity.this, Constants.IS_FIRST_RUN, false);
                    sweetAlertDialog.dismissWithAnimation();
                }
            });
            sweetAlertDialog.show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        initDir();
        FlowerCollector.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FlowerCollector.onPause(this);
        mSearchHistoryLayout.setVisibility(View.GONE);
    }

    private void checkUpdate() {
        if (!(boolean) SPUtils.get(Constants.MUSICUU_PREF, this, Constants.AUTO_CHECK, true)) {
            return;
        }
        final IFlytekUpdate update = IFlytekUpdate.getInstance(this);
        update.setParameter(UpdateConstants.EXTRA_NOTI_ICON, "true");
        update.setParameter(UpdateConstants.EXTRA_STYLE, UpdateConstants.UPDATE_UI_DIALOG);
        update.autoUpdate(this, new IFlytekUpdateListener() {
            @Override
            public void onResult(int errorcode, UpdateInfo result) {

                if (errorcode == UpdateErrorCode.OK && result != null) {
                    if (result.getUpdateType() == UpdateType.NoNeed) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "已经是最新版本！", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                    update.showUpdateInfo(MainActivity.this, result);
                }
            }
        });
    }


    private void initDir() {
        String path = (String) SPUtils.get(Constants.MUSICUU_PREF, this, Constants.SAVE_PATH, "musicuu");
        if (FileUtils.getInstance().isSdCardAvailable()) {
            if (!FileUtils.getInstance().isFileExist(path)) {
                FileUtils.getInstance().creatSDDir(path);
            }
        }
    }


    String mistype = "";
    @Bind(R.id.fab)
    FloatingActionButton floatingActionButton;

    private void startSearchSong() {
        String text = mSearchEditText.getText().toString();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(MainActivity.this, R.string.no_music_name, Toast.LENGTH_SHORT).show();
            return;
        }
        //先存储，再查询
        saveHistory(text);
        actionSearch(text);
    }

    private void actionSearch(String text){
        if (!mistype.equals("wy")) {
            try {
                text = URLEncoder.encode(text, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        Bundle bundle = new Bundle();
        if (mistype.equals("yinyutai")) {
            bundle.putString(Constants.YinyueTai, text);
            startActivity(MainActivity.this, SearchMvActivity.class, bundle);
        } else {
            bundle.putString(Constants.KEY, text);
            bundle.putString(Constants.TYPE, mistype);
            startActivity(MainActivity.this, SearchActivity.class, bundle);
        }
    }

    //存储查询记录
    private void saveHistory(String tarName){
        ArrayList<String> stringList = Sp.getInstance(mContext).getStringList(HISTORY);
        if(!stringList.contains(tarName)){
            stringList.add(tarName);
        }
        Sp.getInstance(mContext).putStringList(HISTORY, stringList);
    }

    private void startActivity(Context ctx, Class<?> resClass, Bundle bundle) {
        Intent i = new Intent(ctx, resClass);
        i.putExtras(bundle);
        startActivity(i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @PermissionGrant(REQUECT_CODE_SDCARD)
    public void requestSdcardSuccess() {

    }

    @PermissionDenied(REQUECT_CODE_SDCARD)
    public void requestSdcardFailed() {
        Toast.makeText(this, "未获取到SD卡权限!将无法使用下载功能", Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
        }


        return super.onOptionsItemSelected(item);
    }

    BottomDialog mBottomDialog;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                mBottomDialog =new BottomDialog(MainActivity.this)
                        .title(getString(R.string.sel_music_type))             //设置标题//传人菜单内容 //设置监听
                        .inflateMenu(R.menu.music_type, new OnItemClickListener() {
                            @Override
                            public void click(Item item) {
                                String type = item.getTitle();
                                if (type.equals(getString(R.string.music_wy))) {
                                    mistype = "wy";
                                    mSearchTitleIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.netease));
                                    mSearchTitleText.setText(type);
                                } else if (type.equals(getString(R.string.music_qq))) {
                                    mistype = "qq";
                                    mSearchTitleIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.qq));
                                    mSearchTitleText.setText(type);
                                } else if (type.equals(getString(R.string.music_kg))) {
                                    mistype = "kg";
                                    mSearchTitleIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.kugou));
                                    mSearchTitleText.setText(type);
                                } else if (type.equals(getString(R.string.music_kw))) {
                                    mistype = "kw";
                                } else if (type.equals(getString(R.string.music_bd))) {
                                    mistype = "bd";
                                } else if (type.equals(getString(R.string.music_tt))) {
                                    mistype = "tt";
                                } else if (type.equals(getString(R.string.music_dx))) {
                                    mistype = "dx";
                                } else if (type.equals(getString(R.string.music_xm))) {
                                    mistype = "xm";
                                    mSearchTitleIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.xiami));
                                    mSearchTitleText.setText(type);
                                } else if (type.equals(getString(R.string.mv_yinyuetai))) {
                                    mistype = "yinyutai";
                                    mSearchTitleIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.yinyuetai));
                                    mSearchTitleText.setText(type);
                                }
                                Toast.makeText(MainActivity.this, "已切换成 " + item.getTitle(), Toast.LENGTH_SHORT).show();
                                mBottomDialog.dismiss();
                            }
                        });
                mBottomDialog.show();
                break;
            case R.id.lv_search_history_clear:
                Log.i("xmg", "click1");
                Sp.getInstance(mContext).removeList(HISTORY);
                mHistoryList.clear();
                mHistoryAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP) {
            // 先隐藏键盘
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(this.getCurrentFocus()
                            .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            //进行搜索操作的方法，在该方法中可以加入mEditSearchUser的非空判断
            startSearchSong();
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP){
            ArrayList<String> stringList = Sp.getInstance(mContext).getStringList(HISTORY);
            mHistoryList.clear();
            mHistoryList.addAll(stringList);

            if(mHistoryList.size() == 0){
                mSearchHistoryLayout.setVisibility(View.GONE);
            }else {
                mSearchHistoryLayout.setVisibility(View.VISIBLE);
                mHistoryAdapter.notifyDataSetChanged();
            }
        }
        return false;
    }

    @Override
    public void onHistoryItemClick(View v, String item, int position) {
        // 先隐藏键盘
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(this.getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //再查询
        actionSearch(item);
    }
}
