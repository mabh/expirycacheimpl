[![Build Status](https://travis-ci.org/mabh/expirycacheimpl.svg?branch=master)](https://travis-ci.org/mabh/expirycacheimpl)


A cache implementation with concurrent expired entries collector (analogous to a concurrent garbage collector)

Store list is a list of StoreListElements

StoreListElement (SLE) (also referred to as data unit (DU)  => 
	LockingMechanism
	Map
	
Each SLE is an independent mutually exclusive data structure from other SLEs
There are 3 kind of threads operating on each DU

	getters - gets from map possible removes also
	putters - puts entries into map
	collector - single thread which removes expired entries from SLE's map
	
	collector and getters/putters are sync using count down latch (CDL) and Phaser in LockingMechanism
	getters and putters are sync using synchronized over CacheEntry (value in the map)
	
	
Collector properties:

1. When collector runs on SLE getters and putters are blocked using CDL
2. Collector waits for existing get/put operations to get over using Phaser
3. When existing get/put operations on that SLE are done they deregister in phaser and collector proceeds to collect
   In the same time new get/put requests are blocked through CDL
4. Collector in end of operation counts down latch, thus enabling runs of waiting getters and putters


Possible Improvements on top of this:
1. checking possible Use of weakreferences so that java GC collects
expired entires

2. collector thread is scheduled 1 thread. Collection can be made
concurrent by multiple threads working on mutually exclusive areas of
the list store.

3. optimizing collector run. Right now its scheduled. Can be possibly
triggered when X % of expired entries accumulate




