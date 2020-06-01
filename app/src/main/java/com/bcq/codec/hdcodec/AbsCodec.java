package com.bcq.codec.hdcodec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbsCodec extends Thread implements Icodec {
    protected final String TAG = this.getClass().getSimpleName();
    private static LinkedBlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<>();
    private OnCodecListeren codecListeren;
    private MediaCodec codec;
    private long startPts = 0;
    private MediaCodec.BufferInfo bufferInfo;

    @Override
    public void setOnCodecListeren(OnCodecListeren ocl) {
        this.codecListeren = ocl;
    }

    @Override
    public void putData(byte[] data) {
        if (null == data) return;
        try {
            dataQueue.put(data);
            startEncode();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean init() {
        if (null != codec) {
            return true;
        }
        String mineType = onGetCodecMime();
        MediaCodecInfo codecInfo = MediaCodecHelper.getCodecByMimeType(mineType);
        if (codecInfo == null) {
            Log.e(TAG, "no support codec for mimeType is " + mineType);
            return false;
        }
        MediaFormat format = onConfigure(codecInfo);
        if (null == format) {
            Log.e(TAG, "configure media_format is null !");
            return false;
        }
        Log.d(TAG, "format: " + format.toString());
        try {
            // aEncoder = MediaCodec.createDecoderByType (AUDIO_MIME_TYPE);
            codec = MediaCodec.createByCodecName(codecInfo.getName());
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            Log.d(TAG, "codec create success ：" + codec.getName());
            return true;
        } catch (IOException e) {
            Log.d(TAG, "crate codec by name error for exception !");
            e.printStackTrace();
        }
        return false;
    }

    boolean encoderLoop = false;

    @Override
    public void stopEncode() {
        encoderLoop = false;
    }

    protected void startEncode() {
        if (codec == null) {
            Log.d(TAG, "start encode for no init encoder !");
            return;
        }
        if (encoderLoop) {
            Log.d(TAG, "start audio encode error for alteraly start, you must stop it before !");
            return;
        }
        encoderLoop = true;
        start();
    }

    @Override
    @Deprecated
    public synchronized void start() {
        super.start();
    }

    @Override
    public void release() {
        Log.d(TAG, "release");
        if (null != codec) {
            //停止音频编码器
            codec.stop();
            //释放音频编码器
            codec.release();
        }
        codec = null;
        dataQueue.clear();
    }

    @Override
    public void run() {
        Log.d(TAG, "run audio encode !");
        codec.start();
        startPts = System.currentTimeMillis() * 1000;
        try {
            while (encoderLoop && !Thread.interrupted()) {
                byte[] data = dataQueue.take();
                bufferInfo = new MediaCodec.BufferInfo();
                data = onPreEncode(data);
                data = MediaCodecHelper.encode(data, codec, startPts, bufferInfo);
                if (null != data && codecListeren != null) {
                    codecListeren.onData(data, bufferInfo);
                }
            }
            release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 编码前数据转换处理
     *
     * @param data
     * @return
     */
    protected byte[] onPreEncode(byte[] data) {
        return data;
    }

    protected abstract String onGetCodecMime();

    protected abstract MediaFormat onConfigure(MediaCodecInfo codecInfo);
}
