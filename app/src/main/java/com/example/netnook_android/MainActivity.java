package com.example.netnook_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.ClientError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Handle the intent sent by the user
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Handle the intent sent by the user
        handleIntent(intent);
    }

    /**
     * Function used for parsing the information the user has shared with the app and directly store in notion
     * @param intent Intent object containing information about how the app was called from outside
     */
    protected void handleIntent(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();
        String url;
        String title;
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                // Parse the information received by the intent
                Bundle extras = intent.getExtras();
                // Check if the bundle contains keys which we can use
                if(extras.containsKey("android.intent.extra.TEXT") && extras.containsKey("android.intent.extra.SUBJECT")){
                    url = extras.get("android.intent.extra.TEXT").toString();
                    title = extras.get("android.intent.extra.SUBJECT").toString();

                    // Construct the request body (JSON object) being sent to the server
                    try{
                        // Send the information to notion
                        sendToNotion(url, title, "fb62e501905e400c988b713255375537", "Testing android version", "secret_Ch48QmwyMenBcDyoIV96xTA4I5WaSy2vlvJMfBhRgEZ");
                    } catch (Exception e) {
                        Log.e("userMsg", "Error occurred");
                    }
                }
            }
        }
    }

    protected void sendToNotion(String url, String title, String dbID, String desc, String intKey){
        RequestQueue volleyQueue = Volley.newRequestQueue(MainActivity.this);
        String fetchUrl = "https://api.notion.com/v1/pages";

        // JSON object to send
        JSONObject requestBody = new JSONObject();

        // Construct the body of the request
        try {

            // Database info
            JSONObject parent = new JSONObject();
            parent.put("type", "database_id");
            parent.put("database_id", dbID);

            // Properties field
            JSONObject textTitle = new JSONObject();
            textTitle.put("content", title);
            JSONArray titleArray = new JSONArray();
            JSONObject text = new JSONObject();
            text.put("text", textTitle);
            titleArray.put(text);
            JSONObject TitleObject = new JSONObject();
            TitleObject.put("title", titleArray);

            JSONObject link = new JSONObject();
            link.put("url", url);

            JSONObject textDescription = new JSONObject();
            textDescription.put("content", desc);
            JSONArray richText = new JSONArray();
            JSONObject rich = new JSONObject();
            rich.put("text", textDescription);
            richText.put(rich);
            JSONObject description = new JSONObject();
            description.put("rich_text",richText);

            JSONObject properties = new JSONObject();
            properties.put("Title", TitleObject);
            properties.put("Link", link);
            properties.put("Description", description);

            requestBody.put("parent", parent);
            requestBody.put("properties", properties);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                fetchUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("userMsg", "Success");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("userMsg", "Failure");
                    }
                }
        ) {
            @Override
            public byte[] getBody() {
                // Convert the JSON request body to bytes.
                return requestBody.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                // Set the content type for the request body.
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() {
                // Set the required headers, including the Authorization header.
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer secret_Ch48QmwyMenBcDyoIV96xTA4I5WaSy2vlvJMfBhRgEZ");
                headers.put("Notion-Version", "2022-06-28");
                return headers;
            }
        };

        // Make the request
        volleyQueue.add(request);
    }
}