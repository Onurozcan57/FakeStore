# MVVM API Calling Project Roadmap

Bu proje, DummyJSON API'sini kullanarak modern bir Android uygulaması geliştirme sürecini kapsamaktadır. MVVM mimarisi, Hilt (Dependency Injection), Retrofit (Network) ve SOLID prensipleri temel alınacaktır.

## User Review Required

> [!IMPORTANT]
> Proje başlangıcında bağımlılıkların (`Hilt`, `Retrofit`, `Navigation`) eklenmesi için `build.gradle` dosyalarında değişiklik yapılacaktır. Bu aşamada sync işlemi gerekebilir.

## Open Questions

- Ödeme kısmı için herhangi bir API kullanılmayacak, sadece başarılı/başarısız senaryosu simüle edilecek. Uygun mu?
- Login işlemi için `dummyjson.com/auth/login` endpoint'ini kullanacağız. Bu işlem başarılı olduğunda dönen Token'ı (`accessToken`) diğer isteklerde (Profil vb.) kullanacak mıyız? (DummyJSON bazı endpoint'ler için auth gerektirir).

## Proposed Changes

### [Phase 1] Project Setup & Dependencies
- `Hilt`, `Retrofit`, `Gson`, `OkHttp` ve `Compose Navigation` bağımlılıklarının eklenmesi.
- Proje klasör yapısının (Data, Domain, UI) oluşturulması.

### [Phase 2] Network Layer & Core Architecture
- `RetrofitInstance` ve API Interface tanımları.
- SOLID prensiplerine uygun Repository katmanının oluşturulması.
- `Hilt` modüllerinin (`NetworkModule`, `RepositoryModule`) tanımlanması.

### [Phase 3] Auth Flow (Login & Profile)
- **Login Screen**: `AuthRepository` üzerinden kullanıcı girişi.
- **Profile Screen**: Giriş yapan kullanıcının bilgilerinin gösterilmesi.
- `AuthViewModel` ile state yönetimi.

### [Phase 4] Product & Cart Flow
- **Product List**: Ürünlerin listelenmesi ve detay sayfası.
- **Cart Logic**: Memory-based (State-driven) sepet yönetimi.
- `ProductViewModel` ve `CartViewModel`.

### [Phase 5] Checkout & Final Polish
- Basit bir ödeme (Checkout) ekranı.
- Hata yönetimi (Error handling) ve Loading state'lerinin eklenmesi.

## Verification Plan

### Automated Tests
- `Hilt` testleri ve Repository birim testleri (MockWebServer ile).

### Manual Verification
- Login işleminin DummyJSON verileriyle doğrulanması.
- Sepete ekleme ve sepet tutarının doğru hesaplandığının kontrolü.
