package com.guxian.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 工单二维码生成工具：生成base64字符串，嵌入打印HTML页面
 */
public class QrCodeUtil {

    /**
     * 生成工单二维码Base64
     * @param workNo 工单编号
     * @param width 二维码宽度
     * @param height 二维码高度
     * @return base64字符串
     */
    public static String generateWorkQrBase64(String workNo, int width, int height) {
        // 二维码扫码跳转地址：工人小程序扫码页面，携带工单号
        String scanUrl = "https://mini.guxian.com/h5/scanWork?workNo=" + workNo;

        Map<EncodeHintType, Object> hints = new HashMap<>(4);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1); // 白边大小

        try {
            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(scanUrl, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", bos);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("工单二维码生成失败", e);
        }
    }

    /**
     * 重载默认尺寸 220*220
     */
    public static String generateWorkQrBase64(String workNo) {
        return generateWorkQrBase64(workNo, 220, 220);
    }
}