package com.lin.common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author linzj
 */
public class BannerUtil {
    public static void printBanner(Class clazz) {
        try (InputStream inputStream = clazz.getResourceAsStream("/banner.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));){
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            // 处理读取文件异常
            e.printStackTrace();
        }
    }
}
