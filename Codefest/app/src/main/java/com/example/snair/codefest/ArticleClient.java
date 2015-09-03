package com.example.snair.codefest;


import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.StringInputStream;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.gson.Gson;


import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ArticleClient {
    public static final String BUCKET_NAME = "codefest-team-a";
    public static final String ARTICLES = "articles";
    public static final String IMAGES = "images";
    public static final String S3_URI_PREFIX = "https://codefest-team-a.s3-eu-west-1.amazonaws.com/";
    private AmazonS3 s3client = new AmazonS3Client((AWSCredentials) null);

    public void putArticle(String name, String article) {
        ArticleService service = new ArticleService();
        service.execute(name,article);
    }

    public void putImage(File imageFile) {

        if (imageFile.exists()) {
            ImageService service = new ImageService(imageFile);
            service.execute();
        }

    }
    public List<String> getArticleNames() {
        return getObjectNames(ARTICLES);
    }

    public List<String> getImageNames() {
        return getObjectNames(IMAGES);
    }

    public URI getImageUri(String name) {
        try {
            return new URI(S3_URI_PREFIX + IMAGES + "/" + name);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getArticle(String name) {
        S3ObjectInputStream stream = s3client.getObject(BUCKET_NAME, ARTICLES + "/" + name).getObjectContent();

        try {
            return CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Closeables.closeQuietly(stream);
        }
    }

    private void putObject(String name, String article, String folder) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(article.length());

        try {
            s3client.putObject(new PutObjectRequest(BUCKET_NAME, folder + "/" + name, new StringInputStream(article), metadata));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getObjectNames(String folder) {
        List<String> list = new ArrayList<>();
        ObjectListing objects = s3client.listObjects(BUCKET_NAME, folder + "/");

        for (S3ObjectSummary object: objects.getObjectSummaries()) {
            list.add(object.getKey().replace(folder + "/", ""));
        }

        return list;
    }

//    public static void main(String[] args) {
//        ArticleClient client = new ArticleClient();
//        client.putArticle("test-article-3", "some-content");
//        client.putImage("test-image-1", new File("/tmp/test-image.jpeg"));
//        List<String> articles = client.getArticleNames();
//
//        for (String articleName: articles) {
//            System.out.println(articleName);
//        }
//
//        System.out.println(client.getArticle("README.md"));
//
//        Gson gson = new Gson();
//        Object o = gson.fromJson("[{\"aa\":\"\b\"}]", List.class);
//        System.out.println(o.getClass().getName());
//        System.out.println(((List) o).get(0).getClass().getName());
//    }


    public class ImageService extends AsyncTask<String,Void,Void>{

        File imageFile;
        public ImageService(File imgFile) {
             imageFile = imgFile;
        }

        @Override
        protected Void doInBackground(String... params) {
            String key = IMAGES + "/" + imageFile.getName();
            Log.v(HomeActivity.TAG,"key ="+key);
            s3client.putObject(new PutObjectRequest(BUCKET_NAME, key, imageFile));
            return null;
        }
    }

    public class ArticleService extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... params) {
            String name = params[0];
            String article = params[1];
            putObject(name, article, ARTICLES);
            return null;
        }
    }
}
