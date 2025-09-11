Feature: Randevu yönetimi - Nişantaşı Klinik

  Scenario: Randevu oluştur, tamamla ve sil
    Given testapp.doctorin.app adresine gidilir
    And Kullanıcı klinik bilgisini "Nişantaşı Klinik" olarak girer
    And "Test" ve "Test123." ile giriş yapılır
    And Doktor "Gaspar Noe" ve Hasta "Wes Anderson" sistemde mevcut
    When Şube ve departman ve Doktor "Gaspar Noe" seçilerek randevu sayfası açılır
    And Randevu adımları Hasta "Wes Anderson" seçilerek tamamlanır randevu "Tamamlandı" durumuna getirilir
    Then Doktor "Gaspar Noe" ile randevu durumu "Tamamlandı" olarak doğrulanır
    When Randevu adımları Hasta "Wes Anderson" seçilerek tamamlanır ve randevu silinir
    Then Randevunun silindiği doğrulanır
