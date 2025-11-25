package com.usei.usei.controllers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.usei.usei.models.Pregunta;
import com.usei.usei.repositories.PreguntaDAO;

/**
  2 Pruebas unitarias para PreguntaBL
  Autor: Paola Quispe pruebas 2,3
 */

@ExtendWith(MockitoExtension.class)
class PreguntaBLTest {

    @Mock
    private PreguntaDAO preguntaDAO;

    @InjectMocks
    private PreguntaBL preguntaBL;

    /**
     Guardar una pregunta y simular el comportamiento del PreguntaDAO.
     Se verifica que el metodo save retorna la misma pregunta.
     */
    @Test
    void save_deberiaGuardarYRetornarPregunta() {
        // 1. preparacion
        Pregunta pregunta = new Pregunta();
        pregunta.setIdPregunta(1L);
        pregunta.setPregunta("¿Buscas trabajo?");
        pregunta.setNumPregunta(1);
        pregunta.setTipoPregunta("seleccion");
        pregunta.setEstado("activo");

        when(preguntaDAO.save(any(Pregunta.class))).thenReturn(pregunta);

        // 2. logica
        Pregunta resultado = preguntaBL.save(pregunta);

        // 3. verificacion
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdPregunta());
        assertEquals("¿Buscas trabajo?", resultado.getPregunta());
        verify(preguntaDAO, times(1)).save(pregunta);
    }

    /**
     El metodo update debe lanzar una excepcion cuando la pregunta NO existe.
     */
    @Test
    void update_deberiaLanzarExcepcionCuandoPreguntaNoExiste() {
        // 1. preparacion
        Pregunta preguntaActualizada = new Pregunta();
        preguntaActualizada.setNumPregunta(2);
        preguntaActualizada.setPregunta("Nueva pregunta");
        preguntaActualizada.setTipoPregunta("texto");
        preguntaActualizada.setEstado("activo");

        Long idInexistente = 99L;

        // DAO retorna vacio, simula que no existe
        when(preguntaDAO.findById(idInexistente)).thenReturn(Optional.empty());

        // 2. logica
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            preguntaBL.update(preguntaActualizada, idInexistente);
        });

        //3. verificacion
        assertTrue(excepcion.getMessage().contains("Pregunta no encontrada"),
                "El mensaje de error debe indicar que la pregunta no existe");

            
        verify(preguntaDAO, never()).save(any()); //no llamar a save

    }
}
