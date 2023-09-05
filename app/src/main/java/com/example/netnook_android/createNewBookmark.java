package com.example.netnook_android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class createNewBookmark extends AppCompatActivity {

    private EditText url;
    private EditText title;
    private EditText description;
    private static String integrationKey;
    private static String databaseID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_bookmark);

        // Instantiate the private properties
        url = findViewById(R.id.urlInput);
        title = findViewById(R.id.titleInput);
        description = findViewById(R.id.descriptionInput);

        // Populate the fields and get the configuration
        Intent i = getIntent();
        Bundle b = i.getExtras();
        integrationKey = b.getString("integrationKey");
        databaseID = b.getString("databaseID");
        url.setText(b.getString("url"));
        title.setText(b.getString("title"));
    }

    /**
     * Method called when the button is clicked to finally save the bookmark in Notion
     * @param v Reference to the button object
     */
    public void submit(View v){
        // Submit it to notion
        sendToNotion(this.integrationKey, description.getText().toString());

        // Return back to the configuration screen
        this.finish();
    }

    /**
     * Create the JSON object for the body of the request to Notion API
     * @param desc Description to be sent
     * @return JSON object used for the body of the request
     */
    protected JSONObject createRequestBody(String desc){
        // JSON object to send
        JSONObject requestBody = new JSONObject();

        try{
            // Database info
            JSONObject parent = new JSONObject();
            parent.put("type", "database_id");
            parent.put("database_id", this.databaseID);

            // Properties field
            JSONObject textTitle = new JSONObject();
            textTitle.put("content", this.title.getText());
            JSONArray titleArray = new JSONArray();
            JSONObject text = new JSONObject();
            text.put("text", textTitle);
            titleArray.put(text);
            JSONObject TitleObject = new JSONObject();
            TitleObject.put("title", titleArray);

            JSONObject link = new JSONObject();
            link.put("url", this.url.getText());

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

            // Connect all objects
            requestBody.put("parent", parent);
            requestBody.put("properties", properties);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return requestBody;
    }

    /**
     * @param intKey Notion integration key
     * Function used for creating a request to the Notion API to create an entry for the given database ID.
     */
    protected void sendToNotion(String intKey, String desc){
        RequestQueue volleyQueue = Volley.newRequestQueue(this);
        // Notion API endpoint
        String fetchUrl = "https://api.notion.com/v1/pages";

        // Construct the body of the request
        JSONObject requestBody = createRequestBody(desc);

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
                headers.put("Authorization", "Bearer " + intKey);
                headers.put("Notion-Version", "2022-06-28");
                return headers;
            }
        };

        // Make the request
        volleyQueue.add(request);
    }

}