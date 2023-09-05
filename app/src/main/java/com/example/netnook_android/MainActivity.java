package com.example.netnook_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import android.content.Context;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class MainActivity extends AppCompatActivity {
    private static String key = "dfnvwuvh02vj-293vfkjwgg23vb23vfjhwbvyHbpqjOvwvacavk''o1cnajcii";

    private EditText integrationKeyField;
    private EditText databaseIDField;
    private Button submit;

    private String integrationKey;
    private String databaseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Handle the intent sent by the user
        handleIntent(getIntent());

        // Get references to the two EditText fields
        integrationKeyField = findViewById(R.id.integrationKeyInput);
        databaseIDField = findViewById(R.id.databaseIDInput);
        submit = findViewById(R.id.submitButton);


        // Check if the user has already configured the keys, if so populate the above two Views with that information
        // Otherwise the function informs the user to enter the keys
        if(checkKeys(this)){
            integrationKeyField.setText(this.integrationKey);
            databaseIDField.setText(this.databaseId);
        }

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

    /**
     * Function used for creating a request to the Notion API to create an entry for the given database ID.
     * @param url URL to be stored
     * @param title Title to be stored
     * @param dbID  Database ID inside of notion to store the new entry
     * @param desc  Description to be stored
     * @param intKey Integration key of the user
     */
    protected void sendToNotion(String url, String title, String dbID, String desc, String intKey){
        RequestQueue volleyQueue = Volley.newRequestQueue(MainActivity.this);
        // Notion API endpoint
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

            // Connect all objects
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

    /**
     * Method to store the integration and database keys in encrypted shared preferences
     * @param context Context
     * @return
     */
    private boolean saveKeys(Context context){
        try{
            String intKey = this.integrationKeyField.getText().toString();
            String dbID = this.databaseIDField.getText().toString();

            // Check if the two fields are empty or not
            if(intKey.isEmpty() || dbID.isEmpty()){
                Toast.makeText(this, "Please enter values for both the fields", Toast.LENGTH_SHORT)
                        .show();
                return false;
            }

            // Either builds or gets the encryption key
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    key,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            // Save the integration key and the database id
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("intKey", intKey);
            editor.putString("dbID", dbID);
            editor.apply();
        }catch (Exception e){
            Log.v("userMsg", e.toString());
            return false;
        }
        return true;
    }

    /**
     * Wrapper function to link the button click and the save keys
     */
    public void saveWrapper(View v){
        saveKeys(this);
    }

    /**
     * Method called at the beginning of the application start to populate the integration key and the database ID fields with the information the user had previously entered.
     * If the keys are in the store, it poluates the two fields in the UI
     * @param context Context
     * @return Returns true if the keys were stored, false otherwise
     */
    private boolean checkKeys(Context context){

        try{
            // Either builds or gets the encryption key
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    key,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            if(sharedPreferences.contains("intKey") &&  sharedPreferences.contains("dbID")){
                this.integrationKey = sharedPreferences.getString("intKey", "Invalid");
                this.databaseId = sharedPreferences.getString("dbID", "Invalid");
                return true;
            }else{
                // Alert the user to make sure that their information is properly entered
                Toast.makeText(this, "Please ensure both the integration and datbase keys are entered", Toast.LENGTH_SHORT).show();
                return false;
            }
        }catch(Exception e){
            Log.v("userMsg", e.toString());
        }
        return false;
    }
}