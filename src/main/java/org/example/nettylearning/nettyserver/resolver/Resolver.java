package org.example.nettylearning.nettyserver.resolver;

import org.example.nettylearning.nettyserver.Message;

public interface Resolver {

    boolean support(Message message);

    Message resolve(Message message);
}
