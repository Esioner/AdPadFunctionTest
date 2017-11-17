package com.esioner.adpadfunctiontest;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vortex.pin.Pin;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RAY_STATUS_SOMEBODY = 1;
    private static final int RAY_STATUS_NOBODY = 0;
    /**
     * 0 ：灭
     * 1 ： 亮
     */
    private int GreenLightStatus;
    private int RedLightStatus;
    private Thread mThread;
    private boolean isStart = false;
    private TextView tvHomeTestResult;
    private boolean isStartRayTest = false;
    private TextView tvRayStatus;
    private Timer mRayTimer;
    private TimerTask mTask;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RAY_STATUS_SOMEBODY:
                    tvRayStatus.setTextColor(getResources().getColor(R.color.green));
                    tvRayStatus.setText("红外线状态：" + 1 + "此时检测到有人");
                    break;
                case RAY_STATUS_NOBODY:
                    tvRayStatus.setTextColor(getResources().getColor(R.color.red));
                    tvRayStatus.setText("红外线状态：" + 0 + "此时检测没有人");
                    break;
                default:
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    //初始化 View
    private void initView() {
        tvHomeTestResult = findViewById(R.id.tv_home_result);
        tvRayStatus = findViewById(R.id.tv_ray_status);
        Button btnOpenRed = findViewById(R.id.btn_red_open);
//      Button btnCloseRed = findViewById(R.id.btn_red_close);
        Button btnOpenGreen = findViewById(R.id.btn_green_open);
//      Button btnCloseGreen = findViewById(R.id.btn_green_close);
        Button btnAlternate = findViewById(R.id.btn_alternate);
        Button btnCloseAll = findViewById(R.id.btn_closeAll);
        Button btnStartRayTest = findViewById(R.id.btn_start_test_ray);
        Button btnStopRayTest = findViewById(R.id.btn_stop_test_ray);
        Button btnExitApp = findViewById(R.id.btn_exit);
        btnOpenRed.setOnClickListener(this);
//      btnCloseRed.setOnClickListener(this);
        btnOpenGreen.setOnClickListener(this);
//      btnCloseGreen.setOnClickListener(this);
        btnAlternate.setOnClickListener(this);
        btnCloseAll.setOnClickListener(this);
        btnStartRayTest.setOnClickListener(this);
        btnStopRayTest.setOnClickListener(this);
        btnExitApp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_red_open:
                openRed();
                break;
//            case R.id.btn_red_close:
//                closeRed();
//                break;
            case R.id.btn_green_open:
                openGreen();
                break;
//            case R.id.btn_green_close:
//                closeGreen();
//                break;
            case R.id.btn_alternate:
                alternateLight();
                break;
            case R.id.btn_closeAll:
                closeAll();
                break;
            case R.id.btn_start_test_ray:
                startTestRay();
                break;
            case R.id.btn_stop_test_ray:
                stopTestRay();
                break;
            case R.id.btn_exit:
                finish();
                break;
            default:
        }
    }


    //开始检测红外
    private void startTestRay() {
        isStartRayTest = true;
        mTask = new TimerTask() {
            @Override
            public void run() {
                int rayStatus = Pin.getData("PC2");
                Log.d("红外线检测", "rayStatus: " + rayStatus);
                if (rayStatus == 0) {
                    mHandler.sendEmptyMessage(RAY_STATUS_NOBODY);
                } else if (rayStatus == 1) {
                    mHandler.sendEmptyMessage(RAY_STATUS_SOMEBODY);
                }
            }
        };
        mRayTimer = new Timer();
        mRayTimer.schedule(mTask, 0, 1000);
    }

    //停止检测红外
    private void stopTestRay() {
        if (mRayTimer != null) {
            mRayTimer.cancel();
            mRayTimer = null;
        }
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        tvRayStatus.setText("");
    }

    //关闭所有灯
    private void closeAll() {
        closeRed();
        closeGreen();
        //判断当前是否在交替闪
        if (isStart) {
            isStart = false;
        }
    }

    //交替闪
    private void alternateLight() {
        closeGreen();
        closeRed();
        mThread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        isStart = true;
                        while (isStart) {
                            try {
                                openGreen();
                                Thread.sleep(500);
                                closeGreen();
                                openRed();
                                Thread.sleep(500);
                                closeRed();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
        mThread.start();
    }

    //亮绿灯
    public void openGreen() {
        if (isOpenLight("PC1")) {
            closeRed();
        }
        if (!isOpenLight("PC0")) {
            Pin.setFunc("PC0", Pin.FUNC_OUTPUT);
            Pin.setData("PC0", Pin.DATA_HIGH);
            Log.d("灯的状态", "openGreen: " + Pin.getData("PC0"));
        }
    }

    //关闭绿灯
    public void closeGreen() {
        if (isOpenLight("PC0")) {
            Pin.setFunc("PC0", Pin.FUNC_DISABLED);
            Pin.setData("PC0", Pin.DATA_LOW);
            Log.d("灯的状态", "openGreen: " + Pin.getData("PC0"));
        }
    }

    //开启红灯
    public void openRed() {
        if (isOpenLight("PC0")) {
            closeGreen();
        }
        if (!isOpenLight("PC1")) {
            Pin.setFunc("PC1", Pin.FUNC_OUTPUT);
            Pin.setData("PC1", Pin.DATA_HIGH);
            Log.d("灯的状态", "openRed: " + Pin.getData("PC1"));
        }
    }

    //关闭红灯
    public void closeRed() {
        if (isOpenLight("PC1")) {
            Pin.setFunc("PC1", Pin.FUNC_DISABLED);
            Pin.setData("PC1", Pin.DATA_LOW);
            Log.d("灯的状态", "openRed: " + Pin.getData("PC1"));
        }
    }

    //判断灯是否开启
    public boolean isOpenLight(String str) {
        int status = Pin.getData(str);
        if (status == 1) {
            return true;
        } else if (status == 0) {
            return false;
        } else {
            return false;
        }
    }

    //监听按键按下事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("Home功能测试", "onKeyDown: " + keyCode);
        if (keyCode == 122) {
            Log.d("Home功能测试", "onKeyDown: " + event.getAction());
            tvHomeTestResult.setTextColor(getResources().getColor(R.color.green));
            tvHomeTestResult.setText("Home键已按下");
        }
        return super.onKeyDown(keyCode, event);
    }

    //监听按键抬起事件
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 122) {
            tvHomeTestResult.setTextColor(getResources().getColor(R.color.red));
            tvHomeTestResult.setText("Home键未按下");
        }
        return super.onKeyUp(keyCode, event);
    }
    @Override
    protected void onStop() {
        closeAll();
        stopTestRay();
        super.onStop();
    }
}
