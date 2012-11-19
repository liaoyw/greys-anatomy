package com.googlecode.greysanatomy.console.network.coder;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import com.googlecode.greysanatomy.console.network.serializer.Serializer;
import com.googlecode.greysanatomy.console.network.serializer.SerializerFactory;

/**
 * cmdЭ�������
 * @author vlinux
 *
 */
public class CmdDecoder extends OneToOneDecoder {

	private final Serializer serializer = SerializerFactory.getInstance();
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception {
		Protocol pro = (Protocol)msg;
		if( pro.getType() == Protocol.TYPE_CMD ) {
			return serializer.decode(pro.getDatas());
		} else if( pro.getType() == Protocol.TYPE_HEARTBEAT ) {
			return null;
		} else {
			// ignore this ����˵Ӧ�����쳣�ź���
			return null;
		}
		
	}

}
