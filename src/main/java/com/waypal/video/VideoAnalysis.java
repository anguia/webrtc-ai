package com.waypal.video;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bytedeco.javacpp.avutil.AVFrame;
import org.bytedeco.javacv.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoAnalysis {
  private static final Logger logger = LoggerFactory.getLogger(VideoAnalysis.class); 
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_hhmm");
  private String jsonFile = "face_detect_" + DATE_FORMAT.format(new Date()) + ".json";
  private long faceFrames = 0;
  private long frames = 0;
  private long frameTotal = 0;
  private String videoFile;
  private int filePos = 0;
  
  
  public VideoAnalysis(String filename, long frameTotal){
    this.videoFile = filename;
    this.frameTotal = frameTotal;
    setJsonFile();
    writeJsonStart();
  }
  
  public void analysis(Frame frame, int faceNum){
    
    if (frame == null)
      return;

    if(faceNum > 0){
      faceFrames++;
    }
    frames++;
    AVFrame picture = (AVFrame) frame.opaque;
    long framePts = picture.pts();
    float framePtsTime = (float)frame.timestamp/1000000L;
    long pos = picture.pkt_pos();
    logger.info("packet info pts={}",framePts);
    logger.info("packet info pts_time={}",framePtsTime);
    logger.info("packet info timestamp={}",frame.timestamp);
    logger.info("packet info position={}",pos);
    logger.info("packet info faceNum={}",faceNum);
    logger.info("Video faceFrame/frames={}/{}", faceFrames, frameTotal);
    String frameElement = "\t\t{\r\n"
        +"\t\t\tid:"+frames+",\r\n"
        +"\t\t\tframe_time:"+framePtsTime+",\r\n"
        +"\t\t\tfaces:"+faceNum+",\r\n"
        +"\t\t},\r\n";
    writeJson(frameElement, filePos);
    filePos += frameElement.getBytes().length;
    writeJsonEnd();
    
  }
  
  public void setVideoFile(String filename){
    this.videoFile = filename;
  }  

  public String getVideoFile(){
    return this.videoFile;
  }
  
  private void setJsonFile(){
    this.jsonFile = videoFile.substring(0, videoFile.lastIndexOf(".")) + "_face_detector.json";
  }
  
  private void writeJsonStart(){
    String jsonStart = "{\r\n" 
        +"\tfilename: "+videoFile+",\r\n"
        +"\tframes: [\r\n\r\n";
    writeJson(jsonStart, filePos);
    filePos = jsonStart.getBytes().length;
  }
  
  private void writeJsonEnd(){
    String jsonEnd = "\t]\r\n"
        +"\tframesNum: "+frames+"\r\n"
        +"\tfaceFramesNum: "+faceFrames+"\r\n}";
    
    writeJson(jsonEnd, filePos);
  }
  
  private void writeJson(String data, int pos){
    try {
      RandomAccessFile raf=new RandomAccessFile(jsonFile,"rw");
      raf.seek(pos);
      raf.write(data.getBytes());
      raf.seek(0);
      raf.close();
    } catch (IOException e) {
      logger.error("Write json file {} exception", jsonFile);
      e.printStackTrace();
    }
  }
}
