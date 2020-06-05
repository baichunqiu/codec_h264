# codec_h264
展示h264的软编码和硬编码
# module功能：
## app：
####1.AudioRecord 录制pcm音频数据
2.camera预览监听获取image，nv21格式的图像数据。
3.使用mediacodec中的aac编码器对pcm编码
4.使用yuv颜色转换库对nv21格式转换为yuv。
5.使用mediacode的h264编码yuv数据编码
6.使用MediaMuxer合成器将aac编码的音频和h264编码的图像和成MP4。
 videocodec：基于ffmpge库的h264软编码
 yuvlibs：nv21数据格式的转换库
