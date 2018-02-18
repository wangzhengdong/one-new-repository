package com.example.stepcount;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by 电脑 on 2018/2/16.
 */

public class SplashActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

    }


   public  void  click(View  view){
       Intent  intent=new Intent();
       intent.setClass(this , MainActivity.class);
       startActivity(intent);
       this.finish();
   }


}
