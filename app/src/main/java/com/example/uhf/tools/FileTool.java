package com.example.uhf.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WuShengjun on 2017/11/3.
 */

public class FileTool {
    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param file 将要删除的文件或目录
     * @param deleteDirectory 是否删除文件夹
     * @return
     */
    public static boolean deleteFile(File file, boolean deleteDirectory) {
        if(file == null || !file.exists())
            return false;
        if (file.isDirectory()) { // 若为目录
            File[] childFiles = file.listFiles(); // 遍历里面所有文件和目录
            // 递归删除目录中的子目录下
            if(childFiles != null) {
                for (int i = 0; i < childFiles.length; i++) {
//                    MyLg.e("directoryFile", "childFilePath=" + childFiles[i].getName());
                    boolean succ = deleteFile(childFiles[i], deleteDirectory);
//                    if (!succ)
//                        return false;
                }
            }
        }

        if(!file.isDirectory() || deleteDirectory) {
            return file.delete();
        }
        // file为文件或目录此时为空，可以删除
//        MyLg.e("deleteFile", "filePath=" + file.getName());
        return false;
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param filePath 将要删除的文件路径
     * @param deleteDirectory 是否删除文件夹
     * @return
     */
    public static boolean deleteFile(String filePath, boolean deleteDirectory) {
        return deleteFile(new File(filePath), deleteDirectory);
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param filePath 将要删除的文件或目录路径
     * @return boolean Returns "true" if all deletions were successful.
     * If a deletion fails, the method stops attempting to
     * delete and returns "false".
     * @return
     */
    public static boolean deleteFile(String filePath) {
        return deleteFile(filePath, true);
    }

    /**
     * 判断SDcard是否可用
     * @return
     */
    public static boolean sdcardExists() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 获取SDcard的总存储量,返回-1则不存在
     * @return
     */
    public static long getSDCardTotalSpace() {
        File file = Environment.getExternalStorageDirectory();
        return getFileTotalSpace(file);
    }

    /**
     * 获取SDcard的剩余存储量,返回-1则不存在
     * @return
     */
    public static long getSDCardUsableSpace() {
        File file = Environment.getExternalStorageDirectory();
        return getFileUsableSpace(file);
    }

    /**
     * 获取文件夹总空间，-1为不存在
     * @param file
     * @return
     */
    public static long getFileTotalSpace(File file) {
        if (file != null && file.exists()) {
            return file.getTotalSpace(); // 文件的总大小（此方法应用于8以上，需要在此方法打上NewApi的注解）
        } else {
            return -1;
        }
    }

    /**
     * 获取文件夹剩余空间，-1为不存在
     * @return
     */
    public static long getFileUsableSpace(File file) {
        if (file != null && file.exists()) {
            return file.getUsableSpace(); // 文件的总大小（此方法应用于8以上，需要在此方法打上NewApi的注解）
        } else {
            return -1;
        }
    }

    /**
     * 获取文件的大小
     * @param file
     * @return 返回字节数b
     */
    public static long getFilesSize(File file) {
        if(file == null || !file.exists()) {
            return 0;
        }
        long size = 0L;
        if(file.isDirectory()) {
            File[] childFiles = file.listFiles(); // 遍历里面所有文件和目录
            // 递归删除目录中的子目录下
            if(childFiles != null) {
                for (int i = 0; i < childFiles.length; i++) {
                    if(childFiles[i].isDirectory()) { // 如果是文件夹再递归
                        size += getFilesSize(childFiles[i]);
                    } else { // 否则就所有文件大小加起来
                        size += childFiles[i].length();
                    }
                }
            }
        } else {
            size += file.length(); // 把文件里所有文件（不是文件夹）大小加起来
        }
        return size;
    }

    /**
     * 获取文件大小
     * @param filePath
     * @return
     */
    public static long getFilesSize(String filePath) {
        return getFilesSize(new File(filePath));
    }/**
     * 保存crash到文件
     * @param ex
     * @param crashFilePath
     * @return crashMessage
     */
    public static String saveCrashFile(Context context, Throwable ex, String crashFilePath) {
        File file = new File(crashFilePath);
        return saveCrashFile(context, ex, file);
    }

    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public static List<String> readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        List<String> stringLines = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                stringLines.add(tempString);
                // 显示行号
                System.out.println("line " + line + ": " + tempString);
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return stringLines;
    }

    /**
     * 保存crash到文件
     * @param ex
     * @param crashPathFile
     * @return crashMessage
     */
    public static String saveCrashFile(Context context, Throwable ex, File crashPathFile) {
        if(!crashPathFile.exists()) { // 创建文件夹
            crashPathFile.mkdirs();
        }
        StringBuffer sb = new StringBuffer();
//        String saveTime = DateUtils.getCurrFormatDate(DateUtils.DATEFORMAT_FULL);
        long saveTime = System.currentTimeMillis();
        sb.append("DateTime: " + saveTime + "\n");
        sb.append("DeviceInfo: " + Build.MANUFACTURER + " " + Build.MODEL + "\n");
        sb.append("AppVersion: " + getPackageInfo(context).versionName
                + "_" + getPackageInfo(context).versionCode + "\n");

        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        ex.printStackTrace(pw);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = cause.getCause();
        }
        pw.close();
        String result = writer.toString();
        sb.append("Excetpion: \n");
        sb.append(result);

        Log.e("CrashHandler", result); // 打印输出，方便开发调试

        // 记录异常到特定文件中
        File crashFile = new File(crashPathFile, "log_v" + getPackageInfo(context).versionName + "_" + getPackageInfo(context).versionCode + "(" + saveTime + ").txt");
        crashFile.setReadOnly();
        try {
            writer = new FileWriter(crashFile);
            writer.write(sb.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e("saveToCrashFile", "" + e.getMessage());
        }
        return result;
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if(info == null) {
            info = new PackageInfo();
        }
        return info;
    }
}
