package kr.ac.kaist.isilab.kailos.pdr;

import java.util.ArrayList;

public class StepDetector {
	private double THRESHOLD = 0.3f;
	private double THRESHOLD_MIN = -0.3f;
	private double THRESHOLD_MAX = 0.3f;
	private double THRESHOLD_DIFF = 0.3f;
	private double mStayValue = 0;
	public static double GRAVITY_ACCELERATION = 9.80665f;
	
	private Double mMinPeak = null;
	private Double mMaxPeak = null;
	
	private int mStepCount = 0;
	private Double mMeanMinPeak;
	private Double mMeanMaxPeak;
	private double mStdMinPeak;
	private double mStdMaxPeak;
	private ArrayList<Double> mListMaxPeak;
	private ArrayList<Double> mListMinPeak;

	private int mWindowSize = 5;
	private WindowStep mStayWin;	// Staying Window
	
	private StateStep mState;
	
	public void init(){
		mListMaxPeak = new ArrayList<Double>();
		mListMinPeak = new ArrayList<Double>();		
		mStayWin = new WindowStep();
		
		mStayWin.setWindowSize(mWindowSize);
		mStayWin.setThreshold(THRESHOLD);
		
		THRESHOLD = 0.3f;
		THRESHOLD_MIN = -0.3f;
		THRESHOLD_MAX = 0.3f;
		THRESHOLD_DIFF = 0.3f;
		mStayValue = 0;
		mStepCount = 0;
		
		mMinPeak = null;
		mMaxPeak = null;
		
		mStepCount = 0;
		mMeanMinPeak = null;
		mMeanMaxPeak = null;
		
		mStdMinPeak = 0;
		mStdMaxPeak = 0;

	}		
	
	public void setWindowSize(int size){
		mWindowSize = size;
	}
	
	public void setThreshold(double threshold){
		THRESHOLD = threshold;
	}
	
	public void setDiffThreshold(double threshold){
		THRESHOLD_DIFF = threshold;
	}		
	
	public boolean isStep(long timestamp, double data){
		mStayWin.setValue(data);
		mStayValue = mStayWin.getStayValue();
		
		boolean bIsStep = false;
		
		if( mMaxPeak != null && mMinPeak != null && data > mMinPeak ) {
			mStepCount++;
//			System.out.println(timestamp + "," + data + " Step : " + mStepCount + " Max : " + mMaxPeak + " Min : " + mMinPeak + " StdMax : " + mStdMaxPeak + " StdMin : " + mStdMinPeak + " Diff : " + Math.abs(mMaxPeak - mMinPeak));

			mListMaxPeak.add(mMaxPeak);
			mListMinPeak.add(mMinPeak);
			
			if(mMeanMinPeak == null && mMeanMaxPeak == null){
				mMeanMinPeak = (double) 0;
				mMeanMaxPeak = (double) 0;
			}
			
			mMeanMinPeak = (mMeanMinPeak * (mStepCount - 1) + mMinPeak) / mStepCount;
			mMeanMaxPeak = (mMeanMaxPeak * (mStepCount - 1) + mMaxPeak) / mStepCount;
			
	        double dStdsum = 0;
	        for(int i=0; i<mListMinPeak.size(); i++)
	        	dStdsum += Math.pow(((Double) mListMinPeak.get(i)).doubleValue() - mMeanMinPeak, 2);

	        mStdMinPeak = Math.sqrt(dStdsum / mListMinPeak.size());
			
	        dStdsum = 0;
	        
	        for(int i=0; i<mListMaxPeak.size(); i++)
	        	dStdsum += Math.pow(((Double) mListMaxPeak.get(i)).doubleValue() - mMeanMaxPeak, 2);

	        mStdMaxPeak = Math.sqrt(dStdsum / mListMaxPeak.size());	        
	        
			mMaxPeak = null;
			mMinPeak = null;			
			
			bIsStep = true;
		}
/*		else if(mMaxPeak == null && mMinPeak == null && mStayWin.getState() == StateStep.STATE_STAYING){
			THRESHOLD = mStayWin.getMean() + THRESHOLD;
		}*/
		
		if( mMeanMaxPeak == null && mMeanMinPeak == null ){
			THRESHOLD_MAX = mStayValue + THRESHOLD;
			THRESHOLD_MIN = mStayValue - THRESHOLD;
		} else {
			//THRESHOLD_MAX = mStayValue + mMeanMaxPeak - THRESHOLD;
			//THRESHOLD_MIN = mStayValue + mMeanMinPeak + THRESHOLD;
			
			//THRESHOLD_MAX = mMeanMaxPeak - THRESHOLD;
			//THRESHOLD_MIN = mMeanMinPeak + THRESHOLD;			
			if( mStdMaxPeak != 0 ) {
				THRESHOLD_MAX = mMeanMaxPeak - mStdMaxPeak - THRESHOLD;
				
				if( THRESHOLD_MAX < mStayValue + THRESHOLD )
					THRESHOLD_MAX = mStayValue + THRESHOLD;
			}
			
			if( mStdMinPeak != 0 ) {
				THRESHOLD_MIN = mMeanMinPeak + mStdMinPeak + THRESHOLD;
				
				if (THRESHOLD_MIN > mStayValue - THRESHOLD )
					THRESHOLD_MIN = mStayValue - THRESHOLD;
			}
			
			if( mStdMinPeak == 0 && mStdMaxPeak == 0 ) {
				THRESHOLD_MAX = mMeanMaxPeak - THRESHOLD;
				THRESHOLD_MIN = mMeanMinPeak + THRESHOLD;
			}
		}
		
		if( data > THRESHOLD_MAX || data < THRESHOLD_MIN ) {			
			if( data > 0 ) {
				if( mMaxPeak == null ) {
					mMaxPeak = data;
					//mState = StateStep.STATE_STEP;
				} else if(data > mMaxPeak){
					mMaxPeak = data;
					//mState = StateStep.STATE_STEP;
				}
				
				mMinPeak = null;
			} else {
				if( mMinPeak == null ) {
					mMinPeak = data;
					//mState = StateStep.STATE_STEP;
				} else {
					if( data < mMinPeak ) {
						mMinPeak = data;
						//mState = StateStep.STATE_STEP;
					}
				}					
			}				
		} /*else {
			//mState = StateStep.STATE_STAYING;
			mMeanStaying
		}*/
		return bIsStep;
	}

	public int getStepCount(){
		return mStepCount;
	}
}
