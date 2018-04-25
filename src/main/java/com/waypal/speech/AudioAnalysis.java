package com.waypal.speech;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.result.WordResult;


public class AudioAnalysis {
  private static final Logger logger = LoggerFactory.getLogger(AudioAnalysis.class); 
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_hhmm");
  private String jsonFile = "audio_detect_" + DATE_FORMAT.format(new Date()) + ".json";
  private long wordTotal = 0;
  private String audioFile;
  private long splitStart = 0;
  private long splitEnd= 0;
  private int splitID = 0;
  private int filePos = 0;
  
  public AudioAnalysis(String filename, long frameTotal){
    this.audioFile = filename;
    setJsonFile();
    writeJsonStart();
  }
  
  public void analysis(ArrayList<WordResult> results){
    int wordNum = 0;
    int noSpeechNum = 0;
    String words = null;
        
    for(WordResult result : results)
    {
      long start = result.getTimeFrame().getStart();
      long end = result.getTimeFrame().getEnd();
      if (result.getWord().isFiller()){
        
        logger.info("audio word:{} is filter, startTime:{}.{}, endTime:{}.{}",
            result.getWord().toString(), start/1000,start%1000, end/1000, end%1000);
        noSpeechNum++;
      }else{
        logger.info("audio word:{}, startTime:{}.{}, endTime:{}.{}",
            result.getWord().toString(), start/1000,start%1000, end/1000, end%1000);
        if (words == null){
          words = result.getWord().toString();
        }else{
          words += " " + result.getWord().toString();
        }
        wordNum++;
      }
    }
    logger.info("split id = {} split start = {} split end = {}", splitID, splitStart, splitEnd);
    logger.info("Speech = {}, words length = {}", words, wordNum);
    wordTotal += wordNum;
    String splitElement = "\t\t{\r\n"
        +"\t\t\tid:"+splitID+",\r\n"
        +"\t\t\tstart:"+splitStart/1000+"."+splitStart%1000+",\r\n"        
        +"\t\t\tend:"+splitEnd/1000+"."+splitEnd%1000+",\r\n"          
        +"\t\t\tnon_voice_num:"+noSpeechNum+",\r\n" 
        +"\t\t\tword_num:"+wordNum+",\r\n"
        +"\t\t\twords:"+words+",\r\n"
        +"\t\t},\r\n";
    writeJson(splitElement, filePos);
    filePos += splitElement.getBytes().length;
    writeJsonEnd();
  }
  
  public void analysis(SpeechResult results){
    int wordNum = 0;
    int nonVoiceNum = 0;
    String words = null;
    
    words = results.getHypothesis();
    for(WordResult result : results.getWords())
    {
      long start = result.getTimeFrame().getStart();
      long end = result.getTimeFrame().getEnd();
      if (result.getWord().isFiller()){
        logger.info("audio word:{} is filter, startTime:{}.{}, endTime:{}.{}",
            result.getWord().toString(), start/1000,start%1000, end/1000, end%1000);
        nonVoiceNum++;
      }else{
        logger.info("audio word:{}, startTime:{}.{}, endTime:{}.{}",
            result.getWord().toString(), start/1000,start%1000, end/1000, end%1000);
        wordNum++;
      }
    }
    logger.info("split id = {} split start = {} split end = {}", splitID, splitStart, splitEnd);
    logger.info("Speech = {}, words length = {}", words, wordNum);
    wordTotal += wordNum;
    String splitElement = "\t\t{\r\n"
        +"\t\t\tid:"+splitID+",\r\n"
        +"\t\t\tstart:"+splitStart/1000+"."+splitStart%1000+",\r\n"        
        +"\t\t\tend:"+splitEnd/1000+"."+splitEnd%1000+",\r\n" 
        +"\t\t\tnon_voice_num:"+nonVoiceNum+",\r\n"   
        +"\t\t\tword_num:"+wordNum+",\r\n"
        +"\t\t\twords:"+words+",\r\n"
        +"\t\t},\r\n";
    writeJson(splitElement, filePos);
    filePos += splitElement.getBytes().length;
    writeJsonEnd();
  }
  
  public void setAudioFile(String filename){
    this.audioFile = filename;
  }
  
  public String getAudioFile(){
    return audioFile;
  }
  
  private void setJsonFile(){
    this.jsonFile = audioFile.substring(0, audioFile.lastIndexOf(".")) + "_audio_detector.json";
  }
  
 
  public void setSplitTime(long start, long duration){
    this.splitStart = start;
    this.splitEnd = start + duration;
  }
  
  public long getSplitStart(){
    return splitStart;
  }
  
  public long getSplitEnd(){
    return splitEnd;
  }
  
  public void setSplitID(int splitID){
    this.splitID = splitID;
  }
  
  public int getSplitID(){
    return splitID;
  }
  
  private void writeJsonStart(){
    String jsonStart = "{\r\n" 
        +"\tfilename: "+audioFile+",\r\n"
        +"\tresults: [\r\n\r\n";
    writeJson(jsonStart, filePos);
    filePos = jsonStart.getBytes().length;
  }
  
  private void writeJsonEnd(){
    String jsonEnd = "\t]\r\n"
        +"\tsplitNum: "+splitID+"\r\n"
        +"\twordsTotal: "+wordTotal+"\r\n}";
    
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
