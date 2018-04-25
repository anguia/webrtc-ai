package com.waypal.video;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaceDetectorDemo {
  private static final Logger logger = LoggerFactory.getLogger(FaceDetectorDemo.class);
  private CanvasFrame canvas = new CanvasFrame("Waypal facedetector");
  private String videoFile;
  private String modelFile;
  private String markFile;
  private Boolean showFlag;
  
  public FaceDetectorDemo(String videoFile, String modelFile, String markFile, Boolean flag) {
    canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    this.videoFile = videoFile;
    this.modelFile = modelFile;
    this.markFile = markFile;
    showFlag = flag;   
  }
    
  public void run() { 
    FaceDetector faceDetector = new FaceDetector(modelFile, markFile);
    OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
    FFmpegFrameGrabber grabberFF = null;
    Frame frame = null;
    IplImage img = null;   
    long frameTotal = 0;
    
    try {
      grabberFF = FFmpegFrameGrabber.createDefault(videoFile);
      grabberFF.start();
      frameTotal = grabberFF.getLengthInFrames();
    }catch (Exception e) {
      logger.error("Failed start ffmpeg grabber.");
    } 

    VideoAnalysis videoAnalysis = new VideoAnalysis(videoFile, frameTotal);
    while (true) {
      try {
        frame = grabberFF.grabImage();
        img = converter.convert(frame);
        if (frame == null || img == null){
          logger.info("File {} detecting is finished");
          logger.info("File {} frame numbers :{}", frameTotal);
          if (showFlag){
            canvas.dispose();
          }
          break;
        }
        int faceNum = faceDetector.detect(img,showFlag);
        videoAnalysis.analysis(frame, faceNum);
        if (showFlag){
          canvas.showImage(converter.convert(img));
        }
      }
      catch (Exception e) {
        logger.error("face detector exception.");
        if (showFlag){
          canvas.dispose();
        }
      }      
    } 
  }
  
  public static void main(String[] args) {
    String videoFile = "test.mp4";
    String modelFile = "haarcascade_frontalface_alt.xml";
    String markFile = "flandmark_model.dat";
    Boolean flag = false;
    for (String cb : args) {
      if(cb.startsWith("video")) {
        String[] kvs = cb.split("=");
        videoFile = kvs[1];
      }else if(cb.startsWith("model")){
        String[] kvs = cb.split("=");
        modelFile = kvs[1];
      }else if(cb.startsWith("mark")){
        String[] kvs = cb.split("=");
        markFile = kvs[1];
      }
      else if(cb.startsWith("show")){
        String[] kvs = cb.split("=");
        if(kvs[1].equals("true")){
          flag = true;
        }
      }
    }
    FaceDetectorDemo gs = new FaceDetectorDemo(videoFile, modelFile, markFile, flag);
    gs.run();
  }
}
