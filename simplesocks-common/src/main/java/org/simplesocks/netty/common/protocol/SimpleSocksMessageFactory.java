package org.simplesocks.netty.common.protocol;

import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.encrypter.CaesarEncrypter;
import org.simplesocks.netty.common.exception.ProtocolParseException;

import java.nio.charset.StandardCharsets;

@Slf4j
public class SimpleSocksMessageFactory {


    public static SimpleSocksMessage newInstance(ByteBuf byteBuf){

        byte type = byteBuf.readByte();
        DataType dataType = DataType.parseByte(type);
        switch (dataType){
            case CONNECT:
            {
                byte authLen = byteBuf.readByte();
                byte encLen = byteBuf.readByte();
                byte[] authBytes = new byte[authLen];
                byteBuf.readBytes(authBytes);
                byte[] encBytes = new byte[encLen];
                byteBuf.readBytes(encBytes);

                byte proxyType = byteBuf.readByte();
                ConnectionMessage.Type proxyTypeEnum = ConnectionMessage.Type.valueOf(proxyType);
                short port = byteBuf.readShort();
                int iPort = port<0 ? 65536 + port : (int)port;  //fix short overflow

                byte offset = byteBuf.readByte();
                CaesarEncrypter e = new CaesarEncrypter(offset);
                int hostLen = byteBuf.readableBytes();
                byte[] hostBytes = new byte[hostLen];
                byteBuf.readBytes(hostBytes);

                authBytes = e.decrypt(authBytes);
                hostBytes = e.decrypt(hostBytes);
                encBytes = e.decrypt(encBytes);
                String auth = new String(authBytes,StandardCharsets.UTF_8);
                String encType = new String(encBytes, StandardCharsets.UTF_8);
                String host = new String(hostBytes, StandardCharsets.UTF_8);
                return new ConnectionMessage(auth, encType, host, iPort, proxyTypeEnum);
            }
            case CONNECT_RESPONSE:{
                byte result = byteBuf.readByte();
                byte encLen = byteBuf.readByte();
                byte[] enc = new byte[encLen];
                byte encPasswordLen = byteBuf.readByte();
                byte[] encIv = new byte[encPasswordLen];
                byteBuf.readBytes(enc);
                String encType = new String(enc, StandardCharsets.UTF_8);
                byteBuf.readBytes(encIv);
                if(result==Constants.RESPONSE_FAIL){
                    return ConnectionResponse.fail(encType);
                }else if(result==Constants.RESPONSE_SUCCESS){
                    return new ConnectionResponse(ServerResponseMessage.Code.SUCCESS, encType, encIv);
                }else{
                    throw new ProtocolParseException("failed to parse response code");
                }
            }
            case PROXY_DATA:{
                String id = getId(byteBuf);
                int dataLen = byteBuf.readableBytes();
                byte[] data = new byte[dataLen];
                byteBuf.readBytes(data);
                return new ProxyDataMessage(id, data);
            }
            case PROXY_DATA_RESPONSE:{
                byte result = byteBuf.readByte();
                String id = getId(byteBuf);
                if(result==Constants.RESPONSE_FAIL){
                    return new ProxyDataResponse(ServerResponseMessage.Code.FAIL,id);
                }else if(result==Constants.RESPONSE_SUCCESS){
                    return new ProxyDataResponse(ServerResponseMessage.Code.SUCCESS, id);
                }else{
                    throw new ProtocolParseException("failed to parse response code");
                }

            }
            default:
                throw new IllegalStateException("unable to parse ss data "+ dataType);
        }
    }

    private static String getId(ByteBuf byteBuf){
        byte idLen = byteBuf.readByte();
        byte[] idBytes = new byte[idLen];
        byteBuf.readBytes(idBytes);
        return new String(idBytes, StandardCharsets.UTF_8);
    }
}
