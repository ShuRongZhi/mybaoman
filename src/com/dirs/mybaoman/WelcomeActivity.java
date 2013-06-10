package com.dirs.mybaoman;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;
import android.widget.Toast;


/**
 * 显示欢迎动画的Activity
 * 开启两个线程，一个调用DataBaseHelper的getTitle方法获取标题
 * 另一个则用来播放动画效果
 * */
public class WelcomeActivity extends Activity {
	private SQLiteDatabase db = null;
	private DataBaseHelper mDataHelper = null;
	private int count = 0;
	private boolean getTitleRes  = true;
	private boolean isStop = false;
	private ImageView image = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		//查询表是否存在，不存在则创建表
		mDataHelper = new DataBaseHelper(getApplicationContext(), "Info");
		if (!mDataHelper.CheckTableExits("config")) {
			Log.d("debug", "表config不存在，创建表");
			String create_sql = "CREATE TABLE config(ItemID INTEGER PRIMARY KEY,ItemTitle TEXT,ImageID TEXT,IsClicked INTEGER)";
			db = mDataHelper.getWritableDatabase();
			db.execSQL(create_sql);
		}
		
		image = (ImageView) findViewById(R.id.welcomeimage);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		//启动播放动画的线程
		new displayAnima().start();
		//启动拉取标题线程
		new getTitleForServer().start();
	}


	
	//拉取标题线程
	class getTitleForServer extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			if(!mDataHelper.getTitile()){
				getTitleRes = false;
			}else{
				//如果获取标题成功，则个updateUI发送成功消息，跳转到MainActivity
				getTitleRes = true;
				//将播放动画线程停止
				isStop = true;
				Message msg = new Message();
				msg.what = 0;
				WelcomeActivity.this.UpdateUI.sendMessage(msg);
			}
		}

	}

	//播放载入动画线程
	class displayAnima extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while(!isStop){
				if(!getTitleRes){
					Message msg = new Message();
					msg.what = -1;
					WelcomeActivity.this.UpdateUI.sendMessage(msg);
					return;
				}else{
					//睡眠700毫秒后轮流播放动画
					try {
						Thread.sleep(700);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						++count;
						Message msg = new Message();
						msg.what = 1;
						WelcomeActivity.this.UpdateUI.sendMessage(msg);
					}
				}
			}
		}

	}

	Handler UpdateUI = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			//获取标题失败
			case -1:
				Log.d("deug", "无法从网络获取标题");
				Toast.makeText(getApplicationContext(),
						R.string.getTitleFailed, Toast.LENGTH_LONG).show();
				image.setImageResource(R.drawable.welcome_err);
				break;
			//获取标题成功
			case 0:
				Intent intent = new Intent();
				intent.setClass(getApplicationContext(), MainActivity.class);
				startActivity(intent);
				// 结束自己
				finish();
				Log.d("debug", "从网络获取标题成功");
				break;
			//获取标题中
			case 1:
				if (count > 4) {
					count = 0;
				}
				switch (count) {
				case 1:
					image.setImageResource(R.drawable.welcome_setp1);
					break;
				case 2:
					image.setImageResource(R.drawable.welcome_setp2);
					break;
				case 3:
					image.setImageResource(R.drawable.welcome_setp3);
					break;
				case 4:
					image.setImageResource(R.drawable.welcome_setp4);
					break;
				}
				break;
			}
		}

	};

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//建议系统回收垃圾
		System.gc();
		finish();
	}
}
