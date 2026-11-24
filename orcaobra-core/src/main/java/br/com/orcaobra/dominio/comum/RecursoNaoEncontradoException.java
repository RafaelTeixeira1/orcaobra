package br.com.orcaobra.dominio.comum;

/** Lançada quando um orçamento, material ou função não existe. */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
