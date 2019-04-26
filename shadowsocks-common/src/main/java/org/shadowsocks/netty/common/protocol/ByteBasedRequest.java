package org.shadowsocks.netty.common.protocol;

import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ByteBasedRequest implements SimpleSocksCmdRequest {

    protected DataType type;


    public ByteBasedRequest(DataType type){
        this.type = type;
    }


    abstract protected byte[] body();


    @Override
    public DataType getType() {
        return type;
    }

    @Override
    public void write(ByteBuf buf) {
        byte[] body = body();
        int len = body.length;
        buf.writeByte(Constants.VERSION1);
        buf.writeInt(len + Constants.LEN_HEAD);
        log.info("send bytes len = {} {}", (len+1),this.getClass().getName());
        buf.writeByte(type.getBit());
        if(len>0){
            buf.writeBytes(body);
        }
    }
}
