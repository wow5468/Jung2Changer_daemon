package com.nexters.jung2.daemon.sentence;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class MerSen
{
  private static Logger logger = Logger.getLogger(MerSen.class.getName());

  public String mergeSentence(SentenceConf sentence, HashMap<String, ArrayList<String>> jung2)
  {
    String strSentence = sentence.getStrSentence();

    HashMap mapMP = sentence.getMapMP();
    HashMap mapChgData = new HashMap();

    ArrayList arrayJung2MM = (ArrayList)jung2.get("MM");
    ArrayList arrayJung2EC = (ArrayList)jung2.get("EC");
    
    ArrayList arrayMM = (ArrayList)mapMP.get("MM");
    ArrayList arrayEC = (ArrayList)mapMP.get("EC");

    for (int i = 0; i < arrayMM.size(); i++) {
      String words = (String)arrayMM.get(i);
      int start = strSentence.indexOf(words);
      int end = start + words.length();

      if (start > -1) {
        String strStartWords = strSentence.substring(0, start);
        String strEndWords = strSentence.substring(end, strSentence.length());

        strSentence = strStartWords + "$" + i + "#" + strEndWords;

        mapChgData.put("$" + i + "#", words);
      }
    }

    for (int i = 0; i < (mapChgData.size() < arrayJung2MM.size() ? mapChgData.size() : arrayJung2MM.size()); i++) {
      String strAddWords = (String)arrayJung2MM.get(i);

      String strChgWords = (String)mapChgData.get("$" + i + "#");
      String strFinalWords = strChgWords;

      logger.debug("mapChgData.size()::" + mapChgData.size());
      logger.debug("i::" + i);
      logger.debug("(mapChgData.size()>1 && (i>0 && (i%3==0 || i==1)))::" + ((mapChgData.size() > 1) && (i > 0) && ((i % 3 == 0) || (i == 1))));
      logger.debug("(mapChgData.size()==1)::" + (mapChgData.size() == 1));
      logger.debug("((mapChgData.size()>1 && (i>0 && (i%3==0 || i==1))) || (mapChgData.size()==1))::" + (((mapChgData.size() > 1) && (i > 0) && ((i % 3 == 0) || (i == 1))) || (mapChgData.size() == 1)));

      if (((mapChgData.size() > 1) && (i > 0) && ((i % 3 == 0) || (i == 1))) || (mapChgData.size() == 1)) {
    	  strFinalWords = strAddWords + " " + strFinalWords;
      }

      strSentence = strSentence.replace("$" + i + "#", strFinalWords);
    }
    
    if(arrayJung2EC.size() > 0 && arrayEC.size() > 0) {
    	strSentence =  strSentence + arrayJung2EC.get(0);
    }
    
    logger.debug("AfterMsg::"+strSentence);
    
    return strSentence;
  }
}