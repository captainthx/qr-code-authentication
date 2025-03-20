package yotsuki.qrcodeauthentication.model;

public record QRCodeGenerateResponse(boolean success, String fileName, String filePath) {
}