package yotsuki.qrcodeauthentication.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import yotsuki.qrcodeauthentication.model.QRCodeGenerateResponse;
import yotsuki.qrcodeauthentication.model.User;
import yotsuki.qrcodeauthentication.utils.RedisKeyUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
@RequiredArgsConstructor
public class QrCodeService {

    @Value("${api.file.image-format}")
    private String imageFormat;

    @Value("${api.file.directory}")
    private String fileDirectory;

    @Value("${api.server.callback-url}")
    private String callbackUrl;

    @Value("${api.qr.width}")
    private int qrWidth;

    @Value("${api.qr.height}")
    private int qrHeight;

    private final RedisTemplate<String, String> redisTemplate;


    private User mockUser() {
        return User.builder().id(1L).username("test").token(UUID.randomUUID().toString()).build();
    }

    public byte[] generateQrCode() {
        var user = mockUser();
        try {
            var rateLimitKey = RedisKeyUtils.rateLimitGenerateQr(user.getId());
            if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
                throw new IOException("Rate limit has been reached");
            }
            var qrCodeWriter = new QRCodeWriter();
            // random qrId  can change to jwt
            var qrId = UUID.randomUUID().toString();

            var redisKey = RedisKeyUtils.qrCodeGenerate(qrId);
            // set to redis
            redisTemplate.opsForValue().set(redisKey, String.valueOf(user.getId()), 5, TimeUnit.MINUTES);

            // set rate limit
            redisTemplate.opsForValue().set(rateLimitKey, "true", 30, TimeUnit.SECONDS);
            // create callback link
            var linkToken = callbackUrl + qrId;
            var bitMatrix = qrCodeWriter.encode(linkToken, BarcodeFormat.QR_CODE, qrWidth, qrHeight);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, imageFormat.toUpperCase(), outputStream);


            return outputStream.toByteArray();
        } catch (Exception e) {
            log.warn("GenerateQrCode-[error]. error:", e);
            return new byte[0];
        }
    }

    public ResponseEntity<?> verifyQrCode(String code) {
        var redisKey = RedisKeyUtils.qrCodeGenerate(code);
        var redisData = redisTemplate.opsForValue().get(redisKey);

        if (!StringUtils.hasText(redisData)) {
            return ResponseEntity.badRequest().body("Invalid QR Code");
        }

        var sessionToken = UUID.randomUUID().toString();
        redisTemplate.delete(redisKey);

        // can add notify
        return ResponseEntity.ok().body(sessionToken);
    }


    private QRCodeGenerateResponse saveQrCodeToDisk(byte[] bytes) {
        var inputStream = new ByteArrayInputStream(bytes);
        BufferedImage image;
        try {
            image = ImageIO.read(inputStream);
            var uploadPath = Paths.get(fileDirectory, "qrcode");
            if (!Files.exists(uploadPath)) {
                log.info("Create folder: " + fileDirectory);
                Files.createDirectories(uploadPath);
            }

            var fileName = UUID.randomUUID() + "." + imageFormat;
            Path filePath = uploadPath.resolve(fileName);
            var destinationFile = filePath.toFile();

            ImageIO.write(image, imageFormat, destinationFile);
            return new QRCodeGenerateResponse(true, fileName, destinationFile.toString());
        } catch (IOException e) {
            log.warn("Error saving qr code to disk: {}", e.getMessage());
            return new QRCodeGenerateResponse(false, null, null);

        }
    }

    public Resource loadAsResource(String filename) {
        try {
            var file = load(filename);
            var resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
        } catch (MalformedURLException e) {
            log.warn("LoadAsResource failed: {}", e.getMessage());
        }
        return null;
    }

    private Path load(String fileName) {
        var path = Paths.get(fileDirectory, "qrcode");
        return path.resolve(fileName);
    }


}
