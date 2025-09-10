Feature: Randevu yönetimi - Nişantaşı Klinik

  Scenario: Randevu oluştur, tamamla ve sil
    Given testapp.doctorin.app adresine gidilir
    And Kullanıcı klinik bilgisini "Nişantaşı Klinik" olarak girer
    And "Test" ve "Test123." ile giriş yapılır
    And Doktor "Gaspar Noe" ve Hasta "Wes Anderson" sistemde mevcut
    When Doktor ve departman seçilerek randevu oluşturulur
    And Randevu adımları tamamlanarak randevu "Tamamlandı" durumuna getirilir
    Then Randevu durumu "Tamamlandı" olarak doğrulanır
    When Oluşturulan randevu silinir
    Then Randevunun silindiği doğrulanır
