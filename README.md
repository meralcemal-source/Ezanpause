# EzanPause

Sadece Android. Ezan vaktinde telefonun medya sesini kapatır, ses odağını alarak Spotify/YouTube gibi uygulamaları duraklatır, **sizin girdiğiniz saniye** kadar sonra sesi geri açar. İnternet, hesap veya sunucu gerekmez; vakitler cihazda hesaplanır (Diyanet ihtiyat payları).

## Ayarlar (ana ekran)
- **Sessizlik süresi (saniye):** 1–7200 (varsayılan 240)
- **Ezandan kaç saniye önce başlasın:** 0–3600 (varsayılan 0)
- Şehir (81 il + Mekke, Medine, Kudüs, Berlin, Londra, Bakü, Lefkoşa)
- Vakit seçimi: İmsak, Öğle, İkindi, Akşam, Yatsı, Perşembe Selâsı
- Bitince sesi geri aç / müziği devam ettir
- **Deneme** düğmesi: 3 sn sonra ayarlı süre kadar sessizlik

## Derleme
Android Studio ile bu klasörü açıp **Run** edin. Ya da komut satırından (JDK 17+ ve Android SDK kurulu):
```bash
gradle wrapper --gradle-version 8.9   # bir kez
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## İlk kurulumda
1. **Tam zamanlı alarm** izni (Android 12+): yoksa vakitler dakikalarca kayabilir.
2. **Bildirim** izni (Android 13+): sessizlik sürerken ön plan servisi bildirimi gerekir.
3. **Pil kısıtlaması yok** (önerilir). Xiaomi/Huawei/Samsung/Oppo'da ek olarak "otomatik başlat" iznini açın.
4. **Denemeyi başlat** ile müzik çalarken, ekran kilitliyken doğrulayın.

## Nasıl çalışır
`Scheduler` ayarlardan her gün için pencereleri hesaplar (takvim bitmez) ve tek bir tam zamanlı alarm zinciri kurar. Pencere başlayınca `SilenceService` (ön plan servisi) `Silencer` ile müzik akışını kapatır ve ses odağını tutar; bitince sesi açar, biz durdurduysak müziği devam ettirir. Yeniden başlatma, güncelleme ve saat/saat dilimi değişiminde zincir kendini kurar.

## Sınırlar
- "Zorla durdur" yaparsanız Android alarmları siler; uygulamayı bir kez açmak yeniden kurar.
- Ses zaten sizin tarafınızdan kapatılmışsa dokunmaz.
- Vakitler ilçe değil il merkezi koordinatlarıyla hesaplanır; İstanbul'da Diyanet tablosundan en fazla ~2 dk sapma görüldü, Ankara'da birebir.
- Play Store için ön plan servisi ("special use") ve tam zamanlı alarm beyanı gerekir.
- Kaynak kodu bu ortamda Android SDK olmadığı için derlenmedi; vakit motoru (`PrayerTimes.java`) ise ayrıca derlenip Diyanet tablolarıyla doğrulandı.


## Yeni: GPS ile şehir ve vakit rehberi

- **Konumdan bul (GPS):** Ayarlar kartındaki düğme, en yakın kayıtlı şehri seçer (konum yalnızca bu anda okunur, saklanmaz ve
  internete gönderilmez). Önce son bilinen konum, yoksa taze konum alınır; izin reddedilirse liste kullanılır.
  Kayıtlı şehirlerden 120 km'den uzaktaysanız uyarı gösterilir.
- **Vakit rehberi:** Ana ekrandaki "Bugünkü vakitler" satırlarından birine (İmsak, Öğle, İkindi, Akşam, Yatsı) dokunun:
  toplam rekât, sünnet/farz/vacip ayrımı, rekât rekât ne okunacağı ve ilgili dualar (Arapça, okunuş, anlam) açılır.
  Ayrıca "Cuma namazı" ve "Dualar ve sûreler" ekranları vardır.
- İçerik Hanefî mezhebine / Diyanet anlatımına göredir; kesin hüküm için ilmihal veya müftülüğe danışın.
  Veri `PrayerGuide.java` içindedir, metinleri oradan düzeltebilirsiniz.
