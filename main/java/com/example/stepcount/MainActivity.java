package com.example.stepcount;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView tvStepCount;
    private Button buttonStart;
    private Button buttonStop;
    private TextView tvTime;
    private TextView tvDistance;

    private StepCountService stepCountService;

    private ServiceConnection   conn=new ServiceConnection() {
       @Override
       public void onServiceConnected(ComponentName name, IBinder service) {
           stepCountService=((StepCountService.Mybinder)service).getService();
           stepCountService.setStepCountInfo( new StepCountInfo(){
               public  void  setInfo(int stepCount, int distance,String time){
                   tvTime.setText(time);
                   tvDistance.setText(""+distance);
                   tvStepCount.setText(""+stepCount);
               }
           });
           stepCountService.setDrawFlag(true);
       }

       @Override
       public void onServiceDisconnected(ComponentName name) {
           stepCountService=null;
       }
   };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvStepCount = (TextView) findViewById(R.id.tv_step_count);
        buttonStart = (Button) findViewById(R.id.button_start);
        buttonStop = (Button) findViewById(R.id.button_stop);
        tvTime = (TextView) findViewById(R.id.tv_time);
        tvDistance = (TextView) findViewById(R.id.tv_distance);
        initView();
        Intent  intent=new Intent();
        intent.setClass(MainActivity.this, StepCountService.class);
        bindService(intent, conn ,BIND_AUTO_CREATE);

    }

    private void initView() {
        buttonStart.setClickable(true);
        buttonStop.setClickable(false);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if(buttonStop.getText().equals(getResources().getString(R.string.stop))){
                Intent  sintent=new Intent();
                sintent.setClass(MainActivity.this, StepCountService.class);
                startService(sintent);
                buttonStart.setClickable(false);
                buttonStop.setClickable(true);
                 /* Intent  intent=new  Intent();
                  intent.setAction("com.example.stepcount.StepReceiver");
                  intent.putExtra("info" ,"start");
                  sendBroadcast(intent);*/

              }else{//restart
                  Intent  intent=new  Intent();
                  intent.setAction("com.example.stepcount.StepReceiver");
                  intent.putExtra("info" ,"start");
                  sendBroadcast(intent);
                  buttonStart.setClickable(false);
                  buttonStop.setClickable(true);
                  buttonStop.setText(getResources().getString(R.string.stop));
              }
            }
        });
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonStop.getText().equals(getResources().getString(R.string.stop))){
                    buttonStop.setText(getResources().getString(R.string.clear));
                    buttonStart.setClickable(true);
                    stopClearRemoteView();

                }else{
                //    unbindService(conn);
                    stopClearRemoteView();
                    Intent  intent=new Intent();
                    intent.setClass(MainActivity.this, StepCountService.class);
                    stopService(intent);
                    buttonStop.setText(getResources().getString(R.string.stop));
                    buttonStop.setClickable(false);

                }

            }

            private void stopClearRemoteView() {
                Intent intent=new  Intent();
                intent.setAction("com.example.stepcount.StepReceiver");
                intent.putExtra("info" ,"stop_or_clear");
                sendBroadcast(intent);
            }
        });

    }



    private static final String TAG = "MainActivity";
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
        stepCountService.setDrawFlag(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        if(stepCountService!=null)
        stepCountService.setDrawFlag(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        unbindService(conn);
    }
}
