package com.dirs.mybaoman;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ListView mListView = null;
	private TextAdapter mAdapter = null;
	private DataBaseHelper mDataBaseHelper = null;
	private SQLiteDatabase db = null;
	private ArrayList<HashMap<String, String>> data = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		// 获得数据库对象及判断表是否存在
		mDataBaseHelper = new DataBaseHelper(getApplicationContext(), "Info");
		mListView = (ListView) findViewById(R.id.MainList);

		// 从数据库中查询标题
		data = this.setTitle();
		//判断查询结果
		if (data == null || data.isEmpty()) {
			Toast.makeText(getApplicationContext(), "Oops:设置标题失败",
					Toast.LENGTH_LONG).show();
			this.getTitleError();
		} else {
			//创建自定义Adapter适配器并设置到ListView上
			mAdapter = new TextAdapter(getApplicationContext(), data,
					R.layout.list_item, new String[] { "ItemName" },
					new int[] { R.id.ItemName });
			mListView.setAdapter(mAdapter);
			//注册点击监听器
			mListView.setOnItemClickListener(new onClickItem());
		}

	}

	// ListView点击监听器
	class onClickItem implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// 更新数据
			setClickStatus(arg2);
			mAdapter.notifyDataSetChanged();
			
			//取得点击的标题文字
			String Title;
			Title = data.get(arg2).get("ItemName");
			Cursor mCursor = null;
			try {
				//从数据库中查询此标题对应的漫画ID和图片ID
				db = mDataBaseHelper.getReadableDatabase();
				mCursor = db.query("config",null,"ItemTitle = ?",
						new String[]{Title}, null, null, null);
				if (mCursor == null || !mCursor.moveToFirst()) {
					Toast.makeText(getApplicationContext(),
							R.string.queryError, Toast.LENGTH_LONG).show();
				} else {
					//把查询到的结果添加到Bundle中，传递给PicActivity
					Intent intent = new Intent();
					intent.setClass(getApplicationContext(), PicActivity.class);
					Bundle mBundle = new Bundle();
					mBundle.putString("ItemID",mCursor.getString(mCursor.getColumnIndex("ItemID")));
					mBundle.putString("ImageID",mCursor.getString(mCursor.getColumnIndex("ImageID")));
					intent.putExtras(mBundle);
					startActivity(intent);
				}
			} catch (SQLiteException e) {
				e.printStackTrace();
			} finally {
				if (db != null) {
					db.close();
				}
				if (mCursor != null) {
					mCursor.close();
				}
			}
		}

	}

	// 将position位置的item设置为已点击
	private void setClickStatus(int position) {
		String Title;
		//取得点击的标题
		Title = data.get(position).get("ItemName");
		try {
			db = mDataBaseHelper.getWritableDatabase();
			//更新对应标题的点击标记
			String update_sql = "UPDATE config SET IsClicked=1 WHERE ItemTitle = ?";
			Object[] mValue = new Object[] { Title };
			db.execSQL(update_sql, mValue);
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	//从数据库中查询标题
	private ArrayList<HashMap<String, String>> setTitle() {
		ArrayList<HashMap<String, String>> localArray = new ArrayList<HashMap<String, String>>();
		Log.d("debug", "setTtitle");
		Cursor mCursor = null;
		db = mDataBaseHelper.getReadableDatabase();
		try {
			mCursor = db.query("config", null, null, null, null, null, null);
			if (mCursor == null || mCursor.getCount() == 0) {
				Toast.makeText(getApplicationContext(), "Oops:查询数据失败",
						Toast.LENGTH_LONG).show();
				return localArray;
			}
			while (mCursor.moveToNext()) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("ItemName",
						mCursor.getString(mCursor.getColumnIndex("ItemTitle")));
				localArray.add(map);
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
			return localArray;
		} finally {
			if (mCursor != null) {
				mCursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return localArray;
	}
	
	private void getTitleError() {
		TextView v = (TextView) findViewById(R.id.getTitleError);
		v.setText(R.string.getTitleFailed);
		v.setVisibility(View.VISIBLE);
		mListView.setVisibility(View.INVISIBLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add("清空缓存");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		JniHelper mJni = JniHelper.getInstance();
		mJni.init();
		mJni.clearCache();
		return super.onOptionsItemSelected(item);
	}
	
	
}
