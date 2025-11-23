package com.usei.usei.controllers;

import com.usei.usei.models.Usuario;
import com.usei.usei.repositories.UsuarioDAO;
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
  2 Pruebas unitarias para UsuarioBL
  Autor: Rosario Calisaya pruebas 1,2
 */
@ExtendWith(MockitoExtension.class)
class UsuarioBLTest {

    @Mock
    private UsuarioDAO usuarioDAO;
    @Mock
    private JavaMailSender mailSender;
    @InjectMocks
    private UsuarioBL usuarioBL;

    /**
      PRIMERA PRUEBA
      update_usuarioExistente_actualizaCamposYGuarda
      En caso de que si exista un usuario en la BD se llama a update() para editar sus datos nuevos
      y se verifica que llama a usuarioDAO.save()
     */
    @Test
    void update_usuarioExistente_actualizaCamposYGuarda() {
        // 1. Preparación de la prueba

        Long id = 1L;

        // Usuario registrado en la BD
        Usuario existente = new Usuario();
        existente.setIdUsuario(id);
        existente.setNombre("Jorge Martínez");
        existente.setTelefono(65918962);
        existente.setCorreo("jorgemartinez@gmail.com");
        existente.setCarrera("Ingenieria de sistemas");
        existente.setRol("Administrador");
        existente.setUsuario("jorge_martinez");
        existente.setContrasenia("fuymrmQmulEaBbkyPCZBTi");

        // nuevos datos
        Usuario nuevosDatos = new Usuario();
        nuevosDatos.setNombre("Jorgito");
        nuevosDatos.setTelefono(777777);
        nuevosDatos.setCorreo("jorgito@gmail.com");
        nuevosDatos.setCarrera("Ingeniería industrial");
        nuevosDatos.setRol("Director");
        nuevosDatos.setUsuario("jorgito");
        nuevosDatos.setContrasenia("12345");

        // El UsuarioDAO devuelve el usuario existente al buscar por id
        when(usuarioDAO.findById(id)).thenReturn(Optional.of(existente));
        when(usuarioDAO.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Lógica de la prueba
        Usuario resultado = usuarioBL.update(nuevosDatos, id);

        // 3. Verificación o Assert
        assertNotNull(resultado);
        assertEquals("Jorgito", resultado.getNombre());
        assertEquals(777777, resultado.getTelefono());
        assertEquals("jorgito@gmail.com", resultado.getCorreo());
        assertEquals("Ingeniería industrial", resultado.getCarrera());
        assertEquals("Director", resultado.getRol());
        assertEquals("jorgito", resultado.getUsuario());
        assertEquals("12345", resultado.getContrasenia());

        // Se llamo una ves al findById y luego a save
        verify(usuarioDAO, times(1)).findById(id);
        verify(usuarioDAO, times(1)).save(any(Usuario.class));
    }

    /**
      SEGUNDA PRUEBA
      findByMail_noExiste_retornaCero
      En caso que no exsita un usuario con ese correo, se verifica y se retorna 0L y llama a
      UsuarioDAO.findByCorreo()
     */
    @Test
    void findByMail_noExiste_retornaCero() {
        // 1. Preparación de la prueba
        String correoInexistente = "noexiste@ucb.edu.bo";

        when(usuarioDAO.findByCorreo(correoInexistente)).thenReturn(null); // UsuarioDAO devuelve null

        // 2. Lógica de la prueba
        Long resultado = usuarioBL.findByMail(correoInexistente);

        // 3. Verificación o Assert
        assertEquals(0L, resultado);  // Si no existe debe ser 0
        verify(usuarioDAO, times(1)).findByCorreo(correoInexistente);
    }
}
