package com.example.snair.codefest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends Activity {

    public static final int TEXT_OPTION = 0;
    public static final int IMAGE_OPTION = 1;
    public static final String TAG = HomeActivity.class.getSimpleName();
    private static final int TAKE_PICTURE_REQUEST_CODE = 1;
    private Button mButtonAdd;
    private Button mButtonSubmit;
    private ListView mOptionRenderer;

    List<EachItem> mArticleItems;

    private View.OnClickListener optionsBucketListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openContentOptionsList();
        }
    };
    private AlertDialog mTextAdderAlertDialog;
    private OptionsAdapter mOptionsAdapter;
    private AlertDialog mImageNameDialog;
    private View.OnClickListener submitClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            submitDataToServer();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mButtonAdd = (Button) findViewById(R.id.btnAdd);
        mButtonAdd.setOnClickListener(optionsBucketListener);
        mButtonSubmit = (Button) findViewById(R.id.btnSubmit);
        mButtonSubmit.setOnClickListener(submitClickListener);
        mOptionRenderer = (ListView)findViewById(R.id.optionRenderer);
        mArticleItems = new ArrayList<EachItem>();
        mOptionsAdapter = new OptionsAdapter(HomeActivity.this, android.R.layout.simple_list_item_2, mArticleItems);
        mOptionRenderer.setAdapter(mOptionsAdapter);

    }

    private void openContentOptionsList() {
        final String[] option = new String[] { "Add Text", "Add Image"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, option);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Option");

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openRenderer(which);
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void openRenderer(int which) {
        if (which == TEXT_OPTION){
            openTextAdder();
        }else if (which == IMAGE_OPTION){
            openImageAdder();
        }

    }

    private void openImageAdder() {
        Log.v(TAG, "openImageAdder");
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, TAKE_PICTURE_REQUEST_CODE);


    }

    private void openTextAdder() {

        logText();
        Log.v(TAG, "openTextAdder");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.text_input, null);
        Button articleMakerButton = (Button)view.findViewById(R.id.articleMakerButton);
        final EditText editText = (EditText)view.findViewById(R.id.articleTextEditor);
        articleMakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    String text = editText.getText().toString();
                    EachItem item = new EachItem(EachItem.TEXT_OPTION,text,HomeActivity.this);
                    mOptionsAdapter.add(item);
                    if (mTextAdderAlertDialog != null){
                        mTextAdderAlertDialog.dismiss();
                    }
                editText.setText("");
            }
        });
        builder.setView(view);
        builder.setTitle("Add text to article");
         mTextAdderAlertDialog = builder.create();
        mTextAdderAlertDialog.show();


    }

    private void logText() {
        for (EachItem item:mArticleItems){
            Log.v(TAG, "item = "+item.getResource());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {


        if (requestCode == TAKE_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            final Bitmap bitmap = (Bitmap)intent.getExtras().get("data");


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.image_name, null);
            Button getImageNameButton = (Button)view.findViewById(R.id.imageNameLayoutButton);
            final EditText editText = (EditText)view.findViewById(R.id.imageNameEditor);
            getImageNameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mImageNameDialog != null) {
                        mImageNameDialog.dismiss();
                    }
                    saveBitmap(bitmap,editText.getText().toString());
                }
            });
            builder.setView(view);
            builder.setTitle("Add image name");
            mImageNameDialog = builder.create();
            mImageNameDialog.show();
        }
    }

    private void saveBitmap(Bitmap bitmap, String bitmapName) {
        Log.v(TAG, "bitmap = " + bitmap.getByteCount());
        Log.v(TAG, "bitmap name= " + bitmapName);
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        String image_name = bitmapName + ".jpg";
        File mypath = new File(directory, image_name);

        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(mypath);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        String fullPath = directory.getAbsolutePath()+"/"+image_name;
        Log.v(TAG, "bitmap fullPath= " + fullPath);
        EachItem item = new EachItem(EachItem.IMAGE_OPTION,fullPath,HomeActivity.this);

        mOptionsAdapter.add(item);
        if (mTextAdderAlertDialog != null){
            mTextAdderAlertDialog.dismiss();
        }


    }

    private void submitDataToServer() {
        ArticleData data = null;
        List<String> jsonData = new ArrayList<String>();


        for (EachItem item : mArticleItems) {
            if (item.getItemType() == EachItem.TEXT_OPTION) {
                data = new ArticleData(ArticleData.ContentType.TEXT, item.getResource());
            } else if (item.getItemType() == EachItem.IMAGE_OPTION) {
                data = new ArticleData(ArticleData.ContentType.IMAGE, item.getResource());
            }
            Gson gson = new Gson();
            String jsonString = gson.toJson(data);
            jsonData.add(jsonString);
        }

        Object[] objects = jsonData.toArray();
        Log.v(TAG, "json data = " + objects.toString());


    }



}
