package kr.ac.kaist.isilab.kailos.pdr;

import java.util.LinkedList;

enum State{
	STATE_NONE, STATE_STRAIGHT, STATE_TURN
}

public class WindowTurn implements Cloneable{
	private LinkedList<Double> mData = new LinkedList<Double>();	// Array of angle data 
	private LinkedList<State> mStateList = new LinkedList<State>();	// Array of state
	private long mTimestamp;	// Timestamp
	private double mSum = 0;	// Summation of angle data
	private double mStdsum = 0;
	private double mMean = 0;	// Mean of angle data
	private double mStraightMean = 0;
	private double mStd = 0;
	private int mWinSize;	// Window size	
	private State mState;	// State of pedestrian
	private boolean mIsStraight = false;
	
	private double THRESHOLD = 30;	// Threshold of angle difference
	
	/**
	 * Constructor
	 * @param	None
	 * @return	None
	 */
	public WindowTurn(){
		mState = State.STATE_NONE;	// Initialize initial state
	}
	
	/**
	 * Return the clone
	 * @param	None
	 * @return	Clone
	 */
	public Object clone() throws CloneNotSupportedException{
		// Deep copy
		WindowTurn tmpWindow = (WindowTurn)super.clone();
		tmpWindow.mData = (LinkedList<Double>)mData.clone();
		tmpWindow.mStateList = (LinkedList<State>)mStateList.clone();
		return tmpWindow;
		//return super.clone();
	}
	
	/**
	 * Set window size
	 * @param	size the size of window
	 * @return	None
	 */
	public void setWindowSize(int size){
		if(size <= 0){
			throw new IllegalArgumentException("length must be greater than zero");
		}
		mWinSize = size;
	}
	
	/**
	 * Set threshold of angle difference
	 * @param	threshold the angle difference
	 * @return	None
	 */
	public void setThreshold(double threshold){
		THRESHOLD = threshold;
	}
	
	/**
	 * Set timestamp of state change
	 * @param	timestamp the time of state change
	 * @return	None
	 */
	public void setTimestamp(long timestamp){
		mTimestamp = timestamp;
	}
	
	/**
	 * Get timestamp of state change
	 * @param	None	
	 * @return	timestamp the time of state change
	 */
	public long getTimestamp(){
		return mTimestamp;
	}	

	public synchronized void setValue(double value){
		int iStraightCnt = 0;
		
		if( mData.size() == mWinSize && mWinSize > 0 ) {
			mSum -= ((Double) mData.getFirst()).doubleValue();
            mData.removeFirst();
            mStateList.removeFirst();
        }
		
		mSum += value;
		mData.addLast(Double.valueOf(value));
        
        // Store state of each data
        if( mData.size() == mWinSize ) {	// Window is used up it's capacity...
        	if( Math.abs(getFirstValue() - getLastValue()) < THRESHOLD ) {	// Threshold check...
        		mStateList.addLast(State.STATE_STRAIGHT);
        	} else {
        		mStateList.addLast(State.STATE_TURN);
        	}
        } else {
        	mStateList.addLast(State.STATE_NONE);
        }
        
        determineState();	// Determine state       
        
        mMean = mSum / mData.size();
        
        mStraightMean = 0;
        mStdsum = 0;
        for(int i = 0; i < mData.size(); i++){
        	mStdsum += Math.pow(((Double) mData.get(i)).doubleValue() - mMean, 2);
        	if(mStateList.get(i) == State.STATE_STRAIGHT){
        		mStraightMean += ((Double) mData.get(i));
        		iStraightCnt++;
        	}
        }
        mStraightMean = mStraightMean / iStraightCnt;
        mStd = Math.sqrt(mStdsum / mData.size());
        

	}	
	
	/**
	 * Determine state
	 * @param	None
	 * @return	None
	 */
	private void determineState(){
		int iCntStraight = 0;
		int iCntTurn = 0;
		int iCntNone = 0;
		
		// Count the number of each of state
        for(int i = 0; i < mStateList.size(); i++){
        	if(mStateList.get(i) == State.STATE_STRAIGHT){
        		iCntStraight++;
        	}
        	else if(mStateList.get(i) == State.STATE_TURN){
        		iCntTurn++;
        	}
        	else{
        		iCntNone++;
        	}
        }		
        
        // Set state which gets more votes than the other
        if(iCntStraight > iCntTurn){
        	if(iCntStraight > iCntNone){
        		mState = State.STATE_STRAIGHT;
        	}
        	else
        		mState = State.STATE_NONE;
        }
        else{
        	if(iCntTurn > iCntNone){
        		mState = State.STATE_TURN;
        	}
        	else
        		mState = State.STATE_NONE;        	
        }
        
        // 
        if(iCntStraight == mStateList.size()){
        	mIsStraight = true;
        }
        else{
        	mIsStraight = false;
        }
	}
	
	public boolean IsStraight(){
		return mIsStraight;
	}
	
	/**
	 * Get state of pedestrian
	 * @param	None
	 * @return	the state of pedestrian
	 */
	public State getState(){
		return mState;
	}
	
	public double getStraightMean(){
		return mStraightMean;
	}
	
	/**
	 * Get mean of angle data within window
	 * @param	None
	 * @return	mean of angle data within window
	 */
	public double getMean(){
		return mMean;
	}
	
	/**
	 * 
	 * @param	
	 * @param	
	 * @return	
	 */
	public double getStd(){
		return mStd;
	}
	
	/**
	 * 
	 * @param	
	 * @param	
	 * @return	
	 */
	public double getFirstValue(){
		return ((Double) mData.getFirst()).doubleValue();
	}
	
	/**
	 * 
	 * @param	
	 * @param	
	 * @return	
	 */
	public double getLastValue(){
		return ((Double) mData.getLast()).doubleValue();
	}
	
	public boolean isContainedData(){
		if(mData.size() == 0)
			return false;
		else
			return true;
	}
}