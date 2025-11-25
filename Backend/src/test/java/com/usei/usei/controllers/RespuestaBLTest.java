package com.usei.usei.controllers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.usei.usei.models.Estudiante;
import com.usei.usei.models.Pregunta;
import com.usei.usei.models.Respuesta;
import com.usei.usei.repositories.RespuestaDAO;

/**
  2 Pruebas unitarias para RespuestaBL
  Autor: Paola Quispe pruebas 4,5,6
 */
@ExtendWith(MockitoExtension.class)
class RespuestaBLTest {

    @Mock
    private RespuestaDAO respuestaDAO;

    @Mock
    private PreguntaService preguntaService;

    @Mock
    private EstudianteService estudianteService;

    @InjectMocks
    private RespuestaBL respuestaBL;

    /**
     Verificar si un estudiante ya llenó la encuesta.
     Se valida que el método hasFilledSurvey retorne TRUE y FALSE según el DAO.
    */
    
    @Test
    void hasFilledSurvey_deberiaRetornarTrueYCero() {
        // true
        when(respuestaDAO.existsByEstudianteIdEstudiante_IdEstudiante(1L))
                .thenReturn(true);

        boolean resultadoTrue = respuestaBL.hasFilledSurvey(1L);

        assertTrue(resultadoTrue);
        verify(respuestaDAO, times(1))
                .existsByEstudianteIdEstudiante_IdEstudiante(1L);

        //false
        when(respuestaDAO.existsByEstudianteIdEstudiante_IdEstudiante(2L))
                .thenReturn(false);

        boolean resultadoFalse = respuestaBL.hasFilledSurvey(2L);

        assertFalse(resultadoFalse);
        verify(respuestaDAO, times(1))
                .existsByEstudianteIdEstudiante_IdEstudiante(2L);
    }


    /**
     Intentar guardar una respuesta cuando la pregunta no existe.
     Se verifica que el metodo save lance excepcion.
    */
    @Test
    void save_deberiaLanzarExcepcionCuandoPreguntaNoExiste() {
        // 1. preparacion
        Respuesta respuesta = new Respuesta();
        
        Pregunta pregunta = new Pregunta();
        pregunta.setIdPregunta(5L);
        respuesta.setPreguntaIdPregunta(pregunta);

        Estudiante estudiante = new Estudiante();
        estudiante.setIdEstudiante(10L);
        respuesta.setEstudianteIdEstudiante(estudiante);

        // DAO simula pregunta NO encontrada
        when(preguntaService.findById(5L)).thenReturn(Optional.empty());

        // 2. logica 
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            respuestaBL.save(respuesta);
        });

        // 3. verificacion
        assertTrue(excepcion.getMessage().contains("Pregunta no encontrada"));
        verify(respuestaDAO, never()).save(any());
    }


    /**
     Buscar respuestas por estudiante con paginacion.
     Se valida que findRespuestasByEstudianteId arme el Pageable correctamente
     y que llame al DAO con los parametros esperados.
     */
    
     @Test
    void findRespuestasByEstudianteId_deberiaLlamarDAOConPageableCorrecto() {

        // 1. preparacion
        Long idEstudiante = 7L;
        String sortBy = "respuesta";
        String sortType = "ASC";
        int page = 0;
        int pageSize = 5;

        Pageable pageableEsperado = PageRequest.of(
                0,
                5,
                Sort.by(Sort.Direction.ASC, "respuesta")
        );

        Page<Respuesta> paginaSimulada = new PageImpl<>(java.util.Collections.emptyList());
        when(respuestaDAO.findByEstudianteIdEstudiante_IdEstudiante(idEstudiante, pageableEsperado))
                .thenReturn(paginaSimulada);

        // 2. logica
        Page<Respuesta> resultado = respuestaBL.findRespuestasByEstudianteId(
                idEstudiante,
                sortBy,
                sortType,
                page,
                pageSize
        );

        // 3. verificacion
        assertNotNull(resultado);
        verify(respuestaDAO, times(1))
                .findByEstudianteIdEstudiante_IdEstudiante(idEstudiante, pageableEsperado);
    }
}
