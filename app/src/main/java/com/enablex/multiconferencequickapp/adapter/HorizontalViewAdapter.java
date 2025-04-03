package com.enablex.multiconferencequickapp.adapter;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enablex.multiconferencequickapp.model.HorizontalViewModel;
import com.enablex.multipartyquickapp.R;

import java.util.List;

import enx_rtc_android.Controller.EnxPlayerView;
import enx_rtc_android.Controller.EnxStream;


public class HorizontalViewAdapter extends RecyclerView.Adapter<HorizontalViewAdapter.MessageViewHolder> {

    private List<HorizontalViewModel> streamArrayList;
    private Context context;
    int mHieght;
    int mWidth;
    boolean mSharedPrersentation;

    public HorizontalViewAdapter(List<HorizontalViewModel> horizontalList, Context context, int hieght, int width, boolean sharedPresentation) {
        this.streamArrayList = horizontalList;
        this.context = context;
        this.mHieght = hieght;
        this.mWidth = width;
        this.mSharedPrersentation = sharedPresentation;
    }

    @Override
    public int getItemCount() {
        if (streamArrayList.size() > 0) {
            return streamArrayList.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, final int position) {
        final EnxStream enxStream = streamArrayList.get(position).getEnxStream();
        final boolean isAudioOnly = streamArrayList.get(position).getEnxStream().isAudioOnlyStream();
        MessageViewHolder messageViewHolder = (MessageViewHolder) holder;

        try {
            if (enxStream == null) {
                return;
            }
            boolean mScreen = enxStream.hasScreen();
            EnxPlayerView enxPlayerView = streamArrayList.get(position).getEnxPlayerView();
            enxPlayerView.setId(Integer.valueOf(enxStream.getId()));
            if (mScreen) {
                enxPlayerView.setScalingType(EnxPlayerView.ScalingType.SCALE_ASPECT_FIT);//SCALE_ASPECT_BALANCED for full view other waise FIT is actual size
                enxPlayerView.setTag("screen");
            } else {
                enxPlayerView.setScalingType(EnxPlayerView.ScalingType.SCALE_ASPECT_BALANCED);
                enxPlayerView.setTag("not_screen");
            }

            String name = enxStream.getName();

            int count = messageViewHolder.relativeLayout.getChildCount();
            if (count > 0) {
                TextView textView = null;
                View surfaceview = null;
                for (int i = 0; i < count; i++) {
                    surfaceview = messageViewHolder.relativeLayout.getChildAt(i);
                    if (surfaceview instanceof EnxPlayerView) {
                        if (i == count - 1) {
                            textView = (TextView) messageViewHolder.relativeLayout.getChildAt(i - 1);
                        } else {
                            textView = (TextView) messageViewHolder.relativeLayout.getChildAt(i + 1);
                        }
                        break;
                    } else {
                        surfaceview = null;
                        textView = (TextView) messageViewHolder.relativeLayout.getChildAt(i);
                    }
                }
                if (surfaceview != null) {
                    EnxPlayerView sv = (EnxPlayerView) surfaceview;
                    messageViewHolder.relativeLayout.removeView(sv);
                    messageViewHolder.relativeLayout.removeView(textView);
                } else {
                    messageViewHolder.relativeLayout.removeView(textView);
                }
            }

            if (mSharedPrersentation) {
                if (position == 0) {
                    messageViewHolder.relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
                } else {
                    messageViewHolder.relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
                }
            } else {
                messageViewHolder.relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
            }

            messageViewHolder.relativeLayout.addView(enxPlayerView);

            if (enxStream.getMedia() != null) {
                enxStream.attachRenderer(enxPlayerView);
            } else {
                Toast.makeText(context, name + "null", Toast.LENGTH_SHORT).show();
            }

            if (!isAudioOnly) {
                messageViewHolder.audioOnlyText.setVisibility(View.GONE);
            } else {
                messageViewHolder.audioOnlyText.setVisibility(View.VISIBLE);
            }

            TextView tv = new TextView(context);
            tv.setTextColor(context.getResources().getColor(R.color.green));
            RelativeLayout.LayoutParams tvlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            tvlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            tvlp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            tv.setLayoutParams(tvlp);
            if (name != null) {
                tv.setText(name);
            }
            messageViewHolder.relativeLayout.addView(tv);


        } catch (Exception e) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relativeLayout;
        TextView audioOnlyText;

        private MessageViewHolder(View view) {
            super(view);
            relativeLayout = (RelativeLayout) view.findViewById(R.id.imageView);
            audioOnlyText = (TextView) view.findViewById(R.id.audioonlyAdapterText);
        }
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontalviewitem, parent, false);
        return new MessageViewHolder(itemView);
    }

    public void setItems(List<HorizontalViewModel> list) {
        this.streamArrayList = list;
    }
}
