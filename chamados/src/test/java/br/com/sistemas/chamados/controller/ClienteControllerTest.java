package br.com.sistemas.chamados.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ClienteService;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ClienteService service;

    private static final String JSON_VALIDO =
        "{\"nome\":\"Ana Souza\",\"email\":\"ana@exemplo.com\",\"telefone\":\"47999999999\"}";

    @Test
    void criar_dadosValidos_retorna201() throws Exception {
        when(service.criar(any())).thenReturn(new ClienteResponse(1L, "Ana Souza", "ana@exemplo.com", "47999999999"));

        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
            .andExpect(status().isCreated());
    }

    @Test
    void criar_dadosInvalidos_retorna400() throws Exception {
        String invalido = "{\"nome\":\"\",\"email\":\"email-invalido\",\"telefone\":\"123\"}";

        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(invalido))
            .andExpect(status().isBadRequest());
    }

    @Test
    void criar_emailDuplicado_retorna409() throws Exception {
        when(service.criar(any())).thenThrow(new RegraNegocioException("Já existe cliente com o e-mail ana@exemplo.com"));

        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
            .andExpect(status().isConflict());
    }

    @Test
    void buscar_idInexistente_retorna404() throws Exception {
        when(service.buscar(99999L)).thenThrow(new RecursoNaoEncontradoException("Cliente 99999 não encontrado"));

        mvc.perform(get("/clientes/99999"))
            .andExpect(status().isNotFound());
    }
}
