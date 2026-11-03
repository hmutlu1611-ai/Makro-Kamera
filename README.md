# Makro Kamera (Android)

Yakın/makro çekim, manuel odak, tam çözünürlükte düzenleme ve belge tarama yapan kamera uygulaması.
Android 10 ve üstü gerekir.

## Özellikler
- **Makro modu:** Telefonun en yakına odaklanabilen arka lensini otomatik seçer, en yakın odak mesafesini gösterir.
- **Lens değiştirme:** Telefonun uygulamalara açtığı tüm arka lensler arasında geçiş.
- **Manuel odak kaydırıcısı:** cm cinsinden odak mesafesi (destekleyen telefonlarda).
- **Dokunarak odak/pozlama**, iki parmakla yakınlaştırma, pozlama ayarı, fener.
- **Ayrıntı odaklı çekim:** sensörün en yüksek çözünürlüğü, en yüksek kalite modu, JPEG %100, yüksek kaliteli kenar işleme.
- **Düzenleyici:** parlaklık, kontrast, doygunluk (0 = siyah-beyaz), sıcaklık, keskinlik, döndürme, öncesi/sonrası karşılaştırma.
  Fotoğrafı 20 kata kadar büyütüp gerçek pikselleri inceleyebilirsin (çift dokun = hızlı yakınlaştır).
  Kaydetme orijinal tam çözünürlükte yapılır.
- **Tarama:** Google ML Kit belge tarayıcı — kenar algılama, perspektif düzeltme, çok sayfa, JPEG + PDF çıktısı.

Kayıt yerleri: `Resimler/MakroKamera`, taramalar `Resimler/MakroKamera/Taramalar`, PDF'ler `İndirilenler/MakroKamera`.

## APK nasıl elde edilir?

### Yol 1 — Bilgisayara hiçbir şey kurmadan (GitHub ile)
1. github.com'da ücretsiz hesap aç, **New repository** ile boş bir depo oluştur.
2. **uploading an existing file** bağlantısına tıkla, bu klasörün içindekilerin hepsini sürükle bırak
   (`.github` klasörü de dahil olmalı; gizli klasörler görünmüyorsa açtır) ve **Commit** et.
3. **Actions** sekmesinde "APK Derle" otomatik başlar (~5 dk).
4. Bitince çalışmaya tıkla, en altta **MakroKamera-apk** dosyasını indir. İçindeki `app-debug.apk` uygulamandır.

### Yol 2 — Android Studio ile
1. Android Studio'yu kur, **File > Open** ile bu klasörü aç, Gradle eşitlemesinin bitmesini bekle.
2. **Build > Build App Bundle(s) / APK(s) > Build APK(s)**.
3. APK: `app/build/outputs/apk/debug/app-debug.apk`

### Telefona kurulum
APK'yı telefona at, dosyaya dokun, "Bilinmeyen kaynaklardan yüklemeye izin ver" uyarısını onayla.

## Not
Tarama özelliği Google Play Hizmetleri olan telefonlarda çalışır (ilk kullanımda modül indirilebilir).
Manuel odak ve lens listesi telefon üreticisinin kameraya ne kadar erişim verdiğine bağlıdır.
