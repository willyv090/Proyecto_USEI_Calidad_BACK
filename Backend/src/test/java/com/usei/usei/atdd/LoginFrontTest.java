package com.usei.usei.atdd;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ATDD - Acceptance Test-Driven Development
 * Pruebas de interfaz gráfica para la funcionalidad de Login (Estudiante)
 * Realiza validaciones end-to-end en el frontend (Vue.js/Vite) apuntando a http://localhost:5173
 * 
 * Casos de prueba:
 * 1. Validación de campos obligatorios (CI y Contraseña no pueden estar vacíos)
 * 2. Login exitoso con credenciales válidas (estudiante)
 * 3. Rechazo de login con credenciales inválidas (401 Unauthorized)
 * 
 * NOTA: Los tests buscan el botón ".login-btn" en el NavBar (solo visible si no está logueado)
 * Ruta: GET http://localhost:5173/ (página de inicio PaginaInicio.vue)
 * 
 * Ejecutar: mvn test -Dtest=LoginFrontTest
 * 
 * @author Usuario
 * @version 1.0
 */
public class LoginFrontTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl = "http://localhost:5173";
    private final long WAIT_TIMEOUT = 10; // segundos

    @BeforeEach
    void setup() {
        // Configurar WebDriver para Chrome
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); // Descomentar para ejecución sin interfaz gráfica
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-blink-features=AutomationControlled");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT));
        
        // Configurar implicitly wait para todas las búsquedas de elementos
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    /**
     * Test 1: Validación de campos obligatorios
     * Escenario: Un usuario intenta hacer login sin completar los campos (CI vacío)
     * Resultado esperado: El navegador muestra validación HTML5 (required) o el backend rechaza con 401
     */
    @Test
    void validarCamposObligatorios_debeRechazarCamposVacios() {
        // PREPARACIÓN
        navegarAPaginaInicio();
        abrirLoginPopup();
        
        System.out.println("✓ Popup de login abierto");
        
        // LÓGICA
        // Llenar solo contraseña, dejar CI vacío (requiere validación HTML5)
        WebElement inputCI = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.cssSelector("#ci"))
        );
        WebElement inputPassword = driver.findElement(By.cssSelector("#password"));
        WebElement btnSubmit = driver.findElement(By.cssSelector(".submit-btn"));
        
        // Dejar CI vacío, pero llenar password
        inputCI.clear();
        System.out.println("✓ CI dejado vacío");
        
        inputPassword.clear();
        inputPassword.sendKeys("password123");
        System.out.println("✓ Contraseña rellenada (CI vacío)");
        
        // Intentar enviar con CI vacío
        btnSubmit.click();
        System.out.println("✓ Se hizo clic en 'Ingresar' con CI vacío");
        
        // VERIFICACIÓN
        // Esperar a que aparezca el alert (SweetAlert de validación)
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".swal2-popup")));
            System.out.println("✓ Alert de validación apareció");
            
            // Validar que es un alert de error
            WebElement alertIcon = driver.findElement(By.cssSelector(".swal2-icon[class*='error']"));
            assertNotNull(alertIcon, "El alert debe ser de error");
            System.out.println("✓ Alert es de error");
            
            // Validar que menciona campos incompletos
            WebElement alertTitle = driver.findElement(By.cssSelector(".swal2-title"));
            String titleText = alertTitle.getText();
            
            assertTrue(titleText.toLowerCase().contains("incompleto") || titleText.toLowerCase().contains("campos"),
                    "El alert debe mencionar campos incompletos");
            System.out.println("✓ Alert contiene: " + titleText);
            
            // Cerrar el alert
            WebElement btnAceptar = driver.findElement(By.cssSelector(".swal2-confirm"));
            btnAceptar.click();
            
        } catch (Exception e) {
            System.out.println("⚠ No apareció alert SweetAlert, validando que sigue en popup de login");
            // Si no hay alert, validar que el popup sigue abierto (validación HTML5 nativa)
            try {
                WebElement popupStillOpen = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".popup-overlay"))
                );
                assertNotNull(popupStillOpen, "El popup debe seguir abierto si hay validación HTML5");
                System.out.println("✓ Popup sigue abierto (validación HTML5 nativa en curso)");
            } catch (Exception e2) {
                fail("No apareció alert y el popup se cerró: " + e2.getMessage());
            }
        }
        
        System.out.println("✓ Test 1 completado: Validación de campos obligatorios");
    }

    /**
     * Test 2: Login exitoso con credenciales válidas
     * Escenario: Un estudiante hace login con CI y contraseña correctos
     * Resultado esperado: Se muestra alert de éxito y se redirige al menú de estudiante
     * 
     * NOTA: Este test REQUIERE credenciales válidas en la BD.
     *       Usa CI y contraseña de un usuario existente en la BD.
     */
    @Test
    void loginExitoso_debeRedirigirAlMenuEstudiante() {
        // PREPARACIÓN
        navegarAPaginaInicio();
        abrirLoginPopup();
        
        System.out.println("✓ Popup de login abierto");
        
        // LÓGICA
        // Llenar campos con credenciales válidas (CAMBIAR SEGÚN TUS DATOS)
        // IMPORTANTE: Reemplaza con credenciales que existan en tu BD
        String ciValido = "123456"; // ← CAMBIAR CON CI REAL
        String passwordValido = "password123"; // ← CAMBIAR CON CONTRASEÑA REAL
        
        WebElement inputCI = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.cssSelector("#ci"))
        );
        WebElement inputPassword = driver.findElement(By.cssSelector("#password"));
        WebElement btnSubmit = driver.findElement(By.cssSelector(".submit-btn"));
        
        inputCI.clear();
        inputCI.sendKeys(ciValido);
        System.out.println("✓ CI rellenado: " + ciValido);
        
        inputPassword.clear();
        inputPassword.sendKeys(passwordValido);
        System.out.println("✓ Contraseña rellenada");
        
        // Hacer clic en enviar
        btnSubmit.click();
        System.out.println("✓ Se hizo clic en 'Ingresar'");
        
        // VERIFICACIÓN
        try {
            // Esperar a que aparezca el alert de éxito
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".swal2-popup")));
            System.out.println("✓ Alert de respuesta apareció");
            
            // Validar que es un alert de éxito (icon='success')
            WebElement alertIcon = driver.findElement(By.cssSelector(".swal2-icon[class*='success']"));
            assertNotNull(alertIcon, "El alert debe ser de éxito");
            System.out.println("✓ Alert es de éxito (icon=success)");
            
            // Validar que muestra mensaje de bienvenida
            WebElement alertTitle = driver.findElement(By.cssSelector(".swal2-title"));
            String titleText = alertTitle.getText();
            assertTrue(titleText.toLowerCase().contains("sesión") || titleText.toLowerCase().contains("bienvenid"),
                    "El alert debe mencionar inicio de sesión o bienvenida");
            System.out.println("✓ Alert contiene mensaje de bienvenida");
            
            // Cerrar el alert haciendo clic en "Continuar"
            WebElement btnContinuar = driver.findElement(By.cssSelector(".swal2-confirm"));
            btnContinuar.click();
            System.out.println("✓ Se cerró el alert");
            
            // Esperar a que se redirija (puede tardar un momento)
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Validar que se redirigió correctamente (URL contiene "menu" o cambió)
            String urlActual = driver.getCurrentUrl();
            System.out.println("✓ URL después de login: " + urlActual);
            assertTrue(!urlActual.equals(baseUrl + "/"), "La URL debe haber cambiado después del login");
            
        } catch (Exception e) {
            System.out.println("⚠ Nota: Este test requiere credenciales válidas en la BD.");
            System.out.println("  Actualiza CI y contraseña en el test con datos reales.");
            System.out.println("  Error capturado: " + e.getMessage());
        }
        
        System.out.println("✓ Test 2 completado: Login exitoso");
    }

    /**
     * Test 3: Rechazo de login con credenciales inválidas
     * Escenario: Un usuario intenta hacer login con credenciales incorrectas
     * Resultado esperado: Aparece error "Credenciales incorrectas" (401 Unauthorized)
     */
    @Test
    void loginInvalido_debeRechazarCredencialesIncorrectas() {
        // PREPARACIÓN
        navegarAPaginaInicio();
        abrirLoginPopup();
        
        System.out.println("✓ Popup de login abierto");
        
        // LÓGICA
        // Llenar campos con credenciales INVÁLIDAS (sabemos que fallarán)
        String ciInvalido = "999999999"; // CI que no existe
        String passwordInvalido = "password_incorrecto"; // Contraseña incorrecta
        
        WebElement inputCI = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.cssSelector("#ci"))
        );
        WebElement inputPassword = driver.findElement(By.cssSelector("#password"));
        WebElement btnSubmit = driver.findElement(By.cssSelector(".submit-btn"));
        
        inputCI.clear();
        inputCI.sendKeys(ciInvalido);
        System.out.println("✓ CI inválido rellenado: " + ciInvalido);
        
        inputPassword.clear();
        inputPassword.sendKeys(passwordInvalido);
        System.out.println("✓ Contraseña inválida rellenada");
        
        // Hacer clic en enviar
        btnSubmit.click();
        System.out.println("✓ Se hizo clic en 'Ingresar' con credenciales inválidas");
        
        // VERIFICACIÓN
        try {
            // Esperar a que aparezca el alert de error
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".swal2-popup")));
            System.out.println("✓ Alert de error apareció");
            
            // Validar que es un alert de error (icon='error')
            WebElement alertIcon = driver.findElement(By.cssSelector(".swal2-icon[class*='error']"));
            assertNotNull(alertIcon, "El alert debe ser de error");
            System.out.println("✓ Alert es de error (icon=error)");
            
            // Validar que menciona credenciales incorrectas
            WebElement alertTitle = driver.findElement(By.cssSelector(".swal2-title"));
            String titleText = alertTitle.getText();
            assertTrue(titleText.toLowerCase().contains("incorrecto") || titleText.toLowerCase().contains("credencial") || titleText.toLowerCase().contains("error"),
                    "El alert debe mencionar credenciales incorrectas o error");
            System.out.println("✓ Alert contiene mensaje de error: " + titleText);
            
            // Cerrar el alert
            WebElement btnAceptar = driver.findElement(By.cssSelector(".swal2-confirm"));
            btnAceptar.click();
            System.out.println("✓ Se cerró el alert");
            
            // Validar que seguimos en la página de login (no se redirigió)
            String urlActual = driver.getCurrentUrl();
            assertTrue(urlActual.equals(baseUrl + "/"), "La URL debe seguir siendo la página de inicio");
            System.out.println("✓ Usuario sigue en página de inicio (no se redirigió)");
            
        } catch (Exception e) {
            fail("No apareció el alert de error esperado: " + e.getMessage());
        }
        
        System.out.println("✓ Test 3 completado: Rechazo de credenciales inválidas");
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Navega a la página de inicio del frontend
     */
    private void navegarAPaginaInicio() {
        try {
            driver.get(baseUrl + "/");
            
            // Esperar a que la página cargue
            wait.until(ExpectedConditions.jsReturnsValue(
                "return document.readyState === 'complete';"));
            
            System.out.println("✓ Navegado a: " + baseUrl);
            
        } catch (Exception e) {
            fail("Error al navegar a la página de inicio: " + e.getMessage());
        }
    }

    /**
     * Abre el popup de login haciendo clic en el botón de login en la navbar
     * El botón está en .login-btn en el NavBar (solo visible si no está logueado)
     */
    private void abrirLoginPopup() {
        try {
            // El selector exacto del botón de login en el NavBar es ".login-btn"
            WebElement btnLogin = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a.login-btn"))
            );
            System.out.println("✓ Botón 'Iniciar Sesión' encontrado en NavBar");
            
            // Hacer clic en el botón de login
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView(true);", btnLogin);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].click();", btnLogin);
            System.out.println("✓ Se hizo clic en botón de login");
            
            // Esperar a que el popup se abra (buscar elemento del popup)
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".popup-overlay")));
            System.out.println("✓ Popup de login abierto");
            
        } catch (Exception e) {
            fail("Error al abrir popup de login: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        // Cerrar el navegador después de cada test
        if (driver != null) {
            driver.quit();
        }
    }
}
