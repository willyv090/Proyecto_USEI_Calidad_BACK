package com.usei.usei.controllers;

import com.usei.usei.models.TipoProblema;
import com.usei.usei.repositories.TipoProblemaDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 2 Pruebas unitarias para TipoProblemaBL
 * Autor: [Tu nombre] pruebas 1,2
 */
@ExtendWith(MockitoExtension.class)
class TipoProblemaTest {

    @Mock
    private TipoProblemaDAO tipoProblemaDAO;

    @InjectMocks
    private TipoProblemaBL tipoProblemaBL;

    /**
     PRIMERA PRUEBA
     save_datosValidos_guardaTipoProblema
     Se proporciona un TipoProblema válido y se verifica que se guarda correctamente en la BD.
     */
    @Test
    void save_datosValidos_guardaTipoProblema() {
        // 1. Preparación de la prueba

        TipoProblema tipoProblema = new TipoProblema();
        tipoProblema.setIdProblema(1L);
        tipoProblema.setProblema("Error de sistema");

        // El TipoProblemaDAO devolverá el mismo TipoProblema que reciba
        when(tipoProblemaDAO.save(any(TipoProblema.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Lógica de la prueba
        TipoProblema resultado = tipoProblemaBL.save(tipoProblema);

        // 3. Verificación o Assert

        // El TipoProblema retornado debe tener los datos correctos
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdProblema());
        assertEquals("Error de sistema", resultado.getProblema());

        // Se verifica que se guardó el TipoProblema
        verify(tipoProblemaDAO, times(1)).save(any(TipoProblema.class));
    }

    /**
     SEGUNDA PRUEBA
     update_tipoProblemaExistente_actualizaYGuarda
     Se actualiza un TipoProblema existente con un nuevo problema. Se verifica que se actualiza
     correctamente el campo "problema" y se guarda en la base de datos.
     */
    @Test
    void update_tipoProblemaExistente_actualizaYGuarda() {
        // 1. Preparación de la prueba

        Long id = 1L;

        // TipoProblema existente en la BD
        TipoProblema existente = new TipoProblema();
        existente.setIdProblema(id);
        existente.setProblema("Error antiguo");

        // Nuevos datos
        TipoProblema nuevosDatos = new TipoProblema();
        nuevosDatos.setProblema("Error actualizado");

        // El TipoProblemaDAO devuelve el TipoProblema existente al buscar por id
        when(tipoProblemaDAO.findById(id)).thenReturn(Optional.of(existente));
        // El TipoProblemaDAO devolverá lo que reciba al guardar
        when(tipoProblemaDAO.save(any(TipoProblema.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Lógica de la prueba
        TipoProblema resultado = tipoProblemaBL.update(nuevosDatos, id);

        // 3. Verificación o Assert

        assertNotNull(resultado);
        assertEquals("Error actualizado", resultado.getProblema());
        assertEquals(id, resultado.getIdProblema());

        // Se verifica que se buscó el TipoProblema existente y se guardó
        verify(tipoProblemaDAO, times(1)).findById(id);
        verify(tipoProblemaDAO, times(1)).save(any(TipoProblema.class));
    }

}
