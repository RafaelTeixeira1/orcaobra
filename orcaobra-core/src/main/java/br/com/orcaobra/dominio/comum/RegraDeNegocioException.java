package br.com.orcaobra.dominio.comum;

/** Lançada quando uma operação viola uma regra do domínio. */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
