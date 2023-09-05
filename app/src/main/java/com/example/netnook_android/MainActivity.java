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
    private static final String key = "dfnvwuvh02vj-293vfkjwgg23vb23vfjhwbvyHbpqjOvwvacavk''o1cnajcii";

    private EditText integrationKeyField;
    private EditText databaseIDField;
    private Button submit;

    private String integrationKey;
    private String databaseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get references to the two EditText fields
        integrationKeyField = findViewById(R.id.integrationKeyInput);
        databaseIDField = findViewById(R.id.databaseIDInput);
        submit = findViewById(R.id.submitButton);


        // Check if the user has already configured the keys, if so populate the above two Views with that information
        // Otherwise the function informs the user to enter the keys
        if(checkKeys(this)){
            integrationKeyField.setText(this.integrationKey);
            databaseIDField.setText(this.databaseId);
            // Handle the intent sent by the user
            handleIntent(getIntent());
        }else{
            Toast.makeText(this,"Please ensure that the configuration is correctly entered",3);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);


        // Check if the user has already configured the keys, if so populate the above two Views with that information
        // Otherwise the function informs the user to enter the keys
        if(checkKeys(this)){
            integrationKeyField.setText(this.integrationKey);
            databaseIDField.setText(this.databaseId);
            // Handle the intent sent by the user
            handleIntent(intent);
        }else{
            Toast.makeText(this,"Please ensure that the configuration is correctly entered",3);
        }
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
                        Intent i = new Intent(this, createNewBookmark.class);
                        // Pass the information into the new page
                        i.putExtra("url", url);
                        i.putExtra("title", title);
                        i.putExtra("integrationKey", this.integrationKey);
                        i.putExtra("databaseID", this.databaseId);
                        startActivity(i);
                    } catch (Exception e) {
                        Log.e("userMsg", "Error occurred");
                    }
                }else{
                    Toast.makeText(this,"Unsopported app", Toast.LENGTH_SHORT);
                }
            }
        }
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
     * Wrapper method for calling the saveKeys method
     * @param v Reference to the button view
     */
    public void saveWrapper(View v){
        if(saveKeys(this)){
            Log.v("userMsg", "Success");
        }
    }
}
