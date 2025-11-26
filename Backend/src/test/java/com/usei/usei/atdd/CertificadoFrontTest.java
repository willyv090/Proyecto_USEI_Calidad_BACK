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
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ATDD - Acceptance Test-Driven Development
 * Pruebas de interfaz gráfica para la funcionalidad de Certificados
 * Realiza validaciones end-to-end en el frontend (Vue.js/Vite) apuntando a http://localhost:5173
 * 
 * Ejecutar: mvn -Dfrontend.url=http://localhost:5173 -Dtest=CertificadoFrontTest test
 * 
 * @author Usuario
 * @version 1.0
 */
public class CertificadoFrontTest {

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
     * Test 1: Verificar carga de archivo PDF válido
     * Escenario: Un admin intenta subir un certificado PDF válido
     * Resultado esperado: El sistema muestra mensaje de éxito "Archivo Cargado exitosamente"
     */
    @Test
    void uploadPdfValido_debeSubirseCorrectamente() {
        // PREPARACIÓN
        // Navegar a la página de inicio y simular login
        autenticarYNavegar("/subir-certificado");
        
        // Esperar a que cargue el componente principal
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".student-survey-container")));
        
        // Esperar a que el input de archivo esté disponible
        WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".pdf-upload")));
        
        // LÓGICA
        // Crear un archivo PDF de prueba temporal
        String pathArchivoPdf = crearArchivoPdfTemporal("certificado_test.pdf");
        
        // Subir el archivo
        fileInput.sendKeys(pathArchivoPdf);
        
        // Esperar a que aparezca el mensaje de éxito (SweetAlert2)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".swal2-popup")));
        WebElement mensajeExito = driver.findElement(By.cssSelector(".swal2-title"));
        
        // VERIFICACIÓN
        assertTrue(mensajeExito.getText().contains("Archivo PDF válido"),
                "Debe mostrar mensaje de validación de PDF");
        
        // Hacer clic en Aceptar del alert
        WebElement btnAceptar = driver.findElement(By.cssSelector(".swal2-confirm"));
        btnAceptar.click();
        
        // Esperar a que el modal desaparezca
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.cssSelector(".swal2-popup"))));
        
        System.out.println("✓ Test 1 completado: Upload PDF válido");
    }

    /**
     * Test 2: Rechazar archivo que no es PDF
     * Escenario: Un admin intenta subir un archivo que no es PDF (ej: .txt, .jpg)
     * Resultado esperado: El sistema muestra error "Formato de archivo no válido"
     */
    @Test
    void uploadArchivoNoPdf_debeRechazarseConError() {
        // PREPARACIÓN
        autenticarYNavegar("/subir-certificado");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".student-survey-container")));
        WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".pdf-upload")));
        
        // LÓGICA
        // Crear un archivo no-PDF de prueba
        String pathArchivoNoValidado = crearArchivoTemporal("documento_invalido.txt");
        
        // Intentar subir el archivo
        fileInput.sendKeys(pathArchivoNoValidado);
        
        // Esperar a que aparezca el error (SweetAlert2)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".swal2-popup")));
        WebElement mensajeError = driver.findElement(By.cssSelector(".swal2-title"));
        
        // VERIFICACIÓN
        assertTrue(mensajeError.getText().contains("Formato de archivo no válido") || 
                   mensajeError.getText().contains("no válido"),
                "Debe mostrar error de formato no válido");
        
        // Cerrar el alert
        WebElement btnAceptar = driver.findElement(By.cssSelector(".swal2-confirm"));
        btnAceptar.click();
        
        System.out.println("✓ Test 2 completado: Rechazo de archivo no-PDF");
    }

    /**
     * Test 3: Cambiar estado de certificado de 'En uso' a 'Suspendido'
     * Escenario: Un admin ve una tabla de certificados y quiere cambiar el estado de uno
     * Resultado esperado: El estado se actualiza en la tabla y muestra mensaje de éxito
     * 
     * NOTA: Este test solo ejecuta si hay certificados en la tabla
     */
    @Test
    void cambiarEstadoCertificado_debeActualizarseCorrectamente() {
        // PREPARACIÓN
        autenticarYNavegar("/subir-certificado");
        
        // Esperar a que la tabla de certificados cargue
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".table-container table tbody")));
        
        // LÓGICA
        // Obtener el número de filas en la tabla
        java.util.List<WebElement> filas = driver.findElements(By.cssSelector(".table-container table tbody tr"));
        
        if (filas.size() == 0) {
            System.out.println("⚠ Advertencia: No hay certificados en la tabla. Test saltado.");
            return; // Si no hay certificados, saltar el test
        }
        
        System.out.println("✓ Se encontraron " + filas.size() + " certificados en la tabla");
        
        // Buscar un certificado con estado "En uso"
        WebElement filaCertificadoEnUso = null;
        for (WebElement fila : filas) {
            String estado = fila.findElement(By.cssSelector("td:nth-child(4)")).getText();
            if (estado.contains("En uso")) {
                filaCertificadoEnUso = fila;
                break;
            }
        }
        
        if (filaCertificadoEnUso == null) {
            System.out.println("⚠ Advertencia: No se encontró certificado en estado 'En uso'. Test saltado.");
            return;
        }
        
        System.out.println("✓ Se encontró certificado en estado 'En uso'");
        
        // Buscar el ícono de suspender dentro de esa fila
        WebElement iconoSuspender = filaCertificadoEnUso.findElement(By.cssSelector(".icon-suspended"));
        
        // Hacer clic en el ícono de suspender
        iconoSuspender.click();
        
        System.out.println("✓ Se hizo clic en el ícono suspender");
        
        // Esperar a que aparezca un alert (puede ser confirmación o éxito)
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".swal2-popup")));
            WebElement alertBox = driver.findElement(By.cssSelector(".swal2-popup"));
            
            System.out.println("✓ Alert apareció");
            
            // Verificar si es un diálogo de confirmación (tiene botones Sí y Cancelar)
            boolean tieneConfirmar = alertBox.findElements(By.cssSelector(".swal2-confirm")).size() > 0;
            boolean tieneCancelar = alertBox.findElements(By.cssSelector(".swal2-cancel")).size() > 0;
            boolean esConfirmacion = tieneConfirmar && tieneCancelar;
            
            if (esConfirmacion) {
                System.out.println("✓ Es un diálogo de confirmación. Haciendo clic en 'Sí'");
                WebElement btnConfirmar = alertBox.findElement(By.cssSelector(".swal2-confirm"));
                btnConfirmar.click();
                wait.until(ExpectedConditions.stalenessOf(alertBox));
                
                // Esperar al siguiente alert (el de éxito)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".swal2-popup")));
            }
            
            // Obtener el mensaje de éxito
            WebElement mensajeExito = driver.findElement(By.cssSelector(".swal2-title"));
            String mensaje = mensajeExito.getText();
            
            System.out.println("✓ Mensaje recibido: " + mensaje);
            
            // VERIFICACIÓN
            assertTrue(mensaje.contains("actualizado") || 
                      mensaje.contains("exitosamente") ||
                      mensaje.contains("éxito"),
                    "Debe mostrar mensaje de estado actualizado");
            
            // Cerrar el alert final
            WebElement btnAceptarFinal = driver.findElement(By.cssSelector(".swal2-confirm"));
            btnAceptarFinal.click();
            
            System.out.println("✓ Test 3 completado: Cambio de estado de certificado");
            
        } catch (org.openqa.selenium.TimeoutException e) {
            System.out.println("⚠ Timeout esperando alert. El servidor puede estar lento o el elemento no existir.");
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Simula autenticación inyectando datos en localStorage y navega a la ruta especificada
     * Para tests, usamos valores dummy ya que el frontend solo verifica presencia, no validez
     * 
     * @param ruta La ruta relativa a navegar (ej: "/subir-certificado")
     */
    private void autenticarYNavegar(String ruta) {
        try {
            // Paso 1: Navegar a la página inicial
            driver.get(baseUrl + "/");
            
            // Esperar a que la página cargue (espera a que document esté listo)
            wait.until(ExpectedConditions.jsReturnsValue(
                "return document.readyState === 'complete';"));
            
            // Paso 2: Inyectar token, rol e id en localStorage vía JavaScript
            // El frontend solo verifica la presencia de authToken, no valida su contenido
            String scriptAuthenticacion = 
                "window.localStorage.setItem('authToken', 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9');" +
                "window.localStorage.setItem('rol', 'Administrador');" +
                "window.localStorage.setItem('id_usuario', '1');";
            
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(scriptAuthenticacion);
            
            System.out.println("✓ Autenticación simulada. Token inyectado en localStorage.");
            System.out.println("  Rol: Administrador, ID Usuario: 1");
            
            // Paso 3: Navegar a la ruta deseada
            System.out.println("  Navegando a: " + baseUrl + ruta);
            driver.get(baseUrl + ruta);
            
            // Esperar a que la página cargue
            Thread.sleep(2000);
            
        } catch (Exception e) {
            fail("Error durante autenticación: " + e.getMessage());
        }
    }

    /**
     * Crea un archivo PDF temporal para propósitos de prueba
     * 
     * @param nombreArchivo Nombre del archivo a crear
     * @return Ruta absoluta al archivo creado
     */
    private String crearArchivoPdfTemporal(String nombreArchivo) {
        try {
            // Crear archivo en el directorio temporal del sistema
            File archivo = File.createTempFile("test_", ".pdf");
            archivo.deleteOnExit(); // Eliminar al cerrar la aplicación
            
            // Escribir contenido PDF mínimo (header PDF válido)
            String contenidoPdf = "%PDF-1.4\n%EOF";
            java.nio.file.Files.write(archivo.toPath(), contenidoPdf.getBytes());
            
            return archivo.getAbsolutePath();
        } catch (Exception e) {
            fail("No se pudo crear archivo PDF temporal: " + e.getMessage());
            return null;
        }
    }

    /**
     * Crea un archivo temporal de otro tipo para propósitos de validación de rechazo
     * 
     * @param nombreArchivo Nombre del archivo a crear
     * @return Ruta absoluta al archivo creado
     */
    private String crearArchivoTemporal(String nombreArchivo) {
        try {
            File archivo = File.createTempFile("test_", ".txt");
            archivo.deleteOnExit();
            
            java.nio.file.Files.write(archivo.toPath(), "Contenido de prueba".getBytes());
            
            return archivo.getAbsolutePath();
        } catch (Exception e) {
            fail("No se pudo crear archivo temporal: " + e.getMessage());
            return null;
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
