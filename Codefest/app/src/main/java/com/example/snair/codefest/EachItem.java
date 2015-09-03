package com.example.snair.codefest;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by snair on 03/09/2015.
 */
public class EachItem {
    public static final int TEXT_OPTION = HomeActivity.TEXT_OPTION;
    public static final int IMAGE_OPTION = HomeActivity.IMAGE_OPTION;
    public static final int VIDEO_OPTION = HomeActivity.VIDEO_OPTION;

    public static final String CODEFEST_APP_EACH_ITEM = "Codefest_app_each_item";
    public static final String RESOURCE_SAVE_KEY = "resource_save_key";
    private final int mItemType;
    private final String mResource;


    public EachItem(int type, String resource, Context context){
        mItemType = type;
        mResource = resource;
        saveResource(context);
    }

    private void saveResource(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(CODEFEST_APP_EACH_ITEM,Context.MODE_PRIVATE).edit();
        editor.putString(RESOURCE_SAVE_KEY, getResource());
        editor.apply();
    }

    public int getItemType() {
        return mItemType;
    }

    public String getResource() {
        return mResource;
    }


}
