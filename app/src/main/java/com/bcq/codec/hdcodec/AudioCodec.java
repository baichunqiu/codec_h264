package com.bcq.codec.hdcodec;

import android.media.AudioFormat;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

/**
 *
 */
public class AudioCodec extends AbsCodec {
    private int sample;
    private int chanel;
    private int bitForamt;
    private String mimeType;

    public AudioCodec(int sample, int channel, int bitForamt, String mimeType) {
        this.sample = sample;
        this.chanel = channel;
        this.bitForamt = bitForamt;
        this.mimeType = mimeType;
    }

    @Override
    protected String onGetCodecMime() {
        return mimeType;
    }

    @Override
    protected MediaFormat onConfigure(MediaCodecInfo codecInfo) {
        MediaFormat audioFormat = MediaFormat.createAudioFormat(mimeType, sample, chanel);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_STEREO);//CHANNEL_IN_STEREO 立体声
        int bitRate = sample * bitForamt * chanel;
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, chanel);
        audioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, sample);
        return audioFormat;
    }
}
