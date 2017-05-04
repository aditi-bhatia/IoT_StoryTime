package com.example.aditi.a272test2;

        import java.io.InputStream;
        import java.net.URL;
        import java.util.ArrayList;

        import java.util.Arrays;
        import java.util.HashSet;
        import java.util.List;
        import java.util.Locale;
        import java.util.Map;
        import java.util.Random;
        import java.util.Set;
        import java.util.Timer;
        import java.util.TimerTask;
        import java.util.concurrent.ExecutionException;

        import android.app.Activity;
        import android.content.ActivityNotFoundException;
        import android.content.Intent;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.speech.RecognizerIntent;
        import android.util.Log;
        import android.view.View;
        import android.webkit.WebView;
        import android.webkit.WebViewClient;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageButton;
        import android.widget.TextView;
        import android.widget.Toast;

        import javax.json.Json;
        import javax.json.JsonArray;
        import javax.json.JsonObject;
        import javax.json.JsonReader;

        import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
        import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.*;
        import com.philips.lighting.hue.listener.PHLightListener;
        import com.philips.lighting.hue.sdk.PHHueSDK;
        import com.philips.lighting.model.PHBridge;
        import com.philips.lighting.model.PHBridgeResource;
        import com.philips.lighting.model.PHHueError;
        import com.philips.lighting.model.PHLight;
        import com.philips.lighting.model.PHLightState;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

public class MainActivity extends Activity {
    private final int SPEECH_RECOGNITION_CODE = 1;
    private TextView txtOutput;
    private ImageButton btnMicrophone;

    private WebView back;
    private Button btn;
    private EditText tmpIn;
    private String api_path = "http://api.giphy.com/v1/gifs/search?&api_key=dc6zaTOxFJmzC&limit=1&rating=y";
    private String query = "&q=";


    private PHHueSDK phHueSDK;
    private static final int MAX_HUE=65535;
    public static final String TAG = "QuickStart";
    private static final Set<String> TEXTS = new HashSet<String>(Arrays.asList(
            new String[] {"BIRD","APPLE","GRASS","SKY"}
    ));
    /*
        Param1: Type of var to send to Task class
        Param2: Name of the method that shows progress
        Param3: Return var type
     */
    private class LightTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {
            randomLights(params[0], params[1], params[2], params[3]);
            return null;
        }

        protected void onPostExecute(String response) {

        }
    }
    public void randomLights(final String anger, String fear, String joy, String sadness) {

        // Random rand = new Random();
        PHBridge bridge = phHueSDK.getSelectedBridge();

        List<PHLight> allLights = bridge.getResourceCache().getAllLights();
        final PHLightState lightState = new PHLightState();
        for (PHLight light : allLights) {


            new Timer().scheduleAtFixedRate(new TimerTask() {

                String[] words = {" BIRD", "APPLE", "SKY","GRASS"};

                @Override
                public void run() {
                    System.out.println(anger);
                    Random rand = new Random();
                    int index = rand.nextInt(4);
                    String word = words[index];
                    if(word.contains("BIRD")){
                        lightState.setHue(10000);
                    }
                    if(word.contains("APPLE")){
                        lightState.setHue(1000);
                    }
                    if (word.contains("SKY"))
                    {
                        lightState.setHue(43255);
                    }

                    if(word.contains("GRASS"))
                    {
                        lightState.setHue(23454);
                    }

                    // lightState.setHue(rand.nextInt(MAX_HUE));
                    //To validate your light-state is valid (before sending to the bridge) you can use:

                }
            }, 0, 1000);//put here time 1000 milliseconds=1 second


            String validState = lightState.validateState();
            bridge.updateLightState(light, lightState, listener);
            bridge.updateLightState(light, lightState);   // If no bridge response is required then use this simpler form.

        }
    }
    PHLightListener listener = new PHLightListener() {

        @Override
        public void onSuccess() {
        }

        @Override
        public void onStateUpdate(Map<String, String> arg0, List<PHHueError> arg1) {
            Log.w(TAG, "Light has updated");
        }

        @Override
        public void onError(int arg0, String arg1) {}

        @Override
        public void onReceivingLightDetails(PHLight arg0) {}

        @Override
        public void onReceivingLights(List<PHBridgeResource> arg0) {}

        @Override
        public void onSearchComplete() {}
    };

    @Override
    protected void onDestroy() {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        if (bridge != null) {

            if (phHueSDK.isHeartbeatEnabled(bridge)) {
                phHueSDK.disableHeartbeat(bridge);
            }

            phHueSDK.disconnect(bridge);
            super.onDestroy();
        }
    }
    private class WatsonUnderstandTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
                    NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27,
                    "9af3dd26-8450-48fb-8878-c1b697a7330e",
                    "esyU4rPHDcMN"
            );
            String toAnalyze = params[0];
            KeywordsOptions keywords= new KeywordsOptions.Builder()
                    .sentiment(true)
                    .emotion(true)
                    .limit(3)
                    .build();

            Features features = new Features.Builder()
                    .keywords(keywords)
                    .build();

            AnalyzeOptions parameters = new AnalyzeOptions.Builder()
                    .text(toAnalyze)
                    .features(features)
                    .build();

            AnalysisResults response = service
                    .analyze(parameters)
                    .execute();
            return response.getKeywords().toString();
        }

        protected void onPostExecute(String response) {
            System.out.println(response);
            try {
                JSONArray arr = new JSONArray(response);
                JSONObject emotion = arr.getJSONObject(0);
                emotion = emotion.getJSONObject("emotion");
                new LightTask().execute(emotion.getString("anger"), emotion.getString("fear"), emotion.getString("joy"), emotion.getString("sadness"));
//                System.out.println("Emotion:::\n"+emotion.getString("anger"));
                for (int i = arr.length() - 1; i >= 0; i--){
                    JSONObject object = arr.getJSONObject(i);
                    new DownloadTask().execute(api_path + query + object.getString("text"));
//                    Thread.sleep(2000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String>{

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
        phHueSDK = PHHueSDK.create();
        txtOutput = (TextView) findViewById(R.id.txt_output);
        btnMicrophone = (ImageButton) findViewById(R.id.btn_mic);

        back = (WebView) findViewById(R.id.bckgrnd);
        btn = (Button) findViewById(R.id.tmpBtn);
        tmpIn = (EditText) findViewById(R.id.tempinput);

        btnMicrophone.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startSpeechToText();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = tmpIn.getText().toString();
                txtOutput.setText(text);
                //if text contains "mother" replace with daughter
                new WatsonUnderstandTask().execute(text);
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
                    txtOutput.setText(text);
//                    tmpIn.setText(text);

//                    try {
//                        new DownloadTask().execute(api_path + query + tmpIn.getText());
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
                break;
            }
        }
    }
}