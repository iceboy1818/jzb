/**
 * The MIT License (MIT)
 * Copyright (c) 2009-2015 HONG LEIMING
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.zbus.mq.net;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.zbus.mq.Message;
import io.zbus.mq.MessageCallback;
 

public class Sync {   
	private ConcurrentMap<String, Ticket> tickets = new ConcurrentHashMap<String, Ticket>();
	  
	
	public Ticket getTicket(String id) {
		if(id == null) return null;
		return tickets.get(id);
	}
 
	public Ticket createTicket(Message req, long timeout) {
		return createTicket(req, timeout, null);
	} 
	
	public Ticket createTicket(Message req, long timeout, MessageCallback callback) {
		Ticket ticket = new Ticket(req, timeout);
		ticket.setCallback(callback);

		if (tickets.putIfAbsent(ticket.getId(), ticket) != null) {
			throw new IllegalArgumentException("duplicate ticket number.");
		}

		return ticket;
	} 
	
	public Ticket removeTicket(String id) {
		if(id == null) return null;
		return tickets.remove(id);
	}
	
	public Ticket removeTicket(Message res) {
		if(res == null) return null;  
		return removeTicket(res.getId());
	}
	
	public void clearTicket(){
		for(Ticket ticket : tickets.values()){
			ticket.countDown();
		}
		tickets.clear();
	}   
	
	public static String nextId(){
		return UUID.randomUUID().toString();  
	}
	 
	public static class Ticket{     
		private CountDownLatch latch = new CountDownLatch(1); 
		private String id;
		private Message request = null; 
		private Message response = null;  
		private MessageCallback callback = null; 
		 
		private long timeout = 1000; 
		private final long startTime = System.currentTimeMillis();  
		
		public Ticket(Message request, long timeout) {  
			this.id = nextId();
			if(request != null){
				request.setId(this.id);
			}  
			
			this.request = request; 
			this.timeout = timeout;
		}  
	 
		public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
			boolean status = this.latch.await(timeout, unit); 
			return status;
		}
	 
		public void await() throws InterruptedException {
			this.latch.await(); 
		}
	 
		public void expired() { 
			this.countDown(); 
		}
	 
		private void countDown() {
			this.latch.countDown();
		}
	 
		public boolean isDone() {
			return this.latch.getCount() == 0;
		}
	 
		public void notifyResponse(Message response) {
			this.response = response;
			if (this.callback != null)
				this.callback.onReturn(response); 
			this.countDown();
		} 
	 
		public MessageCallback getCallback() {
			return callback;
		}
	 
		public void setCallback(MessageCallback callback) {
			this.callback = callback;
		} 
		 
		public String getId() {
			return id;
		}

		public Message request() {
			return this.request;
		}
		
		public Message response() {
			return this.response;
		}
		
		public long getTimeout() {
			return timeout;
		}
		
		public long getStartTime() {
			return startTime;
		}  
	}
}
