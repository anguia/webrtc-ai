package com.waypal.speech;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.SynchronousQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;
import edu.cmu.sphinx.util.TimeFrame;
import edu.cmu.sphinx.util.props.ConfigurationManager;

public class AudioTranscriber {
  /**
   * The logger for this class
   */
  private static final Logger logger = LoggerFactory.getLogger(AudioTranscriber.class);
  private int simpleRate = 16000;
  private ConfigurationManager cm;
  private Configuration config;
  private StreamSpeechRecognizer streamRecognizer = null;
  private LiveSpeechRecognizer liveRecognizer = null;
  
  public AudioTranscriber(String confFile) {
    this.config = new Configuration();
    config.setAcousticModelPath(SphinxConstants.ACOUSTIC_MODEL_EN_US);
    config.setDictionaryPath(SphinxConstants.DICTIONARY_EN_US);
    config.setLanguageModelPath(SphinxConstants.LANGUAGE_MODEL_EN_US);
    config.setSampleRate(this.simpleRate);
    cm = new ConfigurationManager(confFile);
  }
  
  public StreamSpeechRecognizer start(InputStream audioStream, TimeFrame timeFrame) throws IOException{
    streamRecognizer = new StreamSpeechRecognizer(config);
    streamRecognizer.startRecognition(audioStream, timeFrame);
    return streamRecognizer;
  }
  
  public StreamSpeechRecognizer start(InputStream audioStream) throws IOException{
    streamRecognizer = new StreamSpeechRecognizer(config);
    streamRecognizer.startRecognition(audioStream);
    return streamRecognizer;
  }
  
  public LiveSpeechRecognizer start() throws IOException{
    liveRecognizer = new LiveSpeechRecognizer(config);
    liveRecognizer.startRecognition(true);
    return liveRecognizer;
  }
  
  public void stop(){
    if (streamRecognizer != null)
      streamRecognizer.stopRecognition();
    if (liveRecognizer != null)
      liveRecognizer.stopRecognition();
  }
  
  public ArrayList<WordResult> transcribe(InputStream audioStream, TimeFrame timeFrame) throws IOException{
    streamRecognizer = new StreamSpeechRecognizer(config);
    streamRecognizer.startRecognition(audioStream, timeFrame);
    
    ArrayList<WordResult> utteredWords = new ArrayList<>();
    SpeechResult result;
    while ((result = streamRecognizer.getResult()) != null){
      utteredWords.addAll(result.getWords());
    }
    streamRecognizer.stopRecognition();
    
    return utteredWords;
  }
  
  public ArrayList<WordResult> transcribe(InputStream audioStream) throws IOException{
    streamRecognizer = new StreamSpeechRecognizer(config);
    streamRecognizer.startRecognition(audioStream);
    
    ArrayList<WordResult> utteredWords = new ArrayList<>();
    SpeechResult result;
    while ((result = streamRecognizer.getResult()) != null){
      utteredWords.addAll(result.getWords());
    }
    streamRecognizer.stopRecognition();
    
    return utteredWords;
  }
  
  public void transcribeSynchronous(InputStream audioStream, 
      SynchronousQueue<WordResult> queue)throws IOException {
    
    StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(config);
    recognizer.startRecognition(audioStream);
    
    logger.trace("Started chunked transcription");
    SpeechResult result;
    while((result = recognizer.getResult()) != null){
      logger.trace("got a word result of length {}",result.getWords().size());
    
      for(WordResult word : result.getWords()){
        logger.trace("offering {}", word.toString());
        try{
            queue.put(word);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
      }
    }
    recognizer.stopRecognition();
  }
}