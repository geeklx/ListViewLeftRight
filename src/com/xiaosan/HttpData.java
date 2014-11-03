package com.xiaosan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;

public class HttpData extends AsyncTask<String, Void, String> {
	private HttpClient nhttHttpClient;
	private HttpGet nHttpGet;
	private HttpResponse nHttpResponse;
	private HttpEntity nhttHttpEntity;
	private InputStream in;
	private String url;
	private HttpGetDataListener listener;

	public HttpData(String url, HttpGetDataListener listener) {
		this.url = url;
		this.listener = listener;
	}

	@Override
	protected String doInBackground(String... arg0) {
		try {
			nhttHttpClient = new DefaultHttpClient();
			nHttpGet = new HttpGet(url);
			nHttpResponse = nhttHttpClient.execute(nHttpGet);
			nhttHttpEntity = nHttpResponse.getEntity();
			in = nhttHttpEntity.getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = null;
			StringBuffer sb = new StringBuffer();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		listener.getDataUrl(result);
		super.onPostExecute(result);
	}
}
