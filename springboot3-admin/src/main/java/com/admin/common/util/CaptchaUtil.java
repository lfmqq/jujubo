package com.admin.common.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;

/**
 * 简单验证码生成工具（基于 JDK 自带 AWT/ImageIO，无需引入第三方依赖）。
 * 生成 4 位字母数字（去除易混淆字符）并绘制成 PNG 图片，输出 base64。
 */
public class CaptchaUtil {

    /** 验证码字符集（去除 0/O/1/I/l 等易混淆字符） */
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ123456789";
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int LENGTH = 4;

    /**
     * 生成验证码
     *
     * @return 包含验证码明文与 base64 图片（data url）的结果
     */
    public static CaptchaInfo generate() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        Random rand = new Random();

        // 背景
        g.setColor(new Color(240, 248, 255));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        // 边框
        g.setColor(Color.GRAY);
        g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);

        // 干扰线
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
            g.drawLine(rand.nextInt(WIDTH), rand.nextInt(HEIGHT),
                    rand.nextInt(WIDTH), rand.nextInt(HEIGHT));
        }

        // 绘制验证码字符
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            char c = CHARS.charAt(rand.nextInt(CHARS.length()));
            sb.append(c);
            // 使用较深的颜色保证可读性
            g.setColor(new Color(rand.nextInt(80), rand.nextInt(80), rand.nextInt(80)));
            g.setFont(new Font("Arial", Font.BOLD, 28 + rand.nextInt(6)));
            int x = 15 + i * 26;
            int y = 28 + rand.nextInt(8);
            g.drawString(String.valueOf(c), x, y);
        }
        g.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return new CaptchaInfo(sb.toString(), "data:image/png;base64," + base64);
        } catch (Exception e) {
            throw new RuntimeException("验证码生成失败", e);
        }
    }

    /** 验证码生成结果 */
    public static class CaptchaInfo {
        private final String code;
        private final String base64;

        public CaptchaInfo(String code, String base64) {
            this.code = code;
            this.base64 = base64;
        }

        public String getCode() {
            return code;
        }

        public String getBase64() {
            return base64;
        }
    }
}
