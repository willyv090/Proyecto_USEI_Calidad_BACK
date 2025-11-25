package com.usei.usei.controllers;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.usei.usei.models.Noticias;
import com.usei.usei.repositories.NoticiasDAO;

/**
  1 Prueba unitaria para NoticiasBL
  Autor: Paola Quispe prueba 1
 */

@ExtendWith(MockitoExtension.class)
public class NoticiasBLTest {
    @Mock
    private NoticiasDAO noticiasDAO;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private NoticiasBL noticiasBL;

   /**
     Simular que la noticia existe, y verificar que NoticiasBL.delete()
     llame correctamente a noticiasDAO.deleteById.
    */
    @Test
    void delete_deberiaEliminarNoticiaCuandoExiste() {
        //1. preparacion
        Noticias noticia = new Noticias();
        noticia.setIdNoticia(5L);
        
        when(noticiasDAO.findById(5L)).thenReturn(Optional.of(noticia));

        //2. logica
        noticiasBL.delete(5L);

        //3. verificacion
        verify(noticiasDAO, times(1)).deleteById(5L);
    } 
}
