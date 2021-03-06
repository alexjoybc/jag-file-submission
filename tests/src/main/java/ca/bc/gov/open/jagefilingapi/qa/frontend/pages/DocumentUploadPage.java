package ca.bc.gov.open.jagefilingapi.qa.frontend.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DocumentUploadPage {

    private final WebDriver driver;

    @FindBy(xpath = "//*[@data-testid='dropdownzone']/div/input")
    WebElement selectFile;

    @FindBy(xpath = "//label[@for='yes-isAmendment-test-document-2.pdf']")
    WebElement isAmendmentRadioBtn;

    @FindBy(xpath = "//label[@for='no-isSupremeCourtScheduling-test-document-2.pdf']")
    WebElement isSupremeCourtRadioBtn;

    @FindBy(xpath = "//button[@data-test-id='continue-upload-btn']")
    WebElement continueBtn;

    @FindBy(xpath = "//button[@data-test-id='cancel-upload-btn']")
    WebElement cancelUpload;

    @FindBy(xpath = "//button[@data-testid='remove-icon']")
    WebElement removeFileIcon;


    //Initializing the driver:
    public DocumentUploadPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    //Actions:
    public void selectFileToUpload(String file) {
        WebDriverWait wait = new WebDriverWait(driver, 40);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@data-testid='dropdownzone']/div/input")));
        selectFile.sendKeys(file);
    }

    public void clickIsAmendmentRadioBtn() {
        isAmendmentRadioBtn.click();
    }

    public void clickIsSupremeCourtBtn() {
        isSupremeCourtRadioBtn.click();
    }

    public void clickContinueBtn() {
        continueBtn.click();
    }

    public void clickCancelUpload() {
        cancelUpload.click();
    }

    public void clickRemoveFileIcon() {
        Actions action = new Actions(driver);
        action.moveToElement(removeFileIcon).click();
    }
}
