package cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;

/*
 * This is what gets stored in the main list
 */
public final class StoreListElement<K,V> {
	
	final class LockMechanism {
		private volatile CountDownLatch cdlatch = new CountDownLatch(0);
		private volatile Phaser phaser = new Phaser();
		
		public void setLatchToOne() {
			this.cdlatch = new CountDownLatch(1);
		}
		
		public CountDownLatch getLatch() {
			return this.cdlatch;
		}
		
		public Phaser getPhaser() {
			return this.phaser;
		}
	}
	
	private LockMechanism lm = new LockMechanism();
	private Map<K, CacheEntry<K,V>> map = new ConcurrentHashMap<>(Common.CHM_INIT_SIZE, Common.CHM_LF, Common.CHM_CL);
	
	public Map<K, CacheEntry<K,V>> getMap() {
		return this.map;
	}
	
	public LockMechanism getLockMechanism() {
		return this.lm;
	}
}

