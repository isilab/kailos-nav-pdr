package kr.ac.kaist.isilab.kailos.pdr;

import java.util.ArrayList;

enum StateStep {
	STATE_NONE, STATE_STAYING, STATE_STEP
}

public class WindowStep {
	private ArrayList<Double> mData = new ArrayList<Double>();	// Array of angle data 
	private ArrayList<StateStep> mStateList = new ArrayList<StateStep>();	// Array of state
	private long mTimestamp;	// Timestamp
	private double mSum = 0;	// Summation of angle data
	private double mStdsum = 0;
	private double mMean = 0;	// Mean of angle data
	private double mStd = 0;
	private int mWinSize;	// Window size	
	private double mStayValue = 0;
	
	private StateStep mState;	// State of pedestrian
	
	private double THRESHOLD = 0.3;	// Threshold of angle difference
	
	public WindowStep(){
		mState = StateStep.STATE_NONE;	// Initialize initial state
	}
	
	public Object clone() throws CloneNotSupportedException{
		return super.clone();
	}
	
	public void setWindowSize(int size){
		if(size <= 0){
			throw new IllegalArgumentException("length must be greater than zero");
		}
		mWinSize = size;
	}
	
	public void setThreshold(double threshold){
		THRESHOLD = threshold;
	}
	
	public void setTimestamp(long timestamp){
		mTimestamp = timestamp;
	}
	
	public long getTimestamp(){
		return mTimestamp;
	}	
	
	public synchronized void setValue(double value){
		if (mData.size() == mWinSize && mWinSize > 0) {
			mSum -= mData.get(0);
            mData.remove(0);
            mStateList.remove(0);
        }
		
		mSum += value;
		mData.add(Double.valueOf(value));
        
        // Store state of each data
        if(mData.size() == mWinSize) {	// If window is fully stored        	
        	if(value > mStayValue) {
        		if(value > mStayValue + THRESHOLD){
        			mStateList.add(StateStep.STATE_STEP);
        		} else {
        			mStateList.add(StateStep.STATE_STAYING);
        		}
        	} else {
        		if(value < mStayValue - THRESHOLD){
        			mStateList.add(StateStep.STATE_STEP);
        		} else {
        			mStateList.add(StateStep.STATE_STAYING);
        		}
        	}
        } else {
        	mStateList.add(StateStep.STATE_NONE);
        }
        
        mMean = mSum / mData.size();
        
        mStdsum = 0;
        for(int i=0; i<mData.size(); i++){
        	mStdsum += Math.pow(((Double) mData.get(i)).doubleValue() - mMean, 2);
        }
        mStd = Math.sqrt(mStdsum / mData.size());
        
        determineState();	// Determine state
        
        if(mState == StateStep.STATE_STAYING){
        	mStayValue = mMean;
        }
	}
	
	private void determineState(){
		int iCntStaying = 0;
		int iCntStep = 0;
		int iCntNone = 0;
		
		// Count the number of each of state
        for(int i = 0; i < mStateList.size(); i++){
        	if(mStateList.get(i) == StateStep.STATE_STAYING){
        		iCntStaying++;
        	}
        	else if(mStateList.get(i) == StateStep.STATE_STEP){
        		iCntStep++;
        	}
        	else{
        		iCntNone++;
        	}
        }		
        
/*        // Set state which gets more votes than the other
        if(iCntStaying > iCntStep){
        	if(iCntStaying > iCntNone){
        		mState = StateStep.STATE_STAYING;
        	}
        	else
        		mState = StateStep.STATE_NONE;
        }
        else{
        	if(iCntStep > iCntNone){
        		mState = StateStep.STATE_STEP;
        	}
        	else
        		mState = StateStep.STATE_NONE;        	
        }*/
        if(iCntStaying == mStateList.size()){
        	mState = StateStep.STATE_STAYING;
        }
        else{
            if(iCntStep > iCntNone){   
            	mState = StateStep.STATE_STEP;
            }
            else{
            	mState = StateStep.STATE_NONE;
            }
        }
	}
	
	public StateStep getState(){
		return mState;
	}
	
	public double getMean(){
		return mMean;
	}
	
	public double getStayValue(){
		return mStayValue;
	}
	
	public double getStd(){
		return mStd;
	}
	
	public double getFirstValue(){
		return mData.get(0);
	}

	public double getLastValue(){
		return mData.get(mData.size() -1);
	}
}
