package br.com.sistemas.chamados.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ChamadoService;

@WebMvcTest(ChamadoController.class)
class ChamadoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ChamadoService service;

    private static final String JSON_VALIDO = """
        {"titulo":"Sem internet","descricao":"Rede caiu","prioridade":"ALTA","clienteId":1,"categoriaId":1}
        """;

    private ChamadoResponse resposta() {
        return new ChamadoResponse(1L, "Sem internet", "Rede caiu", LocalDateTime.now(), null,
            Prioridade.ALTA, StatusChamado.ABERTO, 1L, "Ana", 1L, "Rede", null, null);
    }

    @Test
    void criar_dadosValidos_retorna201() throws Exception {
        when(service.criar(any())).thenReturn(resposta());

        mvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
            .andExpect(status().isCreated());
    }

    @Test
    void criar_semTitulo_retorna400() throws Exception {
        String invalido = "{\"titulo\":\"\",\"prioridade\":\"ALTA\",\"clienteId\":1,\"categoriaId\":1}";

        mvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content(invalido))
            .andExpect(status().isBadRequest());
    }

    @Test
    void criar_prioridadeInexistente_retorna400() throws Exception {
        String invalido = "{\"titulo\":\"X\",\"prioridade\":\"URGENTE\",\"clienteId\":1,\"categoriaId\":1}";

        mvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content(invalido))
            .andExpect(status().isBadRequest());
    }

    @Test
    void buscar_idInexistente_retorna404() throws Exception {
        when(service.buscar(99999L)).thenThrow(new RecursoNaoEncontradoException("Chamado 99999 não encontrado"));

        mvc.perform(get("/chamados/99999")).andExpect(status().isNotFound());
    }

    @Test
    void alterarStatus_chamadoFechado_retorna409() throws Exception {
        when(service.alterarStatus(eq(1L), any()))
            .thenThrow(new RegraNegocioException("Chamado 1 está fechado e não pode ser alterado"));

        mvc.perform(patch("/chamados/1/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"EM_ATENDIMENTO\"}"))
            .andExpect(status().isConflict());
    }

    @Test
    void listar_statusInvalido_retorna400() throws Exception {
        mvc.perform(get("/chamados?status=XYZ")).andExpect(status().isBadRequest());
    }
}
