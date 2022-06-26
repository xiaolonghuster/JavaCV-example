package xyz.xiaolong.example.javacv.rtmp;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RtmpClient {

    public static void main(String[] args) {
        // captureScreen();
        String picDirPath = "/Users/lixiaolong/tmp/20220626/image/image";
        try {
            transferPicToRtmp(picDirPath, "rtmp://139.159.213.37/live/123");
        } catch (Exception e) {
            e.printStackTrace();
        }
//        File picDirFile = new File(picDirPath);
//        File[] files = picDirFile.listFiles();
//        Arrays.asList(files).forEach(file -> System.out.println(file.getAbsoluteFile()));
    }
    /**
     *
     * 图片转RTMP流
     *
     * @param outRtmpUrl
     * @throws Exception
     * @throws org.bytedeco.javacv.FrameRecorder.Exception
     * @throws InterruptedException
     */
    public static void transferPicToRtmp(String picDirPath, String outRtmpUrl)
            throws Exception, org.bytedeco.javacv.FrameRecorder.Exception, InterruptedException {

        int frameRate = 25;
        FrameRecorder recorder;
        try {
            recorder = FrameRecorder.createDefault(outRtmpUrl, 480, 852);
        } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
            throw e;
        }

        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("flv");
        recorder.setFrameRate(frameRate);
        recorder.setGopSize(frameRate);

        /*************************************************************
         * 设置音频
         ************************************************************/
//        // 不可变(固定)音频比特率
//        recorder.setAudioOption("crf", "0");
//        // 最高质量
//        recorder.setAudioQuality(0);
//        // 音频比特率
//        recorder.setAudioBitrate(192000);
//        // 音频采样率
//        recorder.setSampleRate(44100);
//        // 双通道(立体声)
//        recorder.setAudioChannels(2);
//        // 音频编/解码器
//        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        /*************************************************************
         * 设置音频结束
         ************************************************************/

        System.out.println("准备开始推流...");
        try {
            recorder.start();
        } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
            try {
                System.out.println("录制器启动失败，正在重新启动...");
                if (recorder != null) {
                    System.out.println("尝试关闭录制器");
                    recorder.stop();
                    System.out.println("尝试重新开启录制器");
                    recorder.start();
                }

            } catch (org.bytedeco.javacv.FrameRecorder.Exception e1) {
                throw e;
            }
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();// 本地环境
        Rectangle screenSize = ge.getMaximumWindowBounds();// 获取当前屏幕最大窗口边界
        CanvasFrame frame = new CanvasFrame("图片转RTMP流");// javacv提供的图像展现窗口
        int width = 480;
        int height = 852;
        Frame capturedFrame = null;

        System.out.println("开始推流");
        frame.setBounds((int) (screenSize.getWidth() - width) / 2, (int) (screenSize.getHeight() - height) / 2, width,
                height);// 窗口居中
        frame.setCanvasSize(width, height);// 设置CanvasFrame窗口大小

        File picDirFile = new File(picDirPath);
        File[] files = picDirFile.listFiles();
        Map<Integer, BufferedImage> imageMap = new HashMap<>();
        // ai+
        for (int i = 0; i < files.length; i++) {
            String path = String.format("/Users/lixiaolong/tmp/20220626/image/image/image-%03d.jpeg", i);
            System.out.println(path);
            BufferedImage image = ImageIO.read(new FileInputStream(path));
            imageMap.put(i, image);
        }

        int index = 0;
        Java2DFrameConverter co = new Java2DFrameConverter();
        long startTime = System.currentTimeMillis();
        while (index < imageMap.size()) {
            long stm = System.currentTimeMillis();
            System.out.println("推流..." + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
            capturedFrame = co.getFrame(imageMap.get(index));
            frame.showImage(capturedFrame);
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
            long videoTS = 1000 * (System.currentTimeMillis() - startTime);
            if (videoTS > recorder.getTimestamp()) {
                System.out.println("---------" + videoTS);
                recorder.setTimestamp(videoTS);// 时间戳
            }
//            recorder.setTimestamp(videoTS);// 时间戳
            if (capturedFrame != null) {
                recorder.record(capturedFrame);
            }
            long utm = System.currentTimeMillis() - stm;
            System.out.println("utm:" + utm);
            if (utm < 40) {
                Thread.sleep(40 - utm - 5);
            }
            index++;
        }
        frame.dispose();
        recorder.stop();
        recorder.release();
    }
}
