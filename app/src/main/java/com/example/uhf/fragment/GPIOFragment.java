package com.example.uhf.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.uhf.R;

import com.rscja.deviceapi.RFIDWithUHFA4;
import com.rscja.deviceapi.RFIDWithUHFA8;

import com.rscja.deviceapi.entity.GPIStateEntity;
import com.rscja.deviceapi.exception.ConfigurationException;

import java.util.List;


public class GPIOFragment extends KeyDownFragment implements View.OnClickListener {


    RFIDWithUHFA4 rfidWithUHFA4=null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            rfidWithUHFA4=RFIDWithUHFA4.getInstance();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_gpio, container, false);
        view.findViewById(R.id.btnOutput3On).setOnClickListener(this);
        view.findViewById(R.id.btnOutput3Off).setOnClickListener(this);
        view.findViewById(R.id.btnOutput4On).setOnClickListener(this);
        view.findViewById(R.id.btnOutput4Off).setOnClickListener(this);
        view.findViewById(R.id.btnInputStatus).setOnClickListener(this);


        view.findViewById(R.id.btnOutputOptoCoupler3On).setOnClickListener(this);
        view.findViewById(R.id.btnoutputOptoCoupler3Off).setOnClickListener(this);
        view.findViewById(R.id.btnOutputOptoCoupler4On).setOnClickListener(this);
        view.findViewById(R.id.btnOutputOptoCoupler4Off).setOnClickListener(this);
        view.findViewById(R.id.btnOutputWgData0On).setOnClickListener(this);
        view.findViewById(R.id.btnOutputWgData0Off).setOnClickListener(this);
        view.findViewById(R.id.btnOutputWgData1On).setOnClickListener(this);
        view.findViewById(R.id.btnOutputWgData1Off).setOnClickListener(this);

        view.findViewById(R.id.btnOutputOptoCoupler1On).setOnClickListener(this);
        view.findViewById(R.id.btnoutputOptoCoupler1Off).setOnClickListener(this);
        view.findViewById(R.id.btnOutputOptoCoupler2On).setOnClickListener(this);
        view.findViewById(R.id.btnoutputOptoCoupler2Off).setOnClickListener(this);
        view.findViewById(R.id.btnInputStatusA4).setOnClickListener(this);




        return view;
    }

    public void Output3OnClick(View view){
        try {
            RFIDWithUHFA8.getInstance().output3On();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        Toast.makeText(getActivity(),"ok",Toast.LENGTH_SHORT).show();
    }
    public void Output3OffClick(View view){
        try {
            RFIDWithUHFA8.getInstance().output3Off();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        Toast.makeText(getActivity(),"ok",Toast.LENGTH_SHORT).show();
    }
    public void Output4OnClick(View view){
        try {
            RFIDWithUHFA8.getInstance().output4On();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        Toast.makeText(getActivity(),"ok",Toast.LENGTH_SHORT).show();
    }
    public void Output4OffClick(View view){
        try {
            RFIDWithUHFA8.getInstance().output4Off();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        Toast.makeText(getActivity(),"ok",Toast.LENGTH_SHORT).show();
    }
    public void InputClick(View view)  {
        List<GPIStateEntity>  list= null;
        try {
            list = RFIDWithUHFA8.getInstance().inputStatus();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        if(list==null){
            Toast.makeText(getActivity(),"获取失败",Toast.LENGTH_SHORT).show();
        }else {
            AlertDialog alertDialog1 = new AlertDialog.Builder(getActivity())
                    .setTitle("gpio")//标题
                    .setMessage("input1:"+list.get(0).getGpiState() +"  input2:"+list.get(1).getGpiState()+"  \r\n")//内容
                    .create();
            alertDialog1.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnInputStatus:
                InputClick(null);
                break;
            case R.id.btnOutput3On:
                Output3OnClick(null);
                break;
            case R.id.btnOutput3Off:
                Output3OffClick(null);
                break;
            case R.id.btnOutput4On:
                Output4OnClick(null);
                break;
            case R.id.btnOutput4Off:
                Output4OffClick(null);
                break;
            case R.id.btnOutputOptoCoupler3On:
                rfidWithUHFA4.output3On();
                Toast.makeText(getActivity(),"ok",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnoutputOptoCoupler3Off:
                rfidWithUHFA4.output3Off();
                Toast.makeText(getActivity(),"ok",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnOutputOptoCoupler4On:
                rfidWithUHFA4.output4On();
                Toast.makeText(getActivity(),"ok",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnOutputOptoCoupler4Off:
                rfidWithUHFA4.output4Off();
                Toast.makeText(getActivity(),"ok",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnOutputWgData0On:
                rfidWithUHFA4.outputWgData0On();
                Toast.makeText(getActivity(),"ok",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnOutputWgData0Off:
                rfidWithUHFA4.outputWgData0Off();
                Toast.makeText(getActivity(),"ok",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnOutputWgData1On:
                rfidWithUHFA4.outputWgData1On();
                Toast.makeText(getActivity(),"ok",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnOutputWgData1Off:
                rfidWithUHFA4.outputWgData1Off();
                Toast.makeText(getActivity(),"ok",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnOutputOptoCoupler1On:
                rfidWithUHFA4.output1On();
                Toast.makeText(getActivity(),"ok",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnoutputOptoCoupler1Off:
                rfidWithUHFA4.output1Off();
                Toast.makeText(getActivity(),"ok",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnOutputOptoCoupler2On:
                rfidWithUHFA4.output2On();
                Toast.makeText(getActivity(),"ok",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnoutputOptoCoupler2Off:
                rfidWithUHFA4.output2Off();
                Toast.makeText(getActivity(),"ok",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnInputStatusA4:
                List<GPIStateEntity> list=rfidWithUHFA4.inputStatus();
                if(list==null){
                    Toast.makeText(getActivity(),"获取失败",Toast.LENGTH_SHORT).show();
                }else {
                    AlertDialog alertDialog1 = new AlertDialog.Builder(getActivity())
                            .setTitle("gpio")//标题
                            .setMessage("input1:"+list.get(0).getGpiState() +"  input2:"+list.get(1).getGpiState()+"  \r\n"+
                                        "input3: "+list.get(2).getGpiState()+
                                        " input4: "+list.get(3).getGpiState()+"  \r\n")//内容
                            .create();
                    alertDialog1.show();

                }
                break;

        }
    }
}


