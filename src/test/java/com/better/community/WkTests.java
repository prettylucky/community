package com.better.community;

import java.io.IOException;

/**
 * @Date 7/23/2022
 */
public class WkTests {
    public static void main(String[] args) {
        //需要先装好wktopdf并配置好环境变量
        String cmd = "d:/software/wkhtmltopdf/bin/wkhtmltoimage.exe --quality 75 https://www.nowcoder.com d:/Data/wk-images/3.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
