package com.example.uhf.tools;

import com.example.uhf.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;



public class UIHelper {
    private static Toast mToast;
   
	/**
	 * 弹出Toast消息
	 * 
	 * @param msg
	 */
	public static void ToastMessage(Context cont, String msg) {
        ToastMessage(cont, msg, Toast.LENGTH_SHORT);
	}

	public static void ToastMessage(Context cont, int msg) {
	    ToastMessage(cont, cont.getString(msg));
	}

	public static void ToastMessage(Context cont, String msg, int time) {
        if(mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(cont.getApplicationContext(), msg, time);
        mToast.show();
	}

	/**
     * 显示弹出框消息
     *
     * @param act
     * @param titleInt
     * @param messageInt
     * @param iconInt
     */
    public static void alert(Activity act, int titleInt, int messageInt,
                             int iconInt) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle(titleInt);
            builder.setMessage(messageInt);
            builder.setIcon(iconInt);

            builder.setNegativeButton(R.string.close, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 显示弹出框消息
     *
     * @param act
     * @param titleInt
     * @param message
     * @param iconInt
     */
    public static void alert(Activity act, int titleInt, String message,
                             int iconInt) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle(titleInt);
            builder.setMessage(message);
            builder.setIcon(iconInt);

            builder.setNegativeButton(R.string.close, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
