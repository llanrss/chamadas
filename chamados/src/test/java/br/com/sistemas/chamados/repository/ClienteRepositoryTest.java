package br.com.sistemas.chamados.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import br.com.sistemas.chamados.entity.Cliente;

@DataJpaTest
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository repository;

    @Test
    void existsByEmail_retornaTrueQuandoEmailCadastrado() {
        repository.save(new Cliente("Ana", "ana@exemplo.com", "47999999999"));

        assertTrue(repository.existsByEmail("ana@exemplo.com"));
        assertFalse(repository.existsByEmail("outro@exemplo.com"));
    }

    @Test
    void existsByEmailAndIdNot_ignoraOProprioRegistro() {
        Cliente ana = repository.save(new Cliente("Ana", "ana@exemplo.com", "47999999999"));
        Cliente bia = repository.save(new Cliente("Bia", "bia@exemplo.com", "47988888888"));

        assertFalse(repository.existsByEmailAndIdNot("ana@exemplo.com", ana.getId()));
        assertTrue(repository.existsByEmailAndIdNot("ana@exemplo.com", bia.getId()));
    }
}
