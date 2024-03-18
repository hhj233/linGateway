package com.lin.common.utils;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Objects;

/**
 * @author linzj
 */
@Slf4j
public class RemotingUtil {
    public static final String OS_NAME = System.getProperty("os.name");
    private static boolean isLinuxPlatform = false;
    private static boolean isWindowsPlatform = false;

    static {
        if (OS_NAME != null  && OS_NAME.toLowerCase().contains("linux")) {
            isLinuxPlatform = true;
        }

        if (OS_NAME != null && OS_NAME.toLowerCase().contains("windows")) {
            isWindowsPlatform = true;
        }
    }

    public static boolean isWindowsPlatform() {
        return isWindowsPlatform;
    }

    public static boolean isLinuxPlatform() {
        return isLinuxPlatform;
    }

    public static String parseChannelRemoteAddress(final Channel channel) {
        if(Objects.isNull(channel)) {
            return null;
        }
        SocketAddress socketAddress = channel.remoteAddress();
        final String address = Objects.isNull(socketAddress) ? "" : socketAddress.toString();
        if (address.length() > 0) {
            int index = address.lastIndexOf("/");
            if (index >= 0) {
                return address.substring(index+1);
            }
            return address;
        }
        return address;
    }
}
