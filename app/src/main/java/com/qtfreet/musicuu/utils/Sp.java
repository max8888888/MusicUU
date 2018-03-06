package com.qtfreet.musicuu.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created by Max on 2018/03/06
 */

public class Sp{

    private static Sp sp;
    private static Context mContext;

    public static Sp getInstance(Context context){
        mContext = context;
        if(sp == null){
            sp = new Sp();
        }
        return sp;
    }

    /**
     * 存储ArrayList<String>类型数据
     * @param key 键
     * @param list 值
     */
    public void putStringList(String key, ArrayList<String> list){
        SharedPreferences sp = mContext.getSharedPreferences(SPUtils.FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        int oldListSize = sp.getInt(key+"_size", 0);
        for(int i=0; i<oldListSize; i++) {
            editor.remove(key+"_" + i);
        }
        editor.putInt(key+"_size", list.size());
        for(int i=0; i<list.size(); i++) {
            editor.putString(key+"_" + i, list.get(i));
        }
        editor.apply();
    }

    /**
     * 存储ArrayList<String>类型数据
     * @param key 键
     * @return ArrayList<String>
     */
    public ArrayList<String> getStringList(String key) {
        SharedPreferences sp = mContext.getSharedPreferences(SPUtils.FILE_NAME, Context.MODE_PRIVATE);
        ArrayList<String> list = new ArrayList<String>();
        int size = sp.getInt(key+"_size", 0);
        for(int i=0; i<size; i++) {
            String value = sp.getString(key+"_" + i, "");
            if(!TextUtils.isEmpty(value)){
                list.add(value);
            }
        }
        return list;
    }

    /**
     * 移除存储的列表
     * @param key
     */
    public void removeList(String key){
        SharedPreferences sp = mContext.getSharedPreferences(SPUtils.FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        int oldListSize = sp.getInt(key+"_size", 0);
        for(int i=0; i<oldListSize; i++) {
            editor.remove(key+"_" + i);
        }
        editor.remove(key+"_size");
        editor.apply();
    }
}
