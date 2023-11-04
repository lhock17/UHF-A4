package com.example.uhf.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.example.uhf.R;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.StringUtils;
import com.example.uhf.tools.UIHelper;
import com.rscja.deviceapi.RFIDWithUHFA8;

public class UHFKillFragment extends KeyDownFragment implements OnClickListener {

    private static final String TAG = "UHFKillFragment";

    private UHFMainActivity mContext;

    EditText EtAccessPwd_Write;
    Button btnKill;

    LinearLayout llFilter;
    CheckBox cb_filter;
    EditText etPtr_filter, etLen_filter, etData_filter;
    RadioButton rbEPC_filter, rbTID_filter, rbUser_filter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.uhf_kill_fragment, container, false);
        inits(view);
        return view;
    }

    private void inits(View parent) {
        EtAccessPwd_Write = (EditText) parent.findViewById(R.id.EtAccessPwd_Write);
        btnKill = (Button) parent.findViewById(R.id.btnKill);

        llFilter = (LinearLayout) parent.findViewById(R.id.llFilter);
        cb_filter = (CheckBox) parent.findViewById(R.id.cb_filter);
        etPtr_filter = (EditText) parent.findViewById(R.id.etPtr_filter);
        etLen_filter = (EditText) parent.findViewById(R.id.etLen_filter);
        etData_filter = (EditText) parent.findViewById(R.id.etData_filter);
        rbEPC_filter = (RadioButton) parent.findViewById(R.id.rbEPC_filter);
        rbTID_filter = (RadioButton) parent.findViewById(R.id.rbTID_filter);
        rbUser_filter = (RadioButton) parent.findViewById(R.id.rbUser_filter);

        cb_filter.setOnClickListener(this);
        rbEPC_filter.setOnClickListener(this);
        rbTID_filter.setOnClickListener(this);
        rbUser_filter.setOnClickListener(this);
        llFilter.setOnClickListener(this);
        etData_filter.addTextChangedListener(new CustomTextWatcher(etData_filter));
        cb_filter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onFilterCheck(buttonView);
            }
        });
        EtAccessPwd_Write.requestFocus();
        btnKill.setOnClickListener(new btnKillOnClickListener());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(null);

        mContext = (UHFMainActivity) getActivity();
    }

    private void onFilterCheck(CompoundButton buttonView) {
        if (buttonView.isChecked()) {
            llFilter.setVisibility(View.VISIBLE);
        } else {
            llFilter.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cb_filter:
                onFilterCheck(cb_filter);
                break;
            case R.id.rbEPC_filter:
                etPtr_filter.setText("32");
                break;
            case R.id.rbTID_filter:
                etPtr_filter.setText("0");
                break;
            case R.id.rbUser_filter:
                etPtr_filter.setText("0");
                break;
        }
    }

    public class btnKillOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            String strPWD = EtAccessPwd_Write.getText().toString().trim();// 访问密码

            if (StringUtils.isNotEmpty(strPWD)) {
                if (strPWD.length() != 8) {
                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_addr_must_len8);
                    return;
                } else if (!mContext.vailHexInput(strPWD)) {
                    UIHelper.ToastMessage(mContext, R.string.rfid_mgs_error_nohex);
                    return;
                }
            } else {
                UIHelper.ToastMessage(mContext, R.string.rfid_mgs_error_nopwd);
                return;
            }

            if (cb_filter.isChecked()) { // 过滤
                //----------------过滤----------------------------
                int filterPtr = StringUtils.toInt(etPtr_filter.getText().toString(), -1);
                int filterCnt = StringUtils.toInt(etLen_filter.getText().toString(), -1);
                String filterData = etData_filter.getText().toString();
                int filterBank = RFIDWithUHFA8.Bank_EPC;
                if (filterPtr < 0) {
                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_filter_addr_must_decimal);
                    return;
                }
                if (filterCnt < 0) {
                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_filter_len_must_decimal);
                    return;
                }
                if (filterCnt > 0) {
                    if (filterData == null) filterData = "";
                    int flag = filterCnt / 8 + (filterCnt % 8 == 0 ? 0 : 1);
                    int dataLen = filterData.replace(" ", "").length() / 2;
                    if (dataLen < flag) {
                        UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_filter_fail2);
                        return;
                    }
                }
                if (rbEPC_filter.isChecked()) {
                    filterBank = RFIDWithUHFA8.Bank_EPC;
                } else if (rbTID_filter.isChecked()) {
                    filterBank =RFIDWithUHFA8.Bank_TID;
                } else if (rbUser_filter.isChecked()) {
                    filterBank = RFIDWithUHFA8.Bank_USER;
                }

                //-----------------------------------------------------
                if (mContext.mReader.killTag(strPWD, filterBank, filterPtr, filterCnt, filterData)) {
                    UIHelper.ToastMessage(mContext, R.string.rfid_mgs_kill_succ);
                    mContext.playSound();
                } else {
                    UIHelper.ToastMessage(mContext, R.string.rfid_mgs_kill_fail);
                    mContext.playSound(2);
                }
            } else {
                if (mContext.mReader.killTag(strPWD)) {
                    UIHelper.ToastMessage(mContext, getString(R.string.rfid_mgs_kill_succ));
                    mContext.playSound();
                } else {
                    UIHelper.ToastMessage(mContext, R.string.rfid_mgs_kill_fail);
                    mContext.playSound(2);
                }
            }
        }
    }

    public class CustomTextWatcher implements TextWatcher {

        private static final String TAG = "CustomTextWatcher";
        private boolean mInvalid;
        private int mSelection;
        private String mLastText;

        /**
         * The editText to edit text.
         */
        private EditText mEditText;

        /**
         * Creates an instance of <code>CustomTextWatcher</code>.
         *
         * @param editText the editText to edit text.
         */
        public CustomTextWatcher(EditText editText) {
            super();
            mInvalid = false;
            mLastText = "";
            this.mEditText = editText;
            this.mEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            try {
                String temp = charSequence.toString();
                // Set selection.
                if (mLastText.equals(temp)) {
                    if (mInvalid) {
                        mSelection -= 1;
                    } else {
                        if ((mSelection >= 1) && (temp.length() > mSelection - 1)
                                && (temp.charAt(mSelection - 1)) == ' ') {
                            mSelection += 1;
                        }
                    }
                    int length = mLastText.length();
                    if (mSelection > length) {

                        mEditText.setSelection(length);
                    } else {

                        mEditText.setSelection(mSelection);
                    }
                    mInvalid = false;
                    return;
                }
                mSelection = start;

                // Delete operation.
                if (count == 0) {
                    if ((mSelection >= 1) && (temp.length() > mSelection - 1)
                            && (temp.charAt(mSelection - 1)) == ' ') {
                        mSelection -= 1;
                    }
                    return;
                }

                // Input operation.
                mSelection += count;
                char[] lastChar = (temp.substring(start, start + count)).toCharArray();
                int mid = lastChar[0];
                if (mid >= 48 && mid <= 57) {
                    /* 1-9. */
                } else if (mid >= 65 && mid <= 70) {
                    /* A-F. */
                } else if (mid >= 97 && mid <= 102) {
                    /* a-f. */
                } else {
                    /* Invalid input. */
                    mInvalid = true;
                    temp = temp.substring(0, start) + temp.substring(start + count, temp.length());
                    mEditText.setText(temp);
                    mEditText.setSelection(mEditText.getText().length());
                    return;
                }
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }
}
