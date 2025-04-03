package com.enablex.multiconferencequickapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.enablex.multiconferencequickapp.ApplicationController;
import com.enablex.multiconferencequickapp.web_communication.WebCall;
import com.enablex.multiconferencequickapp.web_communication.WebConstants;
import com.enablex.multiconferencequickapp.web_communication.WebResponse;
import com.enablex.multipartyquickapp.R;

import org.json.JSONException;
import org.json.JSONObject;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener, WebResponse {
    private EditText name;
    private EditText roomId;
    private Button joinRoom;
    private Button createRoom;
    private String token;
    private SharedPreferences sharedPreferences;
    private String room_Id;
    private RadioGroup radioGroup;
    private String role = "moderator";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        getSupportActionBar().setTitle("QuickApp");
        sharedPreferences = ApplicationController.getSharedPrefs();
        setView();
        setClickListener();
        getSupportActionBar().setTitle("Quick App");
        setSharedPreference();
    }

    @Override
    public void onClick(View view) {
        int id=view.getId();

            if(id== R.id.createRoom) {

                new WebCall(this, this, null, WebConstants.getRoomId, WebConstants.getRoomIdCode, false, true).execute();

            }
            else if(id== R.id.joinRoom){
                room_Id = roomId.getText().toString();
                if (validations()) {
                    validateRoomIDWebCall();
                }

        }
    }

    private boolean validations() {
        if (name.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please Enter name", Toast.LENGTH_SHORT).show();
            return false;
        } else if (roomId.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please create Room Id.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void validateRoomIDWebCall() {
        new WebCall(this, this, null, WebConstants.validateRoomId + room_Id, WebConstants.validateRoomIdCode, true, false).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id=item.getItemId();
            if(id== R.id.action_share){
                if (!name.getText().toString().equalsIgnoreCase("") && !roomId.getText().toString().equalsIgnoreCase("")) {
                    String shareBody = "Hi,\n" + name.getText().toString() + " has invited you to join room with Room Id " + roomId.getText().toString();
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    startActivity(sharingIntent);
                } else {
                    Toast.makeText(this, "Please create Room first.", Toast.LENGTH_SHORT).show();
                }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWebResponse(String response, int callCode) {
        switch (callCode) {
            case WebConstants.getRoomIdCode:
                onGetRoomIdSuccess(response);
                break;
            case WebConstants.getTokenURLCode:
                onGetTokenSuccess(response);
                break;
            case WebConstants.validateRoomIdCode:
                onVaidateRoomIdSuccess(response);
                break;
        }

    }

    private void onVaidateRoomIdSuccess(String response) {
        Log.e("responsevalidate", response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.optString("result").trim().equalsIgnoreCase("40001")) {
                Toast.makeText(this, jsonObject.optString("error"), Toast.LENGTH_SHORT).show();
            } else {
                savePreferences();
                getRoomTokenWebCall();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void onGetTokenSuccess(String response) {
        Log.e("responseToken", response);

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.optString("result").equalsIgnoreCase("0")) {
                token = jsonObject.optString("token");
                Log.e("token", token);
                Intent intent = new Intent(DashboardActivity.this, VideoConferenceActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("name", name.getText().toString());
                startActivity(intent);
            } else {
                Toast.makeText(this, jsonObject.optString("error"), Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onGetRoomIdSuccess(String response) {
        Log.e("responseDashboard", response);

        try {
            JSONObject jsonObject = new JSONObject(response);
            room_Id = jsonObject.optJSONObject("room").optString("room_id");
        } catch (JSONException e) {

            e.printStackTrace();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                roomId.setText(room_Id);
            }
        });
    }

    @Override
    public void onWebResponseError(String error, int callCode) {
        Log.e("errorDashboard", error);
    }

    private void setSharedPreference() {
        if (sharedPreferences != null) {
            if (!sharedPreferences.getString("name", "").isEmpty()) {
                name.setText(sharedPreferences.getString("name", ""));
            }
            if (!sharedPreferences.getString("room_id", "").isEmpty()) {
                roomId.setText(sharedPreferences.getString("room_id", ""));
            }
        }
    }

    private void setClickListener() {
        createRoom.setOnClickListener(this);
        joinRoom.setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == 0) {
                    role = "moderator";
                } else {
                    role = "participant";
                }
            }
        });
    }

    private void setView() {
        name = (EditText) findViewById(R.id.name);
        roomId = (EditText) findViewById(R.id.roomId);
        createRoom = (Button) findViewById(R.id.createRoom);
        joinRoom = (Button) findViewById(R.id.joinRoom);
        radioGroup = (RadioGroup) findViewById(R.id.radioButtonGroup);
    }

    private JSONObject jsonObjectToSend() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", "Test Dev Room");
            jsonObject.put("settings", getSettingsObject());
            jsonObject.put("data", getDataObject());
            jsonObject.put("sip", getSIPObject());
            jsonObject.put("owner_ref", "fadaADADAAee");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private JSONObject getSIPObject() {
        JSONObject jsonObject = new JSONObject();
        return jsonObject;
    }

    private JSONObject getDataObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private JSONObject getSettingsObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("description", "Testing");
            jsonObject.put("scheduled", false);
            jsonObject.put("scheduled_time", "");
            jsonObject.put("duration", 50);
            jsonObject.put("participants", 10);
            jsonObject.put("billing_code", 1234);
            jsonObject.put("auto_recording", false);
            jsonObject.put("active_talker", true);
            jsonObject.put("quality", "HD");
            jsonObject.put("wait_moderator", false);
            jsonObject.put("adhoc", false);
            jsonObject.put("mode", "group");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void getRoomTokenWebCall() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name.getText().toString());
            jsonObject.put("role", role);
            jsonObject.put("user_ref", "2236");
            jsonObject.put("roomId", room_Id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!name.getText().toString().isEmpty() && !roomId.getText().toString().isEmpty()) {
            new WebCall(this, this, jsonObject, WebConstants.getTokenURL, WebConstants.getTokenURLCode, false, false).execute();
        }
    }


    private void savePreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name.getText().toString());
        editor.putString("room_id", room_Id);
        editor.commit();

    }
}
