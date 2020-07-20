package org.example.nettylearning.resolver;

import org.example.nettylearning.Message;

public interface Resolver {

    boolean support(Message message);

    Message resolve(Message message);
}
