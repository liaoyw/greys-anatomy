package com.googlecode.greysanatomy.network.coder;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.googlecode.greysanatomy.cmd.BaseCmd;
import com.googlecode.greysanatomy.network.serializer.Serializer;
import com.googlecode.greysanatomy.network.serializer.SerializerFactory;

/**
 * cmd–≠“È±‡¬Î∆˜
 * @author vlinux
 *
 */
public class CmdEncoder extends OneToOneEncoder {

	private Serializer serializer = SerializerFactory.getInstance();
	
	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception {
		
		BaseCmd cmd = (BaseCmd)msg;
		Protocol pro = new Protocol();
		pro.setType(Protocol.TYPE_CMD);
		byte[] datas = serializer.encode(cmd);
		pro.setLength(datas.length);
		pro.setDatas(datas);
		
		return pro;
	}

}
