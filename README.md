# 📌 Multi-Device Authentication System (QR Code)

**ทำขึ้นเพื่อเรียนรู้ สามารถปรับปรุงและเพิ่มประสิทธิภาพขึ้นได้อีก**

🔹1. Primary Device สร้าง QR Code
   1. [ ] ผู้ใช้เปิดแอปบน Primary Device
   2. [ ] 
   3. [ ] ระบบ ตรวจสอบ rate limit ป้องกัน spam การสร้าง QR
   4. [ ] 
   5. [ ] ระบบสร้าง qrId (UUID)
   6. [ ] 
   7. [ ] บันทึก qrId -> userId ลง Redis (หมดอายุ 5 นาที)
   8. [ ] 
   9. [ ] สร้าง callback URL: https://example.com/auth/qr/{qrId}
   10. [ ] 
   11. [ ] สร้าง QR Code ที่มีลิงก์ callback
   12. [ ] 
   13. [ ] ส่ง QR Code ให้ผู้ใช้สแกน

🔹 2. Secondary Device สแกน QR Code
   1. [ ] ผู้ใช้สแกน QR ด้วย Secondary Device
   2. [ ] 
   3. [ ] แอปเปิดลิงก์ https://example.com/auth/qr/{qrId}
   4. [ ] 
   5. [ ] ส่ง qrId ไปยัง Backend เพื่อตรวจสอบ

🔹 3. Backend ตรวจสอบ QR Code
    
* Backend ดึงค่า userId จาก Redis โดยใช้ qrId

  1. [ ] ❌ ถ้า qrId ไม่มีอยู่: ตอบกลับ 400 - Invalid QR Code
  2. [ ] 
  3. [ ] ✅ ถ้า qrId ถูกต้อง:
  4. [ ] 
  5. [ ] สร้าง JWT Token (อายุ 30 นาที)
  6. [ ] 
  7. [ ] ลบ qrId ออกจาก Redis (ป้องกัน reuse)
  8. [ ] 
  9. [ ] ตอบกลับ Secondary Device ด้วย JWT

🔹 4. Secondary Device รับ JWT และใช้งาน

   1. [ ] Secondary Device บันทึก JWT ลง Local Storage / Secure Storage
   2. [ ] 
   3. [ ] ใช้ JWT สำหรับ เรียก API อื่นๆ เพื่อเข้าถึงระบบ
   4. [ ] 
   5. [ ] 🎯 Authentication สำเร็จ! 🎯

🔹 5. JWT Validation (ทุกครั้งที่ Secondary Device ใช้ API)
  1. [ ] ทุกครั้งที่ Secondary Device ใช้ API → ต้องแนบ JWT
      2. [ ] Backend ตรวจสอบ JWT:
        3.[ ] ✅ ถ้า valid: อนุญาตให้เข้าถึง API
        4.[ ] ❌ ถ้าหมดอายุ หรือไม่ถูกต้อง: ตอบกลับ 401 - Unauthorized

🔹 6. การ Logout (Invalidate JWT

1. [ ] ผู้ใช้กด Logout → Backend สามารถทำได้ 2 วิธี:
   2.[ ] Blacklist JWT ใน Redis (เช่น blacklist:{jwtId} หมดอายุพร้อม JWT)
   3.[ ] เปลี่ยน Secret Key (ทุก JWT ที่ออกมาก่อนหน้านี้จะใช้ไม่ได้)


