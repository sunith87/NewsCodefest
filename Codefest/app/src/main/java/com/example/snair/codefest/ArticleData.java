package com.example.snair.codefest;

import com.google.gson.annotations.SerializedName;

/**
 * Created by snair on 03/09/2015.
 */
public class ArticleData {

   public enum ContentType{
       TEXT,IMAGE,VIDEO
   }

    private String type;
    private String content;

    ArticleData(ContentType contentType,String content){
        this.type = contentType.name();
        this.content = content;
    }


    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }


}
