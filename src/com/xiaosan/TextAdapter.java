package com.xiaosan;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TextAdapter extends BaseAdapter {
	private List<ListData> lists;
	private Context context;
	private RelativeLayout Rlayout;

	public TextAdapter(List<ListData> lists, Context context) {
		this.lists = lists;
		this.context = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return lists.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return lists.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		if (lists.get(position).getFlag() == ListData.RECEIVE) {
			Rlayout = (RelativeLayout) inflater
					.inflate(R.layout.leftitem, null);
		}
		if (lists.get(position).getFlag() == ListData.SEND) {
			Rlayout = (RelativeLayout) inflater.inflate(R.layout.rightitem,
					null);
		}
		TextView tv = (TextView) Rlayout.findViewById(R.id.tv);
		TextView time = (TextView) Rlayout.findViewById(R.id.time);
		time.setText(lists.get(position).getTime());
		tv.setText(lists.get(position).getContent());
		return Rlayout;
	}
}
