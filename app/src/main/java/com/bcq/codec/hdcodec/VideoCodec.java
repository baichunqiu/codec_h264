package com.bcq.codec.hdcodec;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

public class VideoCodec extends AbsCodec {
    private static final String TAG = "AudioCodec";
    // I-frames
    private static final int IFRAME_INTERVAL = 5; // 10 between
    //预览格式转换后的数据
    private int mColorFormat = 0;
    private MediaFormat videoFormat;
    private String mimeType;
    private int width;
    private int height;
    private int fps;

    public VideoCodec(int widht, int height, int fps, String mimeType) {
        this.width = widht;
        this.height = height;
        this.fps = fps;
        this.mimeType = mimeType;
    }

    @Override
    protected String onGetCodecMime() {
        return mimeType;
    }

    @Override
    protected MediaFormat onConfigure(MediaCodecInfo codecInfo) {
        mColorFormat = MediaCodecHelper.getRecognizeCorlorFormat(codecInfo, mimeType,RECOGNIZED_COLOR_FORMAT);
        if (mColorFormat == 0) {
            Log.e(TAG, "couldn't find a good color format for codec " + codecInfo.getName());
            return null;
        }
        videoFormat = MediaFormat.createVideoFormat(mimeType, height, width);
        int bitrate = (width * height * 3 / 2) * 8 * fps;
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);//码流
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, fps);//帧率mFps
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat); //设置颜色格式
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);//设置关键帧的时间：3—`8
        return videoFormat;
    }

    @Override
    protected byte[] onPreEncode(byte[] data) {
        //编码前 根据当前colorFromat 进行颜色转换
        return MediaCodecHelper.colorFormat(data, mColorFormat, width, height);
    }
}
