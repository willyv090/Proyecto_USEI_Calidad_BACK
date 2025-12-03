package com.usei.usei.controllers;

import com.usei.usei.models.Encuesta;
import com.usei.usei.models.EncuestaGestion;
import com.usei.usei.models.Pregunta;
import com.usei.usei.repositories.EncuestaGestionDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 10 Pruebas unitarias para EncuestaGestionBL
 * Autor: Nicole Rojas pruebas 1..10
 */
@ExtendWith(MockitoExtension.class)
class EncuestaGestionBLTest {

    @Mock
    private EncuestaGestionDAO encuestaGestionDAO;

    @Mock
    private EncuestaService encuestaService;

    @Mock
    private PreguntaService preguntaService;

    @InjectMocks
    private EncuestaGestionBL encuestaGestionBL;

    /**
     PRIMERA PRUEBA
     save_datosValidos_guardaEncuestaGestion
     Guarda una encuesta_gestion con encuesta y pregunta válidas
     */
    @Test
    void save_datosValidos_guardaEncuestaGestion() {
        // Preparación
        Encuesta encuesta = new Encuesta();
        encuesta.setIdEncuesta(1L);

        Pregunta pregunta = new Pregunta();
        pregunta.setIdPregunta(1L);

        EncuestaGestion eg = new EncuestaGestion();
        eg.setIdEncuestaGestion(1L);
        eg.setEncuestaIdEncuesta(encuesta);
        eg.setPreguntaIdPregunta(pregunta);

        when(encuestaService.findById(1L)).thenReturn(Optional.of(encuesta));
        when(preguntaService.findById(1L)).thenReturn(Optional.of(pregunta));
        when(encuestaGestionDAO.save(any(EncuestaGestion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Lógica
        EncuestaGestion resultado = encuestaGestionBL.save(eg);

        // Verificación
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdEncuestaGestion());
        assertEquals(encuesta, resultado.getEncuestaIdEncuesta());
        assertEquals(pregunta, resultado.getPreguntaIdPregunta());

        verify(encuestaService, times(1)).findById(1L);
        verify(preguntaService, times(1)).findById(1L);
        verify(encuestaGestionDAO, times(1)).save(any(EncuestaGestion.class));
    }

    /**
     SEGUNDA PRUEBA
     save_encuestaNoExiste_lanzaExcepcion
     Si la encuesta asociada no existe, lanza excepción y no guarda
     */
    @Test
    void save_encuestaNoExiste_lanzaExcepcion() {
        Encuesta encuesta = new Encuesta();
        encuesta.setIdEncuesta(99L);

        Pregunta pregunta = new Pregunta();
        pregunta.setIdPregunta(1L);

        EncuestaGestion eg = new EncuestaGestion();
        eg.setEncuestaIdEncuesta(encuesta);
        eg.setPreguntaIdPregunta(pregunta);

        when(encuestaService.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> encuestaGestionBL.save(eg));
        assertTrue(ex.getMessage().contains("encuesta no encontrado"));
        verify(encuestaGestionDAO, never()).save(any(EncuestaGestion.class));
    }

    /**
     TERCERA PRUEBA
     save_preguntaNoExiste_lanzaExcepcion
     Si la pregunta asociada no existe, lanza excepción y no guarda
     */
    @Test
    void save_preguntaNoExiste_lanzaExcepcion() {
        Encuesta encuesta = new Encuesta();
        encuesta.setIdEncuesta(1L);

        Pregunta pregunta = new Pregunta();
        pregunta.setIdPregunta(99L);

        EncuestaGestion eg = new EncuestaGestion();
        eg.setEncuestaIdEncuesta(encuesta);
        eg.setPreguntaIdPregunta(pregunta);

        when(encuestaService.findById(1L)).thenReturn(Optional.of(encuesta));
        when(preguntaService.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> encuestaGestionBL.save(eg));
        assertTrue(ex.getMessage().contains("pregunta no encontrado"));
        verify(encuestaGestionDAO, never()).save(any(EncuestaGestion.class));
    }

    /**
     CUARTA PRUEBA
     update_encuestaGestionExistente_actualizaYCambiaCampos
     Actualiza una encuesta_gestion existente con nuevos valores
     */
    @Test
    void update_encuestaGestionExistente_actualizaYCambiaCampos() {
        Long id = 1L;

        Encuesta encuestaOld = new Encuesta();
        encuestaOld.setIdEncuesta(1L);

        Pregunta preguntaOld = new Pregunta();
        preguntaOld.setIdPregunta(1L);

        EncuestaGestion existente = new EncuestaGestion();
        existente.setIdEncuestaGestion(id);
        existente.setAnio(2024);
        existente.setSemestre(1);
        existente.setEncuestaIdEncuesta(encuestaOld);
        existente.setPreguntaIdPregunta(preguntaOld);

        // Nuevos valores
        Encuesta encuestaNew = new Encuesta();
        encuestaNew.setIdEncuesta(2L);

        Pregunta preguntaNew = new Pregunta();
        preguntaNew.setIdPregunta(2L);

        EncuestaGestion nuevos = new EncuestaGestion();
        nuevos.setAnio(2025);
        nuevos.setSemestre(2);
        nuevos.setEncuestaIdEncuesta(encuestaNew);
        nuevos.setPreguntaIdPregunta(preguntaNew);

        when(encuestaGestionDAO.findById(id)).thenReturn(Optional.of(existente));
        when(encuestaService.findById(2L)).thenReturn(Optional.of(encuestaNew));
        when(preguntaService.findById(2L)).thenReturn(Optional.of(preguntaNew));
        when(encuestaGestionDAO.save(any(EncuestaGestion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EncuestaGestion resultado = encuestaGestionBL.update(nuevos, id);

        assertNotNull(resultado);
        assertEquals(2025, resultado.getAnio());
        assertEquals(2, resultado.getSemestre());
        assertEquals(encuestaNew, resultado.getEncuestaIdEncuesta());
        assertEquals(preguntaNew, resultado.getPreguntaIdPregunta());

        verify(encuestaGestionDAO, times(1)).findById(id);
        verify(encuestaService, times(1)).findById(2L);
        verify(preguntaService, times(1)).findById(2L);
        verify(encuestaGestionDAO, times(1)).save(any(EncuestaGestion.class));
    }

    /**
     QUINTA PRUEBA
     update_encuestaGestionNoExiste_lanzaExcepcion
     Intentar actualizar una encuesta_gestion inexistente debe lanzar excepción
     */
    @Test
    void update_encuestaGestionNoExiste_lanzaExcepcion() {
        Long id = 99L;

        Encuesta encuesta = new Encuesta();
        encuesta.setIdEncuesta(1L);

        Pregunta pregunta = new Pregunta();
        pregunta.setIdPregunta(1L);

        EncuestaGestion nuevos = new EncuestaGestion();
        nuevos.setEncuestaIdEncuesta(encuesta);
        nuevos.setPreguntaIdPregunta(pregunta);

        when(encuestaGestionDAO.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> encuestaGestionBL.update(nuevos, id));
        assertTrue(ex.getMessage().contains("Encuesta_Gestion no encontrado"));
        verify(encuestaGestionDAO, never()).save(any(EncuestaGestion.class));
    }

    /**
     SEXTA PRUEBA
     update_encuestaNoExiste_lanzaExcepcion
     Si la encuesta nueva no existe, debe lanzar excepción
     */
    @Test
    void update_encuestaNoExiste_lanzaExcepcion() {
        Long id = 1L;

        Encuesta encuestaNew = new Encuesta();
        encuestaNew.setIdEncuesta(99L);

        Pregunta preguntaNew = new Pregunta();
        preguntaNew.setIdPregunta(1L);

        EncuestaGestion existente = new EncuestaGestion();
        existente.setIdEncuestaGestion(id);
        existente.setEncuestaIdEncuesta(new Encuesta());
        existente.setPreguntaIdPregunta(new Pregunta());

        EncuestaGestion nuevos = new EncuestaGestion();
        nuevos.setEncuestaIdEncuesta(encuestaNew);
        nuevos.setPreguntaIdPregunta(preguntaNew);

        when(encuestaGestionDAO.findById(id)).thenReturn(Optional.of(existente));
        when(encuestaService.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> encuestaGestionBL.update(nuevos, id));
        assertTrue(ex.getMessage().contains("encuesta no encontrado"));
        verify(encuestaGestionDAO, never()).save(any(EncuestaGestion.class));
    }

    /**
     SEPTIMA PRUEBA
     update_preguntaNoExiste_lanzaExcepcion
     Si la pregunta nueva no existe, debe lanzar excepción
     */
    @Test
    void update_preguntaNoExiste_lanzaExcepcion() {
        Long id = 1L;

        Encuesta encuestaNew = new Encuesta();
        encuestaNew.setIdEncuesta(1L);

        Pregunta preguntaNew = new Pregunta();
        preguntaNew.setIdPregunta(99L);

        EncuestaGestion existente = new EncuestaGestion();
        existente.setIdEncuestaGestion(id);
        existente.setEncuestaIdEncuesta(new Encuesta());
        existente.setPreguntaIdPregunta(new Pregunta());

        EncuestaGestion nuevos = new EncuestaGestion();
        nuevos.setEncuestaIdEncuesta(encuestaNew);
        nuevos.setPreguntaIdPregunta(preguntaNew);

        when(encuestaGestionDAO.findById(id)).thenReturn(Optional.of(existente));
        when(encuestaService.findById(1L)).thenReturn(Optional.of(encuestaNew));
        when(preguntaService.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> encuestaGestionBL.update(nuevos, id));
        assertTrue(ex.getMessage().contains("pregunta no encontrado"));
        verify(encuestaGestionDAO, never()).save(any(EncuestaGestion.class));
    }

    /**
     OCTAVA PRUEBA
     findById_existe_retornaEncuestaGestion
     */
    @Test
    void findById_existe_retornaEncuestaGestion() {
        EncuestaGestion eg = new EncuestaGestion();
        eg.setIdEncuestaGestion(1L);
        eg.setAnio(2024);

        when(encuestaGestionDAO.findById(1L)).thenReturn(Optional.of(eg));

        Optional<EncuestaGestion> resultado = encuestaGestionBL.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getIdEncuestaGestion());
        assertEquals(2024, resultado.get().getAnio());
        verify(encuestaGestionDAO, times(1)).findById(1L);
    }

    /**
     NOVENA PRUEBA
     findById_noExiste_retornaVacio
     */
    @Test
    void findById_noExiste_retornaVacio() {
        when(encuestaGestionDAO.findById(99L)).thenReturn(Optional.empty());

        Optional<EncuestaGestion> resultado = encuestaGestionBL.findById(99L);

        assertFalse(resultado.isPresent());
        verify(encuestaGestionDAO, times(1)).findById(99L);
    }

    /**
     DECIMA PRUEBA
     findAll_retornaIterable
     */
    @Test
    void findAll_retornaIterable() {
        EncuestaGestion eg1 = new EncuestaGestion();
        eg1.setIdEncuestaGestion(1L);
        EncuestaGestion eg2 = new EncuestaGestion();
        eg2.setIdEncuestaGestion(2L);

        java.util.List<EncuestaGestion> list = Arrays.asList(eg1, eg2);
        when(encuestaGestionDAO.findAll()).thenReturn(list);

        Iterable<EncuestaGestion> resultado = encuestaGestionBL.findAll();
        java.util.List<EncuestaGestion> resultadoList = (java.util.List<EncuestaGestion>) resultado;

        assertEquals(2, resultadoList.size());
        verify(encuestaGestionDAO, times(1)).findAll();
    }

}
