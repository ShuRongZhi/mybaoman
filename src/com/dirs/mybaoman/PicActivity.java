package com.dirs.mybaoman;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PicActivity extends Activity {

	private JniHelper mJni = null;
	private ImageView mImage = null;
	private TextView mText = null;
	private ProgressBar mProgress = null;
	private Bundle mBundle = null;
	private String Path = null;
	private Bitmap bm = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mJni = JniHelper.getInstance();
		setContentView(R.layout.activity_pic);

		mImage = (ImageView) findViewById(R.id.Image);
		mText = (TextView) findViewById(R.id.downInfo);
		mProgress = (ProgressBar) findViewById(R.id.downProgress);

		// 取得MainActivity传递过来的数据
		Intent intent = this.getIntent();
		mBundle = intent.getExtras();

		this.setLoadView(true);

		// 启动线程获取图片
		new Thread(loadImage).start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// 如果bitmap没被释放，则释放，并请求gc
		if (bm != null) {
			if (!bm.isRecycled()) {
				bm.recycle();
				System.gc();
			}
		}
	}

	// 更新UI
	Handler updateUI = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			// 下载成功
			case 0:
				FileInputStream in = null;
				try {
					in = new FileInputStream(Path);
					byte[] bt = getBytes(in);
					bm = BitmapFactory.decodeByteArray(bt, 0, bt.length);
					
					//如果获取图片失败
					if (bm == null) {
						//因为这个是子线程，需要重新初始化jni，否则会崩溃
						mJni.init();
						//删除图片
						mJni.delImage(Path);
						
						//设置错误提示
						Log.d("debug", "BitmapFactory返回空Bitmap!!!");
						Toast.makeText(getApplicationContext(),
								R.string.err_unknow, Toast.LENGTH_LONG).show();
						setLoadView(false);
						mImage.setImageResource(R.drawable.down_err);
					} else {
						//将ImageView设置为可见，并载入图片
						setLoadView(false);
						mImage.setImageBitmap(bm);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (OutOfMemoryError oom) {
					//捕获OOM的错误，设置错误提示
					oom.printStackTrace();
					Toast.makeText(getApplicationContext(), R.string.oom,
							Toast.LENGTH_LONG).show();
					setLoadView(false);
					mImage.setImageResource(R.drawable.oom);
				} finally {
					try {
						if (in != null) {
							in.close();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			// 下载失败
			case -1:
				//设置出错信息
				Toast.makeText(getApplicationContext(), R.string.err_unknow,
						Toast.LENGTH_LONG).show();
				setLoadView(false);
				mImage.setImageResource(R.drawable.down_err);
				break;
			}
		}

	};

	// 获取图片
	Runnable loadImage = new Runnable() {

		@SuppressWarnings("null")
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Message msg = new Message();
			//取得传递给此Activity的漫画ID和图片ID
			String Item = mBundle.getString("ItemID");
			String Image = mBundle.getString("ImageID");
			
			//初始化Jni，并调用getImage取得图片路径
			mJni.init();
			Path = mJni.getImage(Item, Image);
			
			//Log.d("debug", "Java 层:" + Path);

			//判断是否成功取得路径
			if (Path.equals("error")) {
				msg.what = -1;
				PicActivity.this.updateUI.sendMessage(msg);
			} else {
				msg.what = 0;
				PicActivity.this.updateUI.sendMessage(msg);
			}
		}
	};

	// 设置界面上控件是否可见
	// 如果flag为ture，则载入控件可见，ImageView控件不可见
	// 否则ImageView可见，载入控件不可见
	void setLoadView(boolean flag) {
		if (flag) {
			mImage.setVisibility(View.INVISIBLE);
			mText.setVisibility(View.VISIBLE);
			mProgress.setVisibility(View.VISIBLE);
		} else {
			mImage.setVisibility(View.VISIBLE);
			mText.setVisibility(View.INVISIBLE);
			mProgress.setVisibility(View.INVISIBLE);
		}
	}
	
	//将inputsteam转换成byte[]数组，减少oom的出现
	private byte[] getBytes(InputStream is) throws IOException {
		ByteArrayOutputStream mByteOut = new ByteArrayOutputStream();
		byte[] mByte = new byte[1024];
		int len = 0;

		while ((len = is.read(mByte, 0, 1024)) != -1) {
			mByteOut.write(mByte, 0, len);
			mByteOut.flush();
		}
		byte[] bytes = mByteOut.toByteArray();
		return bytes;
	}
}
