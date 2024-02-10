package com.example.uhf;

import android.app.Application;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Environment;

import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.exception.ConfigurationException;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

//import com.rscja.deviceapi.BuildConfig;

/**
 * 全局应用程序类：用于保存和调用全局应用配置及访问网络数据
 */
public class AppContext extends Application {

    public RFIDWithUHFUART mReader;
    public final static String DEFAULT_SAVE_PATH = Environment
            .getExternalStorageDirectory()
            + File.separator
            + "handset"
            + File.separator;
    public ExecutorService mExecutorService; // 线程池
    public float audioMaxVolumn = 0;// 返回当前AudioManager对象的最大音量值
    public float audioCurrentVolumn = 0;// 返回当前AudioManager对象的音量值
    HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private SoundPool soundPool;
    private AudioManager am;
    private static AppContext mApp;
    public static AppContext getInstance() {
        return mApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        init();
    }

    /**
     * 初始化
     */
    private void init() {
        try {
            mReader= RFIDWithUHFUART.getInstance();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        mApp = this;
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(this, R.raw.barcodebeep, 1));
        soundMap.put(2, soundPool.load(this, R.raw.serror, 1));
        am = (AudioManager) this.getSystemService(AUDIO_SERVICE);// 实例化AudioManager对象

        audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 返回当前AudioManager对象的最大音量值
        audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);// 返回当前AudioManager对象的音量值



    }


    /**
     * 播放提示音
     *
     * @param id 成功1，失败2
     */
    public void playSound(int id) {
        float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 返回当前AudioManager对象的最大音量值
        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);// 返回当前AudioManager对象的音量值
        float volumnRatio = audioCurrentVolume / audioMaxVolume;
        try {
            soundPool.play(soundMap.get(id), volumnRatio, // 左声道音量
                    volumnRatio, // 右声道音量
                    1, // 优先级，0为最低
                    0, // 循环次数，0不循环，-1永远循环
                    1 // 回放速度 ，该值在0.5-2.0之间，1为正常速度
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }








}
