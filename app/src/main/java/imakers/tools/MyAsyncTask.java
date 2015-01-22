package imakers.tools;



import android.os.AsyncTask;

import imakers.interfaces.MyAsyncTaskInterface;

public class MyAsyncTask extends AsyncTask<MyAsyncTaskInterface, Void, Object> {
	
	MyAsyncTaskInterface Listener;
	
	@Override
	protected void onPostExecute(Object result) {
      Listener.onDone(result);
    }
	@Override
	protected Object doInBackground(MyAsyncTaskInterface... params) {
		Listener = params[0];
		return Listener.onDoing();
	}
}
