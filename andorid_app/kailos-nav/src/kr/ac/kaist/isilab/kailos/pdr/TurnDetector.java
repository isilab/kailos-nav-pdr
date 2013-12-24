package kr.ac.kaist.isilab.kailos.pdr;

public class TurnDetector {
	private WindowTurn mCurWin;	// Current window
	private WindowTurn mPrevWin;	// Previous window which is STATE_STRAIGHT
	private WindowTurn mTurnWin;	// Turning window
	private WindowTurn mStraightWin;	// Straight window
	
	private double THRESHOLD = 15;	// Threshold of minimum angle difference in window
	//private static double THRESHOLD = 10;	// Threshold of minimum angle difference in window
	private double THRESHOLD_DIFF = 45;	// Threshold of minimum angle difference between current and previous window
	//private static double THRESHOLD_DIFF = 30;	// Threshold of minimum angle difference between current and previous window
	private int mWindowSize = 10;	// Window size
	private double mAngleDiff = 0;	// Difference of angle
	private double mAbsoluteAngle = 0;	// Absolute angle
	private double mRelativeAngle = 0;	// Relative angle
	private double mInitialAngle = 0;	// Initial angle
	private double mCurrentAbsAngle = 0;	// Current absolute angle
	
	private long mTurnTimestamp;	// Turn timestamp
	private boolean mIsTurn = false;	// Turn or not

	/**
	 * Initialize window size and angle difference threshold
	 * @param	None
	 * @return	None
	 */
	public void init(){
		mCurWin = new WindowTurn();
		mPrevWin = new WindowTurn();
		mTurnWin = new WindowTurn();
		mStraightWin = new WindowTurn();
		
		mCurWin.setWindowSize(mWindowSize);
		mCurWin.setThreshold(THRESHOLD);
		mPrevWin.setWindowSize(mWindowSize);
		mPrevWin.setThreshold(THRESHOLD);
		mTurnWin.setWindowSize(mWindowSize);
		mTurnWin.setThreshold(THRESHOLD);	
		mStraightWin.setWindowSize(mWindowSize);
		mStraightWin.setThreshold(THRESHOLD);	
		
		mAngleDiff = 0;
		mAbsoluteAngle = 0;
		mRelativeAngle = 0;
		mInitialAngle = 0;
		mCurrentAbsAngle = mInitialAngle;
		mIsTurn = false;
	}	
	
	
	/**
	 * Set window size
	 * @param	size the size of window
	 * @return	None
	 */
	public void setWindowSize(int size){
		mWindowSize = size;
	}
	
	
	/**
	 * Set threshold of minimum angle difference in window
	 * @param	threshold threshold of minimum angle difference in window
	 * @return	None
	 */
	public void setThreshold(double threshold){
		THRESHOLD = threshold;
	}
	
	
	/**
	 * Set threshold of minimum angle difference between current and previous window
	 * @param	threshold threshold of minimum angle difference between current and previous window
	 * @return	None
	 */
	public void setDiffThreshold(double threshold){
		THRESHOLD_DIFF = threshold;
	}	
	
	
	/**
	 * 
	 * @param	
	 * @return	
	 */
	public void setInitialAngle(double dAngle){
		mInitialAngle = dAngle;
		mCurrentAbsAngle = mInitialAngle;
	}
	

	/**
	 * 
	 * @param	
	 * @return	
	 */
	public double getAngleDiff(){
		return mAngleDiff % 360;
	}
	
	
	/**
	 * 
	 * @param	
	 * @return	
	 */
	public double getApproximatedAngle(){
		int dQuotient = 0;
		double dRemainder;
		
		dQuotient = (int) (getAngleDiff() / 90);
		dRemainder = getAngleDiff() % 90;
		
		if(Math.abs(dRemainder) > 25){
			if(dRemainder > 0)
				dQuotient++;
			else
				dQuotient--;
		}
		
		return (90 * dQuotient) % 360;
	}	
	
	
	/**
	 * 
	 * @param	
	 * @return	
	 */
	public double getAbsoluteAngle(){
		mAbsoluteAngle = mAbsoluteAngle % 360 < 0 ? 360 - Math.abs(mAbsoluteAngle % 360) : mAbsoluteAngle % 360;
		return mAbsoluteAngle;
	}	
	
	
	/**
	 * 
	 * @param	
	 * @return	
	 */
	public double getApproximatedAbsAngle(){
		int dQuotient = 0;
		double dRemainder;
		
		dQuotient = (int) (getAbsoluteAngle() / 90);
		dRemainder = getAbsoluteAngle() % 90;
		
		if(Math.abs(dRemainder) > 25){
			if(dRemainder > 0)
				dQuotient++;
			else
				dQuotient--;
		}
		
		return (90 * dQuotient) % 360;
	}	
	

	/**
	 * 
	 * @param	
	 * @return	
	 */
	public double getRelativeAngle(){
		return mRelativeAngle % 360;
	}
	
	
	/**
	 * 
	 * @param	
	 * @return	
	 */
	public double getApproximatedRelativeAngle(){
		int dQuotient = 0;
		double dRemainder;
		
		dQuotient = (int) (getRelativeAngle() / 90);
		dRemainder = getRelativeAngle() % 90;
		
		if(Math.abs(dRemainder) > 25){
			if(dRemainder > 0)
				dQuotient++;
			else
				dQuotient--;
		}
		
		return (90 * dQuotient) % 360;
	}	
	
	
	public double getApproximatedAbsAngle2(){
/*		mCurrentAbsAngle += (-getApproximatedRelativeAngle());
		mCurrentAbsAngle = mCurrentAbsAngle % 360 < 0 ? 360 - Math.abs(mCurrentAbsAngle % 360) : mCurrentAbsAngle % 360;
		return mCurrentAbsAngle;
*/
		mCurrentAbsAngle += (-getApproximatedRelativeAngle());
		mCurrentAbsAngle = mCurrentAbsAngle % 360 < 0 ? 360 - Math.abs(mCurrentAbsAngle % 360) : mCurrentAbsAngle % 360;
		
		int dQuotient = 0;
		double dRemainder;
		
		dQuotient = (int) (mCurrentAbsAngle / 90);
		dRemainder = mCurrentAbsAngle % 90;
		
		if(Math.abs(dRemainder) > 25){
			if(dRemainder > 0)
				dQuotient++;
			else
				dQuotient--;
		}
		
		mCurrentAbsAngle =  (90 * dQuotient) % 360;		
		
		return mCurrentAbsAngle;
	}
	
	
	/**
	 * 
	 * @param	
	 * @return	
	 */
	public void setTurnTimestamp(long lTimestamp){
		mTurnTimestamp = lTimestamp;
	}
	
	
	/**
	 * 
	 * @param	
	 * @return	
	 */
	public long getTurnTimestamp(){
		return mTurnTimestamp;
	}
	
	
	/**
	 * Get a turn point
	 * @param	timestamp time when sensor data were collected
	 * @param	data angle value
	 * @return	
	 */
	public boolean isTurn(long timestamp, double data){
		//
		boolean bTurn = false;
		
		mCurWin.setValue(data);		
		
		if( mCurWin.getState() == State.STATE_STRAIGHT ){
			if(mIsTurn == true){	// If turn point is detected
				
				if(mStraightWin != null && mStraightWin.isContainedData() == true){
					if(Math.abs(data - mStraightWin.getFirstValue()) > THRESHOLD_DIFF){	// If difference of between current and previous STRAIGHT window mean value satisfy turn threshold
						//System.out.println("Timestamp : " + lTimestamp + " Diff deg : " + (data - mPrevWin.getMean()));
						//System.out.println("Timestamp : " + mTurnWin.getTimestamp() + " Diff deg : " + (Math.abs(data) - Math.abs(mPrevWin.getMean())));
						//System.out.println("Timestamp : " + mTurnWin.getTimestamp() + " Diff deg : " + (data - mStraightWin.getMean()));
						//System.out.println("Timestamp : " + mTurnWin.getTimestamp() + " Diff deg : " + (data - mStraightWin.getFirstValue()));
						// Turn!!!	
						//mAngleDiff = data - mStraightWin.getFirstValue();
						//mAngleDiff = data;
						//mAngleDiff = data - mPrevWin.getFirstValue() + getAngleDiff();
						mRelativeAngle = (data - mStraightWin.getFirstValue() + getApproximatedAngle()) - mAngleDiff;
						mAngleDiff = data - mStraightWin.getFirstValue() + getApproximatedAngle();
						mAbsoluteAngle = mInitialAngle - mAngleDiff;	// Gyroscope angle has negative value when direction is clock-wise
						//System.out.println("Timestamp : " + mTurnWin.getTimestamp() + " Diff deg : " + data);
//						System.out.println("Timestamp : " + mTurnWin.getTimestamp() + " Diff deg : " + mAngleDiff);
						setTurnTimestamp(mTurnWin.getTimestamp());
						bTurn = true;
						mStraightWin = null;
						mIsTurn = false;						
					}
				}
				else if(mPrevWin != null && mPrevWin.isContainedData() == true){
					if(Math.abs(data - mPrevWin.getFirstValue()) > THRESHOLD_DIFF){	// If difference of between current and previous STRAIGHT window mean value satisfy turn threshold
						//System.out.println("Timestamp : " + lTimestamp + " Diff deg : " + (data - mPrevWin.getMean()));
						//System.out.println("Timestamp : " + mTurnWin.getTimestamp() + " Diff deg : " + (Math.abs(data) - Math.abs(mPrevWin.getMean())));
						//System.out.println("Timestamp : " + mTurnWin.getTimestamp() + " Diff deg : " + (data - mPrevWin.getFirstValue()));
						//System.out.println("Timestamp : " + mTurnWin.getTimestamp() + " Diff deg : " + (data - mPrevWin.getFirstValue()));
						// Turn!!!
						//mAngleDiff = data - mPrevWin.getFirstValue();
						//mAngleDiff = data;
						//AngleDiff = data - mPrevWin.getFirstValue() + getAngleDiff();
						mRelativeAngle = (data - mPrevWin.getFirstValue() + getApproximatedAngle()) - mAngleDiff;
						mAngleDiff = data - mPrevWin.getFirstValue() + getApproximatedAngle();
						mAbsoluteAngle = mInitialAngle - mAngleDiff;	// Gyroscope angle has negative value when direction is clock-wise
						//System.out.println("Timestamp : " + mTurnWin.getTimestamp() + " Diff deg : " + data);
//						System.out.println("Timestamp : " + mTurnWin.getTimestamp() + " Diff deg : " + mAngleDiff);
						setTurnTimestamp(mTurnWin.getTimestamp());
						bTurn = true;
						mStraightWin = null;
						mIsTurn = false;						
					}	
				}		
			}
			
			// Store previous window
			try {
				mPrevWin = (WindowTurn) mCurWin.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(mCurWin.IsStraight() == true){
				try {
					mStraightWin = (WindowTurn) mCurWin.clone();
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else if(mCurWin.getState() == State.STATE_TURN){
			// Store turn window which is first among turn windows
			if(mIsTurn == false){
				try {	
						mTurnWin = (WindowTurn) mCurWin.clone();
						mTurnWin.setTimestamp(timestamp);
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			mIsTurn = true;
		}
		
		//return null;
		return bTurn;
	}	
}
