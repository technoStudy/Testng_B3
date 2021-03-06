package day21;

import day19.Selectors;
import day19.StudentConstants;
import day21.pom.DepartmentFormPOM;
import day21.pom.LoginPage;
import day21.pom.MenuPOM;
import day21.util.BaseTest;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.List;

public class SchoolDepartmentSuccessfulCreationTest extends BaseTest {
    WebDriver driver;
    private String departmentName;
    private String departmentCode;
    private int numberOfRowsBeforeSave;
    private LoginPage loginPage;
    private MenuPOM menu;
    private DepartmentFormPOM departmentForm;

    @BeforeClass
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", StudentConstants.CHROME_DRIVER_PATH);
        driver = new ChromeDriver(); // TODO: parameterize browser
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, 5);

        driver.get("https://test.campus.techno.study/"); // TODO: parameterize the website
        departmentName = "High School";
        departmentCode = "HS-1";
        loginPage = new LoginPage(driver);
        menu = new MenuPOM(driver);
        departmentForm = new DepartmentFormPOM(driver);
    }

    @Parameters({"username", "password"})
    @Test
    public void login(@Optional("daulet2030@gmail.com") String uname, @Optional("TechnoStudy123@") String upassword) {
        loginPage.fillInUserName(uname);
        loginPage.fillInUserPassword(upassword);
        loginPage.login();
        waitFor(ExpectedConditions.presenceOfElementLocated(loginPage.menu));
    }

    @Test(dependsOnMethods = "login")
    public void navigate() {
        menu.navigateToSetup();
        menu.navigateToSchoolSetupMenu();
        menu.navigateToDepartmentMenu();
        menu.waitToolbarHeader("School Departments");
    }

    @Test(dependsOnMethods = "navigate")
    public void fillDepartmentInfo() {
        departmentForm.addNew();
        departmentForm.fillInName(departmentName);
        Assert.assertEquals(departmentForm.getDepNameInputValue(), departmentName);
        departmentForm.fillInCode(departmentCode);
        Assert.assertEquals(departmentForm.getDepCodeInputValue(), departmentCode);
    }

    @Test(dependsOnMethods = "fillDepartmentInfo")
    public void switchToSectionTab() {
        driver.findElement(Selectors.sectionTab).click();
        // TODO: validate that I am on section tab
    }

    @Test(dataProvider = "sections", dependsOnMethods = "switchToSectionTab")
    public void addSection(String name, String shortName) {
        waitFor(ExpectedConditions.visibilityOfElementLocated(Selectors.plusButtonOverlay));
        driver.findElement(Selectors.plusButtonOverlay).click();
        waitFor(ExpectedConditions.visibilityOfElementLocated(Selectors.nameInput));
        driver.findElement(Selectors.nameInput).sendKeys(name); // TODO: random string
        waitFor(ExpectedConditions.presenceOfElementLocated(Selectors.shortNameInput));
        driver.findElement(Selectors.shortNameInput).sendKeys(shortName);
        driver.findElement(Selectors.addSectionButton).click();

        List<WebElement> rows = driver.findElements(Selectors.sectionRows);
        boolean found = false;
        for (WebElement row : rows) {
            if (row.getText().contains(name) && row.getText().contains(shortName))
                found = true;
        }
        Assert.assertTrue(found, "The section was " + name + "not found, after adding");
    }

    @Test(dependsOnMethods = "addSection")
    public void savingTheDepartment() {
        numberOfRowsBeforeSave = driver.findElements(Selectors.browserTableRows).size();
        driver.findElement(Selectors.saveButton).click();
        waitFor(ExpectedConditions.textToBePresentInElementLocated(Selectors.alert, "School Department successfully created"));
    }

    @Test(dependsOnMethods = "savingTheDepartment")
    public void deleteDepartment() {
        waitFor(ExpectedConditions.numberOfElementsToBeMoreThan(Selectors.browserTableRows, numberOfRowsBeforeSave));

        List<WebElement> rows = driver.findElements(Selectors.browserTableRows);
        boolean found = false;
        for (WebElement row : rows) {
            if (row.getText().contains(departmentName) && row.getText().contains(departmentCode)) {
                found = true;
                row.findElement(Selectors.trashButton).click();
            }
        }
        Assert.assertTrue(found, "The department was " + departmentName + "not found, after saving");

        wait.until(ExpectedConditions.presenceOfElementLocated(Selectors.confirmYes));
        driver.findElement(Selectors.confirmYes).click();

        waitFor(ExpectedConditions.textToBePresentInElementLocated(Selectors.alert, "School Department successfully deleted"));
    }

        @DataProvider(name="sections")
    public Object[][] sectionData() {
        String[][] testData = {
                {"Junior Classes", "Juniors"},
                {"Senior Classes", "Senior"}
        };
        return testData;
    }
}
