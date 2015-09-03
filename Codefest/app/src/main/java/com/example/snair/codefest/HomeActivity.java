package com.example.snair.codefest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class HomeActivity extends AppCompatActivity {

    private Button mButtonAdd;
    private ListView mContentListView;
    private View.OnClickListener optionsBucketListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mContentListView.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mButtonAdd = (Button)findViewById(R.id.btnAdd);
        mContentListView = (ListView)findViewById(R.id.contentListView);
        mButtonAdd.setOnClickListener(optionsBucketListener);
    }




}
