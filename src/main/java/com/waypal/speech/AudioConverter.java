package com.waypal.speech;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioConverter {
  private static final Logger logger = LoggerFactory.getLogger(AudioConverter.class);
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd__hhmm");
  private String outputFile = "audio_convert_" + DATE_FORMAT.format(new Date()) + ".wav";
  private String audioFile;
  private long frames = 0;
  private long duration = 0;
  
  public AudioConverter(String audioFile){
    this.audioFile = audioFile;
  }
  
  public String getResult(){
    return outputFile;
  }
  
  public long getFrames(){
    return frames;
  }
  
  public void setFrames(long frames){
    this.frames = frames;
  }
  
  public long getDuration(){
    return duration;
  }
  
  public void setDuration(long duration){
    this.duration = duration;
  }
  
  public Boolean clearResult(){
    File file = new File(outputFile);
    if(file.exists()&& file.isFile()){
      if (file.delete()){
        logger.info("Delete file:{} is success", outputFile);
        return true;
      }else {
        logger.info("Delete file:{} is failed", outputFile);
        return false;
      }
    }else{
      logger.info("Delete file:{} is not exist", outputFile);
      return false;
    }
  }
  
  public void run(){
    FFmpegFrameGrabber grabberFF = null;
    FFmpegFrameRecorder recorder = null;
    Frame frame = null;    
    
    try {
      
      grabberFF = FFmpegFrameGrabber.createDefault(audioFile);
      grabberFF.start();

      frames = grabberFF.getLengthInFrames();
      duration = grabberFF.getLengthInTime();
      
      recorder = new FFmpegFrameRecorder(outputFile, 0, 0, 1);
      recorder.setSampleRate(16000);
      recorder.setFormat("wav");
      recorder.setAudioCodec(avcodec.AV_CODEC_ID_PCM_S16LE);
      recorder.start();
      
    }catch (Exception e) {
      logger.error("Failed start ffmpeg grabber.");
    }catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
      logger.error("Failed start ffempg recorder.");
    } 
    long frameNum = 0;
    while (true) {
      try {
        frame = grabberFF.grabSamples();
        if (frame == null){
          logger.info("File {} frame num:{} detecting is finished",audioFile, frameNum);
          break;
        }
      }
      catch (Exception e) {
        logger.error("face detector exception.");
      }  
      
      try {
        recorder.record(frame);
        frameNum++;
        logger.info("audio frame sampleRate:{}, audio frame timestamp:{}, audio channels:{}",
            frame.sampleRate, frame.timestamp/1000000L, grabberFF.getAudioChannels());
        setFrames(frameNum);
      } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
        logger.error("record audio file is failed.");
        e.printStackTrace();
      }
    }
    
    try {
      grabberFF.stop();
      recorder.stop();
    } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
      logger.error("Failed stop ffmpeg recorder.");
    } catch (Exception e) {
      logger.error("Failed stop ffmpeg grabber.");
    }  
  }
}