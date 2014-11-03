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
		// ע�ᵽ΢��
		regToWx();
		initview();

	}

	public void regToWx() {
		// ͨ��WXAPIFactory��������ȡIWXAPI��ʵ��
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
		// �Ƿ��������Ȧ
		isTimelineCb = (CheckBox) findViewById(R.id.is_timeline_cb);
		isTimelineCb.setChecked(false);

		btnShare = (Button) findViewById(R.id.btnShare);
		// btnShare.setOnClickListener(this);
		btnShare.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// regToWx();
				// ����appע�ᵽ΢��
				api.registerApp(Constants.APP_ID);
				// ������Ϣ��΢��
				final EditText editor = new EditText(MainActivity.this);
				editor.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT));
				editor.setText(R.string.send_text_default);

				MMAlert.showAlert(MainActivity.this, "���͵�΢��", editor,
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

								// ��ʼ��һ��WXTextObject����
								WXTextObject textObj = new WXTextObject();
								textObj.text = text;

								// ��WXTextObject�����ʼ��һ��WXMediaMessage����
								WXMediaMessage msg = new WXMediaMessage();
								msg.mediaObject = textObj;
								// �����ı����͵���Ϣʱ��title�ֶβ�������
								// msg.title = "Will be ignored";
								msg.description = text;

								// ����һ��Req
								SendMessageToWX.Req req = new SendMessageToWX.Req();
								req.transaction = buildTransaction("text"); // transaction�ֶ�����Ψһ��ʶһ������
								req.message = msg;
								req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline
										: SendMessageToWX.Req.WXSceneSession;

								// ����api�ӿڷ������ݵ�΢��
								api.sendReq(req);
								finish();
							}
						}, null);
			}
		});
		api.handleIntent(getIntent(), this);
		// �����˲���
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
			// ȥ���ո�ͻس�����
			String a = str.replace(" ", "");
			String b = a.replace("\n", "");
			et.setText("");
			ListData listData;
			listData = new ListData(str, ListData.SEND, getTime());
			lists.add(listData);
			// �Ƴ�������ֲ���
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
		// // Toast.makeText(MainActivity.this, "����������Ȧ", Toast.LENGTH_SHORT)
		// // .show();
		// regToWx();
		// // ������Ϣ��΢��
		// final EditText editor = new EditText(MainActivity.this);
		// editor.setLayoutParams(new LinearLayout.LayoutParams(
		// LinearLayout.LayoutParams.FILL_PARENT,
		// LinearLayout.LayoutParams.WRAP_CONTENT));
		// editor.setText(R.string.send_text_default);
		//
		// MMAlert.showAlert(MainActivity.this, "���͵�΢��", editor,
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
		// // ��ʼ��һ��WXTextObject����
		// WXTextObject textObj = new WXTextObject();
		// textObj.text = text;
		//
		// // ��WXTextObject�����ʼ��һ��WXMediaMessage����
		// WXMediaMessage msg = new WXMediaMessage();
		// msg.mediaObject = textObj;
		// // �����ı����͵���Ϣʱ��title�ֶβ�������
		// // msg.title = "Will be ignored";
		// msg.description = text;
		//
		// // ����һ��Req
		// SendMessageToWX.Req req = new SendMessageToWX.Req();
		// req.transaction = buildTransaction("text"); //
		// transaction�ֶ�����Ψһ��ʶһ������
		// req.message = msg;
		// req.scene = isTimelineCb.isChecked() ?
		// SendMessageToWX.Req.WXSceneTimeline
		// : SendMessageToWX.Req.WXSceneSession;
		//
		// // ����api�ӿڷ������ݵ�΢��
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

	// IWXAPI �ǵ�����app��΢��ͨ�ŵ�openapi�ӿ�
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

	// ��ӭ�ﲿ��
	private String getRandomWelcomeTips() {
		String welcome_tip = null;
		welcome_array = this.getResources()
				.getStringArray(R.array.welcome_tips);
		int index = (int) (Math.random() * (welcome_array.length - 1));
		welcome_tip = welcome_array[index];
		return welcome_tip;
	}

	// ʱ�䲿��
	private String getTime() {
		currentTime = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss");
		Date curDate = new Date();
		String str = format.format(curDate);
		if (currentTime - oldTime >= 500) {
			oldTime = currentTime;
			return str;
		} else {
			return "";
		}
	}

	// ΢�ŷ������󵽵�����Ӧ��ʱ����ص����÷���
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

	// ������Ӧ�÷��͵�΢�ŵ�����������Ӧ�������ص����÷���
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

		StringBuffer msg = new StringBuffer(); // ��֯һ������ʾ����Ϣ����
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
