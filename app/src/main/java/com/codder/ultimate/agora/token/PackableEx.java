package com.codder.ultimate.agora.token;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}
