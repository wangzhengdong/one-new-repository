package com.example.stepcount;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by 电脑 on 2018/2/16.
 */

public class StepCountUtil {

    private ArrayList<Float> ylist=new  ArrayList<Float>();
    private     final  float  maxyn=15f;
    private      final    float   minyn=10f;

    private     final  float  maxperiod=1.5f;
    private      final    float   minperiod=0.25f;

    private      boolean   upflag=false;
    public   boolean  restart=true;
    private      float  lastyn=0f;
    private       long  last_topyn_time=System.currentTimeMillis();
    private     DecimalFormat  df= new DecimalFormat("0.00");
    private      int  stepcount=0;
    private     float    stoptime=3.2f;

    public  void generaterStep(double yn, StepCountService service) {

        float y = Float.parseFloat( df.format(yn));
        if(lastyn==0){
            lastyn=y;
        }else{
            if(y>=lastyn){
                upflag=true;
            }else{

                if(upflag==true){

                    if(lastyn>=minyn&&lastyn<=maxyn){
                        if(restart==true){
                            stepcount++;
                            service.setStepCount(stepcount);
                            last_topyn_time=System.currentTimeMillis();
                            restart=false;
                        }else{
                            long period = System.currentTimeMillis()-last_topyn_time ;
                            if(period>stoptime*1000||(period>=minperiod *1000&&period<=maxperiod *1000)){
                                stepcount++;
                                service.setStepCount(stepcount);
                                last_topyn_time=System.currentTimeMillis();
                            }
                        }
                    }
                }
                upflag=false;
            }

        }
        lastyn=y;
    }
}
