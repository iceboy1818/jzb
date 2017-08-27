
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
import io.zbus.mq.JwBusinessData;
import io.zbus.mq.Message;
import io.zbus.mq.kit.JsonKit;


public class JwBusinessCodec extends MessageToMessageCodec<Object, Message> { 
	@Override
	protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception { 
		FullHttpMessage httpMsg = null;
		
		if(msg.getJwBusinessData()!=null){
			JwBusinessData jwBusinessData=msg.getJwBusinessData();
			httpMsg.content().writeBytes(JsonKit.toJSONBytesWithType(jwBusinessData, null));
		}
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
				try {
					JwBusinessData jwBusinessData=JsonKit.convert(new String(data, Message.DEFAULT_ENCODING), JwBusinessData.class);
					msg.setJwBusinessData(jwBusinessData);
				} catch (Exception e) {
					msg.setBody(data);
				}
			}
		}

		out.add(msg);
	} 
}
