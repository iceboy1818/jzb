package io.zbus.mq.net;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.zbus.mq.kit.FileKit;
import io.zbus.mq.logging.Logger;
import io.zbus.mq.logging.LoggerFactory;

public class EventLoop implements Closeable {
	private static final Logger log = LoggerFactory.getLogger(EventLoop.class);
 
	private EventLoopGroup group;   
	private final boolean ownGroup;  
	private SslContext sslContext;  
	private int packageSizeLimit = 1024*1024*32; //maximum of 32M

	public EventLoop() {
		try { 
			group = new NioEventLoopGroup();
			ownGroup = true;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	public EventLoop(EventLoopGroup group){ 
		this.group = group; 
		this.ownGroup = false;
	}
 
	public EventLoop(EventLoop driver){
		this(driver.group); 
		this.packageSizeLimit = driver.packageSizeLimit;
		this.sslContext = driver.sslContext;
	}

	public EventLoop duplicate(){ 
		return new EventLoop(this); 
	} 
	
	public EventLoopGroup getGroup() { 
		//then workerGroup
		return group;
	}

	public SslContext getSslContext() {
		return sslContext;
	}
	
	public boolean isSslEnabled() {
		return sslContext != null;
	} 

	public void setSslContext(SslContext sslContext) { 
		this.sslContext = sslContext;
	} 
	 
	public void setClientSslContext(InputStream certStream) { 
		try {
			SslContextBuilder builder = SslContextBuilder.forClient().trustManager(certStream);
			this.sslContext = builder.build();
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	public void setClientSslContext(String certStreamPath) { 
		InputStream certStream = FileKit.inputStream(certStreamPath);
		if(certStream == null){
			throw new IllegalArgumentException("Certification file(" +certStreamPath + ") not exists");
		}
		
		setClientSslContext(certStream);
		try {
			certStream.close();
		} catch (IOException e) {
			//ignore
		}
	} 
	
	public void setClientSslContextOfSelfSigned() { 
		try {
			SelfSignedCertificate cert = new SelfSignedCertificate(); 
			InputStream certStream = new FileInputStream(cert.certificate()); 
			setClientSslContext(certStream);
			
			try{
				certStream.close();
			} catch(IOException e) {
				//ignore
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	
	public void close() throws IOException { 
		if (ownGroup && group != null) {
			group.shutdownGracefully();
			group = null;
		}
	}
 

	public int getPackageSizeLimit() {
		return packageSizeLimit;
	}

	public void setPackageSizeLimit(int packageSizeLimit) {
		this.packageSizeLimit = packageSizeLimit;
	}   
	
}
