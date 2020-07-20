package org.example.nettylearning.resolver;

import org.example.nettylearning.Message;
import org.example.nettylearning.constants.MessageTypeEnum;

public class PingMessageResolver implements Resolver{
    @Override
    public boolean support(Message message) {
        return message.getMessageType() == MessageTypeEnum.PING;
    }

    @Override
    public Message resolve(Message message) {
        // 接收到ping消息后，返回一个pong消息返回
        System.out.println("receive ping message: " + System.currentTimeMillis());
        Message pong = new Message();
        pong.setMessageType(MessageTypeEnum.PONG);
        return pong;
    }
}
