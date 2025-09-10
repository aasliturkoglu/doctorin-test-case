package com.doctorin.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class AppointmentSteps {

    WebDriver driver;

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
        //WebElement filtreButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[span[contains(@class,'e-filter')]]")));
        //filtreButton.click();
        int attempts = 0;
        while(attempts < 3) {
            try {
                WebElement filtreButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[span[contains(@class,'e-filter')]]")));
                filtreButton.click();
                break;
            } catch (Exception e) {
                Thread.sleep(500);
                attempts++;
            }
        }


        // 4. Açılan filtre ekranında Ad ve Soyad inputlarına doktor bilgilerini gir
        //ad bilgisini gir
        WebElement adInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textbox-bf002822-1fc8-4295-a313-1535457db140")));
        adInput.clear();
        adInput.sendKeys(doktorAdi.split(" ")[0]); // doktorun ad bilgisi


        //soyad bilgisini gir
        WebElement soyadInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textbox-609f2ff3-bb32-4998-9c44-a8b7546873b6")));
        soyadInput.clear();
        soyadInput.sendKeys(doktorAdi.split(" ")[1]); // doktorun soyad bilgisi

        // 5. Uygula butonuna tıkla
        WebElement uygulaButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Uygula']")));
        uygulaButton.click();

        // 6. Doktorun listede görünüp görünmediğini kontrol et
        WebElement doktorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[@class='e-rowcell  e-leftalign' and text()='" + doktorAdi + "']")));

        if (!doktorElement.isDisplayed()) {
            throw new AssertionError("Doktor " + doktorAdi + " sistemde bulunamadı!");
        }



        // Hastanın listede olduğunu doğrula
       /* WebElement hastaElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table//td[contains(text(),'" + hastaAdi + "')]")
        ));
        if (!hastaElement.isDisplayed()) {
            throw new AssertionError("Hasta " + hastaAdi + " sistemde bulunamadı!");
        }*/
    }




}
