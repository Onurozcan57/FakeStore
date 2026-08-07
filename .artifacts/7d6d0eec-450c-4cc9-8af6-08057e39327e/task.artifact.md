# Project Tasks

## Phase 1: Setup
- [ ] Bağımlılıkların eklenmesi (Hilt, Retrofit, Navigation)
- [ ] `Application` sınıfının oluşturulması ve `@HiltAndroidApp` eklenmesi
- [ ] Klasör yapısının oluşturulması (di, data, domain, ui)

## Phase 2: Network & Data
- [ ] API modellerinin (DTO) oluşturulması (LoginResponse, Product, User)
- [ ] `ApiService` arayüzünün tanımlanması
- [ ] `NetworkModule` (Hilt) oluşturulması
- [ ] Repository katmanının oluşturulması (SOLID: Interface + Implementation)

## Phase 3: Authentication
- [ ] `LoginViewModel` ve UI implementation
- [ ] Token saklama mantığı (EncryptedSharedPreferences veya basit bir Singleton)
- [ ] `ProfileViewModel` ve UI implementation

## Phase 4: Products & Cart
- [ ] `ProductList` ekranı ve ViewModel
- [ ] Ürün Detay ekranı
- [ ] Sepet (Cart) state yönetimi (Memory-based)

## Phase 5: Payment & Cleanup
- [ ] Ödeme ekranı (Checkout)
- [ ] Navigasyonun tamamlanması (Login -> Home -> Cart -> Checkout)
- [ ] Genel hata yönetimi
