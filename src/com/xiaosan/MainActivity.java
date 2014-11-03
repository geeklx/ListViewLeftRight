package com.xiaosan;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.sourceforge.simcpux.Constants;
import net.sourceforge.simcpux.GetFromWXActivity;
import net.sourceforge.simcpux.ShowFromWXActivity;
import net.sourceforge.simcpux.uikit.MMAlert;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.ShowMessageFromWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;

public class MainActivity extends Activity implements HttpGetDataListener,
		OnClickListener, IWXAPIEventHandler {
	private Button btnShare;
	private CheckBox isTimelineCb;
	private static final int MMAlertSelect1 = 0;
	private static final int MMAlertSelect2 = 1;
	private static final int MMAlertSelect3 = 2;

	private HttpData httpData;
	private TextView code;
	private TextView text;
	private EditText et;
	private Button btn;
	private List<ListData> lists;
	private ListView lv;
	private String str;
	private TextAdapter adapter;
	private String[] welcome_array;
	private double currentTime, oldTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// 注册到微信
		regToWx();
		initview();

	}

	public void regToWx() {
		// 通过WXAPIFactory工厂，获取IWXAPI的实例
		api = WXAPIFactory.createWXAPI(MainActivity.this, Constants.APP_ID,
				false);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		setIntent(intent);
		api.handleIntent(intent, this);
	}

	public void initview() {
		// 是否分享到朋友圈
		isTimelineCb = (CheckBox) findViewById(R.id.is_timeline_cb);
		isTimelineCb.setChecked(false);

		btnShare = (Button) findViewById(R.id.btnShare);
		// btnShare.setOnClickListener(this);
		btnShare.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// regToWx();
				// 将该app注册到微信
				api.registerApp(Constants.APP_ID);
				// 发送消息到微信
				final EditText editor = new EditText(MainActivity.this);
				editor.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT));
				editor.setText(R.string.send_text_default);

				MMAlert.showAlert(MainActivity.this, "发送到微信", editor,
						getString(R.string.app_share),
						getString(R.string.app_cancel),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String text = editor.getText().toString();
								if (text == null || text.length() == 0) {
									return;
								}

								// 初始化一个WXTextObject对象
								WXTextObject textObj = new WXTextObject();
								textObj.text = text;

								// 用WXTextObject对象初始化一个WXMediaMessage对象
								WXMediaMessage msg = new WXMediaMessage();
								msg.mediaObject = textObj;
								// 发送文本类型的消息时，title字段不起作用
								// msg.title = "Will be ignored";
								msg.description = text;

								// 构造一个Req
								SendMessageToWX.Req req = new SendMessageToWX.Req();
								req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
								req.message = msg;
								req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline
										: SendMessageToWX.Req.WXSceneSession;

								// 调用api接口发送数据到微信
								api.sendReq(req);
								finish();
							}
						}, null);
			}
		});
		api.handleIntent(getIntent(), this);
		// 机器人部分
		et = (EditText) findViewById(R.id.ed);
		btn = (Button) findViewById(R.id.seach);
		btn.setOnClickListener(this);
		lv = (ListView) findViewById(R.id.listview);
		lists = new ArrayList<ListData>();
		adapter = new TextAdapter(lists, this);
		lv.setAdapter(adapter);
		ListData listData;
		listData = new ListData(getRandomWelcomeTips(), ListData.RECEIVE,
				getTime());
		lists.add(listData);
	}

	@Override
	public void onClick(View v) {
		// getTime();
		if (v == btn) {
			str = et.getText().toString();
			// 去掉空格和回车部分
			String a = str.replace(" ", "");
			String b = a.replace("\n", "");
			et.setText("");
			ListData listData;
			listData = new ListData(str, ListData.SEND, getTime());
			lists.add(listData);
			// 移除多余的字部分
			if (lists.size() > 30) {
				for (int i = 0; i < lists.size(); i++) {
					lists.remove(i);
				}
			}
			adapter.notifyDataSetChanged();
			httpData = (HttpData) new HttpData(
					"http://www.tuling123.com/openapi/api?key=ad5f0729523118c422f47e4dba0cf4c6&info="
							+ b, this).execute();
		}
		// if (v == btnShare) {
		// // Toast.makeText(MainActivity.this, "分享至朋友圈", Toast.LENGTH_SHORT)
		// // .show();
		// regToWx();
		// // 发送消息到微信
		// final EditText editor = new EditText(MainActivity.this);
		// editor.setLayoutParams(new LinearLayout.LayoutParams(
		// LinearLayout.LayoutParams.FILL_PARENT,
		// LinearLayout.LayoutParams.WRAP_CONTENT));
		// editor.setText(R.string.send_text_default);
		//
		// MMAlert.showAlert(MainActivity.this, "发送到微信", editor,
		// getString(R.string.app_share),
		// getString(R.string.app_cancel),
		// new DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// String text = editor.getText().toString();
		// if (text == null || text.length() == 0) {
		// return;
		// }
		//
		// // 初始化一个WXTextObject对象
		// WXTextObject textObj = new WXTextObject();
		// textObj.text = text;
		//
		// // 用WXTextObject对象初始化一个WXMediaMessage对象
		// WXMediaMessage msg = new WXMediaMessage();
		// msg.mediaObject = textObj;
		// // 发送文本类型的消息时，title字段不起作用
		// // msg.title = "Will be ignored";
		// msg.description = text;
		//
		// // 构造一个Req
		// SendMessageToWX.Req req = new SendMessageToWX.Req();
		// req.transaction = buildTransaction("text"); //
		// transaction字段用于唯一标识一个请求
		// req.message = msg;
		// req.scene = isTimelineCb.isChecked() ?
		// SendMessageToWX.Req.WXSceneTimeline
		// : SendMessageToWX.Req.WXSceneSession;
		//
		// // 调用api接口发送数据到微信
		// api.sendReq(req);
		// finish();
		// }
		// }, null);
		// }
	}

	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis())
				: type + System.currentTimeMillis();
	}

	// IWXAPI 是第三方app和微信通信的openapi接口
	private IWXAPI api;

	@Override
	public void getDataUrl(String data) {
		// Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();
		// System.err.println(data);
		text(data);
	}

	public void text(String data) {

		try {
			JSONObject jb = new JSONObject(data);
			// code.setText(jb.getString("code"));
			// text.setText(jb.getString("text"));
			ListData listData;
			listData = new ListData(jb.getString("text"), ListData.RECEIVE,
					getTime());
			lists.add(listData);
			adapter.notifyDataSetChanged();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// 欢迎语部分
	private String getRandomWelcomeTips() {
		String welcome_tip = null;
		welcome_array = this.getResources()
				.getStringArray(R.array.welcome_tips);
		int index = (int) (Math.random() * (welcome_array.length - 1));
		welcome_tip = welcome_array[index];
		return welcome_tip;
	}

	// 时间部分
	private String getTime() {
		currentTime = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
		Date curDate = new Date();
		String str = format.format(curDate);
		if (currentTime - oldTime >= 500) {
			oldTime = currentTime;
			return str;
		} else {
			return "";
		}
	}

	// 微信发送请求到第三方应用时，会回调到该方法
	@Override
	public void onReq(BaseReq req) {
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			goToGetMsg();
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			goToShowMsg((ShowMessageFromWX.Req) req);
			break;
		default:
			break;
		}
	}

	// 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
	@Override
	public void onResp(BaseResp resp) {
		int result = 0;

		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			result = R.string.errcode_success;
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			result = R.string.errcode_cancel;
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			result = R.string.errcode_deny;
			break;
		default:
			result = R.string.errcode_unknown;
			break;
		}

		Toast.makeText(this, result, Toast.LENGTH_LONG).show();
	}

	private void goToGetMsg() {
		Intent intent = new Intent(this, GetFromWXActivity.class);
		intent.putExtras(getIntent());
		startActivity(intent);
		finish();
	}

	private void goToShowMsg(ShowMessageFromWX.Req showReq) {
		WXMediaMessage wxMsg = showReq.message;
		WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;

		StringBuffer msg = new StringBuffer(); // 组织一个待显示的消息内容
		msg.append("description: ");
		msg.append(wxMsg.description);
		msg.append("\n");
		msg.append("extInfo: ");
		msg.append(obj.extInfo);
		msg.append("\n");
		msg.append("filePath: ");
		msg.append(obj.filePath);

		Intent intent = new Intent(this, ShowFromWXActivity.class);
		intent.putExtra(Constants.ShowMsgActivity.STitle, wxMsg.title);
		intent.putExtra(Constants.ShowMsgActivity.SMessage, msg.toString());
		intent.putExtra(Constants.ShowMsgActivity.BAThumbData, wxMsg.thumbData);
		startActivity(intent);
		finish();
	}
}
