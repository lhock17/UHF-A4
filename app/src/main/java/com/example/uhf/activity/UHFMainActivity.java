package com.example.uhf.activity;

import com.example.uhf.BuildConfig;
import com.example.uhf.R;

import com.example.uhf.fragment.GPIOFragment;
import com.example.uhf.fragment.UHFKillFragment;
import com.example.uhf.fragment.UHFLockFragment;
import com.example.uhf.fragment.UHFReadFragment;
import com.example.uhf.fragment.UHFReadTagFragment;
import com.example.uhf.fragment.UHFSetFragment;
import com.example.uhf.fragment.UHFUpgradeFragment;
import com.example.uhf.fragment.UHFWriteFragment;


import com.rscja.utility.StringUtility;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTabHost;

/**
 * UHF使用demo
 * <p>
 * 1、使用前请确认您的机器已安装此模块。
 * 2、要正常使用模块需要在\libs\armeabi\目录放置libDeviceAPI.so文件，同时在\libs\目录下放置DeviceAPIver20160728.jar文件。
 * 3、在操作设备前需要调用 init()打开设备，使用完后调用 free() 关闭设备
 * <p>
 * <p>
 * 更多函数的使用方法请查看API说明文档
 *
 * @author wushengjun
 * 更新于 2016年8月9日
 */
public class UHFMainActivity extends BaseTabFragmentActivity {

    private final static String TAG = "MainActivity";
    private FragmentTabHost mTabHost;
    private FragmentManager fm;
    public boolean isBuzzer=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("zp_add", "-------UHFMainActivity  1--------");
        if(BuildConfig.DEBUG) {
           setTitle(String.format("%s(v%s-debug)", getString(R.string.app_name), BuildConfig.VERSION_NAME));
        } else {
           setTitle(String.format("%s(v%s)", getString(R.string.app_name), BuildConfig.VERSION_NAME));
        }
//------
        initViewPageData2();
        initViewPager();
        initTabs();
//------
        initUHF();
        checkReadWritePermission();
    }


    private void initViewPageData2(){
        uhfReadTagFragment = new UHFReadTagFragment();
        lstFrg.add(uhfReadTagFragment);
        lstFrg.add(new UHFReadFragment());
        lstFrg.add(new UHFWriteFragment());
        lstFrg.add(new UHFSetFragment());
        lstFrg.add(new UHFLockFragment());
        lstFrg.add(new UHFKillFragment());
        lstFrg.add(new UHFUpgradeFragment());
        lstFrg.add(new GPIOFragment());
        lstTitles.add(getString(R.string.uhf_msg_tab_scan));
        lstTitles.add(getString(R.string.uhf_msg_tab_read));
        lstTitles.add(getString(R.string.uhf_msg_tab_write));
        lstTitles.add(getString(R.string.uhf_msg_tab_set));
        lstTitles.add(getString(R.string.uhf_msg_tab_lock));
        lstTitles.add(getString(R.string.uhf_msg_tab_kill));
        lstTitles.add(getString(R.string.action_rfid_upgrader));
        lstTitles.add("GPIO");
        lstTitles.add(getString(R.string.LED_Controller));
    }

    @Override
    protected void onDestroy() {
        free();
        super.onDestroy();
    }

    private void free() {
        if (mReader != null) {
            mReader.free();
        }

    }

    public boolean vailHexInput(String str) {

        if (str == null || str.length() == 0) {
            return false;
        }
        if (str.length() % 2 == 0) {
            return StringUtility.isHexNumberRex(str);
        }

        return false;
    }


    /**
     * 读取成功模板声音播放
     */
    public void playSound() {
        if(isBuzzer) {
             mReader.buzzer();
        }
    }
    public void led(){
        mReader.led();
    }
    public void playSound(int i){}

    private void checkReadWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }
}
