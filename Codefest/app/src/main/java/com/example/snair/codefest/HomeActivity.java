package com.example.snair.codefest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.StrictMode;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends Activity {

    public static final int TEXT_OPTION = 0;
    public static final int IMAGE_OPTION = 1;
    public static final int VIDEO_OPTION = 2;
    public static final String TAG = HomeActivity.class.getSimpleName();
    private static final int TAKE_PICTURE_REQUEST_CODE = 1;
    public static final String SLASH = "/";
    private static final int TAKE_VIDEO_REQUEST_CODE = 2;
    private static final Bitmap.CompressFormat IMAGE_FORMAT = Bitmap.CompressFormat.JPEG;
    private static final int SPEECH_REQUEST_CODE = 101;
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
    private AlertDialog mArticleNameDialog;
    private View.OnClickListener submitClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            submitDataToServer();
        }
    };
    private EditText mTextEditor;



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
        mOptionRenderer.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mOptionsAdapter.remove(mArticleItems.get(position));
                return false;
            }
        });
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
        Button articleTextSpeech = (Button) view.findViewById(R.id.articleTextSpeech);
        mTextEditor = (EditText) view.findViewById(R.id.articleTextEditor);
        articleMakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mTextEditor.getText().toString();
                EachItem item = new EachItem(EachItem.TEXT_OPTION, text, HomeActivity.this);
                mOptionsAdapter.add(item);
                if (mTextAdderAlertDialog != null) {
                    mTextAdderAlertDialog.dismiss();
                }
                mTextEditor.setText("");
            }
        });


        articleTextSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
        builder.setView(view);
        builder.setTitle("Add text to article");
        mTextAdderAlertDialog = builder.create();
        mTextAdderAlertDialog.show();


    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                 "No Speech to Text Support",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void logText() {
        for (EachItem item : mArticleItems) {
            Log.v(TAG, "item = " + item.getResource());
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent intent) {

        if (resultCode == RESULT_OK) {

            final Uri uri = intent.getData();
            InputStream inputStream = null;

        long length = 0;
            if (uri != null && requestCode==TAKE_VIDEO_REQUEST_CODE) {
                try {
                    inputStream = getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);
                int size = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                length = returnCursor.getLong(size);
                returnCursor.close();
                showAssetNameDialog(requestCode, inputStream, length);
            } else if (requestCode == TAKE_PICTURE_REQUEST_CODE){
                Bitmap bitmap = (Bitmap) intent.getExtras().get("data");
                Uri imageUri = getImageUri(bitmap);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bitmap.compress(IMAGE_FORMAT, 100, os);
                inputStream = new ByteArrayInputStream(os.toByteArray());
                length = os.toByteArray().length;
                showAssetNameDialog(requestCode, inputStream, length);
            }else if(requestCode == SPEECH_REQUEST_CODE){
                if (mTextEditor != null) {
                   String oldValue =  mTextEditor.getText().toString();

                    ArrayList<String> result = intent
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    StringBuffer buffer = new StringBuffer();
                    buffer.append(oldValue);
                    buffer.append("\n");
                    buffer.append(result.get(0));

                    mTextEditor.setText(buffer.toString());
                }
            }



        }
    }

    private Uri getImageUri(Bitmap bitmap){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void showAssetNameDialog(final int requestCode, InputStream inputStream, long length) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.image_name, null);
        Button getImageNameButton = (Button) view.findViewById(R.id.imageNameLayoutButton);
        final EditText editText = (EditText) view.findViewById(R.id.imageNameEditor);
        final InputStream finalInputStream = inputStream;
        final long finalLength = length;
        getImageNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mImageNameDialog != null) {
                    mImageNameDialog.dismiss();
                }
                uploadAsset(finalInputStream, editText.getText().toString(), finalLength);
                EachItem item = null;
                switch (requestCode) {
                    case TAKE_PICTURE_REQUEST_CODE:
                        String imageResource = editText.getText().toString()+"."+ IMAGE_FORMAT.toString().toLowerCase();
                        item = new EachItem(EachItem.IMAGE_OPTION,
                                imageResource, HomeActivity.this);
                        break;
                    case TAKE_VIDEO_REQUEST_CODE:
                        item = new EachItem(EachItem.VIDEO_OPTION,
                                editText.getText().toString(), HomeActivity.this);
                        break;
                    default:
                        break;

                }

                if (item != null)
                    mOptionsAdapter.add(item);
                if (mTextAdderAlertDialog != null) {
                    mTextAdderAlertDialog.dismiss();
                }
            }
        });
        builder.setView(view);

        switch (requestCode) {
            case TAKE_PICTURE_REQUEST_CODE:
                builder.setTitle("Add image name");
                break;
            case TAKE_VIDEO_REQUEST_CODE:
                builder.setTitle("Add video name");
                break;
            default:
                break;

        }

        mImageNameDialog = builder.create();
        mImageNameDialog.show();
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
            } else if (item.getItemType() == EachItem.VIDEO_OPTION) {
                File imgFile = new File(item.getResource());
                String dataUrl = ArticleClient.S3_URI_PREFIX + ArticleClient.VIDEO + SLASH + imgFile.getName();
                data = new ArticleData(ArticleData.ContentType.IMAGE, dataUrl);
            }

            articleDataArrayList.add(data);
        }


        Gson gson = new Gson();
        String jsonString = gson.toJson(articleDataArrayList);
        Log.v(TAG,"Json = "+jsonString);
        sendArticle(jsonString);

    }


    private void sendArticle( final String json) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.article_name, null);
        Button articleNameButton = (Button) view.findViewById(R.id.articleNameButton);
        final EditText editText = (EditText) view.findViewById(R.id.articleNameText);
        articleNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String articleName = editText.getText().toString();


                if (mArticleNameDialog != null){
                    mArticleNameDialog.dismiss();
                }

                if (articleName != null && !articleName.isEmpty()) {
                    client.putArticle(articleName, json);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Enter a valid name to send the article",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setView(view);
        builder.setTitle("Add name for article");
        mArticleNameDialog = builder.create();
        mArticleNameDialog.show();

    }

    private void uploadAsset(InputStream inputStream, String name, long length) {
        client.putImage(inputStream,name,length);

    }


}
