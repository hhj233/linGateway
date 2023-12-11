package com.lin.client.api;

import lombok.Getter;

@Getter
public enum ApiProtocol {
    HTTP("http", "http协议"),
    DUBBO("dubbo", "dubbo协议")
    ;
    private String protocol;
    private String desc;

    ApiProtocol(String protocol, String desc) {
        this.protocol = protocol;
        this.desc = desc;
    }
}
