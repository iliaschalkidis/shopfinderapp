package kiddo.android.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import static java.lang.Thread.sleep;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import kiddo.android.R;
import kiddo.android.ShopFinderApplication;
import kiddo.android.models.User;

public class MainActivity extends Activity{
    
    private EditText email;
    private EditText password;
    private Button btn;
    private String data;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        email=(EditText)findViewById(R.id.email);
        password =(EditText)findViewById(R.id.password);
        btn=(Button)findViewById(R.id.signin_btn);
        Log.d("ShopFinder","Main.onCreate() called!");
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d("ShopFinder","Main.onPause() called!");
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d("ShopFinder","Main.onDestroy() called!");
    }
    
    public void signIn(View v) throws InterruptedException{
        Log.d("ShopFinder","Main.signIn() called!");
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if(email.getText().toString().length()==0 || password.getText().toString().length()==0){
                    Toast.makeText(this, "Please fill information", Toast.LENGTH_LONG).show();
            }else{
                    new RESTClient().execute(email.getText().toString());
                    sleep(2000);
                    Log.d("ShopFinder", "Data: "+data);
                    ((ShopFinderApplication) this.getApplication()).setUser(new User(data));
                    
                    if (((ShopFinderApplication) this.getApplication()).getUser().getUser_id()!=0){
                        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(this, "Wrong username or password!", Toast.LENGTH_LONG).show();
                    }
            }
        }
        else {
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG).show();
        }
    } 
    
    public void signUp(View v) throws InterruptedException {
        Log.d("ShopFinder","Main.signUp() called!");
        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivity(intent);
    } 
    
    class RESTClient extends AsyncTask<String, Void, String> {

    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected String doInBackground(String... urls) {
        data = downloadUrl("http://192.168.1.3:8084/shopfinder/signin");
        return data;
    }

    @Override
    protected void onPostExecute(String result) {
    }
    
    private String downloadUrl(String myurl){
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            Map<String,Object> params = new LinkedHashMap();
            params.put("email", email.getText().toString());
            params.put("password", password.getText().toString());

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");
            Log.d("ShopFinder","email: "+email.getText().toString()+"password: "+password.getText().toString());
            conn.setRequestProperty("email", email.getText().toString());
            conn.setRequestProperty("password", password.getText().toString());
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            contentAsString =contentAsString.substring(0, contentAsString.lastIndexOf("}")+1);
            Log.d("ShopFinder", "The response is: " + contentAsString);
            return contentAsString;

        // Makes sure that the InputStream is closed after the app is
        // finished using it.
        } 
        catch(Exception ex){
            Log.d("ShopFinder", "POST failed!");
            return "";
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                }
            }
        }
    }
    
    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

}
}