package com.example.uhf.activity;



import static android.os.Environment.getExternalStorageDirectory;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;


import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.aqibmalik.usb_path.USBpath;
import com.example.uhf.AppContext;
import com.example.uhf.R;
import com.example.uhf.adapter.ViewPagerAdapter;
import com.example.uhf.fragment.KeyDownFragment;
import com.example.uhf.fragment.UHFReadTagFragment;
import com.example.uhf.tools.ExcelUtils;
import com.example.uhf.tools.UIHelper;
import com.example.uhf.view.NoScrollViewPager;
import com.rscja.deviceapi.RFIDWithUHFA8;
import com.rscja.deviceapi.enums.AntennaEnum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015-03-10.
 */
public class BaseTabFragmentActivity extends FragmentActivity {
    private static final String TAG ="a8";
    public KeyDownFragment currentFragment=null;
    public RFIDWithUHFA8 mReader;
    UHFReadTagFragment uhfReadTagFragment = null;
    private Handler exportDataHandler;
    private boolean started = false;
    private static final long EXPORT_DATA_INTERVAL = 10 * 60 * 1000; // (10 minutes)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar = getActionBar();
        if(mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        }
        try{
            Class systemProperties=Class.forName("android.os.SystemProperties");
            Method get=systemProperties.getDeclaredMethod("get", String.class);
            String modelInfo=(String)get.invoke(null, "ro.cw.model");
            Log.d(TAG, "cw.model:"+modelInfo);
        }catch (Exception e){
        }
        exportDataHandler = new Handler();
        scheduleExportData();
    }

    // Define a Runnable task to export data
    private Runnable exportDataRunnable = new Runnable() {
        @Override
        public void run() {
            exportData(); // Call the exportData method
            scheduleExportData(); // Schedule the task to run again
        }
    };

    // Schedule the exportData task to run at regular intervals
    private void scheduleExportData() {
        exportDataHandler.postDelayed(exportDataRunnable, EXPORT_DATA_INTERVAL);
    }

    public void initUHF() {

        try {
            mReader = RFIDWithUHFA8.getInstance();
        } catch (Exception ex) {
            toastMessage(ex.getMessage());
            return;
        }
        if (mReader != null) {
            new InitTask().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.UHF_ver:
                getUHFVersion();
                break;
            case R.id.UHF_temperature:
                getUHFTemperature();
                break;
            case R.id.UHF_exportData:
                exportData();
                break;
            default:
                break;
        }
        return true;
    }

    private void exportData() {

        if (uhfReadTagFragment.tagList != null && uhfReadTagFragment.tagList.size() > 0) {
            new ExcelTask(this).execute();
        } else {
            UIHelper.ToastMessage(this, R.string.export_empty_data);
        }

    }
    /**
     * 导出数据任务类
     */
    public class ExcelTask extends AsyncTask<String, Integer, Boolean> {
        protected ProgressDialog mypDialog;
        protected Activity mContxt;
        boolean isSotp = false;

        String pathRoot;
        String path;

        public ExcelTask(Activity act) {
            mContxt = act;

//            writeToFile(act.toString(), mContxt);

            pathRoot = String.valueOf(getExternalStorageDirectory()) + File.separator + "out";
            USBpath usbpath = new USBpath(mContxt);
            if(usbpath.getPath() != null){
                File rootDir = new File(usbpath.getPath());
                pathRoot = rootDir.getPath() + File.separator + "out";
            }

            File directory = new File(pathRoot);
            directory.mkdirs();

//            path = pathRoot + File.separator + GetTimesyyyymmddhhmmss() + ".xls";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            path = pathRoot + File.separator + "activity" + sdf.format(new Date()) + ".csv";
            File file = new File(pathRoot);
            if (!file.exists()) {
                file.mkdirs();
            }
        }

        public void notifySystemToScan(File file) {
            // mLogUtils.info
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            if (file.exists()) {
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                sendBroadcast(intent);
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            File file = new File(path);
            String[] h = new String[]{"TIME", "ID", "ANTENNA", "RSSI"};
            ExcelUtils excelUtils = new ExcelUtils();
            excelUtils.createExcel(file, h);

            List<String[]> list = new ArrayList<>();
            for (int i = 0; !isSotp && i < uhfReadTagFragment.tagList.size(); i++) {
                int pro = (int) (div(i + 1, uhfReadTagFragment.tagList.size(), 2) * 100);
                publishProgress(pro);

                HashMap<String, String> hashMap = uhfReadTagFragment.tagList.get(i);


                String[] data = new String[4];
                data[0] = hashMap.get(UHFReadTagFragment.TAG_TIME);
                data[1] = hashMap.get(UHFReadTagFragment.TAG_EPC);
                data[2] = hashMap.get(UHFReadTagFragment.TAG_ANT);
                data[3] = hashMap.get(UHFReadTagFragment.TAG_RSSI);
                list.add(data);


//                for (int j = 0; j < hashMap.size(); j++) {
//                    String[] data = new String[]{
//                            uhfReadTagFragment.tagList.get(i)
//                            String.valueOf(upcList.get(i).epcs.get(j).getSerial()),
//                            upcList.get(i).epcs.get(j).getEpc(),
//                    };
//                    list.add(data);
//                }
//                if((k!=0) && (k%5000==0)){
//                    //每次最多执行5000行
//                    excelUtils.writeToExcel(list);
//                    list.clear();
//                }
            }


            long begin = System.currentTimeMillis();
            publishProgress(101);
//            excelUtils.writeToExcel(list);
            excelUtils.writeToCSV(list);
            notifySystemToScan(file);
            uhfReadTagFragment.tagList.clear();
            long waitTime = 6000 - (System.currentTimeMillis() - begin);
            sleepTime(waitTime);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mypDialog.cancel();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (values[0] == 101) {
                mypDialog.setMessage("path:" + path);
            } else {
                mypDialog.setProgress(values[0]);
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mypDialog = new ProgressDialog(mContxt);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mypDialog.setMessage("...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.setMax(100);
            mypDialog.setProgress(0);

            mypDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    isSotp = true;
                }
            });

            if (mContxt != null) {
                mypDialog.show();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == 139 || keyCode == 280|| keyCode == 294) {
            if (event.getRepeatCount() == 0) {
                if (currentFragment != null) {
                    currentFragment.myOnKeyDwon();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void toastMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            return mReader.init(BaseTabFragmentActivity.this);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            mypDialog.cancel();

            if (!result) {
                Toast.makeText(BaseTabFragmentActivity.this, "init fail",
                        Toast.LENGTH_SHORT).show();
            } else {
                if (!started) {
                    mReader.setAntennaPower(AntennaEnum.ANT1, 30);

                    mReader.setAntennaPower(AntennaEnum.ANT2, 30);
                    SystemClock.sleep(50);
                    mReader.setAntennaPower(AntennaEnum.ANT3, 30);
                    SystemClock.sleep(50);
                    mReader.setAntennaPower(AntennaEnum.ANT4, 30);
                    SystemClock.sleep(50);
//            mReader.setAntennaPower(AntennaEnum.ANT5, 30);
//            SystemClock.sleep(50);

//            mReader.setAntennaPower(AntennaEnum.ANT6, 30);
//            SystemClock.sleep(50);
//            mReader.setAntennaPower(AntennaEnum.ANT7, 30);
//            SystemClock.sleep(50);
//            mReader.setAntennaPower(AntennaEnum.ANT8, 30);
//            SystemClock.sleep(50);

                    uhfReadTagFragment.readTag();
                    started = true;
                }
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            mypDialog = new ProgressDialog(BaseTabFragmentActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("init...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }
    }

    public void getUHFVersion() {
        if (mReader != null) {
            String rfidVer = mReader.getVersion();//UHF模块版本号
            String hwVer = mReader.getHardwareVersion();
            UIHelper.alert(this, R.string.action_uhf_ver, "UHF module version"+": "+rfidVer+"\n"+"Hardware version"+": "+hwVer, R.drawable.webtext);
            Log.i(TAG, "getUHFVersion: "+rfidVer);
        }
    }

    public void getUHFTemperature() {
        if (mReader != null) {
            String msgStr = String.format(getString(R.string.title_about_Temperature), mReader.getTemperature());
            UIHelper.alert(this, R.string.module_temperature, msgStr, R.drawable.webtext);
        }
    }

//---------------
     protected List<KeyDownFragment> lstFrg = new ArrayList<KeyDownFragment>();
    protected List<String> lstTitles = new ArrayList<String>();
    protected ActionBar mActionBar;

    protected NoScrollViewPager mViewPager;
    protected ViewPagerAdapter mViewPagerAdapter;
    protected void initViewPager() {
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), lstFrg, lstTitles);
        mViewPager = (NoScrollViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setAdapter(mViewPagerAdapter);
    }
    protected void initTabs() {
        if(mActionBar != null)
            for (int i = 0; i < mViewPagerAdapter.getCount(); ++i) {
                addTab(i);
            }
    }
    public void addTab(int position) {
        mActionBar.addTab(mActionBar.newTab()
                .setText(getTabTitle(position))
                .setTabListener(mTabListener));
    }
    public CharSequence getTabTitle(int position) {
        if(mViewPagerAdapter != null) {
            return mViewPagerAdapter.getPageTitle(position);
        }
        return null;
    }
    protected android.app.ActionBar.TabListener mTabListener = new android.app.ActionBar.TabListener() {

        @Override
        public void onTabSelected(android.app.ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            mViewPager.setCurrentItem(tab.getPosition());
            BaseTabFragmentActivity.this.onTabSelected(tab);
        }

        @Override
        public void onTabUnselected(android.app.ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }

        @Override
        public void onTabReselected(android.app.ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            BaseTabFragmentActivity.this.onTabReselected(tab);
        }
    };
    protected void onTabSelected(android.app.ActionBar.Tab tab) {}

    protected void onTabReselected(android.app.ActionBar.Tab tab) {}

    private void sleepTime(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception ex) {
        }
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度，以后的数字四舍五入。
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    private float div(float v1, float v2, int scale) {
        BigDecimal b1 = new BigDecimal(Float.toString(v1));
        BigDecimal b2 = new BigDecimal(Float.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    public String GetTimesyyyymmddhhmmss() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String dt = formatter.format(curDate);
        return dt;
    }
}
