//package com.bcq.codec.hdcodec;
//
//import android.media.MediaCodec;
//import android.media.MediaCodecInfo;
//import android.media.MediaFormat;
//import android.util.Log;
//
//import java.io.IOException;
//import java.util.concurrent.LinkedBlockingQueue;
//
//public class VideoCodec_abs extends Thread implements Icodec {
//    private static final String TAG = "AudioCodec";
//    private static LinkedBlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<>();
//    private OnCodecListeren codecListeren;
//    private MediaCodec vEncoder;                // API >= 16(Android4.1.2)
//    private static final String VIDEO_MIME_TYPE = "video/avc"; // H.264 Advanced Video
//    // I-frames
//    private static final int IFRAME_INTERVAL = 5; // 10 between
//    //预览格式转换后的数据
//    private int mColorFormat = 0;
//    private MediaCodec.BufferInfo vBufferInfo;
//    private volatile boolean videoEncoderLoop = false;
//    private MediaFormat videoFormat;
//    private long startPts;
//    private int widht;
//    private int height;
//
//    @Override
//    public boolean init(int width, int height, int fps) {
//        if (vEncoder != null) {
//            return true;
//        }
//        this.widht = width;
//        this.height = height;
//        Log.d(TAG, "width: " + width + "  height: " + height);
//        vBufferInfo = new MediaCodec.BufferInfo();
//        //选择系统用于编码H264的编码器信息
//        MediaCodecInfo vCodecInfo = MediaCodecHelper.getCodecByMimeType(VIDEO_MIME_TYPE);
//        if (vCodecInfo == null) {
//            Log.e(TAG, "no support codec for mimeType is " + VIDEO_MIME_TYPE);
//            return false;
//        }
//        Log.d(TAG, "======zhongjihao====found video codec: " + vCodecInfo.getName());
//        //根据MIME格式,选择颜色格式
//        mColorFormat = MediaCodecHelper.getRecognizeCorlorFormat(vCodecInfo, VIDEO_MIME_TYPE);
//        if (mColorFormat == 0) {
//            Log.e(TAG, "couldn't find a good color format for codec " + vCodecInfo.getName());
//            return false;
//        }
//        Log.d(TAG, "colorFormat: " + mColorFormat);
//        //根据MIME创建MediaFormat
//        // sensor出来的是逆时针旋转90度的数据，hal层没有做旋转导致APP显示和编码需要自己做顺时针旋转90,这样看到的图像才是正常的
//        videoFormat = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, height, width);
//        int bitrate = (width * height * 3 / 2) * 8 * fps;
//        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);//码流
//        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, fps);//帧率mFps
//        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat); //设置颜色格式
//        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);//设置关键帧的时间：3—`8
//        Log.d(TAG, "videoFormat：" + videoFormat.toString());
//        try {
//            //创建一个MediaCodec
//            vEncoder = MediaCodec.createByCodecName(vCodecInfo.getName());
//            vEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//            Log.d(TAG, "编码器创建完成：" + vEncoder.getName());
//            return true;
//        } catch (IOException e) {
//            Log.d(TAG, "crate codec by name error for exception !");
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    @Override
//    public void setOnCodecListeren(OnCodecListeren ocl) {
//        this.codecListeren = ocl;
//    }
//
//    @Override
//    public void putData(byte[] data) {
//        if (null == data) return;
//        try {
//            dataQueue.put(data);
//            startEncode();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void stopEncode() {
//        videoEncoderLoop = false;
//    }
//
//    private void startEncode() {
//        if (vEncoder == null) {
//            Log.d(TAG, "start encode for no init encoder !");
//            return;
//        }
//        if (videoEncoderLoop) {
//            Log.d(TAG, "start audio encode error for alteraly start, you must stop it before !");
//            return;
//        }
//        videoEncoderLoop = true;
//        start();
//    }
//
//    @Override
//    @Deprecated
//    public synchronized void start() {
//        super.start();
//    }
//
//    @Override
//    public void release() {
//        Log.d(TAG, "release");
//        if (null != vEncoder) {
//            //停止音频编码器
//            vEncoder.stop();
//            //释放音频编码器
//            vEncoder.release();
//        }
//        vEncoder = null;
//        dataQueue.clear();
//    }
//
//    @Override
//    public void run() {
//        Log.d(TAG, "run audio encode !");
//        vEncoder.start();
//        //pts的起始时间
//        startPts = System.currentTimeMillis() * 1000;
//        try {
//            while (videoEncoderLoop && !Thread.interrupted()) {
//
//                byte[] data = dataQueue.take();
//                //根据当前colorFromat 进行颜色转换
//                data = MediaCodecHelper.colorFormat(data, mColorFormat, widht, height);
//                vBufferInfo = new MediaCodec.BufferInfo();
//                data = MediaCodecHelper.encode(data, vEncoder, startPts, vBufferInfo);
//                if (null != data && codecListeren != null) {
//                    codecListeren.onData(data);
//                }
//            }
//            if (null != codecListeren) {
//                codecListeren.onComplete();
//            }
//            release();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//}
