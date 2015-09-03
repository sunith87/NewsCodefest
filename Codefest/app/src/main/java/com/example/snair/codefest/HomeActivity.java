package com.example.snair.codefest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.StrictMode;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.amazonaws.services.cognitosync.model.Platform;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends Activity {

    public static final int TEXT_OPTION = 0;
    public static final int IMAGE_OPTION = 1;
    public static final int VIDEO_OPTION = 2;
    public static final String TAG = HomeActivity.class.getSimpleName();
    private static final int TAKE_PICTURE_REQUEST_CODE = 1;
    public static final String SLASH = "/";
    private static final int TAKE_VIDEO_REQUEST_CODE = 2;
    private Button mButtonAdd;
    private Button mButtonSubmit;
    private ListView mOptionRenderer;
    ArticleClient client = new ArticleClient();

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

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        mButtonAdd = (Button) findViewById(R.id.btnAdd);
        mButtonAdd.setOnClickListener(optionsBucketListener);
        mButtonSubmit = (Button) findViewById(R.id.btnSubmit);
        mButtonSubmit.setOnClickListener(submitClickListener);
        mOptionRenderer = (ListView) findViewById(R.id.optionRenderer);
        mArticleItems = new ArrayList<EachItem>();
        mOptionsAdapter = new OptionsAdapter(HomeActivity.this, android.R.layout.simple_list_item_2, mArticleItems);
        mOptionRenderer.setAdapter(mOptionsAdapter);

    }

    private void openContentOptionsList() {
        final String[] option = new String[]{"Add Text", "Add Image","Add Video"};
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
        if (which == TEXT_OPTION) {
            openTextAdder();
        } else if (which == IMAGE_OPTION) {
            openImageAdder();
        }else if (which == VIDEO_OPTION){
            openVideoAdder();
        }

    }

    private void openImageAdder() {
        Log.v(TAG, "openImageAdder");
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, TAKE_PICTURE_REQUEST_CODE);


    }

    private void openVideoAdder() {
        Log.v(TAG, "openVideoAdder");
        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(cameraIntent, TAKE_VIDEO_REQUEST_CODE);


    }

    private void openTextAdder() {

        logText();
        Log.v(TAG, "openTextAdder");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.text_input, null);
        Button articleMakerButton = (Button) view.findViewById(R.id.articleMakerButton);
        final EditText editText = (EditText) view.findViewById(R.id.articleTextEditor);
        articleMakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                EachItem item = new EachItem(EachItem.TEXT_OPTION, text, HomeActivity.this);
                mOptionsAdapter.add(item);
                if (mTextAdderAlertDialog != null) {
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
        for (EachItem item : mArticleItems) {
            Log.v(TAG, "item = " + item.getResource());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (resultCode == RESULT_OK) {

            final Uri uri = intent.getData();
            InputStream inputStream = null;
            try {
                inputStream = getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Cursor returnCursor =
                    getContentResolver().query(uri, null, null, null, null);
            int size = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();

            final long length = returnCursor.getLong(size);


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.image_name, null);
            Button getImageNameButton = (Button) view.findViewById(R.id.imageNameLayoutButton);
            final EditText editText = (EditText) view.findViewById(R.id.imageNameEditor);
            final InputStream finalInputStream = inputStream;
            getImageNameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mImageNameDialog != null) {
                        mImageNameDialog.dismiss();
                    }
                    uploadImage(finalInputStream,editText.getText().toString(),length);
                }
            });
            builder.setView(view);

            switch (requestCode) {
                case TAKE_PICTURE_REQUEST_CODE:
                    builder.setTitle("Add image name");
                    mImageNameDialog = builder.create();
                    mImageNameDialog.show();
                    break;
                case TAKE_VIDEO_REQUEST_CODE:
                    builder.setTitle("Add video name");

            }

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


        String fullPath = directory.getAbsolutePath() + "/" + image_name;
        Log.v(TAG, "bitmap fullPath= " + fullPath);
        EachItem item = new EachItem(EachItem.IMAGE_OPTION, fullPath, HomeActivity.this);

        mOptionsAdapter.add(item);
        if (mTextAdderAlertDialog != null) {
            mTextAdderAlertDialog.dismiss();
        }


    }

    private void submitDataToServer() {

        List<ArticleData> articleDataArrayList = new ArrayList<ArticleData>();



        for (EachItem item : mArticleItems) {
            ArticleData data = null;
            if (item.getItemType() == EachItem.TEXT_OPTION) {
                data = new ArticleData(ArticleData.ContentType.TEXT, item.getResource());
            } else if (item.getItemType() == EachItem.IMAGE_OPTION) {
                File imgFile = new File(item.getResource());
                String dataUrl = ArticleClient.S3_URI_PREFIX + ArticleClient.IMAGES + SLASH + imgFile.getName();
                data = new ArticleData(ArticleData.ContentType.IMAGE, dataUrl);
            }

            articleDataArrayList.add(data);
        }


        Gson gson = new Gson();
        String jsonString = gson.toJson(articleDataArrayList);
        sendArticle(client, jsonString);

    }


    private void sendArticle(ArticleClient client, String json) {
        client.putArticle("ArticleName", json);
    }

    private void uploadImage(InputStream inputStream, String name, long length) {
        client.putImage(inputStream,name,length);

    }


}
