package io.zbus.mq.net;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.zbus.mq.Message;


public class MessageCodec extends MessageToMessageCodec<Object, Message> { 
	@Override
	protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception { 
		FullHttpMessage httpMsg = null;
		if (msg.getStatus() == null) {// as request
			httpMsg = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.valueOf(msg.getMethod()),
					msg.getUrl());
		} else {// as response
			httpMsg = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
					HttpResponseStatus.valueOf(Integer.valueOf(msg.getStatus())));
		}

		for (Entry<String, String> e : msg.getHeaders().entrySet()) {
			httpMsg.headers().add(e.getKey().toLowerCase(), e.getValue());
		}
		if (msg.getBody() != null) {
			httpMsg.content().writeBytes(msg.getBody());
		}
		String b1=msg.getBodyString();
		out.add(httpMsg);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, Object obj, List<Object> out) throws Exception { 
		if(!(obj instanceof HttpMessage)){
			throw new IllegalArgumentException("HttpMessage object required: " + obj);
		}
		
		HttpMessage httpMsg = (HttpMessage) obj;
		Message msg = new Message();
		Iterator<Entry<String, String>> iter = httpMsg.headers().iteratorAsString();
		while (iter.hasNext()) {
			Entry<String, String> e = iter.next();
			msg.setHeader(e.getKey().toLowerCase(), e.getValue());
		}

		if (httpMsg instanceof HttpRequest) {
			HttpRequest req = (HttpRequest) httpMsg;
			msg.setMethod(req.method().name());
			msg.setUrl(req.uri());
		} else if (httpMsg instanceof HttpResponse) {
			HttpResponse resp = (HttpResponse) httpMsg;
			int status = resp.status().code();
			msg.setStatus(status);
		}

		if (httpMsg instanceof FullHttpMessage) {
			FullHttpMessage fullReq = (FullHttpMessage) httpMsg;
			int size = fullReq.content().readableBytes();
			if (size > 0) {
				byte[] data = new byte[size];
				fullReq.content().readBytes(data);
				msg.setBody(data);
				String tem=msg.getBodyString();
				String tem1=msg.getBodyString();
				tem1="";
			}
		}

		out.add(msg);
	} 
}
