package com.example.snair.codefest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by snair on 03/09/2015.
 */
public class OptionsAdapter extends ArrayAdapter<EachItem> {

    private final List<EachItem> mObjects;
    private final Context mContext;

    public OptionsAdapter(Context context, int resource, List<EachItem> objects) {
        super(context, resource, objects);
        mContext = context;
        mObjects = objects;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return renderView(position, parent, inflater);
    }



    private View renderView(final int position, ViewGroup parent, LayoutInflater inflater) {
        View rowView = inflater.inflate(R.layout.individual_item, parent, false);
        EditText textView = (EditText) rowView.findViewById(R.id.textRenderer);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageRenderer);

        EachItem eachItem = mObjects.get(position);
        Log.d(HomeActivity.TAG, "position = " + position);
        Log.d(HomeActivity.TAG, "text = "+ eachItem.getResource());
        if (eachItem.getItemType() == EachItem.TEXT_OPTION) {
            imageView.setVisibility(View.GONE);
            renderText(position, textView, eachItem);
        } else if (eachItem.getItemType() == EachItem.IMAGE_OPTION) {
            textView.setVisibility(View.GONE);
            renderImage(position, imageView, eachItem);

        }

        return rowView;
    }

    private void renderImage(int position, ImageView imageView, EachItem eachItem) {

        File imgFile = new  File(eachItem.getResource());

        if(imgFile.exists()){

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);

        }

    }

    private void renderText(final int position, EditText textView, EachItem eachItem) {

        textView.setText(eachItem.getResource().toString());
        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mObjects.remove(position);
                mObjects.add(new EachItem(EachItem.TEXT_OPTION,s.toString(),mContext));
            }
        });
    }
}
