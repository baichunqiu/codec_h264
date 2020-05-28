package com.bcq.codec.hdcodec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import com.example.apadmin.cameraphoto.YuvEngineWrap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MediaCodecHelper {
    private final static String TAG = "MediaCodecHelper";//超时时间 10ms
    private final static int TIMEOUT_USEC = 10000;//超时时间 10ms

    /**
     * 根据MineType 获取codec
     *
     * @param mimeType
     * @return
     */
    public static MediaCodecInfo getCodecByMimeType(String mimeType) {
        int count = MediaCodecList.getCodecCount();
        for (int i = 0; i < count; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    /**
     * 获取支持的所有colorFromat
     *
     * @param codecInfo
     * @param mimeType
     * @return
     */
    public static List<Integer> getSupportColorFormat(MediaCodecInfo codecInfo, String mimeType) {
        List<Integer> result = new ArrayList<>();
        MediaCodecInfo.CodecCapabilities ccs = codecInfo.getCapabilitiesForType(mimeType);
        for (Integer corlorFromat : ccs.colorFormats) {
            result.add(corlorFromat);
        }
        return result;
    }

    /**
     * 获取主流格式的colorFormat
     * @param codecInfo
     * @param mimeType
     * @param recognizeColFormats 识别colorFormat的范围
     * @return
     */
    public static int getRecognizeCorlorFormat(MediaCodecInfo codecInfo, String mimeType, int[] recognizeColFormats) {
        List<Integer> result = new ArrayList<>();
        MediaCodecInfo.CodecCapabilities ccs = codecInfo.getCapabilitiesForType(mimeType);
        for (int corFor : ccs.colorFormats) {
            if (recognize(corFor, recognizeColFormats)) {
                return corFor;
            }
        }
        return 0;
    }

    /**
     * 根据cololorFormat转换数据 并旋转90度处理
     *
     * @param input           输入原数据
     * @param recognizeFormat cololorFormat
     * @param width
     * @param height
     * @return
     */
    public static byte[] colorFormat(byte[] input, int recognizeFormat, int width, int height) {
        int yuvSize = width * height * 3 / 2;
        byte[] rotateYuvBuffer = new byte[yuvSize];
        byte[] outYuvBuffer = new byte[yuvSize];
        int[] outWidth = new int[1];
        int[] outHeight = new int[1];
        if (recognizeFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) {
            //nv21格式转为nv12格式
            YuvEngineWrap.newInstance().Nv21ToNv12(input, outYuvBuffer, width, height);
            YuvEngineWrap.newInstance().Nv12ClockWiseRotate90(outYuvBuffer, width, height, rotateYuvBuffer, outWidth, outHeight);
        } else if (recognizeFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) {
            //用于NV21格式转换为I420(YUV420P)格式
            YuvEngineWrap.newInstance().Nv21ToI420(input, outYuvBuffer, width, height);
            YuvEngineWrap.newInstance().I420ClockWiseRotate90(outYuvBuffer, width, height, rotateYuvBuffer, outWidth, outHeight);
        } else if (recognizeFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar) {
            System.arraycopy(input, 0, outYuvBuffer, 0, width * height * 3 / 2);
            YuvEngineWrap.newInstance().Nv21ClockWiseRotate90(outYuvBuffer, width, height, rotateYuvBuffer, outWidth, outHeight);
        } else if (recognizeFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar) {
            //用于NV21格式转换为YV12格式
            YuvEngineWrap.newInstance().Nv21ToYv12(input, outYuvBuffer, width, height);
            YuvEngineWrap.newInstance().Yv12ClockWiseRotate90(outYuvBuffer, width, height, rotateYuvBuffer, outWidth, outHeight);
        }
        return rotateYuvBuffer;
    }

    /**
     * 是否是主流可识别的ColorFormat
     *
     * @param colorFormat
     * @return
     */
    private static boolean recognize(int colorFormat, int[] recognizeColFormats) {
        for (int color : recognizeColFormats) {
            if (color == colorFormat) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取所有支持的codec
     *
     * @return
     */
    private List<MediaCodecInfo> getAllSuportCodec() {
        List<MediaCodecInfo> result = new ArrayList<>();
        int count = MediaCodecList.getCodecCount();
        for (int i = 0; i < count; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (codecInfo.isEncoder()) {
                // 判断是否为编码器，否则直接进入下一次循环
                result.add(codecInfo);
            }
        }
        return result;
    }

    public static byte[] encode(byte[] input, MediaCodec aEncoder, long sPts, MediaCodec.BufferInfo outBufferInfo) {
        //输入缓冲区
        ByteBuffer[] inputBuffers = aEncoder.getInputBuffers();
        //得到当前有效的输入缓冲区的索引
        int inputBufferIndex = aEncoder.dequeueInputBuffer(TIMEOUT_USEC);
        if (inputBufferIndex >= 0) { //输入缓冲区有效
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            //往输入缓冲区写入数据
            inputBuffer.put(input);
            //计算pts，这个值是一定要设置的
            long pts = System.currentTimeMillis() * 1000 - sPts;
            //将缓冲区入队
            aEncoder.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
        }
        //拿到输出缓冲区,用于取到编码后的数据
        ByteBuffer[] outputBuffers = aEncoder.getOutputBuffers();
        //拿到输出缓冲区的索引
        int outputBufferIndex = aEncoder.dequeueOutputBuffer(outBufferInfo, TIMEOUT_USEC);
        Log.d(TAG, "outputBufferIndex: " + outputBufferIndex);
        if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            outputBuffers = aEncoder.getOutputBuffers();
        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            //加入音轨的时刻,一定要等编码器设置编码格式完成后，再将它加入到混合器中，
            // 编码器编码格式设置完成的标志是dequeueOutputBuffer得到返回值为MediaCodec.INFO_OUTPUT_FORMAT_CHANGED
            // TODO: 2020/5/28  此处加入到混合器中
        }
        while (outputBufferIndex >= 0) {
            //数据已经编码成AAC格式
            //outputBuffer保存的就是AAC数据
            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
            if (outputBuffer == null) {
                Log.e(TAG, "outputBuffer is null !");
                return null;
            }
            if ((outBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {//编码结束的标志
                Log.e(TAG, "BUFFER_FLAG_END_OF_STREAM ");
                Thread.currentThread().interrupt();
                return null;
            }
            if ((outBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                // You shoud set output format to muxer here when you target Android4.3 or less
                // but MediaCodec#getOutputFormat can not call here(because INFO_OUTPUT_FORMAT_CHANGED don't come yet)
                // therefor we should expand and prepare output format from buffer data.
                // This sample is for API>=18(>=Android 4.3), just ignore this flag here
                Log.e(TAG, "BUFFER_FLAG_CODEC_CONFIG ");
                MediaFormat format = aEncoder.getOutputFormat();
                format.setByteBuffer("csd-0", outputBuffer);
                outBufferInfo.size = 0;
            }
            if (outBufferInfo.size != 0) {
                // 如果API<=19，需要根据BufferInfo的offset偏移量调整ByteBuffer的位置
                // 并且限定将要读取缓存区数据的长度，否则输出数据会混乱
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    outputBuffer.position(outBufferInfo.offset);
                    outputBuffer.limit(outBufferInfo.offset + outBufferInfo.size);
                }
                byte[] outData = new byte[outBufferInfo.size];
                outputBuffer.get(outData);
                return outData;
            }
            //释放资源
            aEncoder.releaseOutputBuffer(outputBufferIndex, false);
            //拿到输出缓冲区的索引
            outputBufferIndex = aEncoder.dequeueOutputBuffer(outBufferInfo, 0);
        }
        return null;
    }
}
