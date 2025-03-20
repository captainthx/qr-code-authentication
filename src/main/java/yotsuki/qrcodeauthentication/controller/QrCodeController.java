package yotsuki.qrcodeauthentication.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yotsuki.qrcodeauthentication.service.QrCodeService;

@RestController
@RequestMapping("/v1/qrCode")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrCodeService;


    @GetMapping("/{qrId}")
    public ResponseEntity<?> qrCodeCallback(@PathVariable String qrId) {
        return ResponseEntity.ok().body(qrCodeService.verifyQrCode(qrId));
    }

    @PostMapping
    public ResponseEntity<?> generateQrCode() {
        var bytes = qrCodeService.generateQrCode();
        if (bytes.length == 0) {
            return ResponseEntity.badRequest().body("Generate QR code failed");
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(bytes);
    }

}
