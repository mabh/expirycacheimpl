package cache;

import java.util.List;
import java.util.Set;

public final class ExpiredEntriesCollector<K,V> implements Runnable {

	private List<StoreListElement<K,V>> store = null;
	
	public ExpiredEntriesCollector(final List<StoreListElement<K,V>> store) {
		this.store = store;
	}
	
	public void run() {
		for(StoreListElement<K,V> listElement: this.store) {
			
			try {
				//stop the puts and gets using the listElement's latch reset
				listElement.getLockMechanism().setLatchToOne();
				
				//register against listElement's phaser and wait for puts/gets to finish (deregister themselves)
 				listElement.getLockMechanism().getPhaser().register();
				listElement.getLockMechanism().getPhaser().arriveAndAwaitAdvance();
				
				Set<K> keys = listElement.getMap().keySet();
				for(K key: keys) {
					if(listElement.getMap().get(key).isExpired()) {
						listElement.getMap().remove(key);
					}
				}
				
			} finally {
				//remeber to CDL countdown to allow waiting puts/gets to progress
				listElement.getLockMechanism().getLatch().countDown();
			}
		}
	}
}
