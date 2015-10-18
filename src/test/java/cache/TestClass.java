package cache;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class TestClass {
	
	@Test
	public void test1() throws InterruptedException {
		ExpiryCache<String, String> cache = null;
		try {
			cache = new ArrayListIndexedExpiryCache<>(100);
			cache.put("key0", "value0", 4, TimeUnit.SECONDS);
			Assert.assertEquals("value0", cache.get("key0"));
		} finally {
			((ArrayListIndexedExpiryCache<String, String>)cache).shutdown();
		}
	}
	
	@Test
	public void test2() throws InterruptedException {
		ExpiryCache<String, String> cache = null;
		
		try { 
			cache = new ArrayListIndexedExpiryCache<>(100);
			cache.put("key0", "value0", 4, TimeUnit.SECONDS);
			Thread.sleep(5000);
			Assert.assertEquals(null, cache.get("key0"));
		} finally {
			((ArrayListIndexedExpiryCache<String, String>)cache).shutdown();
		}
	}
	
	@Test
	public void test3() throws InterruptedException {
		final ExpiryCache<Integer, String> cache = new ArrayListIndexedExpiryCache<>(1000);;
		
		try {
			ExecutorService service = Executors.newFixedThreadPool(1000);
			final Random rand = new Random();
			final int iterations = 100000;
			
			for(int i = 0; i < 10000; i++) {
				service.submit(new Runnable() {
					public void run() {
						
						for(int i = 0; i < iterations ; i++) {
							Integer key = Integer.valueOf(rand.nextInt(iterations));
							cache.put(key, String.valueOf(key), rand.nextInt(10), TimeUnit.SECONDS);
							//System.out.println("putting " + key.hashCode());
						}
					}
				});
			}
			
			System.out.println("done submitting tasks");
			cache.put(2343245, "rgiurhgeiuhg", 1, TimeUnit.SECONDS);
			String out = cache.get(2343245);
			
			service.awaitTermination(30, TimeUnit.SECONDS);
			service.shutdown();
			
			Assert.assertEquals("rgiurhgeiuhg", out);
		} finally {
			((ArrayListIndexedExpiryCache<Integer, String>)cache).shutdown();
		}
	}
	
	@Test
	public void testCollector() throws InterruptedException {
		ExpiryCache<String, String> cache = null;
		
		try { 
			cache = new ArrayListIndexedExpiryCache<>(100);
			cache.put("key0", "value0", 5, TimeUnit.SECONDS);
			Thread.sleep(2000);
			boolean firstCheck = "value0".equals(cache.get("key0"));
			
			Thread.sleep(10000);
			
			boolean secondCheck = (null == cache.get("key0"));
			
			Assert.assertTrue(firstCheck && secondCheck);
			
			
		} finally {
			((ArrayListIndexedExpiryCache<String, String>)cache).shutdown();
		}
	}
}
