package com.usei.usei.controllers;

import com.usei.usei.models.Certificado;
import com.usei.usei.models.EstadoCertificado;
import com.usei.usei.models.EstadoEncuesta;
import com.usei.usei.models.Estudiante;
import com.usei.usei.models.Usuario;
import com.usei.usei.repositories.CertificadoDAO;
import com.usei.usei.repositories.EstadoCertificadoDAO;
import com.usei.usei.repositories.EstadoEncuestaDAO;
import com.usei.usei.repositories.EstudianteDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 4 Pruebas unitarias para CertificadoBL
 * Autor: [Tu nombre] pruebas 1,2,3,4
 */
@ExtendWith(MockitoExtension.class)
class CertificadoBLTest {

    @Mock
    private CertificadoDAO certificadoDAO;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private EstadoEncuestaDAO estadoEncuestaDAO;

    @Mock
    private EstadoCertificadoDAO estadoCertificadoDAO;

    @Mock
    private EstudianteDAO estudianteDAO;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private CertificadoBL certificadoBL;

    /**
     PRIMERA PRUEBA
     save_datosValidos_guardaCertificadoConUsuario
     Se proporciona un certificado con un usuario válido. Se verifica que el método save()
     asigna correctamente el usuario al certificado y lo guarda en la base de datos.
     */
    @Test
    void save_datosValidos_guardaCertificadoConUsuario() {
        // 1. Preparación de la prueba

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNombre("Admin Usuario");

        Certificado certificado = new Certificado();
        certificado.setIdCertificado(1L);
        certificado.setFormato("formato_certificado.pdf");
        certificado.setVersion(1);
        certificado.setEstado("En uso");
        certificado.setUsuarioIdUsuario(usuario);

        // El UsuarioService devuelve el usuario cuando se busca por ID
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        // El CertificadoDAO devolverá el mismo certificado que reciba
        when(certificadoDAO.save(any(Certificado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Lógica de la prueba
        Certificado resultado = certificadoBL.save(certificado);

        // 3. Verificación o Assert

        // El certificado retornado debe tener el usuario asignado correctamente
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdCertificado());
        assertEquals("formato_certificado.pdf", resultado.getFormato());
        assertEquals(usuario, resultado.getUsuarioIdUsuario());

        // Se verifica que se buscó el usuario y se guardó el certificado
        verify(usuarioService, times(1)).findById(1L);
        verify(certificadoDAO, times(1)).save(any(Certificado.class));
    }

    /**
     SEGUNDA PRUEBA
     save_usuarioNoExiste_lanzaExcepcion
     Se intenta guardar un certificado con un usuario que no existe en la base de datos.
     Se verifica que lanza una excepción y no guarda el certificado.
     */
    @Test
    void save_usuarioNoExiste_lanzaExcepcion() {
        // 1. Preparación de la prueba

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(99L); // ID que no existe

        Certificado certificado = new Certificado();
        certificado.setIdCertificado(1L);
        certificado.setFormato("formato_certificado.pdf");
        certificado.setUsuarioIdUsuario(usuario);

        // El UsuarioService devuelve vacío (usuario no existe)
        when(usuarioService.findById(99L)).thenReturn(Optional.empty());

        // 2. Lógica de la prueba y 3. Verificación o Assert

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> certificadoBL.save(certificado));

        // La excepción debe mencionar que el usuario no fue encontrado
        assertTrue(ex.getMessage().contains("Usuario no encontrado"));

        // Se verifica que no se guardó el certificado si el usuario no existe
        verify(certificadoDAO, never()).save(any(Certificado.class));
    }

    /**
     TERCERA PRUEBA
     update_certificadoExistente_actualizaCamposYGuarda
     Se actualiza un certificado existente con nuevos datos. Se verifica que los campos
     se actualizan correctamente (formato, versión, usuario) y se guarda en la base de datos.
     Nota: El método update() solo actualiza formato, versión y usuario, NO el estado.
     */
    @Test
    void update_certificadoExistente_actualizaCamposYGuarda() {
        // 1. Preparación de la prueba

        Long id = 1L;

        // Certificado existente en la BD
        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setIdUsuario(1L);

        Certificado existente = new Certificado();
        existente.setIdCertificado(id);
        existente.setFormato("formato_viejo.pdf");
        existente.setVersion(1);
        existente.setEstado("En uso");
        existente.setUsuarioIdUsuario(usuarioExistente);

        // Nuevos datos para actualizar
        Usuario usuarioNuevo = new Usuario();
        usuarioNuevo.setIdUsuario(2L);
        usuarioNuevo.setNombre("Nuevo Admin");

        Certificado nuevosDatos = new Certificado();
        nuevosDatos.setFormato("formato_nuevo.pdf");
        nuevosDatos.setVersion(2);
        nuevosDatos.setEstado("Inactivo"); // Este campo NO se actualiza en el método
        nuevosDatos.setUsuarioIdUsuario(usuarioNuevo);

        // El CertificadoDAO devuelve el certificado existente al buscar por id
        when(certificadoDAO.findById(id)).thenReturn(Optional.of(existente));
        // El UsuarioService devuelve el nuevo usuario
        when(usuarioService.findById(2L)).thenReturn(Optional.of(usuarioNuevo));
        // El CertificadoDAO devolverá lo que reciba al guardar
        when(certificadoDAO.save(any(Certificado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Lógica de la prueba
        Certificado resultado = certificadoBL.update(nuevosDatos, id);

        // 3. Verificación o Assert

        assertNotNull(resultado);
        assertEquals("formato_nuevo.pdf", resultado.getFormato());
        assertEquals(2, resultado.getVersion());
        // El estado NO cambia, sigue siendo el original
        assertEquals("En uso", resultado.getEstado());
        assertEquals(usuarioNuevo, resultado.getUsuarioIdUsuario());

        // Se verifica que se buscó el certificado existente y se guardó
        verify(certificadoDAO, times(1)).findById(id);
        verify(usuarioService, times(1)).findById(2L);
        verify(certificadoDAO, times(1)).save(any(Certificado.class));
    }

    /**
     CUARTA PRUEBA
     update_certificadoNoExiste_lanzaExcepcion
     Se intenta actualizar un certificado que no existe en la base de datos.
     Se verifica que lanza una excepción.
     */
    @Test
    void update_certificadoNoExiste_lanzaExcepcion() {
        // 1. Preparación de la prueba

        Long idInexistente = 99L;

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);

        Certificado nuevosDatos = new Certificado();
        nuevosDatos.setFormato("formato_nuevo.pdf");
        nuevosDatos.setVersion(2);
        nuevosDatos.setUsuarioIdUsuario(usuario);

        // El CertificadoDAO devuelve vacío (certificado no existe)
        when(certificadoDAO.findById(idInexistente)).thenReturn(Optional.empty());

        // 2. Lógica de la prueba y 3. Verificación o Assert

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> certificadoBL.update(nuevosDatos, idInexistente));

        // La excepción debe mencionar que el Almacen (Certificado) no fue encontrado
        assertTrue(ex.getMessage().contains("Almacen no encontrado"));

        // Se verifica que no se guardó nada si el certificado no existe
        verify(certificadoDAO, never()).save(any(Certificado.class));
    }

    /**
     QUINTA PRUEBA
     findCertificadoEnUso_existe_retornaCertificado
     Se busca el certificado que está en uso. Se verifica que el método
     retorna correctamente el certificado encontrado.
     */
    @Test
    void findCertificadoEnUso_existe_retornaCertificado() {
        // 1. Preparación de la prueba

        Certificado certificadoEnUso = new Certificado();
        certificadoEnUso.setIdCertificado(1L);
        certificadoEnUso.setFormato("formato_certificado.pdf");
        certificadoEnUso.setEstado("En uso");

        // El CertificadoDAO devuelve el certificado en uso
        when(certificadoDAO.findByEstado("En uso")).thenReturn(Optional.of(certificadoEnUso));

        // 2. Lógica de la prueba
        Optional<Certificado> resultado = certificadoBL.findCertificadoEnUso();

        // 3. Verificación o Assert

        assertTrue(resultado.isPresent());
        assertEquals("En uso", resultado.get().getEstado());
        assertEquals("formato_certificado.pdf", resultado.get().getFormato());

        // Se verifica que se buscó por estado "En uso"
        verify(certificadoDAO, times(1)).findByEstado("En uso");
    }

    /**
     SEXTA PRUEBA
     enviarCertificadoConCondiciones_encuestaNoCompletada_lanzaExcepcion
     Se intenta enviar un certificado cuando la encuesta NO está completada.
     Se verifica que lanza una excepción indicando que no puede enviar.
     */
    @Test
    void enviarCertificadoConCondiciones_encuestaNoCompletada_lanzaExcepcion() {
        // 1. Preparación de la prueba

        Long idEstudiante = 1L;

        // Crear estado de encuesta NO completado
        EstadoEncuesta estadoEncuesta = new EstadoEncuesta();
        estadoEncuesta.setEstado("En progreso"); // No está completado

        // Simulación
        when(estadoEncuestaDAO.findByEstudianteIdEstudiante_IdEstudiante(idEstudiante))
                .thenReturn(Optional.of(estadoEncuesta));

        // 2. Lógica de la prueba y 3. Verificación o Assert

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> certificadoBL.enviarCertificadoConCondiciones(idEstudiante));

        // La excepción debe mencionar que el estado de la encuesta no está completado
        assertTrue(ex.getMessage().contains("estado de la encuesta no está completado"));

        // Se verifica que se validó el estado de encuesta
        verify(estadoEncuestaDAO, times(1)).findByEstudianteIdEstudiante_IdEstudiante(idEstudiante);
        // No debe continuar si la encuesta no está completada
        verify(estudianteDAO, never()).findById(idEstudiante);
    }

    /**
     SÉPTIMA PRUEBA
     enviarCertificadoConCondiciones_estudianteNoEncontrado_lanzaExcepcion
     Se intenta enviar un certificado cuando el estudiante no existe en la BD.
     Se verifica que lanza una excepción indicando que el estudiante no fue encontrado.
     */
    @Test
    void enviarCertificadoConCondiciones_estudianteNoEncontrado_lanzaExcepcion() {
        // 1. Preparación de la prueba

        Long idEstudiante = 99L; // ID que no existe

        // Crear estado de encuesta completado
        EstadoEncuesta estadoEncuesta = new EstadoEncuesta();
        estadoEncuesta.setEstado("Completado");

        // Crear estado de certificado
        EstadoCertificado estadoCertificado = new EstadoCertificado();
        estadoCertificado.setEstado("No enviado");
        estadoCertificado.setCorreoEnviado("");

        // Simulaciones
        when(estadoEncuestaDAO.findByEstudianteIdEstudiante_IdEstudiante(idEstudiante))
                .thenReturn(Optional.of(estadoEncuesta));
        when(estadoCertificadoDAO.findByEstudianteIdEstudiante_IdEstudiante(idEstudiante))
                .thenReturn(Optional.of(estadoCertificado));
        // El estudiante NO existe
        when(estudianteDAO.findById(idEstudiante))
                .thenReturn(Optional.empty());

        // 2. Lógica de la prueba y 3. Verificación o Assert

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> certificadoBL.enviarCertificadoConCondiciones(idEstudiante));

        // La excepción debe mencionar que el estudiante no fue encontrado
        assertTrue(ex.getMessage().contains("Estudiante no encontrado"));

        // Se verifica que se buscó el estudiante
        verify(estudianteDAO, times(1)).findById(idEstudiante);
        // No debe intentar obtener el certificado si el estudiante no existe
        verify(certificadoDAO, never()).findByEstado("En uso");
    }

    /**
     OCTAVA PRUEBA
     enviarCertificadoConCondiciones_noCertificadoEnUso_lanzaExcepcion
     Se intenta enviar un certificado cuando NO existe ninguno en estado "En uso".
     Se verifica que lanza una excepción indicando que no hay certificado disponible.
     */
    @Test
    void enviarCertificadoConCondiciones_noCertificadoEnUso_lanzaExcepcion() {
        // 1. Preparación de la prueba

        Long idEstudiante = 1L;

        // Crear estado de encuesta completado
        EstadoEncuesta estadoEncuesta = new EstadoEncuesta();
        estadoEncuesta.setEstado("Completado");

        Estudiante estudiante = new Estudiante();
        estudiante.setIdEstudiante(idEstudiante);
        estudiante.setNombre("Juan");
        estudiante.setApellido("Perez");
        estudiante.setCorreoInstitucional("juan@ucb.edu.bo");

        // Crear estado de certificado
        EstadoCertificado estadoCertificado = new EstadoCertificado();
        estadoCertificado.setEstado("No enviado");
        estadoCertificado.setCorreoEnviado("");

        // Simulaciones
        when(estadoEncuestaDAO.findByEstudianteIdEstudiante_IdEstudiante(idEstudiante))
                .thenReturn(Optional.of(estadoEncuesta));
        when(estadoCertificadoDAO.findByEstudianteIdEstudiante_IdEstudiante(idEstudiante))
                .thenReturn(Optional.of(estadoCertificado));
        when(estudianteDAO.findById(idEstudiante))
                .thenReturn(Optional.of(estudiante));
        // NO hay certificado en uso
        when(certificadoDAO.findByEstado("En uso"))
                .thenReturn(Optional.empty());

        // 2. Lógica de la prueba y 3. Verificación o Assert

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> certificadoBL.enviarCertificadoConCondiciones(idEstudiante));

        // La excepción debe mencionar que no hay certificado en uso
        assertTrue(ex.getMessage().contains("No hay certificado en uso"));

        // Se verifica que se buscó el certificado en uso
        verify(certificadoDAO, times(1)).findByEstado("En uso");
    }

}
