package com.usei.usei.controllers;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.usei.usei.models.Encuesta;
import com.usei.usei.models.Plazo;
import com.usei.usei.models.Usuario;
import com.usei.usei.repositories.EncuestaDAO;

/**
  4 Pruebas unitarias para RespuestaBL
  Autor: Paola Quispe pruebas 7,8,9,10
 */
@ExtendWith(MockitoExtension.class)
class EncuestaBLTest {

    @Mock
    private EncuestaDAO encuestaDAO;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private PlazoService plazoService;

    @InjectMocks
    private EncuestaBL encuestaBL;

    /**
     Guardar una encuesta con datos validos.
     Se verifica que save() asigne usuario, plazo y guarde correctamente.
     */
    @Test
    void save_datosValidos_guardaEncuestaConUsuarioYPlazo() {

        // 1. preparacion
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);

        Plazo plazo = new Plazo();
        plazo.setIdPlazo(1L);

        Encuesta encuesta = new Encuesta();
        encuesta.setIdEncuesta(1L);
        encuesta.setTitulo("Encuesta de Satisfacción");
        encuesta.setUsuarioIdUsuario(usuario);
        encuesta.setPlazoIdPlazo(plazo);

        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        when(plazoService.findById(1L)).thenReturn(Optional.of(plazo));
        when(encuestaDAO.save(any(Encuesta.class))).thenAnswer(inv -> inv.getArgument(0));

        // 2. logica
        Encuesta resultado = encuestaBL.save(encuesta);

        // 3. verificacion
        assertNotNull(resultado);
        assertEquals("Encuesta de Satisfacción", resultado.getTitulo());
        assertEquals(usuario, resultado.getUsuarioIdUsuario());
        assertEquals(plazo, resultado.getPlazoIdPlazo());

        verify(usuarioService, times(1)).findById(1L);
        verify(plazoService, times(1)).findById(1L);
        verify(encuestaDAO, times(1)).save(any());
    }

    /**
     Intentar guardar una encuesta con un usuario inexistente.
     Se verifica que save() lance excepcion y NO guarde la encuesta.
     */
    @Test
    void save_usuarioNoExiste_lanzaExcepcion() {

        // 1. preparacion
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(99L); // id incorrecto

        Plazo plazo = new Plazo();
        plazo.setIdPlazo(1L);

        Encuesta encuesta = new Encuesta();
        encuesta.setUsuarioIdUsuario(usuario);
        encuesta.setPlazoIdPlazo(plazo);

        when(usuarioService.findById(99L)).thenReturn(Optional.empty());

        // 2. logica
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> encuestaBL.save(encuesta));

        // 3. verificacion
        assertTrue(ex.getMessage().contains("Usuario no encontrado"));

        verify(encuestaDAO, never()).save(any());
        verify(plazoService, never()).findById(anyLong());
    }

    /**
     Actualizar una encuesta existente con nuevos datos.
     Se verifica que update() modifique campos y guarde correctamente.
     */
    @Test
    void update_encuestaExistente_actualizaCamposYGuarda() {

        // 1. preparacion
        Long id = 1L;

        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setIdUsuario(1L);

        Plazo plazoExistente = new Plazo();
        plazoExistente.setIdPlazo(1L);

        Encuesta existente = new Encuesta();
        existente.setIdEncuesta(id);
        existente.setTitulo("Vieja");
        existente.setUsuarioIdUsuario(usuarioExistente);
        existente.setPlazoIdPlazo(plazoExistente);

        Usuario usuarioNuevo = new Usuario();
        usuarioNuevo.setIdUsuario(2L);

        Plazo plazoNuevo = new Plazo();
        plazoNuevo.setIdPlazo(2L);

        Encuesta nuevosDatos = new Encuesta();
        nuevosDatos.setTitulo("Nueva");
        nuevosDatos.setUsuarioIdUsuario(usuarioNuevo);
        nuevosDatos.setPlazoIdPlazo(plazoNuevo);

        when(encuestaDAO.findById(id)).thenReturn(Optional.of(existente));
        when(usuarioService.findById(2L)).thenReturn(Optional.of(usuarioNuevo));
        when(plazoService.findById(2L)).thenReturn(Optional.of(plazoNuevo));
        when(encuestaDAO.save(any(Encuesta.class))).thenAnswer(inv -> inv.getArgument(0));

        // 2. logica
        Encuesta resultado = encuestaBL.update(nuevosDatos, id);

        // 3. verificacion
        assertEquals("Nueva", resultado.getTitulo());
        assertEquals(usuarioNuevo, resultado.getUsuarioIdUsuario());
        assertEquals(plazoNuevo, resultado.getPlazoIdPlazo());

        verify(encuestaDAO, times(1)).save(any());
    }

    /**
     Intentar actualizar una encuesta que no existe.
     Se verifica que update() lance excepción y no guarde nada.
    */
   @Test
    void update_encuestaNoExiste_lanzaExcepcion() {

        // 1. preparacion
        Long idInexistente = 99L;

        Encuesta nuevosDatos = new Encuesta();
        nuevosDatos.setTitulo("Nueva");

        when(encuestaDAO.findById(idInexistente)).thenReturn(Optional.empty());

        // 2. logica
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> encuestaBL.update(nuevosDatos, idInexistente));

        // 3. verificacion
        assertTrue(ex.getMessage().contains("Encuesta no encontrada"));

        verify(encuestaDAO, never()).save(any());
    }










//test disponibles

    /**
     SEXTA PRUEBA
     update_usuarioNoExiste_lanzaExcepcion
     Se intenta actualizar una encuesta existente pero con un usuario que no existe.
     Se verifica que lanza una excepción y no guarda la encuesta.
     */
    @Test
    void update_usuarioNoExiste_lanzaExcepcion() {
        // 1. Preparación de la prueba

        Long id = 1L;

        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setIdUsuario(1L);

        Plazo plazoExistente = new Plazo();
        plazoExistente.setIdPlazo(1L);

        Encuesta existente = new Encuesta();
        existente.setIdEncuesta(id);
        existente.setTitulo("Encuesta Vieja");
        existente.setUsuarioIdUsuario(usuarioExistente);
        existente.setPlazoIdPlazo(plazoExistente);

        // Intentar asignar un usuario que no existe
        Usuario usuarioInexistente = new Usuario();
        usuarioInexistente.setIdUsuario(99L);

        Plazo plazo = new Plazo();
        plazo.setIdPlazo(1L);

        Encuesta nuevosDatos = new Encuesta();
        nuevosDatos.setTitulo("Encuesta Nueva");
        nuevosDatos.setUsuarioIdUsuario(usuarioInexistente);
        nuevosDatos.setPlazoIdPlazo(plazo);

        // El EncuestaDAO devuelve la encuesta existente
        when(encuestaDAO.findById(id)).thenReturn(Optional.of(existente));
        // El UsuarioService NO encuentra el usuario
        when(usuarioService.findById(99L)).thenReturn(Optional.empty());

        // 2. Lógica de la prueba y 3. Verificación o Assert

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> encuestaBL.update(nuevosDatos, id));

        // La excepción debe mencionar que el usuario no fue encontrado
        assertTrue(ex.getMessage().contains("Usuario no encontrado"));

        // Se verifica que no se guardó la encuesta
        verify(encuestaDAO, never()).save(any(Encuesta.class));
    }

    /**
     SÉPTIMA PRUEBA
     update_plazoNoExiste_lanzaExcepcion
     Se intenta actualizar una encuesta existente pero con un plazo que no existe.
     Se verifica que lanza una excepción y no guarda la encuesta.
     */
    @Test
    void update_plazoNoExiste_lanzaExcepcion() {
        // 1. Preparación de la prueba

        Long id = 1L;

        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setIdUsuario(1L);

        Plazo plazoExistente = new Plazo();
        plazoExistente.setIdPlazo(1L);

        Encuesta existente = new Encuesta();
        existente.setIdEncuesta(id);
        existente.setTitulo("Encuesta Vieja");
        existente.setUsuarioIdUsuario(usuarioExistente);
        existente.setPlazoIdPlazo(plazoExistente);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);

        // Intentar asignar un plazo que no existe
        Plazo plazoInexistente = new Plazo();
        plazoInexistente.setIdPlazo(99L);

        Encuesta nuevosDatos = new Encuesta();
        nuevosDatos.setTitulo("Encuesta Nueva");
        nuevosDatos.setUsuarioIdUsuario(usuario);
        nuevosDatos.setPlazoIdPlazo(plazoInexistente);

        // El EncuestaDAO devuelve la encuesta existente
        when(encuestaDAO.findById(id)).thenReturn(Optional.of(existente));
        // El UsuarioService encuentra el usuario
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        // El PlazoService NO encuentra el plazo
        when(plazoService.findById(99L)).thenReturn(Optional.empty());

        // 2. Lógica de la prueba y 3. Verificación o Assert

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> encuestaBL.update(nuevosDatos, id));

        // La excepción debe mencionar que el plazo no fue encontrado
        assertTrue(ex.getMessage().contains("Plazo no encontrado"));

        // Se verifica que no se guardó la encuesta
        verify(encuestaDAO, never()).save(any(Encuesta.class));
    }

    /**
     OCTAVA PRUEBA
     findById_existe_retornaEncuesta
     Se busca una encuesta por ID que existe. Se verifica que el método retorna la encuesta correctamente.
     */
    @Test
    void findById_existe_retornaEncuesta() {
        // 1. Preparación de la prueba

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);

        Plazo plazo = new Plazo();
        plazo.setIdPlazo(1L);

        Encuesta encuesta = new Encuesta();
        encuesta.setIdEncuesta(1L);
        encuesta.setTitulo("Encuesta de Satisfacción");
        encuesta.setDescripcion("Encuesta para medir la satisfacción");
        encuesta.setUsuarioIdUsuario(usuario);
        encuesta.setPlazoIdPlazo(plazo);

        // El EncuestaDAO devuelve la encuesta
        when(encuestaDAO.findById(1L)).thenReturn(Optional.of(encuesta));

        // 2. Lógica de la prueba
        Optional<Encuesta> resultado = encuestaBL.findById(1L);

        // 3. Verificación o Assert

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getIdEncuesta());
        assertEquals("Encuesta de Satisfacción", resultado.get().getTitulo());
        assertEquals("Encuesta para medir la satisfacción", resultado.get().getDescripcion());

        // Se verifica que se buscó la encuesta
        verify(encuestaDAO, times(1)).findById(1L);
    }

    /**
     NOVENA PRUEBA
     findById_noExiste_retornaOptionalVacio
     Se busca una encuesta por ID que no existe. Se verifica que el método retorna Optional vacío.
     */
    @Test
    void findById_noExiste_retornaOptionalVacio() {
        // 1. Preparación de la prueba

        Long idInexistente = 99L;

        // El EncuestaDAO devuelve vacío
        when(encuestaDAO.findById(idInexistente)).thenReturn(Optional.empty());

        // 2. Lógica de la prueba
        Optional<Encuesta> resultado = encuestaBL.findById(idInexistente);

        // 3. Verificación o Assert

        assertFalse(resultado.isPresent());
        assertTrue(resultado.isEmpty());

        // Se verifica que se intentó buscar la encuesta
        verify(encuestaDAO, times(1)).findById(idInexistente);
    }

    /**
     DÉCIMA PRUEBA
     findAll_retornaIterableDeEncuestas
     Se obtienen todas las encuestas. Se verifica que el método retorna un iterable con las encuestas.
     */
    @Test
    void findAll_retornaIterableDeEncuestas() {
        // 1. Preparación de la prueba

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);

        Plazo plazo = new Plazo();
        plazo.setIdPlazo(1L);

        Encuesta encuesta1 = new Encuesta();
        encuesta1.setIdEncuesta(1L);
        encuesta1.setTitulo("Encuesta 1");
        encuesta1.setUsuarioIdUsuario(usuario);
        encuesta1.setPlazoIdPlazo(plazo);

        Encuesta encuesta2 = new Encuesta();
        encuesta2.setIdEncuesta(2L);
        encuesta2.setTitulo("Encuesta 2");
        encuesta2.setUsuarioIdUsuario(usuario);
        encuesta2.setPlazoIdPlazo(plazo);

        java.util.List<Encuesta> encuestasList = Arrays.asList(encuesta1, encuesta2);

        // El EncuestaDAO devuelve las encuestas
        when(encuestaDAO.findAll()).thenReturn(encuestasList);

        // 2. Lógica de la prueba
        Iterable<Encuesta> resultado = encuestaBL.findAll();

        // 3. Verificación o Assert

        assertNotNull(resultado);
        // Convertir a lista para poder contar elementos
        java.util.List<Encuesta> resultadoList = (java.util.List<Encuesta>) resultado;
        assertEquals(2, resultadoList.size());
        assertEquals("Encuesta 1", resultadoList.get(0).getTitulo());
        assertEquals("Encuesta 2", resultadoList.get(1).getTitulo());

        // Se verifica que se obtuvieron todas las encuestas
        verify(encuestaDAO, times(1)).findAll();
    }

}
