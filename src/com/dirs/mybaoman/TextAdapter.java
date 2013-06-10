package com.dirs.mybaoman;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/*
 * 自定义adapter,用于更改item上文字的颜色
 * **/
public class TextAdapter extends BaseAdapter {
	private SQLiteDatabase db = null;
	private DataBaseHelper mDataBaseHelper = null;
	private boolean isTableExits;
	private Context context;
	private List<? extends Map<String, ?>> mData;
	private int mResource;
	private String[] mFrom;
	private int[] mTo;
	private LayoutInflater mLayoutInflater;

	public TextAdapter(Context context, List<? extends Map<String, ?>> data,
			int resource, String[] from, int[] to) {
		this.context = context;
		this.mData = data;
		this.mResource = resource;
		this.mFrom = from;
		this.mTo = to;
		this.mLayoutInflater = (LayoutInflater) context
				.getSystemService(context.LAYOUT_INFLATER_SERVICE);
		mDataBaseHelper = new DataBaseHelper(context, "Info");
		
		if(!mDataBaseHelper.CheckTableExits("config")){
			Log.d("debug","表config不存在");
			isTableExits = false;
		}
		else{
			isTableExits = true;
			db = mDataBaseHelper.getReadableDatabase();
		}
	}

	public int getCount() {

		return this.mData.size();
	}

	public Object getItem(int position) {

		return this.mData.get(position);
	}

	public long getItemId(int position) {

		return position;
	}

	public View getView(int position, View contentView, ViewGroup parent) {
		contentView = this.mLayoutInflater.inflate(this.mResource, parent,
				false);
		// 设置contentView的内容和样式
		for (int index = 0; index < this.mTo.length; index++) {
			if(contentView == null){
				Log.d("debug","contentView is null!");
				contentView =this.mLayoutInflater.inflate(mResource, null);
			}
			TextView textView = (TextView)contentView
					.findViewById(this.mTo[index]);
			//获取Item上的文字
			String TitleName = this.mData.get(position).get(this.mFrom[index])
					.toString();
			textView.setText(TitleName);
			//检查Item是否被点击过，如果返回true，说明已点击，将字体颜色设置为灰色，否则设置成蓝色
			if(isClicked(TitleName)){
				textView.setTextColor(android.graphics.Color.GRAY);
			}else{
				textView.setTextColor(android.graphics.Color.BLUE);
			}
		}
		
		return contentView;
	}

	//从数据库中查询点击情况
	private boolean isClicked(String Title) {
		boolean res = true;
		//如果表不存在，直接返回false
		if(!isTableExits){
			return false;
		}
		db = mDataBaseHelper.getReadableDatabase();
		
		Cursor mCursor = null;
		try{
			//从数据库中查询数据
			mCursor = db.query("config",null,"ItemTitle = ?",new String[]{Title},null,null,null);
			if(mCursor == null || !mCursor.moveToFirst()){
				return false;
			}
			else{
				//取得点击的情况
				int i = mCursor.getInt(mCursor.getColumnIndex("IsClicked"));
				switch(i){
				case 0:
					res = false;
					break;
				case 1:
					res = true;
					break;
				}
			}
		}catch(SQLiteException e){
			e.printStackTrace();
			res = false;
		}finally{
			if(db != null){
				db.close();
			}
			if(mCursor != null){
				mCursor.close();
			}
		}
		
		return res;
	}
}
