package cache;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author MABH
 * an immutable data object representing a cache entry
 * consists of:
 * 1. Key
 * 2. Value
 * 3. Time of entry creation
 * 4. TTL (int)
 * 5. TimeUnit
 */
public final class CacheEntry<K, V> {
	private final K key;
	private final V value;
	private final Date creationTime;
	private int ttl;
	private TimeUnit timeUnit;
	
	public CacheEntry(K key, V value, Date creationTime, int ttl, TimeUnit timeUnit) {
		this.key = key;
		this.value = value;
		this.creationTime = creationTime;
		this.ttl = ttl;
		this.timeUnit = timeUnit;
	}

	public K getKey() {
		return this.key;
	}

	public V getValue() {
		return this.value;
	}

	public Date getCreationTime() {
		return this.creationTime;
	}

	public int getTtl() {
		return this.ttl;
	}

	public TimeUnit getTimeUnit() {
		return this.timeUnit;
	}
	
	/*
	 * is this cache entry expired
	 * if create time + TTL < current date then expired else not
	 */
	public boolean isExpired() {
		//some caches like ehCache treat TTL 0 as never expiring - same behaviour replicated here
		if(this.ttl == 0)
			return false;
		Date incremented = new Date(this.creationTime.getTime() + this.timeUnit.toMillis(this.ttl));
		Date now = new Date();
		return incremented.before(now);
	}
}
