package cache;

import java.util.concurrent.TimeUnit;

public final class Common {

	public static final long COLLECTOR_INIT_DELAY = 10;	//begin after 1 minute
	public static final long COLLECTOR_INIT_PER = 10;	//run every two minute
	public static final TimeUnit COLLECTOR_TU = TimeUnit.SECONDS;

	public static final int MIN_INIT_SIZE = 100;
	public static final int MAX_INIT_SIZE = 100000;
	public static final int DEFAULT_INIT_SIZE = 1000;
	
	public static final int CHM_INIT_SIZE = 16;
	public static final float CHM_LF = (float)0.75;
	public static final int CHM_CL = 16;
}
