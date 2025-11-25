package com.usei.usei.controllers;

import com.usei.usei.models.Encuesta;
import com.usei.usei.models.Plazo;
import com.usei.usei.models.Usuario;
import com.usei.usei.repositories.EncuestaDAO;
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
 * 10 Pruebas unitarias para EncuestaBL
 * Autor: [Tu nombre] pruebas 1,2,3,4,5,6,7,8,9,10
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
     PRIMERA PRUEBA
     save_datosValidos_guardaEncuestaConUsuarioYPlazo
     Se proporciona una encuesta con un usuario y plazo válidos. Se verifica que el método save()
     asigna correctamente el usuario y plazo a la encuesta y la guarda en la base de datos.
     */
    @Test
    void save_datosValidos_guardaEncuestaConUsuarioYPlazo() {
        // 1. Preparación de la prueba

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNombre("Carlos Admin");

        Plazo plazo = new Plazo();
        plazo.setIdPlazo(1L);
        plazo.setEstado("activo");

        Encuesta encuesta = new Encuesta();
        encuesta.setIdEncuesta(1L);
        encuesta.setTitulo("Encuesta de Satisfacción");
        encuesta.setDescripcion("Encuesta para medir la satisfacción");
        encuesta.setUsuarioIdUsuario(usuario);
        encuesta.setPlazoIdPlazo(plazo);

        // El UsuarioService devuelve el usuario cuando se busca por ID
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        // El PlazoService devuelve el plazo cuando se busca por ID
        when(plazoService.findById(1L)).thenReturn(Optional.of(plazo));
        // El EncuestaDAO devolverá la misma encuesta que reciba
        when(encuestaDAO.save(any(Encuesta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Lógica de la prueba
        Encuesta resultado = encuestaBL.save(encuesta);

        // 3. Verificación o Assert

        // La encuesta retornada debe tener el usuario y plazo asignados correctamente
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdEncuesta());
        assertEquals("Encuesta de Satisfacción", resultado.getTitulo());
        assertEquals(usuario, resultado.getUsuarioIdUsuario());
        assertEquals(plazo, resultado.getPlazoIdPlazo());

        // Se verifica que se buscó el usuario y el plazo, y se guardó la encuesta
        verify(usuarioService, times(1)).findById(1L);
        verify(plazoService, times(1)).findById(1L);
        verify(encuestaDAO, times(1)).save(any(Encuesta.class));
    }

    /**
     SEGUNDA PRUEBA
     save_usuarioNoExiste_lanzaExcepcion
     Se intenta guardar una encuesta con un usuario que no existe en la base de datos.
     Se verifica que lanza una excepción y no guarda la encuesta.
     */
    @Test
    void save_usuarioNoExiste_lanzaExcepcion() {
        // 1. Preparación de la prueba

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(99L); // ID que no existe

        Plazo plazo = new Plazo();
        plazo.setIdPlazo(1L);

        Encuesta encuesta = new Encuesta();
        encuesta.setIdEncuesta(1L);
        encuesta.setTitulo("Encuesta de Satisfacción");
        encuesta.setUsuarioIdUsuario(usuario);
        encuesta.setPlazoIdPlazo(plazo);

        // El UsuarioService devuelve vacío (usuario no existe)
        when(usuarioService.findById(99L)).thenReturn(Optional.empty());

        // 2. Lógica de la prueba y 3. Verificación o Assert

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> encuestaBL.save(encuesta));

        // La excepción debe mencionar que el usuario no fue encontrado
        assertTrue(ex.getMessage().contains("Usuario no encontrado"));

        // Se verifica que no se guardó la encuesta si el usuario no existe
        verify(encuestaDAO, never()).save(any(Encuesta.class));
        // Tampoco debe buscar el plazo si el usuario no existe
        verify(plazoService, never()).findById(anyLong());
    }

    /**
     TERCERA PRUEBA
     save_plazoNoExiste_lanzaExcepcion
     Se intenta guardar una encuesta con un plazo que no existe en la base de datos.
     Se verifica que lanza una excepción y no guarda la encuesta.
     */
    @Test
    void save_plazoNoExiste_lanzaExcepcion() {
        // 1. Preparación de la prueba

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);

        Plazo plazo = new Plazo();
        plazo.setIdPlazo(99L); // ID que no existe

        Encuesta encuesta = new Encuesta();
        encuesta.setIdEncuesta(1L);
        encuesta.setTitulo("Encuesta de Satisfacción");
        encuesta.setUsuarioIdUsuario(usuario);
        encuesta.setPlazoIdPlazo(plazo);

        // El UsuarioService devuelve el usuario
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        // El PlazoService devuelve vacío (plazo no existe)
        when(plazoService.findById(99L)).thenReturn(Optional.empty());

        // 2. Lógica de la prueba y 3. Verificación o Assert

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> encuestaBL.save(encuesta));

        // La excepción debe mencionar que el plazo no fue encontrado
        assertTrue(ex.getMessage().contains("Plazo no encontrado"));

        // Se verifica que no se guardó la encuesta si el plazo no existe
        verify(encuestaDAO, never()).save(any(Encuesta.class));
    }

    /**
     CUARTA PRUEBA
     update_encuestaExistente_actualizaCamposYGuarda
     Se actualiza una encuesta existente con nuevos datos (título, descripción, usuario y plazo).
     Se verifica que los campos se actualizan correctamente y se guarda en la base de datos.
     */
    @Test
    void update_encuestaExistente_actualizaCamposYGuarda() {
        // 1. Preparación de la prueba

        Long id = 1L;

        // Encuesta existente en la BD
        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setIdUsuario(1L);

        Plazo plazoExistente = new Plazo();
        plazoExistente.setIdPlazo(1L);

        Encuesta existente = new Encuesta();
        existente.setIdEncuesta(id);
        existente.setTitulo("Encuesta Vieja");
        existente.setDescripcion("Descripción vieja");
        existente.setUsuarioIdUsuario(usuarioExistente);
        existente.setPlazoIdPlazo(plazoExistente);

        // Nuevos datos para actualizar
        Usuario usuarioNuevo = new Usuario();
        usuarioNuevo.setIdUsuario(2L);
        usuarioNuevo.setNombre("Nuevo Admin");

        Plazo plazoNuevo = new Plazo();
        plazoNuevo.setIdPlazo(2L);
        plazoNuevo.setEstado("activo");

        Encuesta nuevosDatos = new Encuesta();
        nuevosDatos.setTitulo("Encuesta Nueva");
        nuevosDatos.setDescripcion("Descripción nueva");
        nuevosDatos.setUsuarioIdUsuario(usuarioNuevo);
        nuevosDatos.setPlazoIdPlazo(plazoNuevo);

        // El EncuestaDAO devuelve la encuesta existente al buscar por id
        when(encuestaDAO.findById(id)).thenReturn(Optional.of(existente));
        // El UsuarioService devuelve el nuevo usuario
        when(usuarioService.findById(2L)).thenReturn(Optional.of(usuarioNuevo));
        // El PlazoService devuelve el nuevo plazo
        when(plazoService.findById(2L)).thenReturn(Optional.of(plazoNuevo));
        // El EncuestaDAO devolverá lo que reciba al guardar
        when(encuestaDAO.save(any(Encuesta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Lógica de la prueba
        Encuesta resultado = encuestaBL.update(nuevosDatos, id);

        // 3. Verificación o Assert

        assertNotNull(resultado);
        assertEquals("Encuesta Nueva", resultado.getTitulo());
        assertEquals("Descripción nueva", resultado.getDescripcion());
        assertEquals(usuarioNuevo, resultado.getUsuarioIdUsuario());
        assertEquals(plazoNuevo, resultado.getPlazoIdPlazo());

        // Se verifica que se buscó la encuesta existente y se guardó
        verify(encuestaDAO, times(1)).findById(id);
        verify(usuarioService, times(1)).findById(2L);
        verify(plazoService, times(1)).findById(2L);
        verify(encuestaDAO, times(1)).save(any(Encuesta.class));
    }

    /**
     QUINTA PRUEBA
     update_encuestaNoExiste_lanzaExcepcion
     Se intenta actualizar una encuesta que no existe en la base de datos.
     Se verifica que lanza una excepción.
     */
    @Test
    void update_encuestaNoExiste_lanzaExcepcion() {
        // 1. Preparación de la prueba

        Long idInexistente = 99L;

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);

        Plazo plazo = new Plazo();
        plazo.setIdPlazo(1L);

        Encuesta nuevosDatos = new Encuesta();
        nuevosDatos.setTitulo("Encuesta Nueva");
        nuevosDatos.setDescripcion("Descripción nueva");
        nuevosDatos.setUsuarioIdUsuario(usuario);
        nuevosDatos.setPlazoIdPlazo(plazo);

        // El EncuestaDAO devuelve vacío (encuesta no existe)
        when(encuestaDAO.findById(idInexistente)).thenReturn(Optional.empty());

        // 2. Lógica de la prueba y 3. Verificación o Assert

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> encuestaBL.update(nuevosDatos, idInexistente));

        // La excepción debe mencionar que la encuesta no fue encontrada
        assertTrue(ex.getMessage().contains("Encuesta no encontrada"));

        // Se verifica que no se guardó nada si la encuesta no existe
        verify(encuestaDAO, never()).save(any(Encuesta.class));
    }

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
