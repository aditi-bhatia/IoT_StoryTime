package com.example.aditi.a272test2;

        import java.io.InputStream;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.util.ArrayList;

        import android.graphics.Movie;
        import java.util.Locale;
        import android.app.Activity;
        import android.content.ActivityNotFoundException;
        import android.content.Intent;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.speech.RecognizerIntent;
        import android.view.View;
        import android.webkit.WebView;
        import android.webkit.WebViewClient;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageButton;
        import android.widget.TextView;
        import android.widget.Toast;
        import android.widget.VideoView;

        import javax.json.Json;
        import javax.json.JsonArray;
        import javax.json.JsonObject;
        import javax.json.JsonReader;

        import static android.graphics.Movie.decodeStream;

public class MainActivity extends Activity {
    private final int SPEECH_RECOGNITION_CODE = 1;
    private TextView txtOutput;
    private ImageButton btnMicrophone;

    private WebView back;
  //  private Button btn;
    private EditText tmpIn;
    private String api_path = "http://api.giphy.com/v1/gifs/search?&api_key=dc6zaTOxFJmzC&limit=1";
    private String query = "&q=";

    /*
        Download Task
        Param1: Type of var to send to class DownloadTask
        Parma2: Name of the method that shows progress
        Params3: Return var type
     */
    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params){
            String gif_url ="";
            InputStream is;
            URL url;
            try {
                url = new URL(params[0]);
                is = url.openStream();
                JsonReader rdr = Json.createReader(is);
                JsonObject obj = rdr.readObject();
                JsonArray results = obj.getJsonArray("data");
                JsonObject result = results.getValuesAs(JsonObject.class).get(0);
                gif_url = result.getJsonObject("images").getJsonObject("original").getString("mp4");

//                System.out.println(gif_url);
//                url = new URL(gif_url);
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.connect();
//                InputStream ls = connection.getInputStream();
//                gif = decodeStream(ls);

            }catch (Exception e){
                e.printStackTrace();
            }
            return gif_url;
        }

        @Override
        protected void onPostExecute(String gif_url){
            super.onPostExecute(gif_url);
            try {
                back.getSettings().setJavaScriptEnabled(true);
                back.setWebViewClient(new WebViewClient());
                back.loadUrl(gif_url);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtOutput = (TextView) findViewById(R.id.txt_output);
        btnMicrophone = (ImageButton) findViewById(R.id.btn_mic);
       /* btnMicrophone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechToText();
            }
        });*/

        back = (WebView) findViewById(R.id.bckgrnd);
      //  btn = (Button) findViewById(R.id.tempButton);
        tmpIn = (EditText) findViewById(R.id.tempinput);
        btnMicrophone.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startSpeechToText();


            }
        });
    }
    /**
     * Start speech to text intent. This opens up Google Speech Recognition API dialog box to listen the speech input.
     * */
    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
         intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speak something...");
        try {
            startActivityForResult(intent, SPEECH_RECOGNITION_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Speech recognition is not supported on this device.",
                    Toast.LENGTH_SHORT).show();

           /* String appPackageName = "com.google.android.googlequicksearchbox";
           try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }*/
        }
    }

    /**
     * Callback for speech recognition activity
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SPEECH_RECOGNITION_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String text = result.get(0);
                   // txtOutput.setText(text);
                    tmpIn.setText(text);

                    try {
                        new DownloadTask().execute(api_path + query + tmpIn.getText());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }
}