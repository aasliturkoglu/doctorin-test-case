package com.doctorin.steps;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
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
    }




}
