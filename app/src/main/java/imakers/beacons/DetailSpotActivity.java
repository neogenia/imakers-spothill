package imakers.beacons;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.Random;

import imakers.classes.ChangeClass;
import imakers.tools.MyActionPanel;
import imakers.tools.MyApplication;
import imakers.tools.MyUtils;


public class DetailSpotActivity extends Activity {

    private MyActionPanel panel;

    @Override
    public void onBackPressed() {
        if(((MyApplication)getApplicationContext()).getChange() != null && ((MyApplication)getApplicationContext()).getChange().size() != 0) {
            ((MyApplication)getApplicationContext()).setIsChange(true);
        }
        else {
            ((MyApplication)getApplicationContext()).setIsChange(false);
        }

        if(getIntent().getBooleanExtra("notify", false)) {
            ((MyApplication)getApplicationContext()).setCurrentSpots(new ArrayList<Integer>());
            startActivity(new Intent(DetailSpotActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        }
        else {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_spot);

        //Nastavování custom panel, který jsem vytvořil + akci při back tlačítku
        panel = new MyActionPanel(this, "DETAIL SPOTU");
        panel.setActionForBack(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //panel.hideBack();

        //share kampaně
        panel.showShare(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = "";
                try {

                    s = "Právě jsem využil " + getIntent().getStringExtra("name_spot") + (getIntent().getStringExtra("name").isEmpty() ? "" : " od poskytovatele " + getIntent().getStringExtra("name")) + ". Využijte také výhody aplikace <a href='http://www.spothill.com'>spothill</a>.";

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(s));
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });


        //nastavování webview
        WebView webView = (WebView)findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setInitialScale(30);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        MyUtils.showDialog(this);


        //zde jsem musel přepsat načítání kvůli dialogu a načítání změn
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                view.loadUrl(url);

                return true;
            }

            public void onPageFinished(WebView view, String url) {

                Log.v("kalkub",url);

                //načítání změn, který jsou inteligentní, může být více změn najednou

                if(url.contains("#")) {
                    ((MyApplication)getApplicationContext()).setIsChange(true);
                    ChangeClass changeClass = new ChangeClass(getIntent().getLongExtra("id", 1), url.substring(url.lastIndexOf("#")+1));

                    Boolean can = true;

                    if(((MyApplication)getApplicationContext()).getChange() == null) {
                        ((MyApplication)getApplicationContext()).setChange(new ArrayList<ChangeClass>());
                    }

                    for (int i = 0; i < ((MyApplication)getApplicationContext()).getChange().size(); i++) {

                        if(((MyApplication)getApplicationContext()).getChange().get(i).getType().equals(changeClass.getType())) {
                            can = false;
                            ((MyApplication)getApplicationContext()).getChange().remove(i);
                            break;
                        }

                    }

                    if(can) {
                        ((MyApplication)getApplicationContext()).getChange().add(changeClass);
                    }

                }

                MyUtils.dissmissDialog(DetailSpotActivity.this);
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                MyUtils.dissmissDialog(DetailSpotActivity.this);
            }
        });


        webView.loadUrl(MyApplication.API_URL+"api/campaign/" + getIntent().getLongExtra("id", 1) + "/?hash="+((MyApplication)getApplicationContext()).getHash());
    }


}
