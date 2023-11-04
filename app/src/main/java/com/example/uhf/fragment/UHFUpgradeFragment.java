package com.example.uhf.fragment;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.uhf.R;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.filebrowser.FileManagerActivity;
import com.example.uhf.tools.StringUtils;
import com.example.uhf.tools.UIHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class UHFUpgradeFragment extends KeyDownFragment {
    String TAG = "UHFUpgradeActivity_zp";
    private Button btnBrowser;
    private Button btnUpgrade;
    private EditText etPath;
    private String version = "";
    private TextView tvMsg;
    private GetPathBroadcastReceiver mGetpathReceiver = null;
    private UHFMainActivity mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_uhfupgrade, container, false);
        init(view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mGetpathReceiver != null) {
            getActivity().unregisterReceiver(mGetpathReceiver);
        }
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = (UHFMainActivity) getActivity();
    }
    private void init(View view) {
        btnBrowser = (Button) view.findViewById(R.id.btnBrowser);
        btnUpgrade = (Button) view.findViewById(R.id.btnUpgrade);
        etPath = (EditText) view.findViewById(R.id.et_file);
        TextView  tvResult = (TextView) view.findViewById(R.id.tvResult);
        tvMsg = (TextView) view.findViewById(R.id.tvMsg);

        btnBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FileManagerActivity.class);
                getActivity().startActivity(intent);
            }
        });
        btnUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filePath = etPath.getText().toString();
                if (StringUtils.isEmpty(filePath)) {
                    UIHelper.ToastMessage(getActivity(), R.string.up_msg_sel_file);
                    return;
                }
                if (filePath.toLowerCase().lastIndexOf(".bin") < 0) {
                    UIHelper.ToastMessage(getActivity(), R.string.msg_file_format_err);
                    return;
                }
                tvMsg.setText("");
                new UpgradeTask(filePath).execute();
            }
        });
        mGetpathReceiver = new GetPathBroadcastReceiver();
        IntentFilter filterPosition = new IntentFilter();
        filterPosition.addAction(FileManagerActivity.Path_ACTION);
        getActivity().registerReceiver(mGetpathReceiver, filterPosition);
    }
    public class UpgradeTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;
        String mFileName;

        public UpgradeTask(String filename) {
            mFileName = filename;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mypDialog.setMessage((values[0] * 100 / values[1]) + "% " + getActivity().getString(R.string.app_msg_Upgrade));
            tvMsg.setText("version:" + version);
        }
        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            boolean result = false;
            File uFile = new File(mFileName);
            if (!uFile.exists()) {
                return false;
            }
            long uFileSize = uFile.length();
            Log.d(TAG, "uFileSize=" + uFileSize);
            int packageCount = (int) (uFileSize / 64);
            Log.d(TAG, "packageCount=" + packageCount);
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(mFileName, "r");
            } catch (FileNotFoundException e) {
            }
            if (raf == null) {
                return false;
            }
            /*
            Log.d(TAG, "UHF上电");
            if (uhf != null && !uhf.isPowerOn()) {
                if (!uhf.init()) {
                    Log.d(TAG, "UHF上电失败");
                    return false;
                }
            }
            */
            version = mContext.mReader.getVersion();//获取版本号
            Log.d(TAG, "UHF uhfJump2Boot");
            if (!mContext.mReader.uhfJump2Boot()) {
                Log.d(TAG, "uhfJump2Boot 失败");
                return false;
            }
            sleep(2000);
            Log.d(TAG, "UHF uhfStartUpdate");
            if (!mContext.mReader.uhfStartUpdate()) {
                Log.d(TAG, "uhfStartUpdate 失败");
                return false;
            }
            int pakeSize = 64;
            byte[] currData = new byte[(int) uFileSize];
            for (int k = 0; k < packageCount; k++) {
                int index = k * pakeSize;
                try {
                    int rsize = raf.read(currData, index, pakeSize);
                    Log.d(TAG, "beginPack=" + index + " endPack=" + (index + pakeSize - 1) + " rsize=" + rsize);
                } catch (IOException e) {
                    stop();
                    return false;
                }

                if (mContext.mReader.uhfUpdating(Arrays.copyOfRange(currData, index, index + pakeSize))) {
                    result = true;
                    publishProgress(index + pakeSize, (int) uFileSize);
                    //sleep(10);
                } else {
                    Log.d(TAG, "uhfUpdating 失败");
                    stop();
                    return false;
                }

            }
            if (uFileSize % pakeSize != 0) {
                int index = packageCount * pakeSize;
                int len = (int) (uFileSize % pakeSize);
                try {
                    int rsize = raf.read(currData, index, len);
                    Log.d(TAG, "beginPack=" + index + " countPack=" + len + " rsize=" + rsize);
                } catch (IOException e) {
                    stop();
                    return false;
                }
                if (mContext.mReader.uhfUpdating(Arrays.copyOfRange(currData, index, index + len))) {
                    result = true;
                    publishProgress((int) uFileSize, (int) uFileSize);
                } else {
                    Log.d(TAG, "uhfUpdating 失败");
                    stop();
                    return false;
                }
            }
            stop();
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mypDialog.cancel();
            etPath.setText("");
            if (!result) {
                UIHelper.ToastMessage(getActivity(), R.string.uhf_msg_upgrade_fail);
                tvMsg.setText(R.string.uhf_msg_upgrade_fail);
                tvMsg.setTextColor(Color.RED);
            } else {
                UIHelper.ToastMessage(getActivity(), R.string.uhf_msg_upgrade_succ);
                tvMsg.setText(R.string.uhf_msg_upgrade_succ);
                tvMsg.setTextColor(Color.GREEN);
            }
            sleep(2000);
            tvMsg.setText(tvMsg.getText() + " version=" + mContext.mReader.getVersion());
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mypDialog = new ProgressDialog(getActivity());
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage(getActivity().getString(R.string.app_msg_Upgrade));
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.setCancelable(false);
            mypDialog.show();
        }


        private void sleep(int time) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void stop() {
            Log.d(TAG, "UHF uhfStopUpdate");
            if (!mContext.mReader.uhfStopUpdate())
                Log.d(TAG, "uhfStopUpdate 失败");
            sleep(2000);
        }
    }

    private class GetPathBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(FileManagerActivity.Path_ACTION)) {
                String strFilePath = intent.getStringExtra(FileManagerActivity.Path_Key);
                etPath.setText(strFilePath);
            }
        }
    }
    private void ss(){

    }
}
