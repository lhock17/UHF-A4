package com.example.uhf.fragment;


import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.uhf.R;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.UIHelper;
import com.lidroid.xutils.util.LogUtils;
import com.rscja.CWDeviceInfo;
import com.rscja.deviceapi.entity.AntennaPowerEntity;
import com.rscja.deviceapi.entity.AntennaState;
import com.rscja.deviceapi.entity.Gen2Entity;
import com.rscja.deviceapi.enums.AntennaEnum;
import com.rscja.utility.StringUtility;

import java.util.ArrayList;
import java.util.List;

public class UHFSetFragment extends KeyDownFragment implements OnClickListener {
    private static final int GET_FRE = 1;
    private static final int GET_PWM = 2;
    private static final int GET_LINK_PARAMS = 3;
    private static final int GET_ANT = 4;
    private static final int GET_POWER = 5;
    private static final int GET_VERSION = 6;
    private static final int GET_CW = 7;
    Spinner spSessionID, spInventoried, SpinnerANT;
    EditText etAntWorkTime;
    Button btnSetSession, btnGetSession;
    private String TAG = "UHFSetFragment";
    private UHFMainActivity mContext;
    private Button btnSetFre;
    private Button btnGetFre;
    private Spinner spMode;
    private LinearLayout ll_freHop;
    private Spinner spPower;
    private EditText et_worktime;
    private EditText et_waittime;
    private Button btnWorkWait;
    private Spinner spFreHop; //频点列表
    private Button btnSetFreHop; //设置频点设置
    private Button btnGetWait; //获取空占比
    private Button btnSetAgreement; //设置协议
    private Spinner SpinnerAgreement; //协议列表
    private Button btnSetLinkParams; //设置链路参数
    private Button btnGetLinkParams; //获取链路参数
    private Spinner splinkParams; //链路参数列表
    private CheckBox cbTagFocus; //打开tagFocus
    private CheckBox cbFastID; //打开FastID
    private CheckBox cbEPC_TID; //打开EPC+TID
    private CheckBox cbAntAll;
    private RadioButton rb_Brazil; // 巴西频点
    private RadioButton rb_America; // 美国频点
    private RadioButton rb_Others; //其他频点
    private ArrayAdapter adapter; //频点列表适配器
    private DisplayMetrics metrics;
    private AlertDialog dialog;
    private Button btSetAnt, btGetAnt;
    private CheckBox cbAnt1, cbAnt2, cbAnt3, cbAnt4, cbAnt5, cbAnt6, cbAnt7, cbAnt8;
    private CheckBox cbContinuousWave, cbBuzzer;
    private String[] arrayMode;
    private Button btnOutput3On, btnOutput3Off, btnOutput4On, btnOutput4Off, btnInputStatus;
    private Button btnSetPower;
    private Button btnGetPower;
    private LinearLayout ll_a8, ll_antA8;
    private Spinner spPower1, spPower2, spPower3, spPower4, spPower5, spPower6, spPower7, spPower8;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_FRE:
                    int idx = (int) msg.obj;
                    if (idx != -1) {
                        int count = spMode.getCount();
                        spMode.setSelection(idx > count - 1 ? count - 1 : idx);
                        if (msg.arg1 == 1)
                            UIHelper.ToastMessage(mContext, R.string.uhf_msg_read_frequency_succ);
                    } else if (msg.arg1 == 1) {
                        UIHelper.ToastMessage(mContext, R.string.uhf_msg_read_frequency_fail);
                    }
                    break;
                case GET_PWM:
                    int[] pwm = (int[]) msg.obj;
                    if (pwm == null || pwm.length < 2) {
                        if (msg.arg1 == 1)
                            UIHelper.ToastMessage(mContext, R.string.uhf_msg_read_pwm_fail);
                        return;
                    }
                    et_worktime.setText(pwm[0] + "");
                    et_waittime.setText(pwm[1] + "");
                    et_worktime.setSelection(et_worktime.getText().toString().length());
                    et_waittime.setSelection(et_waittime.getText().toString().length());
                    break;
                case GET_LINK_PARAMS:
                    int index = (int) msg.obj;
                    Log.e(TAG, "getLinkParams=" + index);
                    if (index != -1) {
                        splinkParams.setSelection(index);
                        if (msg.arg1 == 1)
                            UIHelper.ToastMessage(mContext, R.string.uhf_msg_get_para_succ);
                    } else if (msg.arg1 == 1) {
                        UIHelper.ToastMessage(mContext, R.string.uhf_msg_get_para_fail);
                    }
                    break;
                case GET_ANT:

                    if (msg.obj == null) {
                        if (msg.arg1 == 1)
                            UIHelper.ToastMessage(mContext, R.string.uhf_msg_get_para_fail);
                    } else {
                        List<AntennaState> ant = (List<AntennaState>) msg.obj;
                        for (int k = 0; k < ant.size(); k++) {
                            AntennaState antennaState = ant.get(k);
                            switch (antennaState.getAntennaName()) {
                                case ANT1:
                                    cbAnt1.setChecked(antennaState.isEnable());
                                    break;
                                case ANT2:
                                    cbAnt2.setChecked(antennaState.isEnable());
                                    break;
                                case ANT3:
                                    cbAnt3.setChecked(antennaState.isEnable());
                                    break;
                                case ANT4:
                                    cbAnt4.setChecked(antennaState.isEnable());
                                    break;
                                case ANT5:
                                    cbAnt5.setChecked(antennaState.isEnable());
                                    break;
                                case ANT6:
                                    cbAnt6.setChecked(antennaState.isEnable());
                                    break;
                                case ANT7:
                                    cbAnt7.setChecked(antennaState.isEnable());
                                    break;
                                case ANT8:
                                    cbAnt8.setChecked(antennaState.isEnable());
                                    break;
                            }
                        }
                        if (msg.arg1 == 1)
                            UIHelper.ToastMessage(mContext, R.string.uhf_msg_get_para_succ);
                    }
                    break;
                case GET_POWER:
//                    int iPower = (int) msg.obj;
//                    if (iPower > -1) {
//                        int position = iPower - 5;
//                        int count = spPower.getCount();
//                        spPower.setSelection(position > count - 1 ? count - 1 : position);
//                        if (msg.arg1 == 1)
//                            UIHelper.ToastMessage(mContext, R.string.uhf_msg_read_power_succ);
//                    } else {
//                        if (msg.arg1 == 1)
//                            UIHelper.ToastMessage(mContext, R.string.uhf_msg_read_power_fail);
//                    }
//                    break;
                    List<AntennaPowerEntity> powerList = (List<AntennaPowerEntity>) msg.obj;
                    //     Log.i("powerList", "powerList=  " + powerList.size());
                    if (powerList != null) {
                        for (AntennaPowerEntity entity : powerList) {
                            if (entity.getAnt() == AntennaEnum.ANT1) {
                                int ant1Power = entity.getPower();
                                spPower1.setSelection(ant1Power - 5);
                            } else if (entity.getAnt() == AntennaEnum.ANT2) {
                                int ant2Power = entity.getPower();
                                spPower2.setSelection(ant2Power - 5);
                            } else if (entity.getAnt() == AntennaEnum.ANT3) {
                                int ant3Power = entity.getPower();
                                spPower3.setSelection(ant3Power - 5);
                            } else if (entity.getAnt() == AntennaEnum.ANT4) {
                                int ant4Power = entity.getPower();
                                spPower4.setSelection(ant4Power - 5);
                            } else if (entity.getAnt() == AntennaEnum.ANT5) {
                                int ant5Power = entity.getPower();
                                spPower5.setSelection(ant5Power - 5);
                            } else if (entity.getAnt() == AntennaEnum.ANT6) {
                                int ant6Power = entity.getPower();
                                spPower6.setSelection(ant6Power - 5);
                            } else if (entity.getAnt() == AntennaEnum.ANT7) {
                                int ant7Power = entity.getPower();
                                spPower7.setSelection(ant7Power - 5);
                            } else if (entity.getAnt() == AntennaEnum.ANT8) {
                                int ant8Power = entity.getPower();
                                spPower8.setSelection(ant8Power - 5);
                            }
                        }
                        if (msg.arg1 == 1)
                            UIHelper.ToastMessage(mContext, R.string.uhf_msg_read_power_succ);
                    } else {
                        if (msg.arg1 == 1)
                            UIHelper.ToastMessage(mContext, R.string.uhf_msg_read_power_fail);
                    }
                    break;
                case GET_VERSION:
                    int arrPow = (int) msg.obj;
                    ArrayAdapter adapter = ArrayAdapter.createFromResource(getContext(), arrPow, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spPower.setAdapter(adapter);
                    break;
                case GET_CW:
                    int flag = (int) msg.obj;
                    if (flag == 1) {
                        cbContinuousWave.setChecked(true);
                        if (msg.arg1 == 1)
                            UIHelper.ToastMessage(getContext(), R.string.uhf_msg_get_para_succ);
                    } else if (flag == 0) {
                        cbContinuousWave.setChecked(false);
                        if (msg.arg1 == 1)
                            UIHelper.ToastMessage(getContext(), R.string.uhf_msg_get_para_succ);
                    } else {
                        if (msg.arg1 == 1)
                            UIHelper.ToastMessage(getContext(), R.string.uhf_msg_get_para_fail);
                    }
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_uhfset, container, false);

        inits(root);
        return root;
    }

    private void inits(View view) {
        initA8Views(view);

        ll_a8 = view.findViewById(R.id.ll_a8);
        ll_antA8 = view.findViewById(R.id.ll_antA8);

        spSessionID = view.findViewById(R.id.spSessionID);
        spInventoried = view.findViewById(R.id.spInventoried);
        btnGetSession = view.findViewById(R.id.btnGetSession);
        btnSetSession = view.findViewById(R.id.btnSetSession);

        btnGetSession.setOnClickListener(this);
        btnSetSession.setOnClickListener(this);

        btnOutput3On = (Button) view.findViewById(R.id.btnOutput3On);
        btnOutput3Off = (Button) view.findViewById(R.id.btnOutput3Off);
        btnOutput4On = (Button) view.findViewById(R.id.btnOutput4On);
        btnOutput4Off = (Button) view.findViewById(R.id.btnOutput4Off);
        btnInputStatus = (Button) view.findViewById(R.id.btnInputStatus);

        btnOutput3On.setOnClickListener(this);
        btnOutput3Off.setOnClickListener(this);
        btnOutput4On.setOnClickListener(this);
        btnOutput4Off.setOnClickListener(this);
        btnInputStatus.setOnClickListener(this);

        btnSetFre = (Button) view.findViewById(R.id.BtSetFre);
        btnSetFre.setOnClickListener(new SetFreOnclickListener());
        btnGetFre = (Button) view.findViewById(R.id.BtGetFre);
        btnGetFre.setOnClickListener(new GetFreOnclickListener());

        arrayMode = getResources().getStringArray(R.array.arrayMode);
        spMode = (Spinner) view.findViewById(R.id.SpinnerMode);
//        spMode.setOnItemSelectedListener(new MyOnTouchListener());

//        spPower = view.findViewById(R.id.spPower);

        ll_freHop = view.findViewById(R.id.ll_freHop);

//        btnSetPower = view.findViewById(R.id.btnSetPower);
//        btnSetPower.setOnClickListener(this);
//        btnGetPower = view.findViewById(R.id.btnGetPower);
//        btnGetPower.setOnClickListener(this);

        spPower1 = view.findViewById(R.id.spPower1);
        spPower2 = view.findViewById(R.id.spPower2);
        spPower3 = view.findViewById(R.id.spPower3);
        spPower4 = view.findViewById(R.id.spPower4);
        spPower5 = view.findViewById(R.id.spPower5);
        spPower6 = view.findViewById(R.id.spPower6);
        spPower7 = view.findViewById(R.id.spPower7);
        spPower8 = view.findViewById(R.id.spPower8);


        btnSetPower = view.findViewById(R.id.btnSetPower);
        btnGetPower = view.findViewById(R.id.btnGetPower);
        btnSetPower.setOnClickListener(new SetPowerOnclickListener());
        btnGetPower.setOnClickListener(new GetPowerOnclickListener());

        et_worktime = view.findViewById(R.id.et_worktime);
        et_waittime = view.findViewById(R.id.et_waittime);
        btnWorkWait = view.findViewById(R.id.btnWorkWait);
        btnWorkWait.setOnClickListener(new SetPWMOnclickListener());
        spFreHop = view.findViewById(R.id.spFreHop);
        btnSetFreHop = view.findViewById(R.id.btnSetFreHop);
        btnSetFreHop.setOnClickListener(this);
        btnGetWait = view.findViewById(R.id.btnGetWait);
        btnGetWait.setOnClickListener(this);
        btnSetAgreement = view.findViewById(R.id.btnSetAgreement);
        btnSetAgreement.setOnClickListener(this);
        SpinnerAgreement = view.findViewById(R.id.SpinnerAgreement);
        btnSetLinkParams = view.findViewById(R.id.btnSetLinkParams);
        btnSetLinkParams.setOnClickListener(this);
        btnGetLinkParams = view.findViewById(R.id.btnGetLinkParams);
        btnGetLinkParams.setOnClickListener(this);
        splinkParams = view.findViewById(R.id.splinkParams);

        cbTagFocus = view.findViewById(R.id.cbTagFocus);
        cbFastID = view.findViewById(R.id.cbFastID);
        cbEPC_TID = view.findViewById(R.id.cbEPC_TID);

        cbTagFocus.setOnCheckedChangeListener(new OnMyCheckedChangedListener());
        cbFastID.setOnCheckedChangeListener(new OnMyCheckedChangedListener());
        cbEPC_TID.setOnCheckedChangeListener(new OnMyCheckedChangedListener());


        cbContinuousWave = (CheckBox) view.findViewById(R.id.cbContinuousWave);
        cbContinuousWave.setSelected(false);
        cbContinuousWave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int flag = cbContinuousWave.isChecked() ? 1 : 0;
                setCW(flag, true);
            }
        });

        rb_Brazil = view.findViewById(R.id.rb_Brazil);
        rb_Brazil.setOnClickListener(this);
        rb_America = view.findViewById(R.id.rb_America);
        rb_America.setOnClickListener(this);
        rb_Others = view.findViewById(R.id.rb_Others);
        rb_Others.setOnClickListener(this);
        rb_Brazil.requestFocus();

        String model = CWDeviceInfo.getDeviceInfo().getModelAndCpu();
        Log.e("TAG", "model:    " + model);
        if ("A4_RK_3568_110".equals(model) || (CWDeviceInfo.getDeviceInfo().getModel().equals("A4") && CWDeviceInfo.getDeviceInfo().getCpuType().equals("8953"))) {
            ll_a8.setVisibility(View.GONE);
            ll_antA8.setVisibility(View.GONE);
        }
    }

    private void initA8Views(View view) {

        cbBuzzer = (CheckBox) view.findViewById(R.id.cbBuzzer);
        cbBuzzer.setOnClickListener(this);

        btSetAnt = (Button) view.findViewById(R.id.btSetAnt);
        btGetAnt = (Button) view.findViewById(R.id.btGetAnt);
        cbAnt1 = (CheckBox) view.findViewById(R.id.cbAnt1);
        cbAnt2 = (CheckBox) view.findViewById(R.id.cbAnt2);
        cbAnt3 = (CheckBox) view.findViewById(R.id.cbAnt3);
        cbAnt4 = (CheckBox) view.findViewById(R.id.cbAnt4);
        cbAnt5 = (CheckBox) view.findViewById(R.id.cbAnt5);
        cbAnt6 = (CheckBox) view.findViewById(R.id.cbAnt6);
        cbAnt7 = (CheckBox) view.findViewById(R.id.cbAnt7);
        cbAnt8 = (CheckBox) view.findViewById(R.id.cbAnt8);
        cbAntAll = (CheckBox) view.findViewById(R.id.cbAntAll);
        btSetAnt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                List<AntennaState> list = new ArrayList<>();
                list.add(new AntennaState(AntennaEnum.ANT1, cbAnt1.isChecked()));
                list.add(new AntennaState(AntennaEnum.ANT2, cbAnt2.isChecked()));
                list.add(new AntennaState(AntennaEnum.ANT3, cbAnt3.isChecked()));
                list.add(new AntennaState(AntennaEnum.ANT4, cbAnt4.isChecked()));
                list.add(new AntennaState(AntennaEnum.ANT5, cbAnt5.isChecked()));
                list.add(new AntennaState(AntennaEnum.ANT6, cbAnt6.isChecked()));
                list.add(new AntennaState(AntennaEnum.ANT7, cbAnt7.isChecked()));
                list.add(new AntennaState(AntennaEnum.ANT8, cbAnt8.isChecked()));
                if (mContext.mReader.setANT(list)) {
                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_succ);
                } else {
                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_fail);
                }
            }
        });
        cbAntAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cbAnt1.setChecked(cbAntAll.isChecked());
                cbAnt2.setChecked(cbAntAll.isChecked());
                cbAnt3.setChecked(cbAntAll.isChecked());
                cbAnt4.setChecked(cbAntAll.isChecked());
                cbAnt5.setChecked(cbAntAll.isChecked());
                cbAnt6.setChecked(cbAntAll.isChecked());
                cbAnt7.setChecked(cbAntAll.isChecked());
                cbAnt8.setChecked(cbAntAll.isChecked());
            }
        });
        btGetAnt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getAnt(true);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        /*
           开启子线程获取参数，Handler更新UI,防止fragment打开卡顿
		 */
//        new Thread() {
//            @Override
//            public void run() {
//                getVersion();
        getFre(false);
//                getLinkParams(false);
        getAnt(false);
        getCW(false);
        getSession();
        //      }
        //   }.start();
    }

    private void getAnt(boolean isToast) {
        List<AntennaState> ant = mContext.mReader.getANT();
        Message msg = mHandler.obtainMessage(GET_ANT, ant);
        msg.arg1 = isToast ? 1 : 0;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = (UHFMainActivity) getActivity();
        cbBuzzer.setChecked(mContext.isBuzzer);
    }

    public void getFre(boolean isToast) {
        int idx = mContext.mReader.getFrequencyMode();
        idx = getModeIndex(idx);
        Message msg = mHandler.obtainMessage(GET_FRE, idx);
        msg.arg1 = isToast ? 1 : 0;
        mHandler.sendMessage(msg);
    }

    public void GetPower(boolean showToast) {
        List<AntennaPowerEntity> powerList = mContext.mReader.getAntennaPower();
        sendMsg(GET_POWER, powerList, showToast ? 1 : 0);
        Log.i(TAG, "OnClick_GetPower() iPower=" + powerList);
    }

//    private void getVersion() {
//        String ver = mContext.mReader.getVersion();
//        Log.e("aaaa","version: "+mContext.mReader.getVersion());
//        int arrPow = R.array.arrayPower;
//        if (ver != null && ver.contains("RLM")) {
//            arrPow = R.array.arrayPower2;
//        }
//        Message msg = mHandler.obtainMessage(GET_VERSION, arrPow);
//        mHandler.sendMessage(msg);
//    }

    /**
     * 获取链路参数
     */
    public void getLinkParams(boolean isToast) {
        int idx = mContext.mReader.getRFLink();
        Message msg = mHandler.obtainMessage(GET_LINK_PARAMS, idx);
        msg.arg1 = isToast ? 1 : 0;
        mHandler.sendMessage(msg);
    }

    /**
     * 设置频点
     *
     * @param value 频点数值
     * @return 是否设置成功
     */
    private boolean setFreHop(float value) {
        boolean result = mContext.mReader.setFreHop(value);
        if (result) {
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_frehop_succ);
        } else {
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_frehop_fail);
//            mContext.playSound(2);
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btnSetFreHop: //设置频点
//			showFrequencyDialog();
                View view = spFreHop.getSelectedView();
                if (view instanceof TextView) {
                    String freHop = ((TextView) view).getText().toString().trim();
                    setFreHop(Float.valueOf(freHop)); //设置频点
                }
                break;
            case R.id.btnGetWait: //获取空占比
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_read_pwm_fail);
                break;
            case R.id.btnSetAgreement: //设置协议
                if (mContext.mReader.setProtocol(SpinnerAgreement.getSelectedItemPosition())) {
                    UIHelper.ToastMessage(mContext, R.string.setAgreement_succ);
                } else {
                    UIHelper.ToastMessage(mContext, R.string.setAgreement_fail);
//                    mContext.playSound(2);
                }
                break;
            case R.id.btnSetLinkParams: //设置链路参数
                int link = splinkParams.getSelectedItemPosition();
                Log.e(TAG, "setLinkParams=" + link);
                if (mContext.mReader.setRFLink(link)) {
                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_succ);
                } else {
                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_fail);
//                    mContext.playSound(2);
                }
                break;
            case R.id.btnGetLinkParams: //获取链路参数
                getLinkParams(true);
                break;
            case R.id.rb_Brazil:
                onClick_rbBRA(v);
                break;
            case R.id.rb_America:
                onClick_rbAmerica(v);
                break;
            case R.id.rb_Others:
                onClick_rbOthers(v);
                break;
            case R.id.cbBuzzer:
                mContext.isBuzzer = cbBuzzer.isChecked();
                break;
            case R.id.btnGetSession:
                if (getSession()) {
                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_get_para_succ);
                } else {
                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_get_para_fail);
                }
                break;
            case R.id.btnSetSession:
                setSession();
                break;
            default:
                break;
        }
    }

    /**
     * 显示频点设置
     */
    private void showFrequencyDialog() {
        if (dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//	        builder.setTitle(R.string.btSetFrequency);
            View view = getActivity().getLayoutInflater().inflate(R.layout.uhf_dialog_frequency, null);
            ListView listView = (ListView) view.findViewById(R.id.listView_frequency);
            ImageView iv = (ImageView) view.findViewById(R.id.iv_dismissDialog);
            iv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                }
            });

            String[] strArr = getResources().getStringArray(R.array.arrayFreHop);
            listView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.item_text1, strArr));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // TODO Auto-generated method stub
                    if (view instanceof TextView) {
                        TextView tv = (TextView) view;
                        float value = Float.valueOf(tv.getText().toString().trim());
                        setFreHop(value); //设置频点
                        dialog.dismiss();
                    }
                }

            });

            builder.setView(view);
            dialog = builder.create();
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);

            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = getWindowWidth() - 100;
            params.height = getWindowHeight() - 200;
            dialog.getWindow().setAttributes(params);
        } else {
            dialog.show();
        }
    }

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public int getWindowWidth() {
        if (metrics == null) {
            metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        }
        return metrics.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public int getWindowHeight() {
        if (metrics == null) {
            metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        }
        return metrics.heightPixels;
    }

    private void updateFreHop(int arrayResId) {
        ArrayAdapter adapter = ArrayAdapter.createFromResource(mContext, arrayResId, android.R.layout.simple_spinner_item);
        spFreHop.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void onClick_rbBRA(View view) {
        updateFreHop(R.array.arrayFreHop_bra);
    }

    public void onClick_rbAmerica(View view) {
        updateFreHop(R.array.arrayFreHop_us);
    }

    public void onClick_rbOthers(View view) {
        updateFreHop(R.array.arrayFreHop);
    }

    private int getMode(String modeName) {
        if (modeName.equals(getString(R.string.China_Standard_840_845MHz))) {
            return 0x01;
        } else if (modeName.equals(getString(R.string.China_Standard_920_925MHz))) {
            return 0x02;
        } else if (modeName.equals(getString(R.string.ETSI_Standard))) {
            return 0x04;
        } else if (modeName.equals(getString(R.string.United_States_Standard))) {
            return 0x08;
        } else if (modeName.equals(getString(R.string.China_Standard_plus))) {
            return 0x08;
        } else if (modeName.equals(getString(R.string.Korea))) {
            return 0x16;
        } else if (modeName.equals(getString(R.string.Japan))) {
            return 0x32;
        } else if (modeName.equals(getString(R.string.South_Africa_915_919MHz))) {
            return 0x33;
        } else if (modeName.equals(getString(R.string.New_Zealand))) {
            return 0x34;
        } else if (modeName.equals(getString(R.string.Morocco))) {
            return 0x80;
        }
        return 0x08;
    }

    private String getModeName(int mode) {
        switch (mode) {
            case 0x01:
                return getString(R.string.China_Standard_840_845MHz);
            case 0x02:
                return getString(R.string.China_Standard_920_925MHz);
            case 0x04:
                return getString(R.string.ETSI_Standard);
            case 0x08:
                return getString(R.string.United_States_Standard);
            case 0x16:
                return getString(R.string.Korea);
            case 0x32:
                return getString(R.string.Japan);
            case 0x33:
                return getString(R.string.South_Africa_915_919MHz);
            case 0x34:
                return getString(R.string.New_Zealand);
            case 0x80:
                return getString(R.string.Morocco);
            default:
                return getString(R.string.United_States_Standard);
        }
    }

    private int getModeIndex(String modeName) {
        for (int i = 0; i < arrayMode.length; i++) {
            if (arrayMode[i].equals(modeName)) {
                return i;
            }
        }
        return 0;
    }

    private int getModeIndex(int mode) {
        return getModeIndex(getModeName(mode));
    }

    /**
     * 设置连续波
     *
     * @param flag
     * @param showToast
     */
    private void setCW(int flag, boolean showToast) {
        boolean res = mContext.mReader.setCW(flag);
        if (showToast) {
            if (res) {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_succ);
            } else {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_fail);
            }
        }
    }

    /**
     * 获取连续波
     *
     * @param showToast
     */
    private void getCW(boolean showToast) {
        int flag = mContext.mReader.getCW();
        sendMsg(GET_CW, flag, showToast ? 1 : 0);
    }

    private void sendMsg(int what, Object obj, int arg1) {
        Message msg = mHandler.obtainMessage(what, obj);
        msg.arg1 = arg1;
        mHandler.sendMessage(msg);
    }

    private boolean getSession() {
        Gen2Entity p = mContext.mReader.getGen2();
        if (p != null) {

            spSessionID.setSelection(p.getQuerySession());
            spInventoried.setSelection(p.getQuerySession());
            return true;
        }
        return false;
    }

    private void setSession() {
        int seesionid = spSessionID.getSelectedItemPosition();
        int inventoried = spInventoried.getSelectedItemPosition();
        if (seesionid < 0 || inventoried < 0) {
            return;
        }
        Gen2Entity entity = mContext.mReader.getGen2();
        if (entity != null) {

            entity.setQuerySession(seesionid);
            entity.setQueryTarget(inventoried);
            if (mContext.mReader.setGen2(entity)) {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_succ);
            } else {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_fail);
            }
        } else {
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_fail);
        }
    }

    /**
     * 工作模式下拉列表点击选中item监听
     */
    public class MyOnTouchListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (spMode.getSelectedItem().toString().equals(getString(R.string.United_States_Standard))) {
                ll_freHop.setVisibility(View.VISIBLE);
                rb_America.setChecked(true); //默认美国频点
            } else {
                ll_freHop.setVisibility(View.GONE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public class SetFreOnclickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            String modeName = spMode.getSelectedItem().toString();
            int mode = getMode(modeName);
            if (mContext.mReader.setFrequencyMode((byte) mode)) {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_frequency_succ);
            } else {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_frequency_fail);
//                mContext.playSound(2);
            }
        }
    }

    public class SetPWMOnclickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_pwm_fail);
//                mContext.playSound(2);
        }
    }

    public class GetFreOnclickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            getFre(true);
        }
    }

    public class OnMyCheckedChangedListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.cbTagFocus:
                    if (mContext.mReader.setTagFocus(isChecked)) {
                        if (isChecked) {
                            cbTagFocus.setText(R.string.tagFocus_off);
                        } else {
                            cbTagFocus.setText(R.string.tagFocus);
                        }
                        UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_succ);
                    } else {
                        UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_fail);
//                        mContext.playSound(2);
                    }
                    break;
                case R.id.cbFastID:
                    if (mContext.mReader.setFastID(isChecked)) {
                        if (isChecked) {
                            cbFastID.setText(R.string.fastID_off);
                        } else {
                            cbFastID.setText(R.string.fastID);
                        }
                        UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_succ);
                    } else {
                        UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_fail);
//                        mContext.playSound(2);
                    }
                    break;
                case R.id.cbEPC_TID:
                    if (isChecked) {
                        cbEPC_TID.setText(R.string.EPC_TID_off);
                        if (mContext.mReader.setEPCAndTIDMode()) {
                            UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_succ);
                        } else {
                            UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_fail);
                        }
                    } else {
                        cbEPC_TID.setText(R.string.EPC_TID);
                        if (mContext.mReader.setEPCMode()) {
                            UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_succ);
                        } else {
                            UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_fail);
                        }
                    }
                    break;
            }
        }
    }

    //设置天线功率

    private class SetPowerOnclickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            int iPower1 = spPower1.getSelectedItemPosition() + 5;
            int iPower2 = spPower2.getSelectedItemPosition() + 5;
            int iPower3 = spPower3.getSelectedItemPosition() + 5;
            int iPower4 = spPower4.getSelectedItemPosition() + 5;
            int iPower5 = spPower5.getSelectedItemPosition() + 5;
            int iPower6 = spPower6.getSelectedItemPosition() + 5;
            int iPower7 = spPower7.getSelectedItemPosition() + 5;
            int iPower8 = spPower8.getSelectedItemPosition() + 5;
            if (!mContext.mReader.setAntennaPower(AntennaEnum.ANT1, iPower1)) {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_power_fail_1);
                return;
            }
            SystemClock.sleep(50);
            if (!mContext.mReader.setAntennaPower(AntennaEnum.ANT2, iPower2)) {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_power_fail_2);
                return;
            }
            SystemClock.sleep(50);
            if (!mContext.mReader.setAntennaPower(AntennaEnum.ANT3, iPower3)) {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_power_fail_3);
                return;
            }
            SystemClock.sleep(50);
            if (!mContext.mReader.setAntennaPower(AntennaEnum.ANT4, iPower4)) {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_power_fail_4);
                return;
            }
            SystemClock.sleep(50);
            if (!mContext.mReader.setAntennaPower(AntennaEnum.ANT5, iPower5)) {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_power_fail_5);
                return;
            }
            SystemClock.sleep(50);
            if (!mContext.mReader.setAntennaPower(AntennaEnum.ANT6, iPower6)) {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_power_fail_6);
                return;
            }
            SystemClock.sleep(50);
            if (!mContext.mReader.setAntennaPower(AntennaEnum.ANT7, iPower7)) {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_power_fail_7);
                return;
            }
            SystemClock.sleep(50);
            if (!mContext.mReader.setAntennaPower(AntennaEnum.ANT8, iPower8)) {
                UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_power_fail_8);
                return;
            }
            UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_power_succ);
        }
    }


    //获取功率
    public class GetPowerOnclickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            GetPower(true);
        }
    }



    /*
    public void Output3OnClick(View view){
        try {
            RFIDWithUHFA8.getInstance().output3On();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        Toast.makeText(mContext,"ok",Toast.LENGTH_SHORT).show();
    }
    public void Output3OffClick(View view){
        try {
            RFIDWithUHFA8.getInstance().output3Off();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        Toast.makeText(mContext,"ok",Toast.LENGTH_SHORT).show();
    }
    public void Output4OnClick(View view){
        try {
            RFIDWithUHFA8.getInstance().output4On();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        Toast.makeText(mContext,"ok",Toast.LENGTH_SHORT).show();
    }
    public void Output4OffClick(View view){
        try {
            RFIDWithUHFA8.getInstance().output4Off();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        Toast.makeText(mContext,"ok",Toast.LENGTH_SHORT).show();
    }
    public void InputClick(View view){
        try {
            int[] Data=RFIDWithUHFA8.getInstance().inputStatus();
            if(Data==null){
                Toast.makeText(mContext,"获取失败",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(mContext,"input1: "+Data[0] +" \r\ninput2: "+Data[1],Toast.LENGTH_SHORT).show();
            }
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

    }
    */
}
