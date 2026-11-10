package br.com.sistemas.chamados.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import br.com.sistemas.chamados.entity.*;

@DataJpaTest
class ChamadoRepositoryTest {

    @Autowired private ChamadoRepository chamados;
    @Autowired private ClienteRepository clientes;
    @Autowired private CategoriaRepository categorias;

    private Cliente cliente;
    private Categoria categoria;

    @BeforeEach
    void preparar() {
        cliente = clientes.save(new Cliente("Ana", "ana@exemplo.com", "47999999999"));
        categoria = categorias.save(new Categoria("Hardware", "Problemas físicos"));
        salvar("Mouse quebrado", Prioridade.BAIXA, StatusChamado.ABERTO);
        salvar("Servidor fora", Prioridade.CRITICA, StatusChamado.EM_ATENDIMENTO);
        salvar("Teclado falhando", Prioridade.BAIXA, StatusChamado.FECHADO);
    }

    private void salvar(String titulo, Prioridade p, StatusChamado s) {
        Chamado c = new Chamado();
        c.setTitulo(titulo);
        c.setPrioridade(p);
        c.setStatus(s);
        c.setDataAbertura(LocalDateTime.now());
        c.setCliente(cliente);
        c.setCategoria(categoria);
        chamados.save(c);
    }

    @Test
    void filtrar_semFiltros_retornaTodos() {
        Page<Chamado> page = chamados.filtrar(null, null, null, PageRequest.of(0, 10));
        assertEquals(3, page.getTotalElements());
    }

    @Test
    void filtrar_porStatus() {
        Page<Chamado> page = chamados.filtrar(StatusChamado.ABERTO, null, null, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void filtrar_combinandoStatusEPrioridade() {
        Page<Chamado> page = chamados.filtrar(StatusChamado.FECHADO, Prioridade.BAIXA, cliente.getId(), PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Teclado falhando", page.getContent().get(0).getTitulo());
    }

    @Test
    void filtrar_respeitaPaginacao() {
        Page<Chamado> page = chamados.filtrar(null, null, null, PageRequest.of(0, 2));
        assertEquals(2, page.getContent().size());
        assertEquals(2, page.getTotalPages());
    }
}
