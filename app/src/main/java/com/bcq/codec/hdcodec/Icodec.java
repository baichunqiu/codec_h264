package com.bcq.codec.hdcodec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;

/**
 * 硬编码的codec抽象接口
 * 使用示例：
 * <p> Icodec codec = new AudioCodec(48000,2,16, Icodec.MIME_TYPE_AAC);          </p>
 * <p> Icodec codec = new VideoCodec(1280,720,30, Icodec.MIME_TYPE_H264);        </p>
 * <p> codec.init();                                                             </p>
 * <p> codec.putData(null);                                                      </p>
 * <p> codec.stopEncode();                                                       </p>
 * <p> codec.setOnCodecListeren(new Icodec.OnCodecListeren() {                   </p>
 * <p>      @Override                                                            </p>
 * <p>      public void onData(byte[] data, MediaCodec.BufferInfo bufferInfo) {  </p>
 * <p>      }                                                                    </p>
 * <p> });                                                                       </p>
 */
public interface Icodec {
    String MIME_TYPE_AAC = "audio/mp4a-latm";//aac
    String CODEC_NAME_AAC = "OMX.google.aac.encoder";
    String MIME_TYPE_H264 = "video/avc";// H.264 Advanced
    String CODEC_NAME_H264 = "OMX.google.h264.encoder";
    int[] RECOGNIZED_COLOR_FORMAT = new int[]{//主流camera预览格式对应的colorformat
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar, //对应Camera预览格式NV12
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,//对应Camera预览格式I420(YV21/YUV420P)
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar,//对应Camera预览格式NV21
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar////对应Camera预览格式YV12
    };

    /**
     * 初始化
     *
     * @return success or init error
     */
    boolean init();

    /**
     * 设置状态监听
     *
     * @param ocl
     */
    void setOnCodecListeren(OnCodecListeren ocl);

    /**
     * 向编码器队列中添加待处理原始数据
     *
     * @param data
     */
    void putData(byte[] data);

    /**
     * 手动停止编码,停止以后自动释放
     * 编码任务完成后也自动释放
     */
    void stopEncode();

    /**
     * 释放资源
     */
    void release();

    /**
     * codec 处理状态监听
     */
    interface OnCodecListeren {
        void onData(byte[] data, MediaCodec.BufferInfo bufferInfo);
    }
}
