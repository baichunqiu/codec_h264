//package com.bcq.codec.hdcodec;
//
//import android.media.AudioFormat;
//import android.media.MediaCodec;
//import android.media.MediaCodecInfo;
//import android.media.MediaFormat;
//import android.util.Log;
//
//import java.io.IOException;
//import java.util.concurrent.LinkedBlockingQueue;
//
//public class AudioCodec_abs extends Thread implements Icodec {
//    private static final String TAG = "AudioCodec";
//    private static LinkedBlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<>();
//    private OnCodecListeren codecListeren;
//    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";
//    private MediaCodec aEncoder;                // API >= 16(Android4.1.2)
//    private MediaCodec.BufferInfo aBufferInfo;        // API >= 16(Android4.1.2)
//    private MediaCodecInfo audioCodecInfo;
//    private MediaFormat audioFormat;
//    private volatile boolean audioEncoderLoop = false;
//    private volatile boolean aEncoderEnd = false;
//    private final int TIMEOUT_USEC = 10000;//超时时间 10ms
//    private long startPts;
//
//    @Override
//    public boolean init(int sample, int chanel, int format) {
//        if (aEncoder != null) {
//            return true;
//        }
//        aBufferInfo = new MediaCodec.BufferInfo();
//        audioCodecInfo = MediaCodecHelper.getCodecByMimeType(AUDIO_MIME_TYPE);
//        if (audioCodecInfo == null) {
//            Log.e(TAG, "no support codec for mimeType is " + AUDIO_MIME_TYPE);
//            return false;
//        }
//        audioFormat = MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, sample, chanel);
//        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
//        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_STEREO);//CHANNEL_IN_STEREO 立体声
//        int bitRate = sample * format * chanel;
//        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
//        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, chanel);
//        audioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, sample);
//        Log.d(TAG, "format: " + audioFormat.toString());
//        try {
//            // aEncoder = MediaCodec.createDecoderByType (AUDIO_MIME_TYPE);
//            aEncoder = MediaCodec.createByCodecName(audioCodecInfo.getName());
//            aEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//            Log.d(TAG, "编码器创建完成：" + aEncoder.getName());
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
//    private void stopAudioEncode() {
//        aEncoderEnd = true;
//    }
//
//    private void startEncode() {
//        if (aEncoder == null) {
//            Log.d(TAG, "start encode for no init encoder !");
//            return;
//        }
//        if (audioEncoderLoop) {
//            Log.d(TAG, "start audio encode error for alteraly start, you must stop it before !");
//            return;
//        }
//        audioEncoderLoop = true;
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
//        if (null != aEncoder) {
//            //停止音频编码器
//            aEncoder.stop();
//            //释放音频编码器
//            aEncoder.release();
//        }
//        aEncoder = null;
//        dataQueue.clear();
//    }
//
//    @Override
//    public void run() {
//        Log.d(TAG, "run audio encode !");
//        aEncoderEnd = false;
//        aEncoder.start();
//        startPts = System.currentTimeMillis() * 1000;
//        try {
//            while (audioEncoderLoop && !Thread.interrupted()) {
//                byte[] data = dataQueue.take();
//                aBufferInfo = new MediaCodec.BufferInfo();
//                data = MediaCodecHelper.encode(data, aEncoder, startPts, aBufferInfo);
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
//
//    /**
//     * 编码音频
//     *
//     * @param input
//     */
////    private void encodeAudio(byte[] input) {
////        try {
////            //输入缓冲区
////            ByteBuffer[] inputBuffers = aEncoder.getInputBuffers();
////            //得到当前有效的输入缓冲区的索引
////            int inputBufferIndex = aEncoder.dequeueInputBuffer(TIMEOUT_USEC);
////            if (inputBufferIndex >= 0) { //输入缓冲区有效
////                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
////                inputBuffer.clear();
////                //往输入缓冲区写入数据
////                inputBuffer.put(input);
////                //计算pts，这个值是一定要设置的
////                long pts = System.currentTimeMillis() * 1000 - presentationTimeUs;
////                if (aEncoderEnd) {
////                    //结束时，发送结束标志
////                    aEncoder.queueInputBuffer(inputBufferIndex, 0, input.length, pts, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
////                } else {
////                    //将缓冲区入队
////                    aEncoder.queueInputBuffer(inputBufferIndex, 0, input.length,
////                            pts, 0);
////                }
////            }
////            //拿到输出缓冲区,用于取到编码后的数据
////            ByteBuffer[] outputBuffers = aEncoder.getOutputBuffers();
////            //拿到输出缓冲区的索引
////            int outputBufferIndex = aEncoder.dequeueOutputBuffer(aBufferInfo, TIMEOUT_USEC);
////            Log.d(TAG, "=====zhongjihao====Audio======outputBufferIndex: " + outputBufferIndex);
////            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
////                outputBuffers = aEncoder.getOutputBuffers();
////            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
////                //加入音轨的时刻,一定要等编码器设置编码格式完成后，再将它加入到混合器中，
////                // 编码器编码格式设置完成的标志是dequeueOutputBuffer得到返回值为MediaCodec.INFO_OUTPUT_FORMAT_CHANGED
////                // TODO: 2020/5/28  此处加入到混合器中
////            } else if (outputBufferIndex >= 0) {
////                //数据已经编码成AAC格式
////                //outputBuffer保存的就是AAC数据
////                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
////                if (outputBuffer == null) {
////                    Log.e(TAG, "outputBuffer is null !");
////                    return;
////                }
////                if ((aBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {//编码结束的标志
////                    Log.e(TAG, "BUFFER_FLAG_END_OF_STREAM ");
////                    audioEncoderLoop = false;
////                    interrupt();
////                    if (null != codecListeren) codecListeren.onComplete();
////                    return;
////                }
////                if ((aBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
////                    // You shoud set output format to muxer here when you target Android4.3 or less
////                    // but MediaCodec#getOutputFormat can not call here(because INFO_OUTPUT_FORMAT_CHANGED don't come yet)
////                    // therefor we should expand and prepare output format from buffer data.
////                    // This sample is for API>=18(>=Android 4.3), just ignore this flag here
////                    Log.e(TAG, "BUFFER_FLAG_CODEC_CONFIG ");
////                    MediaFormat format = aEncoder.getOutputFormat();
////                    format.setByteBuffer("csd-0", outputBuffer);
////                    aBufferInfo.size = 0;
////                }
////                if (aBufferInfo.size != 0) {
////                    // 如果API<=19，需要根据BufferInfo的offset偏移量调整ByteBuffer的位置
////                    // 并且限定将要读取缓存区数据的长度，否则输出数据会混乱
////                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
////                        outputBuffer.position(aBufferInfo.offset);
////                        outputBuffer.limit(aBufferInfo.offset + aBufferInfo.size);
////                    }
////                    byte[] outData = new byte[aBufferInfo.size];
////                    outputBuffer.get(outData);
////                    if (null != codecListeren) codecListeren.onData(outData);
////                }
////                //释放资源
////                aEncoder.releaseOutputBuffer(outputBufferIndex, false);
////                //拿到输出缓冲区的索引
////                outputBufferIndex = aEncoder.dequeueOutputBuffer(aBufferInfo, 0);
////            }
////        } catch (Exception t) {
////            Log.e(TAG, "encode audio error : " + t.toString());
////        }
////    }
//}
