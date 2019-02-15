package com.enablex.multiconferencequickapp.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enablex.multiconferencequickapp.R;
import com.enablex.multiconferencequickapp.adapter.HorizontalViewAdapter;
import com.enablex.multiconferencequickapp.model.HorizontalViewModel;
import com.enablex.multiconferencequickapp.model.UserModel;
import com.enablex.multiconferencequickapp.utilities.OnDragTouchListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import enx_rtc_android.Controller.EnxPlayerView;
import enx_rtc_android.Controller.EnxRoom;
import enx_rtc_android.Controller.EnxRoomObserver;
import enx_rtc_android.Controller.EnxRtc;
import enx_rtc_android.Controller.EnxStream;
import enx_rtc_android.Controller.EnxStreamObserver;

public class VideoConferenceActivity extends AppCompatActivity implements EnxRoomObserver, EnxStreamObserver, View.OnClickListener {
    EnxRtc enxRtc;
    String token;
    String name;
    EnxPlayerView enxPlayerView;
    FrameLayout moderator;
    FrameLayout participant;
    ImageView disconnect;
    ImageView mute, video, camera, volume;
    private TextView audioOnlyText, dummyText;
    EnxRoom enxRooms;
    boolean isVideoMuted = false;
    boolean isAudioMuted = false;
    RelativeLayout rl;
    ArrayList<UserModel> userArrayList;
    Gson gson;
    EnxStream localStream;
    int PERMISSION_ALL = 1;

    List<HorizontalViewModel> list;
    private RecyclerView mHorizontalRecyclerView;
    private HorizontalViewAdapter horizontalAdapter;
    private LinearLayoutManager horizontalLayoutManager;
    private int screenWidth;
    ActionBar actionBar;
    RelativeLayout bottomView;
    boolean touchView;

    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_conference);

        actionBar = getSupportActionBar();

        getPreviousIntent();
        actionBar.setTitle(name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            } else {
                initialize();
            }
        }
    }

    private void initialize() {
        setUI();
        setClickListener();
        userArrayList = new ArrayList<>();
        list = new ArrayList<>();
        gson = new Gson();
        enxRtc = new EnxRtc(this, this, this);
        localStream = enxRtc.joinRoom(token, getLocalStreamJsonObjet());
        enxPlayerView = new EnxPlayerView(this, EnxPlayerView.ScalingType.SCALE_ASPECT_BALANCED, true);
        Log.e("localStream", localStream.toString());
        localStream.attachRenderer(enxPlayerView);
        moderator.addView(enxPlayerView);

        mHorizontalRecyclerView = (RecyclerView) findViewById(R.id.horizontalRecyclerView);

        horizontalAdapter = new HorizontalViewAdapter(list, this, screenWidth, screenWidth, false);

        horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mHorizontalRecyclerView.setLayoutManager(horizontalLayoutManager);
        mHorizontalRecyclerView.setAdapter(horizontalAdapter);
    }

    private void setClickListener() {
        disconnect.setOnClickListener(this);
        mute.setOnClickListener(this);
        video.setOnClickListener(this);
        camera.setOnClickListener(this);
        volume.setOnClickListener(this);
        moderator.setOnTouchListener(new OnDragTouchListener(moderator));

        participant.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                handleTouchListner();
                return false;
            }
        });
    }

    private void setUI() {
        moderator = (FrameLayout) findViewById(R.id.moderator);
        participant = (FrameLayout) findViewById(R.id.participant);
        disconnect = (ImageView) findViewById(R.id.disconnect);
        mute = (ImageView) findViewById(R.id.mute);
        video = (ImageView) findViewById(R.id.video);
        camera = (ImageView) findViewById(R.id.camera);
        volume = (ImageView) findViewById(R.id.volume);
        dummyText = (TextView) findViewById(R.id.dummyText);
        audioOnlyText = (TextView) findViewById(R.id.audioonlyText);
        rl = (RelativeLayout) findViewById(R.id.rl);
        bottomView = (RelativeLayout) findViewById(R.id.bottomView);

        audioOnlyText.setVisibility(View.GONE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels / 3;
    }

    private JSONObject getLocalStreamJsonObjet() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("audio", true);
            jsonObject.put("video", true);
            jsonObject.put("data", true);
            JSONObject videoSize = new JSONObject();
            videoSize.put("minWidth", 720);
            videoSize.put("minHeight", 480);
            videoSize.put("maxWidth", 1280);
            videoSize.put("maxHeight", 720);
            jsonObject.put("videoSize", videoSize);
            jsonObject.put("audioMuted", "true");
            jsonObject.put("videoMuted", "true");
            JSONObject attributes = new JSONObject();
            attributes.put("name", "myStream");
            jsonObject.put("attributes", attributes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void getPreviousIntent() {
        if (getIntent() != null) {
            token = getIntent().getStringExtra("token");
            name = getIntent().getStringExtra("name");
        }
    }

    @Override
    public void onRoomConnected(EnxRoom enxRoom, JSONObject jsonObject) {
        enxRooms = enxRoom;
        if (enxRoom != null) {
            enxRooms.publish(localStream);
        }
    }

    @Override
    public void onRoomError(JSONObject jsonObject) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VideoConferenceActivity.this, jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onUserConnected(JSONObject jsonObject) {
        Log.e("userConnected", jsonObject.toString());
        UserModel userModel = gson.fromJson(jsonObject.toString(), UserModel.class);
        userArrayList.add(userModel);
    }

    @Override
    public void onUserDisConnected(JSONObject jsonObject) {
        Log.e("userConnected", jsonObject.toString());
        UserModel userModel = gson.fromJson(jsonObject.toString(), UserModel.class);
        for (UserModel userModel1 : userArrayList) {
            if (userModel1.getClientId().equalsIgnoreCase(userModel.getClientId())) {
                userArrayList.remove(userModel);
            }
        }
    }

    @Override
    public void onPublishedStream(EnxStream enxStream) {
    }

    @Override
    public void onUnPublishedStream(EnxStream enxStream) {

    }

    @Override
    public void onStreamAdded(EnxStream enxStream) {
        if (enxStream != null) {
            enxRooms.subscribe(enxStream);
        }
    }

    @Override
    public void onSubscribedStream(EnxStream enxStream) {
    }

    @Override
    public void onUnSubscribedStream(EnxStream enxStream) {

    }

    @Override
    public void onRemovedStream(EnxStream enxStream) {

    }

    public void onRoomDisConnected(JSONObject jsonObject) {
        this.finish();
    }

    EnxPlayerView activePlayerView;

    @Override
    public void onActiveTalkerList(JSONObject jsonObject) {
        Log.e("activeList", jsonObject.toString());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (list != null) {
                        for (int i = 0; i < list.size(); i++) {
                            EnxPlayerView playerView = list.get(i).getEnxPlayerView();
                            EnxStream enxStream = list.get(i).getEnxStream();
                            if (playerView != null) {
                                playerView.release();
                                playerView = null;
                            }
                            if (enxStream != null) {
                                enxStream.detachRenderer();
                            }
                        }
                        list.removeAll(list);
                        list = null;
                    }

                    Map<String, EnxStream> map = enxRooms.getRemoteStreams();
                    JSONArray jsonArray = jsonObject.getJSONArray("activeList");


                    if (jsonArray.length() == 0) {
                        dummyText.setVisibility(View.VISIBLE);
                        audioOnlyText.setVisibility(View.GONE);
                        View temp = participant.getChildAt(0);
                        participant.removeView(temp);
                        return;
                    } else {
                        dummyText.setVisibility(View.GONE);
                    }

                    list = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonStreamid = jsonArray.getJSONObject(i);
                        String strteamID = jsonStreamid.getString("streamId");
                        String stremName = jsonStreamid.getString("name");
                        String mediatype = jsonStreamid.getString("mediatype");
                        EnxStream stream = map.get(strteamID);
                        JSONObject attributes = stream.getAttributes();
                        attributes.put("name", stremName);
                        attributes.put("actualName", stremName);

                        HorizontalViewModel horizontalRecyclerViewModel = new HorizontalViewModel();
                        EnxPlayerView remotePlayer = new EnxPlayerView(VideoConferenceActivity.this, EnxPlayerView.ScalingType.SCALE_ASPECT_BALANCED, true);
                        horizontalRecyclerViewModel.setEnxStream(stream);
                        horizontalRecyclerViewModel.setEnxPlayerView(remotePlayer);
                        horizontalRecyclerViewModel.setMediaType(mediatype);
                        horizontalRecyclerViewModel.setAudioOnly(getAudioOnly(mediatype));
                        list.add(horizontalRecyclerViewModel);

                        if (i == 0) {
                            if (activePlayerView == null) {
                                activePlayerView = new EnxPlayerView(VideoConferenceActivity.this, EnxPlayerView.ScalingType.SCALE_ASPECT_BALANCED, true);
                                activePlayerView.setZOrderMediaOverlay(false);
                                participant.addView(activePlayerView);
                            }

                            if (stream.getMedia() != null) {
                                stream.attachRenderer(activePlayerView);
                            } else {
                                Toast.makeText(VideoConferenceActivity.this, stremName + "null", Toast.LENGTH_SHORT).show();
                            }
                            actionBar.setTitle(stremName);
                            if (getAudioOnly(mediatype)) {
                                audioOnlyText.setVisibility(View.VISIBLE);
                                View temp1 = participant.getChildAt(0);
                                participant.removeView(temp1);
                            } else {
                                audioOnlyText.setVisibility(View.GONE);
                            }
                        }
                    }

                    List<HorizontalViewModel> tempList = new ArrayList<>();
                    for (int i = 0; i < list.size(); i++) {
                        if (i != 0) {
                            tempList.add(list.get(i));
                        }
                    }
                    horizontalAdapter.setItems(tempList);
                    horizontalAdapter.notifyDataSetChanged();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onEventError(JSONObject jsonObject) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VideoConferenceActivity.this, jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean getAudioOnly(String str) {
        if (str.equalsIgnoreCase("audio") || str.equalsIgnoreCase("audioOnly")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onAudioEvent(JSONObject jsonObject) {
        try {
            String message = jsonObject.getString("msg");
            if (message.equalsIgnoreCase("success")) {
                if (!isAudioMuted) {
                    mute.setImageResource(R.drawable.mute);
                    isAudioMuted = true;
                } else {
                    mute.setImageResource(R.drawable.unmute);
                    isAudioMuted = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoEvent(JSONObject jsonObject) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String message = jsonObject.getString("msg");
                    if (message.equalsIgnoreCase("Video on")) {
                        video.setImageResource(R.drawable.video_visible);
                        isVideoMuted = false;
                    } else if (message.equalsIgnoreCase("Video off")) {
                        video.setImageResource(R.drawable.video_off);
                        isVideoMuted = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onReceivedData(JSONObject jsonObject) {

    }

    @Override
    public void onRemoteStreamAudioMute(JSONObject jsonObject) {

    }

    @Override
    public void onRemoteStreamAudioUnMute(JSONObject jsonObject) {

    }

    @Override
    public void onRemoteStreamVideoMute(JSONObject jsonObject) {

    }

    @Override
    public void onRemoteStreamVideoUnMute(JSONObject jsonObject) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.disconnect:
                if (enxRooms != null) {
                    if (enxPlayerView != null) {
                        enxPlayerView.release();
                        enxPlayerView = null;
                    }

                    if (activePlayerView != null) {
                        activePlayerView.release();
                        activePlayerView = null;
                    }
                    enxRooms.disconnect();
                } else {
                    finish();
                }
                break;
            case R.id.mute:
                if (localStream != null) {
                    if (!isAudioMuted) {
                        localStream.muteSelfAudio(false);
                    } else {
                        localStream.muteSelfAudio(true);
                    }
                }
                break;
            case R.id.video:
                if (localStream != null) {
                    if (!isVideoMuted) {
                        isVideoMuted = true;
                        localStream.muteSelfVideo(false);
                    } else {
                        isVideoMuted = false;
                        localStream.muteSelfVideo(true);
                    }
                }
                break;
            case R.id.camera:
                localStream.switchCamera();
                camera.setImageResource(R.drawable.camera);
                break;
            case R.id.volume:
                if (enxRooms != null) {
                    showRadioButtonDialog();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    initialize();
                } else {
                    Toast.makeText(this, "Please enable permissions to further proceed.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void handleTouchListner() {
        if (touchView) {
            bottomView.setVisibility(View.VISIBLE);
            touchView = false;
        } else {
            bottomView.setVisibility(View.GONE);
            touchView = true;
        }
    }

    private void showRadioButtonDialog() {

        // custom dialog
        final Dialog dialog = new Dialog(VideoConferenceActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.radiogroup);
        List<String> stringList = new ArrayList<>();  // here is list

        List<String> deviceList = enxRooms.getDevices();
        for (int i = 0; i < deviceList.size(); i++) {
            stringList.add(deviceList.get(i));
        }
        RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);
        String selectedDevice = enxRooms.getSelectedDevice();
        if (selectedDevice != null) {
            for (int i = 0; i < stringList.size(); i++) {
                RadioButton rb = new RadioButton(VideoConferenceActivity.this); // dynamically creating RadioButton and adding to RadioGroup.
                rb.setText(stringList.get(i));
                rg.addView(rb);
                if (selectedDevice.equalsIgnoreCase(stringList.get(i))) {
                    rb.setChecked(true);
                }

            }
            dialog.show();
        }

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for (int x = 0; x < childCount; x++) {
                    RadioButton btn = (RadioButton) group.getChildAt(x);
                    if (btn.getId() == checkedId) {
                        Log.e("selected RadioButton->", btn.getText().toString());
                        enxRooms.setAudioDevice(btn.getText().toString());
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (enxRooms != null) {
            enxRooms = null;
        }
        if (enxRtc != null) {
            enxRtc = null;
        }
    }
}
