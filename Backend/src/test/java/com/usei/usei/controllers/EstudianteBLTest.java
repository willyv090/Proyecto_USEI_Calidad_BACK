package com.usei.usei.controllers;

import com.usei.usei.models.Estudiante;
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
  3 Pruebas unitarias para EstudianteBL
  Autor: Rosario Calisaya pruebas 1,2,3
 */
@ExtendWith(MockitoExtension.class)
class EstudianteBLTest {

    @Mock
    private EstudianteDAO estudianteDAO;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EstudianteBL estudianteBL;

    /**
     PRIMERA PRUEBA
     Crear un estudiante y realizar la simulación del comportamiento de EstudianteDAO
     llamando al método save de EstudinteBL, se verifica que retorne el mismo
     estudiante que se creo en el EstudianteDAO.
     */
    @Test
    void save_deberiaGuardarYRetornarEstudiante() {
        // 1. Preparación de la prueba
        Estudiante estudiante = new Estudiante();
        estudiante.setIdEstudiante(1L);
        estudiante.setNombre("Rosario");
        estudiante.setApellido("Calisaya");

        when(estudianteDAO.save(any(Estudiante.class))).thenReturn(estudiante);

        // 2. Lógica de la prueba
        Estudiante resultado = estudianteBL.save(estudiante);

        // 3. Verificación o Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdEstudiante());
        assertEquals("Rosario", resultado.getNombre());
        verify(estudianteDAO, times(1)).save(estudiante);
    }

    /**
     SEGUNDA PRUEBA
     findById() cuando el estudiante existe
      Se prepara una simluación del DAO para encontrar al estudiante mediante su ID y se verifica
     su existencia y los datos correctos.
     */
    @Test
    void findById_existente_deberiaRetornarEstudiante() {
        // 1. Preparación de la prueba
        Estudiante estudiante = new Estudiante();
        estudiante.setIdEstudiante(10L);
        estudiante.setNombre("Paola");

        when(estudianteDAO.findById(10L)).thenReturn(Optional.of(estudiante));

        // 2. Lógica de la prueba
        Optional<Estudiante> resultado = estudianteBL.findById(10L);

        // 3. Verificación o Assert
        assertTrue(resultado.isPresent());
        assertEquals("Paola", resultado.get().getNombre());
    }

    /**
     TERCERA PRUEBA
     Prueba de findByCorreoInst() cuando NO existe el estudiante se prepara la simulacion del DAO
     donde retorna NULL y llama a findByCorreoInst. luego se hace la verificación (debe devolver 0L)
     */
    @Test
    void findByCorreoInst_noExistente_deberiaRetornarCero() {
        // 1. Preparación de la prueba
        String correoInexistente = "noexisteprueba@ucb.edu.bo";
        when(estudianteDAO.findByCorreoInstitucional(correoInexistente)).thenReturn(null);

        // 2. Lógica de la prueba
        Long id = estudianteBL.findByCorreoInst(correoInexistente);

        // 3. Verificación o Assert
        assertEquals(0L, id);
        verify(estudianteDAO, times(1)).findByCorreoInstitucional(correoInexistente);
    }
}
