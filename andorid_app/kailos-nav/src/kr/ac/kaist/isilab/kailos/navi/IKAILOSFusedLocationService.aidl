package kr.ac.kaist.isilab.kailos.navi;

interface IKAILOSFusedLocationService {
	void startPDR();
	void stopPDR();
	
	void notifyLocalizationResult(String strEstimatedLocation);
}