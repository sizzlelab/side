package eu.mobileguild.utils;


public class ThreadUtil {
	public static void throwExceptionOrBeSilent(Boolean throwOrReturn) {
		if (throwOrReturn.equals(NestableCalculationRunnable.THROW_EXCEPTION)) {
			throw new RuntimeException(new InterruptedException());
		}
		return;
	}

	public static void throwExceptionOrBeSilent(Boolean throwOrReturn, InterruptedException ie) {
		if (throwOrReturn.equals(NestableCalculationRunnable.THROW_EXCEPTION)) {
			throw new RuntimeException(ie);
		}
		return;
	}
	
	public static void throwExceptionOrBeSilent(Boolean throwOrReturn, RuntimeException ex) {
		if (ex.getCause() instanceof InterruptedException && throwOrReturn.equals(NestableCalculationRunnable.RETURN)) {
			return;
		}
		throw ex;
	}
	
	public static void throwIfInterruped() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}
	}
}
