package yotsuki.qrcodeauthentication.utils;

public class RedisKeyUtils {

    public RedisKeyUtils() {
    }

    public static String qrCodeGenerate(String id) {
        return "QR:" + id;
    }

    public static String rateLimitGenerateQr(Long userId){
        return "RATE_LIMIT:" + userId;
    }
}
