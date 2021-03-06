package com.nexters.jung2.daemon.sentence;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import kr.co.shineware.nlp.komoran.core.MorphologyAnalyzer;
import kr.co.shineware.util.common.model.Pair;

import com.nexters.jung2.daemon.util.CommUtil;

public class ConvertThread implements Runnable {
	private static Logger logger = Logger.getLogger(ConvertThread.class.getName());
	
	private int intSpacing = 0;
	private int intAddWords = 0;
	
	private static MorphologyAnalyzer analyzer = new MorphologyAnalyzer("/home/was/jung2_daemon/datas/");
	private static String jdbcDriver = "org.postgresql.Driver";
	private static String jdbcUrl = "";
	private static String jdbcId = "";
	private static String jdbcPw = "";
	private static String strSql = "SELECT NO, CATE, BEFOREMSG FROM JW_PROCMSG WHERE AFTERMSG IS NULL";
	private static String strSql1 = "SELECT MP, CONTENTS, TYPE1, TYPE2, TYPE3, TYPE4 FROM JW_WORDS "
									+ "WHERE CATEGORY = ? "
									+ "ORDER BY random() LIMIT 20";
	private static String strSql2 = "UPDATE jw_procmsg SET aftermsg=? WHERE NO = ?";
	
	/* Connection Pool 구현 전까지 static으로한다. */
	private static Connection conn = null;
	
	private static CommUtil commutil = new CommUtil();
	
	@Override
	public void run() {
		logger.info(this.getClass().getName() + "를 시작합니다.");
		
		while(true) {
			String strCate = "";
			String strNo = "";
			String strOriginMsg = "";
			String strAfterMsg = "";

			
			PreparedStatement pstm = null;
			ResultSet rs = null;
			
			PreparedStatement pstm1 = null;
			ResultSet rs1 = null;
			
			PreparedStatement pstm2 = null;

			
			try {
				Class.forName(jdbcDriver);
				conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);

				pstm = conn.prepareStatement(strSql);
		    	rs = pstm.executeQuery();
		    	
				while(rs.next()) {
					strOriginMsg = rs.getString("BEFOREMSG");
					strCate = rs.getString("CATE");
					strNo	= rs.getString("NO");
					long start = System.currentTimeMillis(); 
					logger.info("#####################################################");
					logger.info("start::"+start+",strOriginMsg::"+strOriginMsg+",strCate::"+strCate+",strNo::"+strNo);
					ArrayList<String> arrayMM = new ArrayList<String>();
					ArrayList<String> arrayMA = new ArrayList<String>();
					ArrayList<String> arrayEC = new ArrayList<String>();
					
					ArrayList<String> arrayJung2MM = new ArrayList<String>();
					ArrayList<String> arrayJung2EC = new ArrayList<String>();
					HashMap<String, ArrayList<String>> mapJung2 = new HashMap<String, ArrayList<String>>();
					HashMap<String, ArrayList<String>> mapSentence = new HashMap<String, ArrayList<String>>();
					
					 List<List<Pair<String,String>>> result = analyzer.analyze(strOriginMsg);
				        
				        for (List<Pair<String, String>> eojeolResult : result) {
				        	intSpacing++;
				        	
					        for (Pair<String, String> wordMorph : eojeolResult) {
					        	
				        		
					        	String strWordSecond = wordMorph.getSecond();
					        	String strWordFirst = wordMorph.getFirst();
					        	logger.debug("strWordSecond::"+strWordSecond+", strWordFirst::"+strWordFirst);
					        	
					        	//각 어절의 앞 단어만 가져오도록 한다.
					        	if(intAddWords<intSpacing) {
					        		intAddWords++;
					        		if((strWordSecond.indexOf("NN") > -1 ) || (strWordSecond.equals("NP")) || (strWordSecond.equals("NR"))){
					        			arrayMM.add(strWordFirst);
					        		}
					        	}
					        	
					        	if (strWordSecond.equals("VV") || strWordSecond.equals("VA")) {
					        		arrayMA.add(strWordFirst);
					        	} else if (strWordSecond.equals("EC")) {
					        		arrayEC.add(strWordFirst);   		
					        	}
					        }
				        }
				        logger.debug("arrayMM::"+arrayMM.toString());
				        logger.debug("arrayMA::"+arrayMA.toString());
				        logger.debug("arrayEC::"+arrayEC.toString());

				        pstm1 = conn.prepareStatement(strSql1);
				        pstm1.setString(1, strCate);
				        rs1 = pstm1.executeQuery();

			        	while(rs1.next()) {
			        		Random rand = new Random();
			        		ArrayList<String> arrayAvailableWords = new ArrayList<String>();
			        		String strContents = rs1.getString("contents").trim();
			        		String strMp	   = rs1.getString("MP");
			        		
			        		if(strMp.equals("MM")) {
			        			String strContetnsBottom = strContents.substring(strContents.length()-1, strContents.length());
				        		
				        		commutil.isTCheck(arrayAvailableWords, strContetnsBottom, rs1.getString("type1"), 1);
				        		commutil.isTCheck(arrayAvailableWords, strContetnsBottom, rs1.getString("type2"), 2);
				        		commutil.isTCheck(arrayAvailableWords, strContetnsBottom, rs1.getString("type3"), 3);
				        		commutil.isTCheck(arrayAvailableWords, strContetnsBottom, rs1.getString("type4"), 4);
				        		
				        		strContents = strContents.substring(0, strContents.length()-1);
				        		strContents = strContents + arrayAvailableWords.get(rand.nextInt(arrayAvailableWords.size()));
				        		
				        		logger.debug("arrayAvailableWords Size::"+arrayAvailableWords.size());
				        		logger.debug("contetns::"+strContents);
				        		
				        		arrayJung2MM.add(strContents);
			        		} else if (strMp.equals("EC")) {
			        			arrayJung2EC.add(strContents);
			        		}
			        		
			        	}
			        	mapJung2.put("MM", arrayJung2MM);
			        	mapJung2.put("EC", arrayJung2EC);
			        	
			        	logger.debug("mapJung2::"+mapJung2.toString());
			        	
			        	mapSentence.put("MM", arrayMM);
			        	mapSentence.put("MA", arrayMA);
			        	mapSentence.put("EC", arrayEC);
			        	
			        	SentenceConf senconf = new SentenceConf(strOriginMsg, mapSentence);
			        	MerSen cmersen = new MerSen();
			        	strAfterMsg = cmersen.mergeSentence(senconf, mapJung2);
			        	
			        	pstm2 = conn.prepareStatement(strSql2);
			        	pstm2.setString(1, strAfterMsg);
			        	pstm2.setString(2, strNo);
			        	pstm2.executeUpdate();
			        	
						long end = System.currentTimeMillis(); 
						logger.info("소요시간::"+(end-start));

				}
			} catch(Exception e) {
				logger.error("오류가 발생하였습니다. ::", e);
	        } finally {
	        	try { if(rs!=null) rs.close(); } catch (Exception e) {}
	        	try { if(rs1!=null) rs1.close(); } catch (Exception e) {}
				try { if(pstm!=null) pstm.close(); } catch (Exception e) {}
				try { if(pstm1!=null) pstm1.close(); } catch (Exception e) {}
				try { if(pstm2!=null) pstm2.close(); } catch (Exception e) {}
	            try { if(conn!=null) conn.close(); } catch (Exception e) {}
	        }
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.error("오류가 발생하였습니다. ::", e);
			}

		}		
	}

}
