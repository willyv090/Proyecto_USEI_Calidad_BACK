package com.usei.usei.controllers;

import com.usei.usei.models.Plazo;
import com.usei.usei.repositories.PlazoDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
    3 Pruebas unitarias para PlazoBL
    Autor: Rosario Calisaya pruebas 1,2,3
 */
@ExtendWith(MockitoExtension.class)
class PlazoBLTest {

    @Mock
    private PlazoDAO plazoDAO;

    @InjectMocks
    private PlazoBL plazoBL;

    /**
     PRIMERA PRUEBA
       save_cuandoHayPlazoActivo_loMarcaAntiguoYGuardaNuevo
      Si ya existe un plazo con estado "activo", guardamos un nuevo plazo y
      verificamos que el plazo anterior pasa a estado "antiguo" y luego
      se llama a save() para el antiguo y para el nuevo.
     */
    @Test
    void save_cuandoHayPlazoActivo_loMarcaAntiguoYGuardaNuevo() {
        // 1. Preparación de la prueba

        Plazo plazoActivo = new Plazo();
        plazoActivo.setIdPlazo(1L);
        plazoActivo.setEstado("activo"); //plazo existente activo

        // nuevo plazo
        Plazo nuevoPlazo = new Plazo();
        nuevoPlazo.setIdPlazo(2L);
        nuevoPlazo.setEstado("activo"); // el nuevo será el activo luego
        Date fechaFin = Date.from(LocalDate.of(2025, 1, 10)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
        nuevoPlazo.setFechaFinalizacion(fechaFin);
        nuevoPlazo.setFechaModificacion(fechaFin);

        // plazoDAO devuelve el plazo activo actual
        when(plazoDAO.findByEstado("activo")).thenReturn(Optional.of(plazoActivo));
        // Cuando se llame a save con cualquier plazo, devuelve el mismo objeto
        when(plazoDAO.save(any(Plazo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Lógica de la prueba
        Plazo resultado = plazoBL.save(nuevoPlazo);

        // 3. Verificación o Assert

        // El plazo anterior debe quedar como "antiguo"
        assertEquals("antiguo", plazoActivo.getEstado());

        // El nuevo plazo debe ser el que retorna el método
        assertEquals(2L, resultado.getIdPlazo());

        // se verifica que guardo primero el antiguo y luego el nuevo
        verify(plazoDAO, times(2)).save(any(Plazo.class)); // 2 llamadas al save
        // se verifica que se busco el plazo activo
        verify(plazoDAO, times(1)).findByEstado("activo");
    }

    /**
       SEGUNDA PRUEBA
       save_ajustaFechasSumandoUnDia
        En caso de que no hay plazos activos, guardamos un plazo con fechaFinalizacion y
        fechaModificacion luego se verifican ambas fechas que se incrementan en 1 día.
     */
    @Test
    void save_ajustaFechasSumandoUnDia() {
        // 1. Preparación de la prueba

        when(plazoDAO.findByEstado("activo")).thenReturn(Optional.empty()); // no hay plazo activo

        LocalDate fechaOriginal = LocalDate.of(2025, 1, 10); // original
        Date fechaDate = Date.from(fechaOriginal.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Plazo plazo = new Plazo();
        plazo.setIdPlazo(3L);
        plazo.setFechaFinalizacion(fechaDate);
        plazo.setFechaModificacion(fechaDate);
        plazo.setEstado("activo");

        when(plazoDAO.save(any(Plazo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Lógica de la prueba
        Plazo resultado = plazoBL.save(plazo);

        // 3. Verificación o Assert

        // fecha de un dia despues
        LocalDate fechaEsperada = fechaOriginal.plusDays(1);
        Date fechaEsperadaDate = Date.from(fechaEsperada.atStartOfDay(ZoneId.systemDefault()).toInstant());

        assertEquals(fechaEsperadaDate, resultado.getFechaFinalizacion());
        assertEquals(fechaEsperadaDate, resultado.getFechaModificacion());

        // No debe encontrar ningún plazo activo
        verify(plazoDAO, times(1)).findByEstado("activo");
        // Se guardó solo una vez el nuevo plazo
        verify(plazoDAO, times(1)).save(any(Plazo.class)); // 1 llamada
    }

    /**
      TERCERA PRUEBA
      findAll_conPaginacion_delegaAlDAO
      Se pide una página de plazos y se verifica que el service llama al DAO con el Pageable correcto
      retornando la Page que da el DAO.
     */
    @Test
    void findAll_conPaginacion_delegaAlDAO() {

        // 1. Preparación de la prueba
        Pageable pageable = PageRequest.of(0, 5);

        Plazo p1 = new Plazo();
        p1.setIdPlazo(1L);
        Plazo p2 = new Plazo();
        p2.setIdPlazo(2L);

        Page<Plazo> page = new PageImpl<>(Arrays.asList(p1, p2), pageable, 2);

        when(plazoDAO.findAll(pageable)).thenReturn(page);

        // 2. Lógica de la prueba
        Page<Plazo> resultado = plazoBL.findAll(pageable);

        // 3. Verificación o Assert
        assertEquals(2, resultado.getTotalElements());
        assertEquals(2, resultado.getContent().size());
        verify(plazoDAO, times(1)).findAll(pageable);
    }
}
