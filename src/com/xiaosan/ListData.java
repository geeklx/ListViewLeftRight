package com.xiaosan;

public class ListData {
	private String content;
	public static final int SEND = 1;
	public static final int RECEIVE = 2;
	private int flag;
	private String time;

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}
	
	public ListData(String content, int flag, String time) {
		setContent(content);
		setFlag(flag);
		setTime(time);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}
