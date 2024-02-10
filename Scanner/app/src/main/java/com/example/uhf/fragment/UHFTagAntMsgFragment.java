package com.example.uhf.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.uhf.R;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.FileTool;
import com.example.uhf.tools.StringUtils;
import com.example.uhf.tools.UIHelper;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UHFTagAntMsgFragment extends KeyDownFragment implements View.OnClickListener {

    private String TAG = "UHFTagAntMsgFragment";

    private UHFMainActivity mContext;
    private Button BtInventory, BtClear;
    private TextView tv_count, tv_totalNum;
    private ListView LvTags;
    private ArrayList<HashMap<String, String>> tagList;
    private SimpleAdapter adapter;

    private ImageView imageView1, imageView2, imageView3, imageView4;

    private List<String> tempDatas;

    private Map<String, String> demonstrationMap = new HashMap<>();
    private static final String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    private static final String fileName = rootPath + "db.txt";

    private int totalNum;
    private boolean loopFlag;

    private static final int WHAT_READ = 1;
    private static final int WHAT_DEMONSTRATION = 2;
    private static final int WHAT_CLEAR_IMAGE = 3;

    private long currDemonstrationTime = System.currentTimeMillis();
    private static final int displayTime = 200;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_READ:
                    UHFTAGInfo info = (UHFTAGInfo) msg.obj;
                    addDataToList(mergeTidEpc(info.getTid(), info.getEPC()), info.getRssi(), info.getAnt());
//                    mContext.playSound();
                    break;
                case WHAT_DEMONSTRATION:
                    Bitmap bmp = (Bitmap) msg.obj;
                    int ant = msg.arg1;
                    if(bmp != null) {
                        currDemonstrationTime = System.currentTimeMillis();
                        if (ant == 1) {
                            imageView1.setImageBitmap(bmp);
                        } else if (ant == 2) {
                            imageView2.setImageBitmap(bmp);
                        } else if (ant == 3) {
                            imageView3.setImageBitmap(bmp);
                        } else if (ant == 4) {
                            imageView4.setImageBitmap(bmp);
                        }
                    }
                    break;
                case WHAT_CLEAR_IMAGE:
                    clearImage();
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "UHFTagAntMsgFragment.onCreateView");
        View view = inflater.inflate(R.layout.uhf_tag_ant_msg_fragment, container, false);
        inits(view);
        return view;
    }

    private void inits(View view) {
        BtInventory = view.findViewById(R.id.BtInventory);
        BtInventory.setOnClickListener(this);
        BtClear = view.findViewById(R.id.BtClear);
        BtClear.setOnClickListener(this);

        tv_count = (TextView) view.findViewById(R.id.tv_count);
        tv_totalNum = (TextView) view.findViewById(R.id.tv_totalNum);

        tagList = new ArrayList<>();
        tempDatas = new ArrayList<>();
        LvTags = (ListView) view.findViewById(R.id.LvTags);
        adapter = new SimpleAdapter(getContext(), tagList, R.layout.listtag_items,
                new String[]{UHFReadTagFragment.TAG_EPC, UHFReadTagFragment.TAG_LEN, UHFReadTagFragment.TAG_COUNT, UHFReadTagFragment.TAG_RSSI, UHFReadTagFragment.TAG_ANT},
                new int[]{R.id.TvTagUii, R.id.TvTagLen, R.id.TvTagCount, R.id.TvTagRssi, R.id.TvAnt});
        LvTags.setAdapter(adapter);

        imageView1 = view.findViewById(R.id.imageView1);
        imageView2 = view.findViewById(R.id.imageView2);
        imageView3 = view.findViewById(R.id.imageView3);
        imageView4 = view.findViewById(R.id.imageView4);

        List<String> list = FileTool.readFileByLines(fileName);
        if(list != null) {
            for (String src : list) {
                String[] arr = src.split(",");
                if(arr != null && arr.length >= 2) {
                    Log.e(TAG, "demonstrationMap>>epc=" + arr[0] + ", path=" + arr[1]);
                    if(arr[0] != null) {
                        demonstrationMap.put(arr[0], arr[1]);
                    }
                }
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = (UHFMainActivity) getActivity();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.BtInventory:
                readTag();
                break;
            case R.id.BtClear:
                clearData();
                break;
        }
    }

    private void readTag() {
        if (BtInventory.getText().equals(getString(R.string.btInventory))) { // 识别标签
            if (mContext.mReader.startInventoryTag()) {
                BtInventory.setText(getString(R.string.title_stop_Inventory));
                loopFlag = true;
                setViewEnabled(false);
                new TagThread().start();
            } else {
                mContext.mReader.stopInventory();
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_open_fail);
            }
        } else {// 停止识别
            stopInventory();
        }
    }

    private void setViewEnabled(boolean enabled) {
        BtClear.setEnabled(enabled);
    }

    private void demonstration(String epcAndTid, String ant) {
        try {
            if (!TextUtils.isEmpty(epcAndTid) && !TextUtils.isEmpty(ant)) {
                String pathName = demonstrationMap.get(epcAndTid);
                if(pathName.startsWith(File.separator)) {
                    pathName = pathName.substring(1, pathName.length());
                }

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true; // 这个inJustDecodeBounds很重要,不加载到内存
                BitmapFactory.decodeFile(rootPath + pathName, options);

                Log.e(TAG, "width=" + imageView1.getWidth() + ", height=" + imageView1.getHeight());
                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, imageView1.getWidth(), imageView1.getHeight());

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                //避免出现内存溢出的情况，进行相应的属性设置。
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inDither = true;
                Bitmap bmp = BitmapFactory.decodeFile(rootPath + pathName, options);

                Message msg = handler.obtainMessage(WHAT_DEMONSTRATION, bmp);
                msg.arg1 = Integer.valueOf(ant.trim());
                handler.sendMessage(msg);
            }
        } catch (Exception e) {
            Log.e(TAG, "demonstration>>>error:" + e.getMessage());
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options opt, int width, int height) {
        // 获取到这个图片的原始宽度和高度
        int picWidth = opt.outWidth;
        int picHeight = opt.outHeight;

        // isSampleSize是表示对图片的缩放程度，比如值为2图片的宽度和高度都变为以前的1/2
        int inSampleSize = 1;
        // 根据屏的大小和图片大小计算出缩放比例
        if (picWidth > picHeight) {
            if (picWidth > width)
                inSampleSize = picWidth / width;
        } else {
            if (picHeight > height)
                inSampleSize = picHeight / height;
        }

        return inSampleSize;
    }

    /**
     * 添加EPC到列表中
     *
     * @param
     */
    private void addDataToList(String epcAndTid, String rssi, String ant) {
        if (StringUtils.isNotEmpty(epcAndTid)) {
            int index = checkIsExist(epcAndTid);
            HashMap<String, String> map = new HashMap<>();
            map.put(UHFReadTagFragment.TAG_EPC, epcAndTid);
            map.put(UHFReadTagFragment.TAG_COUNT, String.valueOf(1));
            map.put(UHFReadTagFragment.TAG_RSSI, rssi);
            map.put(UHFReadTagFragment.TAG_ANT, ant);
            if (index == -1) {
                tagList.add(map);
                tempDatas.add(epcAndTid);
                tv_count.setText(String.valueOf(adapter.getCount()));
            } else /*if(index < tagList.size())*/ {
                int tagCount = Integer.parseInt(tagList.get(index).get(UHFReadTagFragment.TAG_COUNT), 10) + 1;
                map.put(UHFReadTagFragment.TAG_COUNT, String.valueOf(tagCount));
                tagList.set(index, map);
            }
            tv_totalNum.setText(String.valueOf(++totalNum));
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 停止识别
     */
    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            setViewEnabled(true);
            if (mContext.mReader.stopInventory()) {
                BtInventory.setText(getString(R.string.btInventory));
            } else {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_stop_fail);
            }
        }

        clearImage();
    }

    private void clearData() {
        totalNum = 0;
        tv_count.setText("0");
        tv_totalNum.setText("0");

        tagList.clear();
        tempDatas.clear();
        adapter.notifyDataSetChanged();

        clearImage();
    }

    private void clearImage() {
        imageView1.setImageBitmap(null);
        imageView2.setImageBitmap(null);
        imageView3.setImageBitmap(null);
        imageView4.setImageBitmap(null);
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
                k++;
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
            while (loopFlag) {
                uhftagInfo = mContext.mReader.readTagFromBuffer();
                if (uhftagInfo != null) {
                    msg = handler.obtainMessage(WHAT_READ, uhftagInfo);
                    handler.sendMessage(msg);

                    demonstration(mergeTidEpc(uhftagInfo.getTid(), uhftagInfo.getEPC()), uhftagInfo.getAnt()); // 演示展示
                }

                // 如果规定时间内没读到，则清除图片展示
                if(System.currentTimeMillis() - currDemonstrationTime > displayTime) {
                    handler.sendEmptyMessage(WHAT_CLEAR_IMAGE);
                }
            }
        }
    }

    private String mergeTidEpc(String tid, String epc) {
        if (!TextUtils.isEmpty(tid) && !tid.equals("0000000000000000") && !tid.equals("000000000000000000000000")) {
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
