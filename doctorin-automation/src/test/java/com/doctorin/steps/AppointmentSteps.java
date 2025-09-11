package com.doctorin.steps;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

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

       /*// 2.Personel Yönetimi butonuna tıkla
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
        }*/

    }

    @When("Şube ve departman seçilerek randevu oluşturulur")
    public void sube_ve_departman_secilerek_randevu_olusturulur() {
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

        // 6. "Kabul et" butonuna tıkla
        WebElement kabulEtButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@data-testid='accept-button' and text()='Kabul et']")
        ));
        kabulEtButton.click();
        logger.info(() -> "'Kabul et' butonuna tıklandı.");



    }




}
