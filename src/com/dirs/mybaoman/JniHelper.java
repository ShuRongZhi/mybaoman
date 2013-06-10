package com.dirs.mybaoman;

import android.util.Log;

public class JniHelper {
	private static JniHelper instance = null;
	private JniHelper(){};
	
	public synchronized static JniHelper getInstance(){
		if(instance == null){
			instance = new JniHelper();
		}
		return instance;
	}
	
	//载入so库
	static{
		try{
			System.loadLibrary("network");
		}catch(UnsatisfiedLinkError e){
			e.printStackTrace();
		}
	}
	//初始化函数，用于初始化工作
	public native void init();
	//获取图片，参数要求为漫画ID和图片ID
	//如果当前图片已缓存，返回图片路径，否则返回空
	public native String getImage(String ItemID,String ImageID);
	//清空缓存文件夹
	public native void clearCache();
	
	//删除图片，指定路径
	//因为有可能下载成功，但是图片不完整，需要在java层判断是否成功载入图片，如果失败就删除该图片
	public native boolean delImage(String name);
	
	//回调函数，用于通知下载出错
	public void downloadError(int errCode){
		Log.d("debug","errorCode: " + errCode);
	}
}
