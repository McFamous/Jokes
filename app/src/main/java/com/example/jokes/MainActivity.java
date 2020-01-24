package com.example.jokes;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static com.example.jokes.NetworkUtils.generateURL;
import static com.example.jokes.NetworkUtils.getResponseFromURL;

public class MainActivity extends AppCompatActivity {

    private  Button button;
    private  EditText editText;
    private  ListView listView;
    private  TextView textView;
    private  WebView webView;
    private static ArrayAdapter<String> adapter;
    private static ArrayList<String> data = new ArrayList<>();
    private static int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button= findViewById(R.id.button);
        editText = findViewById(R.id.editText);
        listView = findViewById(R.id.listView);
        textView = findViewById(R.id.textView);
        webView = findViewById(R.id.webView);

        if(savedInstanceState != null){
            data = savedInstanceState.getStringArrayList("list");
        }
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.item_jokes:
                        button.setVisibility(View.VISIBLE);
                        editText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.GONE);
                        webView.setVisibility(View.GONE);
                        setTitle("Jokes");
                        break;
                    case R.id.item_web:
                        button.setVisibility(View.GONE);
                        editText.setVisibility(View.GONE);
                        listView.setVisibility(View.GONE);
                        textView.setVisibility(View.VISIBLE);
                        setTitle("Api info");
                        break;
                }
                return false;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.clear();
                adapter.notifyDataSetChanged();
                if(!editText.getText().toString().equals(""))
                    setCount(Integer.parseInt(editText.getText().toString()));
                URL generate = generateURL(editText.getText().toString());
                new Task().execute(generate);

            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void onClick(View v) {

                webView.setVisibility(View.VISIBLE);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.loadUrl("http://www.icndb.com/api");

                WebViewClient webViewClient = new WebViewClient(){
                    @TargetApi(Build.VERSION_CODES.N)
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        view.loadUrl(request.getUrl().toString());
                        return true;
                    }
                    @SuppressWarnings("deprecation")
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                        Toast.makeText(getApplicationContext(),"Start loading",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        Toast.makeText(getApplicationContext(),"Loading finished",Toast.LENGTH_SHORT).show();
                    }
                };

                webView.setWebViewClient(webViewClient);

            }
        });
    }

    public static int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("list", data);
    }

    class Task extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            String response = null;
            try {
                response = getResponseFromURL(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject json = new JSONObject(response);
                JSONArray jsonArray = json.getJSONArray("value");

                for(int i = 0; i < count; i++){
                    JSONObject jokeInfo = jsonArray.getJSONObject(i);
                    String joke = jokeInfo.getString("joke");
                    if(joke.contains("&quot;")){
                        String str = joke.replace("&quot;","\"");
                        data.add(str);
                    }
                    else
                        data.add(joke);
                }
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
