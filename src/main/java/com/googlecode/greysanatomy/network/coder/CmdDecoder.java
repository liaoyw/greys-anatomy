package com.googlecode.greysanatomy.network.coder;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import com.googlecode.greysanatomy.cmd.BaseCmd;
import com.googlecode.greysanatomy.network.serializer.Serializer;
import com.googlecode.greysanatomy.network.serializer.SerializerFactory;

/**
 * cmd–≠“ÈΩ‚¬Î∆˜
 * @author vlinux
 *
 */
public class CmdDecoder extends OneToOneDecoder {

	private Serializer serializer = SerializerFactory.getInstance();
	
	public int showxx() {
		return 0;
	}
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception {
		Protocol pro = (Protocol)msg;
		if( pro.getType() != Protocol.TYPE_CMD ) {
			return null;
		}
		BaseCmd cmd = serializer.decode(pro.getDatas());
		return cmd;
	}

}
