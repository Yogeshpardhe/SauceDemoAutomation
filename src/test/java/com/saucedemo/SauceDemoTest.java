
package com.saucedemo;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SauceDemoTest {

    private static final String URL = "https://www.saucedemo.com/";

    private WebDriverWait waitFor(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    private void login(WebDriver driver) {
        driver.get(URL);

        waitFor(driver).until(
            ExpectedConditions.visibilityOfElementLocated(
                By.id("user-name")
            )
        ).sendKeys("standard_user");

        driver.findElement(By.id("password"))
              .sendKeys("secret_sauce");

        driver.findElement(By.id("login-button")).click();

        waitFor(driver).until(
            ExpectedConditions.visibilityOfElementLocated(
                By.className("inventory_list")
            )
        );
    }

    @Test
    public void validLogin() {
        WebDriver driver = new ChromeDriver();

        try {
            login(driver);

            Assert.assertEquals(driver.getTitle(), "Swag Labs");
            Assert.assertEquals(
                driver.findElement(By.className("title")).getText(),
                "Products"
            );
        } finally {
            driver.quit();
        }
    }

    @Test
    public void invalidLogin() {
        WebDriver driver = new ChromeDriver();

        try {
            driver.get(URL);

            driver.findElement(By.id("user-name"))
                  .sendKeys("wrong_user");

            driver.findElement(By.id("password"))
                  .sendKeys("wrong_password");

            driver.findElement(By.id("login-button")).click();

            String error = waitFor(driver).until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[data-test='error']")
                )
            ).getText();

            Assert.assertTrue(
                error.contains("Username and password do not match"),
                "Unexpected login error: " + error
            );
        } finally {
            driver.quit();
        }
    }

    @Test
    public void productListingValidation() {
        WebDriver driver = new ChromeDriver();

        try {
            login(driver);

            Assert.assertEquals(
                driver.findElement(By.className("title")).getText(),
                "Products"
            );

            int count = driver.findElements(
                By.className("inventory_item")
            ).size();

            Assert.assertTrue(
                count > 0,
                "No products were displayed."
            );
        } finally {
            driver.quit();
        }
    }

    @Test
    public void addProductToCart() {
        WebDriver driver = new ChromeDriver();

        try {
            login(driver);

            driver.findElement(
                By.id("add-to-cart-sauce-labs-backpack")
            ).click();

            String count = waitFor(driver).until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.className("shopping_cart_badge")
                )
            ).getText();

            Assert.assertEquals(count, "1");
        } finally {
            driver.quit();
        }
    }

    @Test
    public void cartContentValidation() {
        WebDriver driver = new ChromeDriver();

        try {
            login(driver);

            driver.findElement(
                By.id("add-to-cart-sauce-labs-backpack")
            ).click();

            driver.findElement(
                By.className("shopping_cart_link")
            ).click();

            String productName = waitFor(driver).until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.className("inventory_item_name")
                )
            ).getText();

            Assert.assertEquals(
                productName,
                "Sauce Labs Backpack"
            );
        } finally {
            driver.quit();
        }
    }

    @Test
    public void checkoutHappyPath() {
        WebDriver driver = new ChromeDriver();

        try {
            login(driver);

            // Add product to cart
            driver.findElement(
                By.id("add-to-cart-sauce-labs-backpack")
            ).click();

            // Open cart
            driver.findElement(
                By.className("shopping_cart_link")
            ).click();

            // Start checkout
            driver.findElement(By.id("checkout")).click();

            String infoTitle = waitFor(driver).until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.className("title")
                )
            ).getText();

            Assert.assertEquals(
                infoTitle,
                "Checkout: Your Information"
            );

            // Fill customer information
            WebElement firstName = waitFor(driver).until(
                ExpectedConditions.elementToBeClickable(
                    By.id("first-name")
                )
            );
            firstName.clear();
            firstName.sendKeys("Yogesh");

            WebElement lastName = waitFor(driver).until(
                ExpectedConditions.elementToBeClickable(
                    By.id("last-name")
                )
            );
            lastName.clear();
            lastName.sendKeys("Pardhe");

            WebElement postalCode = waitFor(driver).until(
                ExpectedConditions.elementToBeClickable(
                    By.id("postal-code")
                )
            );
            postalCode.clear();
            postalCode.sendKeys("444505");

            // Print values for diagnosis
            System.out.println(
                "First name entered: " +
                firstName.getAttribute("value")
            );
            System.out.println(
                "Last name entered: " +
                lastName.getAttribute("value")
            );
            System.out.println(
                "Postal code entered: " +
                postalCode.getAttribute("value")
            );

            // Confirm the fields contain the expected values
            Assert.assertEquals(
                firstName.getAttribute("value"),
                "Yogesh",
                "First name field was not populated."
            );

            Assert.assertEquals(
                lastName.getAttribute("value"),
                "Pardhe",
                "Last name field was not populated."
            );

            Assert.assertEquals(
                postalCode.getAttribute("value"),
                "444505",
                "Postal code field was not populated."
            );

            // Continue to checkout overview
            driver.findElement(By.id("continue")).click();

            // Wait for overview or a visible validation error
            try {
                waitFor(driver).until(
                    ExpectedConditions.or(
                        ExpectedConditions.urlContains(
                            "checkout-step-two"
                        ),
                        ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("[data-test='error']")
                        )
                    )
                );
            } catch (org.openqa.selenium.TimeoutException e) {
                System.out.println(
                    "Checkout URL: " + driver.getCurrentUrl()
                );
                System.out.println(
                    "Checkout title: " +
                    driver.findElement(By.className("title")).getText()
                );
            }

            List<WebElement> errors = driver.findElements(
                By.cssSelector("[data-test='error']")
            );

            if (!errors.isEmpty() && errors.get(0).isDisplayed()) {
                Assert.fail(
                    "Checkout validation error: " +
                    errors.get(0).getText()
                );
            }

            String overviewTitle = waitFor(driver).until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.className("title")
                )
            ).getText();

            Assert.assertEquals(
                overviewTitle,
                "Checkout: Overview",
                "Checkout did not reach the overview page."
            );

            // Finish order
            driver.findElement(By.id("finish")).click();

            String confirmation = waitFor(driver).until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.className("complete-header")
                )
            ).getText();

            Assert.assertEquals(
                confirmation,
                "Thank you for your order!"
            );

        } finally {
            driver.quit();
        }
    }
}