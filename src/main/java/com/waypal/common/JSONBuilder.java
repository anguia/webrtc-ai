package com.waypal.common;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.cmu.sphinx.result.WordResult;

public class JSONBuilder {
  /**
   * Identifier for the word
   */
  private static final String JSON_WORD = "word";

  /**
   * Identifier for the time when the word started to get spoken
   */
  private static final String JSON_TIMESTAMP_START = "start";

  /**
   * Identifier for the time when the word stopped being spoken
   */
  private static final String JSON_TIMESTAMP_END = "end";

  /**
   * identifier that the word is a fill word, e.g a sigh
   */
  private static final String JSON_FILL_WORD = "filler";

  /**
   * Builds the array of words from the Sphinx4 into a JSON array
   * @param results the ArrayList of WordResults from a Speech-to-text
   *                transcription
   * @return a JSONArray holding a JSONObject for each word, with additional
   * information
   *
   */
  @SuppressWarnings("unchecked") //for JSONArray.add()
  public JSONArray buildSpeechToTextResult(ArrayList<WordResult> results)
  {
      JSONArray toReturn = new JSONArray();
      for(WordResult result : results)
      {
          toReturn.add(this.buildWordObject(
                  result.getWord().toString(),
                  result.getTimeFrame().getStart(),
                  result.getTimeFrame().getEnd(),
                  result.getWord().isFiller()
          ));
      }
      return toReturn;
  }

  /**
   * Create a JSONObject with a word, start, end and filler value based
   * on a WordResult
   * @param result the WordResult whose values will be held in the JSONObject
   * @return a JSONObject holding the word, start, end and filler
   * values of the given WordResult
   */
  public JSONObject buildWordObject(WordResult result)
  {
      return buildWordObject(
              result.getWord().toString(),
              result.getTimeFrame().getStart(),
              result.getTimeFrame().getEnd(),
              result.getWord().isFiller());
  }

  /**
   * Create a JSONObject with a word, start, end and filler value based
   * on a WordResult
   * @param word the word value of the JSONObject
   * @param start  the start value of the JSONObject
   * @param end the end value of the JSONObject
   * @param filler the filler value of the JSONObject
   * @return a JSONObject holding the word, start, end and filler
   * values of the given WordResult
   */
  @SuppressWarnings("unchecked") //for JSONObject.put()
  public JSONObject buildWordObject(String word, long start,
                                    long end, boolean filler)
  {
      JSONObject jsonWord = new JSONObject();
      jsonWord.put(JSON_WORD, word);
      jsonWord.put(JSON_TIMESTAMP_START, start);
      jsonWord.put(JSON_TIMESTAMP_END, end);
      jsonWord.put(JSON_FILL_WORD, filler);
      return jsonWord;
  }
}
