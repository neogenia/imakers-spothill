package imakers.tools;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import imakers.interfaces.MyAsyncLisener;
import imakers.interfaces.MyAsyncTaskInterface;

//To samé, jak MyHttpClient, krom, že s odesíláním JSON
public class MyHttpClientJSON {

	private static AsyncHttpClient client = new AsyncHttpClient();
	private final static String BASE_URL = MyApplication.API_URL+"api/";

	public static void post(final Activity ac, final String url, final JSONObject object,
			final MyAsyncLisener lisener) throws UnsupportedEncodingException {
		new MyAsyncTask().execute(new MyAsyncTaskInterface() {

			@Override
			public void onDone(Object Result) {
				if (Result != null) {
					post(ac, url, (StringEntity) Result, lisener); 
					
				}

			}

			@Override
			public Object onDoing() {
				try {
					StringEntity se = new StringEntity(object.toString(), HTTP.UTF_8);
					se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
					return se;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		});

	}

	public static void post(Activity activity , final String url, StringEntity params,
			final MyAsyncLisener lisener) {
		post(activity, url, params, lisener, 1);
	}

	public static void post(final Activity context,final String url, final StringEntity params,
			final MyAsyncLisener lisener, final int attempts) {
		try {
			String s = EntityUtils.toString(params);
			Log.v("kalkub",BASE_URL + url + " " + s);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(attempts == 1) {
			client.setTimeout(10000);
		}
		else if(attempts == 0) {
			client.setTimeout(5000);
		}
		client.post(context,
                BASE_URL + url, params, "application/json",
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String content = new String(responseBody);
                        if (content == null && attempts > 0) {
                            post(context, url, params, lisener, attempts - 1);
                        } else {
                            lisener.onComplete(MyUtils.getJSONObject(content, context));
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        String content = null;
                        if(responseBody != null){
                            content = new String(responseBody);
                        }
                        lisener.onComplete(MyUtils.getJSONObject(content, context));
                    }
                });
	}
}
