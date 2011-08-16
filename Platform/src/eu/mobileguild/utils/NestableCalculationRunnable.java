package eu.mobileguild.utils;

public interface NestableCalculationRunnable extends Runnable {

	Boolean THROW_EXCEPTION = Boolean.FALSE;
	Boolean RETURN = Boolean.TRUE;	
	
	void setThrowInterrupedException(boolean throwOrReturn);
}
