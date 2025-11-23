package com.usei.usei.controllers;

import com.usei.usei.models.Soporte;
import com.usei.usei.models.TipoProblema;
import com.usei.usei.models.Usuario;
import com.usei.usei.repositories.SoporteDAO;
import com.usei.usei.repositories.UsuarioDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
    2 Pruebas unitarias para SoporteBL
    Autor: Rosario Calisaya pruebas 1,2
 */
@ExtendWith(MockitoExtension.class)
class SoporteBLTest {

    @Mock
    private SoporteDAO soporteDAO;
    @Mock
    private UsuarioDAO usuarioDAO;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private TipoProblemaService tipoProblemaService;
    @InjectMocks
    private SoporteBL soporteBL;

    /**
       PRIMERA PRUEBA
       save_datosValidos_guardaSoporteConUsuarioYTipo
       Se da un caso donde el tipo de problema y el usuario existen y se llama a save()
       en SoporteBL luego se verifica si asigna bien el TipoProblema y el Usuario al
       reporte de soporte para llamar a soporteDAO.save()
     */
    @Test
    void save_datosValidos_guardaSoporteConUsuarioYTipo() {
        // 1. Preparación de la prueba

        Soporte soporte = new Soporte();
        TipoProblema tipoProblema = new TipoProblema();
        tipoProblema.setIdProblema(1L);
        soporte.setTipoProblema(tipoProblema);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(5L);
        soporte.setUsuario(usuario);

        TipoProblema tipoDesdeServicio = new TipoProblema();
        tipoDesdeServicio.setIdProblema(1L);

        Usuario usuarioDesdeServicio = new Usuario();
        usuarioDesdeServicio.setIdUsuario(5L);
        usuarioDesdeServicio.setNombre("Carlos");

        // Caso de simulacion donde existen el tipo de problema y el usuario
        when(tipoProblemaService.findById(1L)).thenReturn(Optional.of(tipoDesdeServicio));
        when(usuarioService.findById(5L)).thenReturn(Optional.of(usuarioDesdeServicio));

        // El SoporteDAO devolverá lo que reciba
        when(soporteDAO.save(any(Soporte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Lógica de la prueba
        Soporte resultado = soporteBL.save(soporte);

        // 3. Verificación o Assert
        assertNotNull(resultado);
        assertEquals(tipoDesdeServicio, resultado.getTipoProblema());  // TipoProblema asignado
        assertEquals(usuarioDesdeServicio, resultado.getUsuario());    // Usuario asignado

        verify(tipoProblemaService, times(1)).findById(1L);
        verify(usuarioService, times(1)).findById(5L);
        verify(soporteDAO, times(1)).save(any(Soporte.class)); // Se guardó en el SoporteDAO
    }

    /**
       SEGUNDA PRUEBA
       save_tipoProblemaNoExiste_lanzaExcepcion
       Se da el caso en que el tipo de problema NO existe en la BD entonces se verifica
        lanzado una excepción y ya no llama al soporteDAO.save().
     */
    @Test
    void save_tipoProblemaNoExiste_lanzaExcepcion() {
        // 1. Preparación de la prueba
        Soporte soporte = new Soporte();

        TipoProblema tipoProblema = new TipoProblema();
        tipoProblema.setIdProblema(99L);  // id que no existe
        soporte.setTipoProblema(tipoProblema);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(5L);
        soporte.setUsuario(usuario);

        // Se simula que NO existe el tipo de problema
        when(tipoProblemaService.findById(99L)).thenReturn(Optional.empty());

        // 2. Lógica de la prueba y 3. Verificación o Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> soporteBL.save(soporte));

        // La alerta debe mencionar que no se encontro el tipo de problema
        assertTrue(ex.getMessage().contains("Tipo de Problema"));

        // verifica
        verify(soporteDAO, never()).save(any(Soporte.class)); // no deberia guardar si no encuentra el tipo de problema
    }
}
