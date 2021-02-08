package org.example.nettylearning.nettyserver.resolver;

import org.example.nettylearning.nettyserver.Message;
import org.example.nettylearning.nettyserver.constants.MessageTypeEnum;

import java.util.concurrent.atomic.AtomicInteger;

public class RequestMessageResolver implements Resolver{

    private static final AtomicInteger counter = new AtomicInteger(1);

    @Override
    public boolean support(Message message) {
        return message.getMessageType() == MessageTypeEnum.REQUEST;
    }

    @Override
    public Message resolve(Message message) {
        // 接收到request消息之后，对消息进行处理，这里主要是将其打印出来
        int index = counter.getAndIncrement();
        System.out.println("[trx: " + message.getSessionId() + "]"
                + index + ". receive request: " + message.getBody());
        System.out.println("[trx: " + message.getSessionId() + "]"
                + index + ". attachments: " + message.getAttachments());

        // 处理完成后，生成一个响应消息返回
        Message response = new Message();
        response.setMessageType(MessageTypeEnum.RESPONSE);
        response.setBody("nice to meet you too!");
        response.addAttachment("name", "lj");
        response.addAttachment("hometown", "nj");
        return response;
    }
}
