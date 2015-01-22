package imakers.tools;

import android.app.Activity;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import imakers.interfaces.MyAsyncLisener;

//Třída pro asynchronní odesílání requestů na server
public class MyHttpClient {
	
	private static AsyncHttpClient client = new AsyncHttpClient();
    public static Activity localActivity = null;

    public static void get(final Activity activity, final String url, RequestParams params,  final MyAsyncLisener lisener) {
        if(localActivity == null) {
            localActivity = activity;
        }
        get(url, params, lisener, 3);
    }

    public static void get(final String url, RequestParams params,  final MyAsyncLisener lisener) {
        get(url, params, lisener, 3);
    }

	public static void get(final String url, final RequestParams params,  final MyAsyncLisener lisener, final int repeat) {

        try {

            MyUtils.Log(localActivity).debug("URL: {} PARAMS: {}", url, params.toString());


        } catch (Exception e) {
            e.printStackTrace();
        }


        Log.v("kalkub", url);

	    client.setTimeout(10000);

        client.get(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String content = new String(responseBody);
                if(repeat > 0 && content == null) {
                    get(url, params, lisener, repeat-1);
                }
                else {
                    lisener.onComplete(MyUtils.getJSONObject(content, localActivity));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {


                String content = "";

                if(responseBody == null) {
                    content = null;
                }
                else {
                    content = new String(responseBody);
                }
                lisener.onComplete(MyUtils.getJSONObject(content, localActivity));

            }
        });
	}



}
