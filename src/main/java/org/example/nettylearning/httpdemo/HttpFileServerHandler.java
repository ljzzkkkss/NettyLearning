package org.example.nettylearning.httpdemo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String LOCATION = "location";
    private static final String CONNECTION = "connection";
    private final String url;
    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

    public HttpFileServerHandler(String url) {
        this.url = url;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request)throws Exception{
        if(!request.getDecoderResult().isSuccess()){
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        if(!request.getMethod().equals(HttpMethod.GET)){
            sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }
        final String uri = request.getUri();
        final String path = sanitizeUri(uri);
        if(null == path){
            sendError(ctx,HttpResponseStatus.FORBIDDEN);
            return;
        }

        File file = new File(path);
        if(file.isHidden() || !file.canRead()){
            sendError(ctx,HttpResponseStatus.NOT_FOUND);
            return;
        }

        if(file.isDirectory()){
            if(uri.endsWith("/")){
                sendListening(ctx, file);
            }else {
                sendRedirect(ctx, uri + "/");
            }
            return;
        }

        if(!file.isFile()){
            sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }

        RandomAccessFile randomAccessFile;
        try {
            randomAccessFile = new RandomAccessFile(file,"r");//以只读方式打开
        } catch (FileNotFoundException e) {
            sendError(ctx,HttpResponseStatus.NOT_FOUND);
            e.printStackTrace();
            return;
        }
        long fileLength = randomAccessFile.length();
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
        HttpHeaders.setContentLength(response,fileLength);
        setContentTypeHeader(response,file);
        if(HttpHeaders.isKeepAlive(response)){
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }
        ctx.write(response);
        ChannelFuture sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile,0,fileLength,8192),ctx.newProgressivePromise());
        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                if(total < 0){//total unknown
                    System.err.println("Transfer progress: " + progress);
                }else{
                    System.err.println("Transfer progress : " + progress + "/" + total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) {
                System.out.println("Transfer completed.");

            }
        });
        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if(!HttpHeaders.isKeepAlive(response)){
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure : " + status.toString() + System.lineSeparator(), StandardCharsets.UTF_8));
        response.headers().set(CONTENT_TYPE,"text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private String sanitizeUri(String uri) throws UnsupportedEncodingException {
        try {
            uri = URLDecoder.decode(uri,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            uri = URLDecoder.decode(uri,"ISO-8859-1");
        }
        if(!uri.startsWith(url)){
            return null;
        }
        if(!uri.startsWith("/")){
            return null;
        }
        uri = uri.replaceAll("/", Matcher.quoteReplacement(File.separator));
        if(uri.contains(File.separator + ".") || uri.contains("." + File.separator) || uri.startsWith(".") || uri.endsWith(".") || INSECURE_URI.matcher(uri).matches()){
            return null;
        }

        return System.getProperty("user.dir") + File.separator + uri;
    }

    private void sendListening(ChannelHandlerContext ctx, File dir){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(CONTENT_TYPE,"text/html; charset=UTF-8");
        StringBuilder builder = new StringBuilder();
        String dirPath = dir.getPath();
        builder.append("<!DOCTYPE html>\r\n").append(System.lineSeparator())
                .append("<html><head><title>")
                .append(dirPath).append(" 目录").append("</title></head><body>\r\n")
                .append("<h3>").append(dirPath).append("目录：").append("</h3>\r\n")
                .append("<ul>").append("<li>链接：<a href=\"../\">..</a></li>\r\n");
        for (File f : Objects.requireNonNull(dir.listFiles())){
            if(f.isHidden() || !f.canRead()){
                continue;
            }
            String name = f.getName();
            if(!ALLOWED_FILE_NAME.matcher(name).matches()){
                continue;
            }
            builder.append("<li>链接：<a href=\"").append(name).append("\">").append(name).append("</a></li>\r\n");
        }
        builder.append("</ul></body></html>\r\n");
        ByteBuf byteBuf = Unpooled.copiedBuffer(builder,StandardCharsets.UTF_8);
        response.content().writeBytes(byteBuf);
        byteBuf.release();
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendRedirect(ChannelHandlerContext ctx,String newUri){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.FOUND);
        response.headers().set(LOCATION,newUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void setContentTypeHeader(HttpResponse response, File file){
        MimetypesFileTypeMap mimeTYpesMap = new MimetypesFileTypeMap();
        response.headers().set(CONTENT_TYPE,mimeTYpesMap.getContentType(file.getPath()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if(ctx.channel().isActive()){
            sendError(ctx,HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
