package tk.amrom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ){
        // String inputPPT = "/Users/linjinbao66/code/ppt2video/pp.pptx";
        // String outFolder = "/Users/linjinbao66/code/ppt2video/images";
        // try {
        //     new App().pptToImage(inputPPT, outFolder);
        // } catch (FileNotFoundException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // } catch (IOException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }
        String inputFolder = "/Users/linjinbao66/code/ppt2video/images";
        String outputVideo = "output_video.mp4";
        try {
            new App().imagesToVideo(inputFolder, outputVideo);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //ppt2image
    public void pptToImage(String inputFile, String outputDir) throws FileNotFoundException, IOException {
        // 加载PPT文件
        XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(inputFile));
        Dimension pgsize = ppt.getPageSize();

        // 创建输出目录（如果不存在的话）
        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        // 遍历幻灯片并保存为图片
        int idx = 1;
        for (XSLFSlide slide : ppt.getSlides()) {
            // 创建BufferedImage并绘制幻灯片
            BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = img.createGraphics();
            slide.draw(graphics);

            // 定义输出文件路径
            File outputfile = new File(outputDir, "slide_" + idx++ + ".png");

            // 保存为PNG文件
            ImageIO.write(img, "png", outputfile);
        }

        ppt.close();
    }

    //image2video
    public void imagesToVideo(String inputDir, String outputFile) throws IOException {
        // 获取目录中的所有图片
        File dir = new File(inputDir);
        File[] imageFiles = dir.listFiles((dir1, name) -> name.endsWith(".png")); // 只处理 PNG 图片
        if (imageFiles == null || imageFiles.length == 0) {
            throw new IOException("No images found in the directory.");
        }

        // 创建视频输出
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 1920, 1080);
        recorder.setFormat("mp4"); // 设置视频格式
        recorder.setFrameRate(1.0 / 5.0); // 每张图片 5 秒
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // 使用 H.264 编解码器
        recorder.setVideoBitrate(1000000); // 设置视频比特率
        recorder.start();

        // 遍历每张图片并添加到视频中
        for (File imageFile : imageFiles) {
            BufferedImage img = javax.imageio.ImageIO.read(imageFile);
            Frame frame = convertToFrame(img);
            recorder.record(frame);
        }

        // 完成录制
        recorder.stop();
        recorder.release();
    }

    // 将 BufferedImage 转换为 Frame
    private Frame convertToFrame(BufferedImage image) {
        Java2DFrameConverter converter = new Java2DFrameConverter();
        return converter.convert(image);
    }
}
