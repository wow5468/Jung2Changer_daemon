package com.nexters.jung2.daemon.util;

import java.util.ArrayList;

public class CommUtil {
	
	public ArrayList<String> isTCheck(ArrayList<String> arrayData, String strContentsBottom, String strType, int intType){
		if(!(strType.equals("F"))){
			if(intType == 1){
				arrayData.add(strContentsBottom+"는");
			} else if (intType == 2){
				arrayData.add(strContentsBottom+"을");
			} else if (intType == 3) {
				arrayData.add(strType);
			} else if (intType == 4) {
				arrayData.add(strType);
			}
		}
		return arrayData;
	}

}
