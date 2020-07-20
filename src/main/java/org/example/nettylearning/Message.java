package org.example.nettylearning;

import org.example.nettylearning.constants.MessageTypeEnum;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Message {
    private int magicNumber;
    private byte mainVersion;
    private byte subVersion;
    private byte modifyVersion;
    private String sessionId;

    private MessageTypeEnum messageType;
    private Map<String, String> attachments = new HashMap<>();
    private String body;

    public Map<String, String> getAttachments() {
        return Collections.unmodifiableMap(attachments);
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments.clear();
        if (null != attachments) {
            this.attachments.putAll(attachments);
        }
    }

    public void addAttachment(String key, String value) {
        attachments.put(key, value);
    }

    public int getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(int magicNumber) {
        this.magicNumber = magicNumber;
    }

    public byte getMainVersion() {
        return mainVersion;
    }

    public void setMainVersion(byte mainVersion) {
        this.mainVersion = mainVersion;
    }

    public byte getSubVersion() {
        return subVersion;
    }

    public void setSubVersion(byte subVersion) {
        this.subVersion = subVersion;
    }

    public byte getModifyVersion() {
        return modifyVersion;
    }

    public void setModifyVersion(byte modifyVersion) {
        this.modifyVersion = modifyVersion;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public MessageTypeEnum getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageTypeEnum messageType) {
        this.messageType = messageType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
