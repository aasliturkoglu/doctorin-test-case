package com.doctorin.steps;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class AppointmentSteps {

    WebDriver driver;
    // Logger nesnesini tanımla
    private static final Logger logger = LoggerFactory.getLogger(AppointmentSteps.class);


    //siteye gidilir.
    @Given("testapp.doctorin.app adresine gidilir")
    public void navigate_to_doctorin() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://testapp.doctorin.app");
    }

    //Klinik bilgisi girilir.
    @And("Kullanıcı klinik bilgisini {string} olarak girer")
    public void kullanici_klinik_bilgisini_olarak_girer(String klinikAdi) {

        // 1. Değiştir butonuna tıkla
        WebElement degistirButton = driver.findElement(By.id("AbpTenantSwitchLink"));
        degistirButton.click();


        // 2. Klinik adını input alanına gir
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // input alanının görünmesini bekle
        WebElement klinikInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Input_Name")));
        klinikInput.clear();
        klinikInput.sendKeys(klinikAdi);


        // 3. Kaydet butonuna tıkla
        WebElement kaydetButton = driver.findElement(By.xpath("//button[span[text()='Kaydet']]"));
        kaydetButton.click();


    }

    //Kullanıcı adı ve şifre ile giriş yapılır.
    @And("{string} ve {string} ile giriş yapılır")
    public void giris_yapilir(String username, String password) {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Kullanıcı adı girilir.
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("LoginInput_UserNameOrEmailAddress")));
        //stale sorunu için try catch
        try {
            usernameInput.clear();
            usernameInput.sendKeys(username);
        } catch (StaleElementReferenceException e) {
            usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("LoginInput_UserNameOrEmailAddress")));
            usernameInput.clear();
            usernameInput.sendKeys(username);
        }

        // Şifre girilir.
        WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password-input")));
        passwordInput.clear();
        passwordInput.sendKeys(password);

        // Login
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[name='Action'][value='Login']")));
        loginButton.click();
    }

    // Doktor ve hastanın sistemde mevcut olduğunu doğrular
    @And("Doktor {string} ve Hasta {string} sistemde mevcut")
    public void doktor_ve_hasta_sistemde_mevcut(String doktorAdi, String hastaAdi) throws InterruptedException {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));



        // Doktorun listede olduğunu doğrula
        // 1. Kaynaklar butonuna tıkla.
        WebElement kaynaklarMenu = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[p[text()='Kaynaklar']]")));
        kaynaklarMenu.click();

       // 2.Personel Yönetimi butonuna tıkla
        WebElement personelYonetimi = wait.until(ExpectedConditions.elementToBeClickable(By.id("MenuItem_ResourceService_StaffManagement")));
        personelYonetimi.click();

        // 3. Filtreleme butonuna tıkla
        By filtreBtnLocator = By.xpath("//button[span[contains(@class,'e-filter')]]");
        int attempts = 0;
        boolean clicked = false;

        while(attempts < 3 && !clicked) {
            try {
                // Filtre butonunun clickable olmasını bekle
                WebElement filtreButton = new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.elementToBeClickable(filtreBtnLocator));

                // JS ile click
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", filtreButton);

                // Kısa bekle, form açıldı mı kontrol et
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//label[text()='Ad']/following-sibling::span//input")
                ));

                clicked = true; // başarılı
            } catch (Exception e) {
                attempts++;
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }

        if (!clicked) {
            throw new RuntimeException("Filtre butonuna tıklanamadı!");
        }



        // 4. Açılan filtre ekranında Ad ve Soyad inputlarına doktor bilgilerini gir

        //ad bilgisini gir
        WebElement adInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//label[text()='Ad']/following-sibling::span//input")));
        adInput.click();
        adInput.clear();
        adInput.sendKeys(doktorAdi.split(" ")[0]); // doktorun ad bilgisi
        //adInput.sendKeys("Gaspar");


        //soyad bilgisini gir
        WebElement soyadInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//label[text()='Soyad']/following-sibling::span//input")));
        soyadInput.click();
        soyadInput.clear();
        //soyadInput.sendKeys("Noe");
        soyadInput.sendKeys(doktorAdi.split(" ")[1]); // doktorun soyad bilgisi


        // 5. Uygula butonuna tıkla
        WebElement uygulaButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Uygula']")));
        uygulaButton.click();

        // 6. Doktorun listede görünüp görünmediğini kontrol et
        try {
            WebElement doktorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//td[@class='e-rowcell  e-leftalign' and text()='" + doktorAdi + "']")));
            // Element bulunduysa
            logger.info(() -> "Doktor " + doktorAdi + " sistemde mevcut.");
        } catch (TimeoutException e) {
            logger.error(() -> "Doktor " + doktorAdi + " sistemde bulunamadı!");
            throw new AssertionError("Doktor " + doktorAdi + " sistemde bulunamadı!");
        }


        //Hastanın listede olduğunu doğrula

        // 1. menu butonuna tıkla-->menuye dön
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'flex') and contains(@class,'items-center') and contains(@class,'justify-center')]")
        ));
        menuButton.click();

        // 2. Hasta Kabul butonuna tıkla
        WebDriverWait searchbarwait = new WebDriverWait(driver, Duration.ofSeconds(10));

        //Overlay varsa kaybolmasını bekle
        searchbarwait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("div.backdrop-blur-xs")
        ));

        // 3. Search bar elementine tıkla
        WebElement searchBar = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("search-input")
        ));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchBar);

        searchBar.clear(); //temizle
        searchBar.sendKeys(hastaAdi); //adını yaz
        searchBar.sendKeys(Keys.ENTER); //enter tuşuna bas

        // 4. Hastanın listede görünüp görünmediğini kontrol et
        try {
            //translate olmadan önce case sensitive nedeniyle görmüyordu.
            WebElement hastaElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//td[contains(@class,'e-rowcell')]//p[" +
                            "translate(normalize-space(.), 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')='"
                            + hastaAdi.toUpperCase() + "']")
            ));

            // Element bulunduysa
            logger.info(() -> "Hasta " + hastaAdi + " sistemde mevcut.");

        } catch (TimeoutException e) { //element bulunmadıysa
            logger.error(() -> "Hasta " + hastaAdi + " sistemde bulunamadı!");
            throw new AssertionError("Hasta " + hastaAdi + " sistemde bulunamadı!");
        }

    }

    @When("Şube ve departman ve Doktor {string} seçilerek randevu sayfası açılır")
    public void sube_ve_departman_secilerek_randevu_olusturulur(String doktorAdi) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 1. menu butonuna tıkla-->menuye dön
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'flex') and contains(@class,'items-center') and contains(@class,'justify-center')]")
        ));
        menuButton.click();
        logger.info(() -> "menu butonuna tıklandı.");

        // 2. Randevu butonuna tıkla
        WebElement randevuMenu = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[.//p[text()='Randevu']]")));
        randevuMenu.click();
        logger.info(() -> "randevu butonuna tıklandı.");


        // 3. Filtrele butonuna tıkla
        WebElement filtreleButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-testid='filter-button']")
        ));
        filtreleButton.click();
        logger.info(() -> "Filtrele butonuna tıklandı.");

        // 4. Şube butonuna tıkla
        WebElement subeDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//span[contains(@class,'e-ddl') and .//input[@placeholder='Seç']])[1]") // 1. dropdown = Şube
        ));
        subeDropdown.click();
        logger.info(() -> "Şube butonuna tıklandı.");

        // şube-->"Bağdat Cadde"yi seç
        WebElement subeSecenek = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ul[contains(@class,'e-list-parent')]//li[normalize-space(text())='Bağdat Cadde']")
        ));
        subeSecenek.click();
        logger.info(() -> "Şuba olarak Bagdat secildi");

       // 5. Departman butonuna tıkla
        WebElement departmanDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//span[contains(@class,'e-ddl') and .//input[@placeholder='Seç']])[2]") // 2. dropdown = Departman
        ));
        departmanDropdown.click();
        logger.info(() -> "Departman butonuna tıklandı.");

        // Departman--> Çocuk hastalıkları seç
        WebElement departmanSecenek = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ul[contains(@id,'_options')]/li[text()='Çocuk hastalıkları']")
        ));
        departmanSecenek.click();
        logger.info(() -> "Departman olarak Çocuk hastalıkları seçildi.");


        //6. Kaynaklar butonundaki çarpıya(X) tıkla.
        WebElement clearButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[contains(@class,'e-clear-icon')]")
        ));
        clearButton.click();
        logger.info(() -> "X butonuna tıklandı, alan temizlendi.");

        //Doktor adını yaz ve seç.
        WebElement doctorInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@class,'e-multiselect') and contains(@placeholder,'Seç')]")
        ));

        // Doktor adını yaz
        doctorInput.sendKeys(doktorAdi);

        // Enter'a bas
        doctorInput.sendKeys(Keys.ENTER);


        // 7. "Kabul et" butonuna tıkla
        WebElement kabulEtButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@data-testid='accept-button' and text()='Kabul et']")
        ));
        kabulEtButton.click();
        logger.info(() -> "'Kabul et' butonuna tıklandı.");



        //8. filtreleme doğru oldu mu, kontrol et.
        WebElement doktorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("p[data-testid='resource-name']")
        ));

        // Text değerini al
        String doktorText = doktorElement.getText().trim();

        // Kontrol et
        if(doktorText.equals("Gaspar Noe")) {
            logger.info(() -> "Doktor Gaspar Noe randevu sayfasında mevcut.");
        } else {
            logger.error(() -> "Doktor Gaspar Noe randevu sayfasında bulunamadı!");
            throw new AssertionError("Filtreleme hatalı, test durduruluyor! Doktor: " + doktorText);
        }


    }

    @And("Randevu adımları Hasta {string} seçilerek tamamlanır randevu {string} durumuna getirilir")
    public void randevu_tamamlanir(String hastaAdi, String durum) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        //1. Randevu oluşturma.
        // Takvim hücresine tıkla
        WebElement takvimHucre = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//td[contains(@class,'e-work-cells') and contains(@class,'e-work-hours')])[1]")
        ));
        takvimHucre.click();

        takvimHucre.click();
        logger.info(() -> "Takvim hücresine tıklandı.");

        //Hasta Arama butonuna tıkla.
        WebElement hastaInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[data-testid='appointment-patient-search']")
        ));

        //hasta adını yaz.
        hastaInput.click();
        hastaInput.clear();
        hastaInput.sendKeys(hastaAdi);
        logger.info(() -> "Hasta adı '" + hastaAdi + "' arama alanına yazıldı.");

        //Ara butonuna tıkla.
        WebElement araButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@type='submit' and normalize-space(text())='Ara']")
        ));
        araButton.click();
        logger.info(() -> "Ara butonuna tıklandı.");

        //Hastayı seç
        WebElement hastaSec = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//p[@data-testid='patient-name-0' and normalize-space(text())='" + hastaAdi + "']")
        ));
        hastaSec.click();
        logger.info(() -> "Hasta seçildi: " + hastaAdi);


        // Kaydet butonuna tıkla
        WebElement kaydetButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@data-testid='save-button' and normalize-space(text())='Kaydet']")
        ));
        kaydetButton.click();
        logger.info(() -> "Kaydet butonuna tıklandı.");

        // Takvim hücresine tekrar tıkla
        WebElement takvimHucreTekrar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//td[contains(@class,'e-work-cells') and contains(@class,'e-work-hours')])[1]")
        ));

        // Hücrede randevu elementini bul
        List<WebElement> randevuElementleri = takvimHucre.findElements(By.xpath(".//p"));

        if(randevuElementleri.size() > 0) {
            // Randevu var mı, varsa hasta adını kontrol et
            String hucreHastaAdi = randevuElementleri.get(0).getText().trim();
            if(hucreHastaAdi.equals(hastaAdi)) {
                logger.info(() -> "Randevu doğru hasta için oluşturulmuş: " + hucreHastaAdi);
            } else {
                logger.error(() -> "Randevu başka bir hasta için oluşturulmuş! Hücredeki isim: " + hucreHastaAdi);
                throw new AssertionError("Randevu yanlış hasta için oluşturulmuş! Beklenen: " + hastaAdi + ", Hücredeki: " + hucreHastaAdi);
            }
        } else {
            logger.error(() -> "Hücrede randevu bulunamadı!");
            throw new AssertionError("Hücrede randevu bulunamadı!");
        }

        takvimHucreTekrar.click();
        logger.info(() -> "Takvimdeki günün ilk mesai hücresine tıklandı.");

        // Check-in butonunu bul ve tıkla
        WebElement checkInButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-testid='status-button']")
        ));
        checkInButton.click();
        logger.info(() -> "'Check-in' butonuna tıklandı.");

        //admission butonuna tıkla.
        WebElement admissionButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-testid='status-button']")
        ));
        admissionButton.click();
        logger.info(() -> "'Admission' butonuna tıklandı.");

        //kaydet butonuna tıkla.
        WebElement kaydetButtonTamamlama = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space(text())='Kaydet']")
        ));
        kaydetButtonTamamlama.click();
        logger.info(() -> "'Kaydet' butonuna tıklandı.");


    }

    @Then("Doktor {string} ile randevu durumu {string} olarak doğrulanır")
    public void randevu_durumu_dogrulanir(String expectedStatus, String doktorAdi) {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 1. Randevu sayfasına dön

        //menu butonuna tıkla
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'flex') and contains(@class,'items-center') and contains(@class,'justify-center')]")
        ));
        menuButton.click();
        logger.info(() -> "Menü butonuna tıklandı");

        //randevu butonuna tıkla.
        WebElement randevuMenu = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[.//p[text()='Randevu']]")));
        randevuMenu.click();
        logger.info(() -> "randevu butonuna tıklandı.");

        // 3. Filtrele butonuna tıkla
        WebElement filtreleButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-testid='filter-button']")
        ));
        filtreleButton.click();
        logger.info(() -> "Filtrele butonuna tıklandı.");

        // 4. Şube butonuna tıkla
        WebElement subeDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//span[contains(@class,'e-ddl') and .//input[@placeholder='Seç']])[1]") // 1. dropdown = Şube
        ));
        subeDropdown.click();
        logger.info(() -> "Şube butonuna tıklandı.");

        // şube-->"Bağdat Cadde"yi seç
        WebElement subeSecenek = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ul[contains(@class,'e-list-parent')]//li[normalize-space(text())='Bağdat Cadde']")
        ));
        subeSecenek.click();
        logger.info(() -> "Şuba olarak Bagdat secildi");

        // 5. Departman butonuna tıkla
        WebElement departmanDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//span[contains(@class,'e-ddl') and .//input[@placeholder='Seç']])[2]") // 2. dropdown = Departman
        ));
        departmanDropdown.click();
        logger.info(() -> "Departman butonuna tıklandı.");

        // Departman--> Çocuk hastalıkları seç
        WebElement departmanSecenek = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ul[contains(@id,'_options')]/li[text()='Çocuk hastalıkları']")
        ));
        departmanSecenek.click();
        logger.info(() -> "Departman olarak Çocuk hastalıkları seçildi.");


        //kontrol edilmedi.//
        //6. Kaynaklar butonundaki çarpıya(X) tıkla.
        WebElement clearButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[contains(@class,'e-clear-icon')]")
        ));
        clearButton.click();
        logger.info(() -> "X butonuna tıklandı, alan temizlendi.");

        //Doktor adını yaz ve seç.
        WebElement doctorInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@class,'e-multiselect') and contains(@placeholder,'Seç')]")
        ));

        // Doktor adını yaz
        doctorInput.sendKeys(doktorAdi);

        // Enter'a bas
        doctorInput.sendKeys(Keys.ENTER);


        // 7. "Kabul et" butonuna tıkla
        WebElement kabulEtButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@data-testid='accept-button' and text()='Kabul et']")
        ));
        kabulEtButton.click();
        logger.info(() -> "'Kabul et' butonuna tıklandı.");

        //8. takvim hücresine tıkla.
        WebElement takvimHucreTekrar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//td[contains(@class,'e-work-cells') and contains(@class,'e-work-hours')])[1]")
        ));
        takvimHucreTekrar.click();
        logger.info(() -> "Takvimdeki günün ilk mesai hücresine tıklandı.");

        //9. Tamamlandı durum kontrolü
        WebElement statusElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//p[contains(text(),'Kontrol -')]")
        ));

        // 10. Text değerini al ve sadece durumu ayır
        String fullText = statusElement.getText().trim(); // "Kontrol - Tamamlandı"
        String actualStatus = fullText.split("-")[1].trim(); // "Tamamlandı"

        // 11. Durumu kontrol et
        if (actualStatus.equalsIgnoreCase(expectedStatus)) {
            logger.info(() -> "Randevu durumu beklenen durumla eşleşiyor: " + actualStatus);
        } else {
            logger.error(() -> "Randevu durumu beklenen durumdan farklı! Beklenen: "
                    + expectedStatus + ", Güncel durum: " + actualStatus);
            throw new AssertionError("Randevu durumu beklenen değerden farklı! Beklenen: "
                    + expectedStatus + ", Güncel durum: " + actualStatus);
        }



    }

    @When("Randevu adımları Hasta {string} seçilerek tamamlanır ve randevu silinir")
    public void randevu_adimlari_tamamlanir_ve_silinir(String hastaAdi) {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        //1. Randevu oluşturma.
        // Takvim hücresine tıkla
        WebElement takvimHucre = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//td[contains(@class,'e-work-cells') and contains(@class,'e-work-hours')])[4]")
        ));
        takvimHucre.click();

        takvimHucre.click();
        logger.info(() -> "Takvim hücresine tıklandı.");

        //Hasta Arama butonuna tıkla.
        WebElement hastaInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[data-testid='appointment-patient-search']")
        ));

        //hasta adını yaz.
        hastaInput.click();
        hastaInput.clear();
        hastaInput.sendKeys(hastaAdi);
        logger.info(() -> "Hasta adı '" + hastaAdi + "' arama alanına yazıldı.");

        //Ara butonuna tıkla.
        WebElement araButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@type='submit' and normalize-space(text())='Ara']")
        ));
        araButton.click();
        logger.info(() -> "Ara butonuna tıklandı.");

        //Hastayı seç
        WebElement hastaSec = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//p[@data-testid='patient-name-0' and normalize-space(text())='" + hastaAdi + "']")
        ));
        hastaSec.click();
        logger.info(() -> "Hasta seçildi: " + hastaAdi);


        // Kaydet butonuna tıkla
        WebElement kaydetButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@data-testid='save-button' and normalize-space(text())='Kaydet']")
        ));
        kaydetButton.click();
        logger.info(() -> "Kaydet butonuna tıklandı.");

        // Takvim hücresine tekrar tıkla
        WebElement takvimHucreTekrar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//td[contains(@class,'e-work-cells') and contains(@class,'e-work-hours')])[4]")
        ));

        // Hücrede randevu elementini bul
        List<WebElement> randevuElementleri = takvimHucre.findElements(By.xpath(".//p"));

        if(randevuElementleri.size() > 0) {
            // Randevu var mı, varsa hasta adını kontrol et
            String hucreHastaAdi = randevuElementleri.get(0).getText().trim();
            if(hucreHastaAdi.equals(hastaAdi)) {
                logger.info(() -> "Randevu doğru hasta için oluşturulmuş: " + hucreHastaAdi);
            } else {
                logger.error(() -> "Randevu başka bir hasta için oluşturulmuş! Hücredeki isim: " + hucreHastaAdi);
                throw new AssertionError("Randevu yanlış hasta için oluşturulmuş! Beklenen: " + hastaAdi + ", Hücredeki: " + hucreHastaAdi);
            }
        } else {
            logger.error(() -> "Hücrede randevu bulunamadı!");
            throw new AssertionError("Hücrede randevu bulunamadı!");
        }

        takvimHucreTekrar.click();
        logger.info(() -> "Takvimdeki günün ilk mesai hücresine tıklandı.");

        //sil butonuna tıkla.
        WebElement silButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-testid='appointment-delete-button']")
        ));
        silButton.click();
        logger.info(() -> "Sil butonuna tıklandı.");

    }

    @Then("Randevunun silindiği doğrulanır")
    public void randevunun_silindigi_dogrulanir() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Takvim hücresine tekrar tıkla
        WebElement takvimHucreTekrar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//td[contains(@class,'e-work-cells') and contains(@class,'e-work-hours')])[4]")
        ));

        // Hücrede randevu elementini bul
        List<WebElement> randevuElementleri = takvimHucreTekrar.findElements(By.xpath(".//p"));

        // Randevu silinmiş mi kontrol et
        if (randevuElementleri.isEmpty()) {
            logger.info(() -> "Randevu başarıyla silindi. Hücrede artık randevu bulunmuyor.");
        } else {
            logger.error(() -> "Randevu silinemedi! Hücrede hala randevu mevcut.");
            throw new AssertionError("Randevu silinemedi! Hücrede hala randevu mevcut.");
        }


    }


}
