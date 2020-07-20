package org.example.nettylearning.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.commons.lang3.StringUtils;
import org.example.nettylearning.constants.Constants;
import org.example.nettylearning.Message;
import org.example.nettylearning.constants.MessageTypeEnum;

import java.nio.charset.Charset;
import java.util.UUID;

public class MessageEncoder extends MessageToByteEncoder<Message> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf out) throws Exception {
        //空消息不写入channel
        if (message.getMessageType() != MessageTypeEnum.EMPTY) {
            out.writeInt(Constants.MAGIC_NUMBER);//写入魔数
            out.writeByte(Constants.MAIN_VERSION);//写入主版本号
            out.writeByte(Constants.SUB_VERSION);//写入子版本号
            out.writeByte(Constants.MODIFY_VERSION);//写入修订版本号

            //如果没有sessionId
            if (StringUtils.isBlank(message.getSessionId())) {
                // 生成一个sessionId，并将其写入到字节序列中
                String sessionId = UUID.randomUUID().toString().toLowerCase();
                message.setSessionId(sessionId);
                out.writeCharSequence(sessionId, Charset.defaultCharset());
            }
            out.writeByte(message.getMessageType().getType());// 写入当前消息的类型
            out.writeShort(message.getAttachments().size());// 写入当前消息的附加参数数量
            message.getAttachments().forEach((key, value) -> {
                Charset charset = Charset.defaultCharset();
                out.writeInt(key.length());// 写入键的长度
                out.writeCharSequence(key, charset);// 写入键数据
                out.writeInt(value.length());// 希尔值的长度
                out.writeCharSequence(value, charset);// 写入值数据
            });

            if (null == message.getBody()) {
                out.writeInt(0);// 如果消息体为空，则写入0，表示消息体长度为0
            } else {
                out.writeInt(message.getBody().length());
                out.writeCharSequence(message.getBody(), Charset.defaultCharset());
            }
        }
    }
}
