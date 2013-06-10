package com.dirs.mybaoman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper {
	private static final int VERSION = 1;
	private SQLiteDatabase db = null;

	public DataBaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	public DataBaseHelper(Context context, String name, int version) {
		this(context, name, null, version);
	}

	public DataBaseHelper(Context context, String name) {
		this(context, name, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.d("debug", "DataBase Open");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	// 查询表是否存在
	public boolean CheckTableExits(String TableName) {
		if (TableName == null) {
			return false;
		}
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = this.getReadableDatabase();
			String sql = "select count(*) as c from sqlite_master where type ='table' and name ='"
					+ TableName.trim() + "' ";
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	// 从网站获取标题
	public boolean getTitile() {
		try {
			Log.d("debug","getTitle");
			db = this.getWritableDatabase();
			URL url = new URL("http://m.baozoumanhua.com");
			HttpURLConnection mConnection = (HttpURLConnection) (url
					.openConnection());
			mConnection.setDoOutput(false);
			mConnection.setDoInput(true);
			mConnection.setUseCaches(false);
			mConnection.setRequestMethod("GET");
			mConnection.connect();

			// 判断返回结果
			int returncode = mConnection.getResponseCode();
			if (returncode != HttpURLConnection.HTTP_OK) {
				throw new Exception("下载网页失败，返回码：" + returncode);
			}

			InputStream in = mConnection.getInputStream();
			BufferedReader mBufferReader = new BufferedReader(
					new InputStreamReader(in));
			String res = "";
			String t = null;
			while ((t = mBufferReader.readLine()) != null) {
				res += t;
			}
			Log.d("debug","下载网页完成!");
			// 获取暴漫的正则表达式
			Pattern getItem = Pattern
					.compile("<div.+?data-article_id=\"(.+?)\".+?\\s.+?<h4>(.+?)</h4>\\s.+?<img alt=.+?/original/(.+?.)\".+?>");
			Matcher m = getItem.matcher(res);
			String insert_sql = "INSERT OR IGNORE INTO config(ItemID,ItemTitle,ImageID,IsClicked) VALUES (?,?,?,?)";
			if(!m.find()){
				Log.d("debug","没有找到匹配的结果!");
				return false;
			}
			while (m.find()) {
				if (m.group(1).isEmpty() || m.group(2).isEmpty()
						|| m.group(3).isEmpty()) {
					Log.d("debug", "获取暴漫标题时获取到空值，忽略！");
				} else {
					Object[] mValue = new Object[] {m.group(1),
							m.group(2), m.group(3),0};
					db.execSQL(insert_sql, mValue);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (db != null) {
				db.close();
			}
		}
		return true;
	}
}
