package io.zbus.mq.net;

import java.io.Closeable;
import java.io.IOException;

public interface Session extends Closeable{
	void onMessage(Object message) throws IOException;
	void onError(Throwable error) throws Exception;
	void onActive() throws IOException;
	void onInactive() throws IOException;
	void write(Object message) throws IOException;
	boolean isActive();
}
