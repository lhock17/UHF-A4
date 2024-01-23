package com.example.uhf.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.uhf.R;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.NumberTool;
import com.example.uhf.tools.StringUtils;
import com.example.uhf.tools.UIHelper;
import com.rscja.deviceapi.RFIDWithUHFA8;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class UHFReadTagFragment extends KeyDownFragment {
    private static String TAG = "UHFReadTagFragment";
    private boolean loopFlag = false;
    private int inventoryFlag = 1;
    public ArrayList<HashMap<String, String>> tagList;
    SimpleAdapter adapter;
    Button BtClear;
    TextView tv_count, tv_totalNum, tv_time;
    RadioGroup RgInventory;
    RadioButton RbInventorySingle;
    RadioButton RbInventoryLoop;
    EditText etTime;

    Button BtInventory;
    ListView LvTags;
    private UHFMainActivity mContext;
    private HashMap<String, String> map;

    private CheckBox cbFilter;
    private ViewGroup layout_filter;

    public static final String TAG_EPCAndTidUser = "TAG_EPCAndTidUser";
    public static final String TAG_EPC = "tagEpc";
    public static final String TAG_TID = "tagTid";
    public static final String TAG_USER = "tagUser";
    public static final String TAG_LEN = "tagLen";
    public static final String TAG_COUNT = "tagCount";
    public static final String TAG_RSSI = "tagRssi";
    public static final String TAG_ANT = "tagAnt";
    public static final String TAG_TIME = "tagTime";

    private int totalNum;
    private List<String> tempDatas;

    private Button btnSetFilter;
    ExecutorService executorService=null;
    public boolean isStop=false;
    private HashMap<String, Long> lastScans = new HashMap<>();
    public boolean mute = true;
    public int tagWait = 5 * 1000; // 5 seconds

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                UHFTAGInfo info = (UHFTAGInfo) msg.obj;
                addDataToList(mergeTidEpc(info.getTid(),info.getEPC(),info.getUser()),info.getEPC(),info.getTid(),info.getUser(), info.getRssi(), info.getAnt());
                if (!mute) {
                    mContext.playSound();
                }
                mContext.led();
            } else {
                setTotalTime();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "UHFReadTagFragment.onCreateVetTimeiew");
        View view = inflater.inflate(R.layout.uhf_readtag_fragment, container, false);
        inits(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "UHFReadTagFragment.onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mContext = (UHFMainActivity) getActivity();
        executorService=Executors.newFixedThreadPool(20);
    }

    private void inits(View view) {
        BtClear = (Button) view.findViewById(R.id.BtClear);
        tv_count = (TextView) view.findViewById(R.id.tv_count);
        tv_totalNum = (TextView) view.findViewById(R.id.tv_totalNum);
        tv_time = (TextView) view.findViewById(R.id.tv_time);
        RgInventory = (RadioGroup) view.findViewById(R.id.RgInventory);
        etTime= (EditText) view.findViewById(R.id.etTime);

        RbInventorySingle = (RadioButton) view.findViewById(R.id.RbInventorySingle);
        RbInventoryLoop = (RadioButton) view.findViewById(R.id.RbInventoryLoop);

        tagList = new ArrayList<>();
        tempDatas = new ArrayList<>();
        BtInventory = (Button) view.findViewById(R.id.BtInventory);
        LvTags = (ListView) view.findViewById(R.id.LvTags);
        adapter = new SimpleAdapter(getContext(), tagList, R.layout.listtag_items,
                new String[]{TAG_EPC, TAG_LEN, TAG_COUNT, TAG_RSSI, TAG_ANT},
                new int[]{R.id.TvTagUii, R.id.TvTagLen, R.id.TvTagCount, R.id.TvTagRssi, R.id.TvAnt});
        LvTags.setAdapter(adapter);

        BtClear.setOnClickListener(new BtClearClickListener());
        RgInventory.setOnCheckedChangeListener(new RgInventoryCheckedListener());
        BtInventory.setOnClickListener(new BtInventoryClickListener());

        initFilter(view); // 初始化过滤
        clearData();
    }

    private void initFilter(View view) {
        layout_filter = view.findViewById(R.id.layout_filter);
        layout_filter.setVisibility(View.GONE);
        cbFilter = view.findViewById(R.id.cbFilter);
        cbFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layout_filter.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        final EditText etLen = (EditText) view.findViewById(R.id.etLen);
        final EditText etPtr = (EditText) view.findViewById(R.id.etPtr);
        final EditText etData = (EditText) view.findViewById(R.id.etData);
        final RadioButton rbEPC = (RadioButton) view.findViewById(R.id.rbEPC);
        final RadioButton rbTID = (RadioButton) view.findViewById(R.id.rbTID);
        final RadioButton rbUser = (RadioButton) view.findViewById(R.id.rbUser);
        btnSetFilter = (Button) view.findViewById(R.id.btSet);

        btnSetFilter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int filterBank =RFIDWithUHFA8.Bank_EPC;
                if (rbEPC.isChecked()) {
                    filterBank = RFIDWithUHFA8.Bank_EPC;
                } else if (rbTID.isChecked()) {
                    filterBank = RFIDWithUHFA8.Bank_TID;
                } else if (rbUser.isChecked()) {
                    filterBank = RFIDWithUHFA8.Bank_USER;
                }
                if (etLen.getText().toString() == null || etLen.getText().toString().isEmpty()) {
                    UIHelper.ToastMessage(mContext, "数据长度不能为空");
                    return;
                }
                if (etPtr.getText().toString() == null || etPtr.getText().toString().isEmpty()) {
                    UIHelper.ToastMessage(mContext, "起始地址不能为空");
                    return;
                }
                int ptr = StringUtils.toInt(etPtr.getText().toString(), 0);
                int len = StringUtils.toInt(etLen.getText().toString(), 0);
                String data = etData.getText().toString().trim();
                if (len > 0) {
                    String rex = "[\\da-fA-F]*"; //匹配正则表达式，数据为十六进制格式
                    if (data == null || data.isEmpty() || !data.matches(rex)) {
                        UIHelper.ToastMessage(mContext, "过滤的数据必须是十六进制数据");
                        return;
                    }

                    if (mContext.mReader.setFilter(filterBank, ptr, len, data)) {
                        UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_filter_succ);
                    } else {
                        UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_filter_fail);
                    }
                } else {
                    //禁用过滤
                    String dataStr = "";
                    if (mContext.mReader.setFilter(RFIDWithUHFA8.Bank_EPC, 0, 0, dataStr)
                            && mContext.mReader.setFilter(RFIDWithUHFA8.Bank_TID, 0, 0, dataStr)
                            && mContext.mReader.setFilter(RFIDWithUHFA8.Bank_USER, 0, 0, dataStr)) {
                        UIHelper.ToastMessage(mContext, R.string.msg_disable_succ);
                    } else {
                        UIHelper.ToastMessage(mContext, R.string.msg_disable_fail);
                    }
                }
                cbFilter.setChecked(false);
            }
        });
        CheckBox cb_filter = (CheckBox) view.findViewById(R.id.cb_filter);
        rbEPC.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rbEPC.isChecked()) {
                    etPtr.setText("32");
                }
            }
        });
        rbTID.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rbTID.isChecked()) {
                    etPtr.setText("0");
                }
            }
        });
        rbUser.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rbUser.isChecked()) {
                    etPtr.setText("0");
                }
            }
        });

        cb_filter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) { //启用过滤

                } else { //禁用过滤

                }
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (!isVisibleToUser) {
            // 停止识别
            stopInventory();
        }
        super.setUserVisibleHint(isVisibleToUser);
        Log.i(TAG, "setUserVisibleHint>>>isVisibleToUser=" + isVisibleToUser);
    }

    @Override
    public void onResume() {
        super.onResume();
        mContext.currentFragment=this;
    }

    @Override
    public void onPause() {
        Log.i(TAG, "UHFReadTagFragment.onPause");
        super.onPause();
        // 停止识别
         stopInventory();
        mContext.currentFragment=null;
    }

    /**
     * 添加EPC到列表中
     *
     * @param
     */
    private void addDataToList(String tidAndEPCUser,String Epc, String Tid, String User, String rssi, String ant) {
        if (StringUtils.isNotEmpty(Epc)) {

            if (!lastScans.containsKey(Epc)) {
                lastScans.put(Epc, System.currentTimeMillis() - tagWait);
            }
            if (System.currentTimeMillis() - lastScans.get(Epc) > tagWait) {
                lastScans.put(Epc, System.currentTimeMillis());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
                String dt = formatter.format(curDate);

                String IdAndEPCAndDt = tidAndEPCUser + dt;

                //            int index = checkIsExist(IdAndEPCAndDt);
                map = new HashMap<>();
                map.put(TAG_EPCAndTidUser, tidAndEPCUser);
                map.put(TAG_TIME, dt);
                map.put(TAG_EPC, Epc);
                map.put(TAG_TID, Tid);
                map.put(TAG_USER, User);
                //            map.put(TAG_COUNT, String.valueOf(1));
                map.put(TAG_RSSI, rssi);
                map.put(TAG_ANT, ant);
                //            if (index == -1) {
                tagList.add(map);
                tempDatas.add(IdAndEPCAndDt);
                tv_count.setText(String.valueOf(adapter.getCount()));
                //            } else {
                //                int tagCount = Integer.parseInt(tagList.get(index).get(TAG_COUNT), 10) + 1;
                //                map.put(TAG_COUNT, String.valueOf(tagCount));
                //                tagList.set(index, map);
                //            }
                tv_totalNum.setText(String.valueOf(++totalNum));
                adapter.notifyDataSetChanged();
            }
        }
    }

    private long mStartTime;
    private void setTotalTime() {
        if(loopFlag) {
            float useTime = (System.currentTimeMillis() - mStartTime) / 1000.0F;
            double dTime = NumberTool.getPointDouble(1, useTime);
            tv_time.setText(dTime + "s");
            String strTime = etTime.getText().toString();


            int time = 999999999;
            if (!TextUtils.isEmpty(strTime)) {
                time = Integer.parseInt(strTime);
            }
            if (dTime >= time) {
//                stopInventory();
            }
        }
    }

    public class BtClearClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            clearData();
        }
    }

    private void clearData() {
        totalNum = 0;
        tv_count.setText("0");
        tv_totalNum.setText("0");
        tv_time.setText("0s");

        tagList.clear();
        tempDatas.clear();
        adapter.notifyDataSetChanged();
    }

    public class RgInventoryCheckedListener implements OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == RbInventorySingle.getId()) {
                // 单步识别
                inventoryFlag = 0;
                cbFilter.setChecked(false);
                cbFilter.setVisibility(View.INVISIBLE);
            } else if (checkedId == RbInventoryLoop.getId()) {
                // 单标签循环识别
                inventoryFlag = 1;
                cbFilter.setVisibility(View.VISIBLE);
            }
        }
    }

    public class BtInventoryClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            readTag();
        }
    }

    public void readTag() {
        if (BtInventory.getText().equals(mContext.getString(R.string.btInventory)))// 识别标签
        {
            switch (inventoryFlag) {
                case 0:// 单步
                    mStartTime = System.currentTimeMillis();
                    UHFTAGInfo uhftagInfo = mContext.mReader.inventorySingleTag();
                    if (uhftagInfo != null) {
                        tv_count.setText(String.valueOf(adapter.getCount()));
                        tv_totalNum.setText(String.valueOf(totalNum));
                        addDataToList(mergeTidEpc(uhftagInfo.getTid(), uhftagInfo.getEPC(), uhftagInfo.getUser()), uhftagInfo.getEPC(), uhftagInfo.getTid(), uhftagInfo.getUser(), uhftagInfo.getRssi(), uhftagInfo.getAnt());
                        // setTotalTime();
                        mContext.playSound();
                    } else {
                        UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_fail);
                    }
                    break;
                case 1:// 单标签循环
                    if (mContext.mReader.startInventoryTag()) {
                        BtInventory.setText(mContext.getString(R.string.title_stop_Inventory));
                        loopFlag = true;
                        isStop=false;
                        setViewEnabled(false);
                        mStartTime = System.currentTimeMillis();
                        new TagThread().start();
                    } else {
                        mContext.mReader.stopInventory();
                        UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_open_fail);
                    }
                    break;
                default:
                    break;
            }
        } else {// 停止识别
            
            stopInventory();
        //    setTotalTime();
        }
    }

    private void setViewEnabled(boolean enabled) {
        RbInventorySingle.setEnabled(enabled);
        RbInventoryLoop.setEnabled(enabled);
        cbFilter.setEnabled(enabled);
        btnSetFilter.setEnabled(enabled);
        BtClear.setEnabled(enabled);
        etTime.setEnabled(enabled);
    }

    /**
     * 停止识别
     */
    private synchronized  void stopInventory() {
        if (loopFlag && !isStop) {
            isStop=true;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (mContext.mReader.stopInventory()) {
                        SystemClock.sleep(200);
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BtInventory.setText(mContext.getString(R.string.btInventory));
                                loopFlag = false;
                                setViewEnabled(true);
                            }
                        });
                    } else {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_stop_fail);
                                loopFlag = false;
                                setViewEnabled(true);
                            }
                        });

                    }


                }
            });

        }
    }

    /**
     * 二分查找，找到该值在数组中的下标，否则为-1
     */
    static int binarySearch(List<String> array, String src) {
        if(array == null) {
            return -1;
        }
        int left = 0;
        int right = array.size() - 1;
        // 这里必须是 <=
        while (left <= right) {
            if (compareString(array.get(left), src)) {
                return left;
            } else if (left != right) {
                if (compareString(array.get(right), src))
                    return right;
            }
            left++;
            right--;
        }
        return -1;
    }

    static boolean compareString(String str1, String str2) {
        if (str1.length() != str2.length()) {
            return false;
        } else if (str1.hashCode() != str2.hashCode()) {
            return false;
        } else {
            char[] value1 = str1.toCharArray();
            char[] value2 = str2.toCharArray();
            int size = value1.length;
            for (int k = 0; k < size; k++) {
                if (value1[k] != value2[k]) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 判断EPC是否在列表中
     *
     * @param epc 索引
     * @return
     */
    public int checkIsExist(String epc) {
        return binarySearch(tempDatas, epc);
    }

    class TagThread extends Thread {
        public void run() {
            UHFTAGInfo uhftagInfo;
            Message msg;
            long time=0;
            while (loopFlag) {
                uhftagInfo = mContext.mReader.readTagFromBuffer();
                if (uhftagInfo != null) {
                    msg = handler.obtainMessage();
                    msg.obj = uhftagInfo;
                    msg.what=1;
                    handler.sendMessage(msg);
                }

                if(System.currentTimeMillis()-time>100){
                    time=System.currentTimeMillis();
                    Log.e("AABB","111");
                    handler.sendEmptyMessage(2);
                }
            }
        }
    }

    private String mergeTidEpc(String tid, String epc,String user) {
        if (!TextUtils.isEmpty(user)) {
            return "TID:" + tid + "\nEPC:" + epc+ "\nUser:" + user;
        }else if (!TextUtils.isEmpty(tid) && !tid.equals("0000000000000000") && !tid.equals("000000000000000000000000")) {
            return "TID:" + tid + "\nEPC:" + epc;
        } else {
            return epc;
        }
    }

    @Override
    public void myOnKeyDwon() {
        readTag();
    }

}
