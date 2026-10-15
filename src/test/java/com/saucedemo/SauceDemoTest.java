
package com.saucedemo;

import java.time.Duration;

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
        return new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // Common login method
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

    // Test 1: Valid login
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

            System.out.println("Valid login test passed.");

        } finally {
            driver.quit();
        }
    }

    // Test 2: Invalid login
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

            System.out.println("Invalid login test passed.");

        } finally {
            driver.quit();
        }
    }

    // Test 3: Product listing
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

            Assert.assertTrue(count > 0, "No products were displayed.");

            System.out.println("Products displayed: " + count);

        } finally {
            driver.quit();
        }
    }

    // Test 4: Add product to cart
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

            System.out.println("Add to cart test passed.");

        } finally {
            driver.quit();
        }
    }

    // Test 5: Cart content validation
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

            waitFor(driver).until(
                ExpectedConditions.urlContains("cart.html")
            );

            String productName = waitFor(driver).until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.className("inventory_item_name")
                )
            ).getText();

            Assert.assertEquals(productName, "Sauce Labs Backpack");

            System.out.println("Cart content test passed.");

        } finally {
            driver.quit();
        }
    }

    // Test 6: Complete checkout
    @Test
    public void checkoutHappyPath() {
        WebDriver driver = new ChromeDriver();

        try {
            login(driver);

            // Add product to cart
            driver.findElement(
                By.id("add-to-cart-sauce-labs-backpack")
            ).click();

            waitFor(driver).until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.className("shopping_cart_badge")
                )
            );

            // Open cart
            driver.findElement(
                By.className("shopping_cart_link")
            ).click();

            waitFor(driver).until(
                ExpectedConditions.urlContains("cart.html")
            );

            waitFor(driver).until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.className("cart_list")
                )
            );

            // Click Checkout
            WebElement checkoutButton = waitFor(driver).until(
                ExpectedConditions.elementToBeClickable(
                    By.id("checkout")
                )
            );

            checkoutButton.click();

            // Verify checkout information page
            String infoTitle = waitFor(driver).until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.className("title")
                )
            ).getText();

            Assert.assertEquals(
                infoTitle,
                "Checkout: Your Information"
            );

            // Enter first name
            WebElement firstName = waitFor(driver).until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.id("first-name")
                )
            );

            firstName.clear();
            firstName.sendKeys("Yogesh");

            // Enter last name
            WebElement lastName = driver.findElement(
                By.id("last-name")
            );

            lastName.clear();
            lastName.sendKeys("Pardhe");

            // Enter postal code
            By postalCodeLocator = By.id("postal-code");

            WebElement postalCode = driver.findElement(
                postalCodeLocator
            );

            postalCode.clear();
            postalCode.sendKeys("444505");

            waitFor(driver).until(
                ExpectedConditions.attributeToBe(
                    postalCodeLocator,
                    "value",
                    "444505"
                )
            );

            // Verify customer information
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
                driver.findElement(postalCodeLocator)
                      .getAttribute("value"),
                "444505",
                "Postal code field was not populated."
            );

            System.out.println("Customer information entered successfully.");

            // Continue to checkout overview
            driver.findElement(By.id("continue")).click();

            String overviewTitle = waitFor(driver).until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.className("title")
                )
            ).getText();

            Assert.assertEquals(
                overviewTitle,
                "Checkout: Overview"
            );

            // Verify product
            String productName = waitFor(driver).until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.className("inventory_item_name")
                )
            ).getText();

            Assert.assertEquals(productName, "Sauce Labs Backpack");

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

            System.out.println("Checkout happy path test passed.");

        } finally {
            driver.quit();
        }
    }
}
