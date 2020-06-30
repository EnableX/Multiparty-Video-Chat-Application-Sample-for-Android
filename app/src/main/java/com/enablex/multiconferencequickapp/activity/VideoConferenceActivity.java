package com.enablex.multiconferencequickapp.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
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

import enx_rtc_android.Controller.EnxActiveTalkerViewObserver;
import enx_rtc_android.Controller.EnxPlayerView;
import enx_rtc_android.Controller.EnxReconnectObserver;
import enx_rtc_android.Controller.EnxRoom;
import enx_rtc_android.Controller.EnxRoomObserver;
import enx_rtc_android.Controller.EnxRtc;
import enx_rtc_android.Controller.EnxStream;
import enx_rtc_android.Controller.EnxStreamObserver;

public class VideoConferenceActivity extends AppCompatActivity implements EnxRoomObserver, EnxStreamObserver, View.OnClickListener, EnxReconnectObserver, EnxActiveTalkerViewObserver {
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
    ProgressDialog progressDialog;
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
        localStream = enxRtc.joinRoom(token, getLocalStreamJsonObjet(), getReconnectInfo(), null);
        enxPlayerView = new EnxPlayerView(this, EnxPlayerView.ScalingType.SCALE_ASPECT_BALANCED, true);
        localStream.attachRenderer(enxPlayerView);
        moderator.addView(enxPlayerView);
        progressDialog = new ProgressDialog(this);
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
            jsonObject.put("audioMuted", "false");
            jsonObject.put("videoMuted", "false");
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
//received when user connected with Enablex room
        enxRooms = enxRoom;
        if (enxRooms != null) {
            enxRooms.publish(localStream);
            enxRooms.setReconnectObserver(this);
            enxRoom.setActiveTalkerViewObserver(this::onActiveTalkerList);
        }
    }

    @Override
    public void onRoomError(JSONObject jsonObject) {
        //received when any error occurred while connecting to the Enablex room
        Toast.makeText(VideoConferenceActivity.this, jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onUserConnected(JSONObject jsonObject) {
        // received when a new remote participant joins the call
        UserModel userModel = gson.fromJson(jsonObject.toString(), UserModel.class);
        userArrayList.add(userModel);
    }

    @Override
    public void onUserDisConnected(JSONObject jsonObject) {
        // received when a  remote participant left the call
        UserModel userModel = gson.fromJson(jsonObject.toString(), UserModel.class);
        for (UserModel userModel1 : userArrayList) {
            if (userModel1.getClientId().equalsIgnoreCase(userModel.getClientId())) {
                userArrayList.remove(userModel);
            }
        }
    }

    @Override
    public void onPublishedStream(EnxStream enxStream) {
        //received when audio video published successfully to the other remote users
    }

    @Override
    public void onUnPublishedStream(EnxStream enxStream) {
//received when audio video unpublished successfully to the other remote users
    }

    @Override
    public void onStreamAdded(EnxStream enxStream) {
        //received when a new stream added
        if (enxStream != null) {
            enxRooms.subscribe(enxStream);
        }
    }

    @Override
    public void onSubscribedStream(EnxStream enxStream) {
        //received when a remote stream subscribed successfully
    }

    @Override
    public void onUnSubscribedStream(EnxStream enxStream) {
//received when a remote stream unsubscribed successfully
    }

    public void onRoomDisConnected(JSONObject jsonObject) {
        //received when Enablex room successfully disconnected
        this.finish();
    }

    EnxPlayerView activePlayerView;

    @Override
    public void onActiveTalkerList(JSONObject jsonObject) {  // Depricated
//received when Active talker update happens
       /* try {
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
                EnxStream stream = map.get(strteamID);

                HorizontalViewModel horizontalRecyclerViewModel = new HorizontalViewModel();
                EnxPlayerView remotePlayer = new EnxPlayerView(VideoConferenceActivity.this, EnxPlayerView.ScalingType.SCALE_ASPECT_BALANCED, true);
                horizontalRecyclerViewModel.setEnxStream(stream);
                horizontalRecyclerViewModel.setEnxPlayerView(remotePlayer);
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
                        Toast.makeText(VideoConferenceActivity.this, stream.getName() + "null", Toast.LENGTH_SHORT).show();
                    }
                    actionBar.setTitle(stream.getName());
                    if (stream.isAudioOnlyStream()) {
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
        }*/
    }

    RecyclerView mRecyclerView;

    boolean touch = false;

    @Override
    public void onActiveTalkerList(RecyclerView recyclerView) {

        mRecyclerView = recyclerView;
        if (recyclerView == null) {
            participant.removeAllViews();
            dummyText.setVisibility(View.VISIBLE);

        } else {
            dummyText.setVisibility(View.GONE);
            participant.removeAllViews();
            participant.addView(recyclerView);

        }

        if (touch) {
            return;
        }
        if (mRecyclerView != null) {
            touch = true;
            mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                @Override
                public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
                    if (motionEvent.getAction() == 1) {
                        handleTouchListner();
                    }
                    return false;
                }

                @Override
                public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

                }

                @Override
                public void onRequestDisallowInterceptTouchEvent(boolean b) {

                }
            });
        }

    }

    @Override
    public void onEventError(JSONObject jsonObject) {
//received when any error occurred for any room event
        Toast.makeText(VideoConferenceActivity.this, jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEventInfo(JSONObject jsonObject) {
// received for different events update
    }

    @Override
    public void onNotifyDeviceUpdate(String s) {
// received when when new media device changed
    }

    @Override
    public void onAcknowledgedSendData(JSONObject jsonObject) {
// received your chat data successfully sent to the other end
    }

    @Override
    public void onMessageReceived(JSONObject jsonObject) {
// received when chat data received at room
    }


    @Override
    public void onUserDataReceived(JSONObject jsonObject) {
// received when custom data received at room
    }

    @Override
    public void onSwitchedUserRole(JSONObject jsonObject) {
// received when user switch their role (from moderator  to participant)
    }

    @Override
    public void onUserRoleChanged(JSONObject jsonObject) {
// received when user role changed successfully
    }

    @Override
    public void onConferencessExtended(JSONObject jsonObject) {

    }

    @Override
    public void onConferenceRemainingDuration(JSONObject jsonObject) {

    }

    @Override
    public void onAckDropUser(JSONObject jsonObject) {

    }

    @Override
    public void onAckDestroy(JSONObject jsonObject) {

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
        //received when audio mute/unmute happens
        try {
            String message = jsonObject.getString("msg");
            if (message.equalsIgnoreCase("Audio On")) {
                mute.setImageResource(R.drawable.unmute);
                isAudioMuted = false;
            } else if (message.equalsIgnoreCase("Audio Off")) {
                mute.setImageResource(R.drawable.mute);
                isAudioMuted = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoEvent(JSONObject jsonObject) {
        //received when video mute/unmute happens
        try {
            String message = jsonObject.getString("msg");
            if (message.equalsIgnoreCase("Video On")) {
                video.setImageResource(R.drawable.ic_videocam);
                isVideoMuted = false;
            } else if (message.equalsIgnoreCase("Video Off")) {
                video.setImageResource(R.drawable.ic_videocam_off);
                isVideoMuted = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceivedData(JSONObject jsonObject) {
//received when chat data received at room level
    }

    @Override
    public void onRemoteStreamAudioMute(JSONObject jsonObject) {
//received when any remote stream mute audio
    }

    @Override
    public void onRemoteStreamAudioUnMute(JSONObject jsonObject) {
//received when any remote stream unmute audio
    }

    @Override
    public void onRemoteStreamVideoMute(JSONObject jsonObject) {
//received when any remote stream mute video
    }

    @Override
    public void onRemoteStreamVideoUnMute(JSONObject jsonObject) {
//received when any remote stream unmute video
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
                        localStream.muteSelfAudio(true);
                    } else {
                        localStream.muteSelfAudio(false);
                    }
                }
                break;
            case R.id.video:
                if (localStream != null) {
                    if (!isVideoMuted) {
                        localStream.muteSelfVideo(true);
                    } else {
                        localStream.muteSelfVideo(false);
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
                        enxRooms.switchMediaDevice(btn.getText().toString());
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

    public JSONObject getReconnectInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("allow_reconnect",true);
            jsonObject.put("number_of_attempts",3);
            jsonObject.put("timeout_interval",15);
            jsonObject.put("activeviews","view");//view

            JSONObject object = new JSONObject();
            object.put("audiomute",true);
            object.put("videomute",true);
            object.put("bandwidth",true);
            object.put("screenshot",true);
            object.put("avatar",true);

            object.put("iconColor", getResources().getColor(R.color.colorPrimary));
            object.put("iconHeight",30);
            object.put("iconWidth",30);
            object.put("avatarHeight",200);
            object.put("avatarWidth",200);
            jsonObject.put("playerConfiguration",object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public void onReconnect(String message) {
        // received when room tries to reconnect due to low bandwidth or any connection interruption
        try {
            if (message.equalsIgnoreCase("Reconnecting")) {
                progressDialog.setMessage("Wait, Reconnecting");
                progressDialog.show();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUserReconnectSuccess(EnxRoom enxRoom, JSONObject jsonObject) {
        // received when reconnect successfully completed
        try {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    EnxPlayerView playerView = list.get(i).getEnxPlayerView();
                    EnxStream enxStream = list.get(i).getEnxStream();
                    if (playerView != null) {
                        playerView.release();
                    }
                    if (enxStream != null) {
                        enxStream.detachRenderer();
                    }
                }
                list.removeAll(list);
                list = null;
            }
            Toast.makeText(this, "Reconnect Success", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
