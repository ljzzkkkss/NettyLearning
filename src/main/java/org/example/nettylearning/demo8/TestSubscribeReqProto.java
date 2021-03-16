package org.example.nettylearning.demo8;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

public class TestSubscribeReqProto {

    private static byte[] encode(SubscribeReqProto.SubscribeReq req){
        return req.toByteArray();
    }

    private static SubscribeReqProto.SubscribeReq decode(byte[] body) throws InvalidProtocolBufferException {
        return SubscribeReqProto.SubscribeReq.parseFrom(body);
    }

    private static SubscribeReqProto.SubscribeReq createSubscribeReq(){
        SubscribeReqProto.SubscribeReq.Builder builder = SubscribeReqProto.SubscribeReq.newBuilder();
        builder.addSubReqID(1);
        builder.addUserName("Liu Jun");
        builder.addProductName("Netty book");
        List<String> addressList = new ArrayList<>();
        addressList.add("NanJing YuHuaTai");
        addressList.add("BeiJing LiuLiChang");
        addressList.add("ShenZhen HongShuLin");
        builder.addAllAddress(addressList);
        return builder.build();
    }

    /**
     *
     * @param args
     * @throws InvalidProtocolBufferException
     */
    public static void main(String[] args) throws InvalidProtocolBufferException {
        SubscribeReqProto.SubscribeReq req = createSubscribeReq();
        System.out.println("Before encode : " + req.toString());
        SubscribeReqProto.SubscribeReq decodedReq = decode(encode(req));
        System.out.println("After decode : " + decodedReq.toString());
        System.out.println("Assert equal : --> " + decodedReq.equals(req));
    }
}
