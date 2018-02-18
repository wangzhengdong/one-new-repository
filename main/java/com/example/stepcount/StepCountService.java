package com.example.stepcount;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import static android.content.ContentValues.TAG;

/**
 * Created by 电脑 on 2018/2/16.
 */

public class StepCountService extends Service {


    private  static final int    stop=1;
    private  static final int    running=2;
    private   static  final  int  puse=3;
    private  int  state=stop;
    private SensorManager sensorManager;
    private Sensor sensor;
    private RemoteViews remoteView;
    private  int  stepCount  ;
    private  int  distance  ;
    private  long  time;
    private long starttime;
    private  StepReceiver  recevier;
    private IntentFilter filter;
    private StepCountUtil util;


    float   a1=0.3f;
    float   a2=0.8f;
    float   a3=-0.12f;
    private float  Yn;
    private float lastYn;
    private float lastlastYn;
    private boolean drawFlag=true;

    private SensorEventListener   listener=new SensorEventListener() {
        float[] values;
        float tempvalues;
        @Override
        public void onSensorChanged(SensorEvent event) {
            values= event.values;
            tempvalues = (float)Math.sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2]);
            if(lastlastYn==0){
                Yn=tempvalues;

            }else{
                Yn=a1*tempvalues+a2*lastYn+a3*lastlastYn;
            }
            lastYn=Yn;
            lastlastYn=lastYn;
            util.generaterStep(Yn, StepCountService.this);
            time=(System.currentTimeMillis()-starttime);

            Log.i(TAG, "onSensorChanged: "+drawFlag);
            if(drawFlag){
                Log.i(TAG, "onSensorChanged: ");
                stepCountInfo.setInfo(stepCount,distance,time+"");
            }
            setRemoteView();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private Notification notification;
    private NotificationManager mNotificationManager;

    public void setDrawFlag(boolean drawFlag) {
        this.drawFlag = drawFlag;
    }

    private StepCountInfo stepCountInfo;

    public void setStepCountInfo(StepCountInfo stepCountInfo) {
        this.stepCountInfo=stepCountInfo;
    }


    class  Mybinder  extends Binder{
      public StepCountService   getService(){
          return StepCountService.this;
      }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Mybinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
         util=new StepCountUtil();
        recevier = new StepReceiver();
        filter=new  IntentFilter();
        filter.addAction("com.example.stepcount.StepReceiver");
        registerReceiver(recevier,filter);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder  builder=new NotificationCompat.Builder(this);
        remoteView=new RemoteViews("com.example.stepcount",R.layout.remoteview_layout);
        Intent  intent=new  Intent();
        intent.setAction("com.example.stepcount.StepReceiver");
        intent.putExtra("info" ,"start");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.remoteview_button_start,pendingIntent);
        intent.putExtra("info" ,"stop_or_clear");
        pendingIntent=PendingIntent.getBroadcast(this, 0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.remoteview_button_stop,pendingIntent);
        builder.setCustomBigContentView(remoteView);
        builder.setSmallIcon(R.drawable.icon);
        builder.setTicker("开始计步");
        notification=builder.build();
        setRemoteView();
        startForeground(1 ,notification);

        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }


   public  void  setStepCount(int  stepCount){
       this.stepCount=stepCount;
   }
    private void setRemoteView() {
        remoteView.setTextViewText(R.id.remoteview_tv_step ,""+stepCount);
        remoteView.setTextViewText(R.id.remoteview_tv_distance ,""+distance);
        remoteView.setTextViewText(R.id.remoteview_tv_time ,""+time);
      //  startForeground(1 ,notification);
      //  notification.bigContentView.setTextViewText(R.id.remoteview_tv_time ,""+time);

         mNotificationManager.notify(1,notification);

    }

    private String getTimeStr(long time) {
     String  str=null;
        if(time<60){
            str=time+" 秒";
        }else  if(60<=time&&time<60*60){
            str=time/60+" 分";
        }else  if(60*60<=time&&time<60*60*24){
            str=time/60+" 小时";
        }else  if(60*60*24<=time&&time<60*60*24*30){
            str=time/60+" 天";
        }else  if(60*60*24*30<=time&&time<60*60*24*30*12){
            str=time/60+" 月";
        }else  if(60*60*24*30*12<=time){
            str=time/60+" 年";
        }
        return str;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("onStartCommand","onStartCommand");

          if(state==stop){
              starttime=System.currentTimeMillis();
              sensorManager.registerListener(listener,sensor,SensorManager.SENSOR_DELAY_NORMAL,null);
              state=running;
          }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        sensorManager.unregisterListener(listener);
        stopForeground(true);
        unregisterReceiver(recevier);
    }

    private  class  StepReceiver  extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
           String info=intent.getStringExtra("info");
            if(info.equals("start")){
                if(state==running)
                    return;
                if(state==stop)
                starttime=System.currentTimeMillis();
               sensorManager.registerListener(listener,sensor,SensorManager.SENSOR_DELAY_GAME,null);
               state=running;
            }else  if(info.equals("stop_or_clear")){
                if(state==running){
                    state=puse;
                    sensorManager.unregisterListener(listener);
                }else  if(state==puse){
                    stepCount=0  ;
                    distance=0  ;
                    time=0 ;
                    state=stop;
                    setRemoteView();
                }

            }

        }
    }

}
