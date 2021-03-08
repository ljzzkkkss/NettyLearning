package org.example.nettylearning.nettyserver.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.example.nettylearning.nettyserver.constants.Constants;
import org.example.nettylearning.nettyserver.Message;
import org.example.nettylearning.nettyserver.constants.MessageTypeEnum;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) throws Exception {
        Message message = new Message();
        message.setMagicNumber(byteBuf.readInt());  // 读取魔数
        message.setMainVersion(byteBuf.readByte()); // 读取主版本号
        message.setSubVersion(byteBuf.readByte()); // 读取次版本号
        message.setModifyVersion(byteBuf.readByte());// 读取修订版本号
        byte[] sessionId = new byte[Constants.SESSION_ID_LENGTH];
        byteBuf.readBytes(sessionId);// 读取sessionId
        message.setSessionId(new String(sessionId));

        message.setMessageType(MessageTypeEnum.get(byteBuf.readByte()));// 读取当前的消息类型
        short attachmentSize = byteBuf.readShort();// 读取附件长度
        for (short i = 0; i < attachmentSize; i++) {
            int keyLength = byteBuf.readableBytes();// 读取键长度和数据
            byte[] key = new byte[keyLength];
            byteBuf.readBytes(key);
            int valueLength = byteBuf.readableBytes();// 读取值长度和数据
            byte[] value = new byte[valueLength];
            byteBuf.readBytes(value);
            message.addAttachment(new String(key), new String(value));
        }

        int bodyLength = byteBuf.readableBytes();// 读取消息体长度和数据
        byte[] body = new byte[bodyLength];
        byteBuf.readBytes(body);
        message.setBody(new String(body));
        out.add(message);
    }
}

