package br.com.orcaobra.api.adaptador.saida.pdf;

import br.com.orcaobra.aplicacao.porta.saida.GeradorDeDocumentoProposta;
import br.com.orcaobra.dominio.proposta.Proposta;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

/**
 * Padrão Template Method: define o esqueleto fixo de toda proposta (a ordem das seções)
 * e deixa cada seção para as subclasses. Um novo layout (ex.: proposta resumida, outra marca)
 * só precisa sobrescrever as seções que mudam.
 */
public abstract class PropostaPdfTemplate implements GeradorDeDocumentoProposta {

    protected static final Color COR_PRIMARIA = new Color(0x1F, 0x3A, 0x5F);
    protected static final Color COR_CLARA = new Color(0xEE, 0xF2, 0xF7);
    protected static final Color COR_TEXTO_SUAVE = new Color(0x5B, 0x67, 0x75);

    protected static Font fonte(float tamanho) {
        return FontFactory.getFont(FontFactory.HELVETICA, tamanho, Color.BLACK);
    }

    protected static Font fonteNegrito(float tamanho, Color cor) {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, tamanho, cor);
    }

    /** O método-template: a sequência é sempre a mesma. */
    @Override
    public final byte[] gerar(Proposta proposta) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 42, 42, 42, 54);
        try {
            PdfWriter writer = PdfWriter.getInstance(documento, saida);
            writer.setPageEvent(new Rodape(proposta));
            documento.addTitle("Proposta " + proposta.numero());
            documento.addAuthor(proposta.empresa().nome());
            documento.open();

            cabecalho(documento, proposta);
            dadosDoClienteEObra(documento, proposta);
            tabelaDeServicos(documento, proposta);
            resumoDeValores(documento, proposta);
            condicoesComerciais(documento, proposta);
            assinatura(documento, proposta);
        } catch (DocumentException e) {
            throw new IllegalStateException("Falha ao gerar o PDF da proposta", e);
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }
        return saida.toByteArray();
    }

    protected abstract void cabecalho(Document doc, Proposta p) throws DocumentException;

    protected abstract void dadosDoClienteEObra(Document doc, Proposta p) throws DocumentException;

    protected abstract void tabelaDeServicos(Document doc, Proposta p) throws DocumentException;

    protected abstract void resumoDeValores(Document doc, Proposta p) throws DocumentException;

    protected abstract void condicoesComerciais(Document doc, Proposta p) throws DocumentException;

    protected abstract void assinatura(Document doc, Proposta p) throws DocumentException;

    /** Rodapé com número da proposta e página, desenhado ao fim de cada página. */
    private static final class Rodape extends PdfPageEventHelper {
        private final Proposta proposta;

        private Rodape(Proposta proposta) {
            this.proposta = proposta;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Phrase texto = new Phrase(proposta.numero() + "  ·  " + proposta.empresa().nome()
                    + "  ·  página " + writer.getPageNumber(), FontFactory.getFont(FontFactory.HELVETICA, 8, COR_TEXTO_SUAVE));
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, texto,
                    (document.left() + document.right()) / 2, document.bottom() - 24, 0);
        }
    }
}
