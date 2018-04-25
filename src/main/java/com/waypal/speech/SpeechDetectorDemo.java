package com.waypal.speech;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.bytedeco.javacv.FrameGrabber.Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;
import edu.cmu.sphinx.util.TimeFrame;

public class SpeechDetectorDemo {
  private static final Logger logger = LoggerFactory.getLogger(SpeechDetectorDemo.class);
  private static final long durationPerFrame = 4096*1000/2/16000;
  private long totalDuration;
  private String audioFile;
  private String confFile;
  
  ArrayList<WordResult> results = null;
  private AudioAnalysis  analysiser = null;
  private AudioTranscriber transcriber = null;
  
  public SpeechDetectorDemo(String audioFile, String confFile){
    this.audioFile = audioFile;
    this.confFile = confFile;
    this.analysiser = new AudioAnalysis(audioFile, 0);
    this.transcriber = new AudioTranscriber(confFile);
    logger.info("test voice activity detect!!!");
  }
  
  public void streamSplitRecognizer(String outputFile, int framesNum){
    long splitDuration = framesNum * durationPerFrame;
    long start = 0;
    long end = 0;
    int splitID = 0;


    try {
      InputStream stream = null; 
      while(start < totalDuration){ 
        stream = new FileInputStream(outputFile);
        end = start+ splitDuration;
        logger.info("##################################################");
        logger.info("###recognize start:{}---end:{}", start, end);
        logger.info("##################################################");
        TimeFrame timeFrame = new TimeFrame(start, end);
        splitID++;
        analysiser.setSplitID(splitID);
        analysiser.setSplitTime(start, splitDuration);
        analysiser.analysis(transcriber.transcribe(stream, timeFrame));
        start = end;
      }
    } catch (IOException e1) {
      logger.info("IOException: Create input stream {} is failed", outputFile);
      e1.printStackTrace();
    } 
  }
  
  public void streamRecognizer(String outputFile){
    
    InputStream stream = null;  
    StreamSpeechRecognizer recognizer = null;
    try {
      stream = new FileInputStream(outputFile);
      recognizer = transcriber.start(stream);
    } catch (IOException e) {
      logger.error("Create audio transcribe is failed.");
    }
    SpeechResult results;
    while ((results = recognizer.getResult()) != null)
    {
      analysiser.analysis(results);
    }
    transcriber.stop();
  }
  
  public void liveRecognizer(){
    AudioTranscriber transcriber = new AudioTranscriber(confFile);
    LiveSpeechRecognizer liveRecognizer = null;
    AudioAnalysis audioAnalysis = new AudioAnalysis(audioFile, 0);
    try {
      liveRecognizer = transcriber.start();
    } catch (IOException e) {
      logger.error("Create audio transcribe is failed.");
    }
    SpeechResult results;
    while ((results = liveRecognizer.getResult()) != null)
    {
      audioAnalysis.analysis(results);
    }
    transcriber.stop();
  }
  
  public void streamRun(){
    AudioConverter converter = new AudioConverter(audioFile);
    converter.run();
    // phinx4 use millisecond and ffmpeg and audio file use microsecond
    this.totalDuration = converter.getDuration()/1000;
    streamSplitRecognizer(converter.getResult(), 50);
    converter.clearResult();
  }
  
  public void liveRun(){
    liveRecognizer();
  }
  
  public static void main(String[] args) throws Exception {
    String audioFile = "test.mp4";
    String confFile = "default.conf.xml";
    for (String cb : args) {
      if(cb.startsWith("audio")) {
        String[] kvs = cb.split("=");
        audioFile = kvs[1];
      }else if(cb.startsWith("conf")) {
        String[] kvs = cb.split("=");
        confFile = kvs[1];
      }
    }
    SpeechDetectorDemo detector = new SpeechDetectorDemo(audioFile, confFile);
    //detector.liveRun(); 
    detector.streamRun();
  }
}