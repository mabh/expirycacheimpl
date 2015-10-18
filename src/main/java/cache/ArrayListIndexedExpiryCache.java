package cache;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author MABH
 * Stores cache entries as CacheEntry immutable object in indexed CHMs
 */
public final class ArrayListIndexedExpiryCache<K,V> implements ExpiryCache<K, V> {

	//storage for cache entries
	private List<StoreListElement<K,V>> store = null;
	private int initialSize;
	private ScheduledExecutorService schExecutor = null;
	
	public ArrayListIndexedExpiryCache(int initialSize) {
		if(initialSize > Common.MAX_INIT_SIZE || initialSize < Common.MIN_INIT_SIZE) {
			this.initialSize = Common.DEFAULT_INIT_SIZE;
		} else {
			this.initialSize = initialSize;
		}
		this.store = new ArrayList<>(initialSize);
		for(int i = 0; i < this.initialSize; i++) {
			this.store.add(new StoreListElement<K,V>());
		}
		
		//collector set TODO: improvement in parallelizing expired collectors over independent units in list
		this.schExecutor = Executors.newScheduledThreadPool(1);
		this.schExecutor.scheduleAtFixedRate(new ExpiredEntriesCollector<>(store),
											Common.COLLECTOR_INIT_DELAY,
											Common.COLLECTOR_INIT_PER,
											Common.COLLECTOR_TU);
		
		
	}
	
	public void shutdown() {
		this.schExecutor.shutdown();
	}

	private StoreListElement<K,V> getIndexedStoreListElement(K key) {
		int hash = key.hashCode();
		int index = hash % this.initialSize;
		return this.store.get(index);
	}
	
	/*
	 * put a key with the given value in the cache
	 * overwrite previous value for the same key if necessary with the new value
	 */
	public void put(K key, V value, int ttl, TimeUnit timeUnit) {
		CacheEntry newEntry = new CacheEntry<K, V>(key, value, new Date(), ttl, timeUnit);
		
		try {
			//await on CDL if collector is running - progress on CDL when 0 - collector counts down 
			this.getIndexedStoreListElement(key).getLockMechanism().getLatch().await();
			
			//collector not running - register on phaser 
			this.getIndexedStoreListElement(key).getLockMechanism().getPhaser().register();
			
			if(this.getIndexedStoreListElement(key).getMap().containsKey(key)) {
				CacheEntry<K,V> existingEntry = this.getIndexedStoreListElement(key).getMap().get(key);
				
				/*
				 * synch with gets also since gets can optionally remove entries from map if expired
				 * we do not want gets and puts to concurrently run otherwise get can possibly remove a 
				 * fresh non expired entry
				 */
				synchronized (existingEntry) {
					this.getIndexedStoreListElement(key).getMap().put(key, newEntry);
				}
				
			} else {
				this.getIndexedStoreListElement(key).getMap().put(key, newEntry);
			}
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			/*
			 * reduce the number of parties which collector has to wait on
			 * once all parties except collector are dereg collector progresses
			 */
			
			this.getIndexedStoreListElement(key).getLockMechanism().getPhaser().arriveAndDeregister();
		}
		
		
	}

	/*
     * get an entry if not expired else return null
     * Average case complexity = O(1)
	 * 		- balanced scenario - indexed CHM bucket contained 1 entry or none
	 * Worst case complexity = O(n)
	 * 		- all keys had same hashcode but were not equal - same CHM bucket in a linked list
	 */
	public V get(K key) {
		try {
			//await on CDL if collector is running - progress on CDL when 0 - collector counts down 
			this.getIndexedStoreListElement(key).getLockMechanism().getLatch().await();
			
			//collector not running - register on phaser 
			this.getIndexedStoreListElement(key).getLockMechanism().getPhaser().register();
			
			if(this.getIndexedStoreListElement(key).getMap().containsKey(key)) {
				CacheEntry<K,V> existingEntry = this.getIndexedStoreListElement(key).getMap().get(key);
				
				/*
				 * synch with puts also since gets can optionally remove entries from map if expired
				 * we do not want gets and puts to concurrently run otherwise get can possibly remove a 
				 * fresh non expired entry
				 */
				synchronized (existingEntry) {
					if(existingEntry.isExpired()) {
						this.getIndexedStoreListElement(key).getMap().remove(key);
						return null;
					}
					return existingEntry.getValue();
				}
				
			} else {
				return null;
			}
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		} finally {
			/*
			 * reduce the number of parties which collector has to wait on
			 * once all parties except collector are dereg collector progresses
			 */
			
			this.getIndexedStoreListElement(key).getLockMechanism().getPhaser().arriveAndDeregister();
		}
	
	}
}









