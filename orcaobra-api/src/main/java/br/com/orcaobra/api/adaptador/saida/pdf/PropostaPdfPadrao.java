package br.com.orcaobra.api.adaptador.saida.pdf;

import br.com.orcaobra.dominio.proposta.DadosEmpresa;
import br.com.orcaobra.dominio.proposta.Proposta;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Layout padrão da proposta comercial. */
@Component
public class PropostaPdfPadrao extends PropostaPdfTemplate {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale PT_BR = Locale.of("pt", "BR");

    @Override
    protected void cabecalho(Document doc, Proposta p) throws DocumentException {
        DadosEmpresa e = p.empresa();
        PdfPTable t = new PdfPTable(new float[]{3f, 2f});
        t.setWidthPercentage(100);

        PdfPCell empresa = semBorda(new PdfPCell());
        empresa.addElement(new Paragraph(e.nome(), fonteNegrito(16, COR_PRIMARIA)));
        empresa.addElement(new Paragraph(texto(e.responsavelTecnico()) + "  ·  " + texto(e.registroProfissional()),
                fonte(9)));
        empresa.addElement(new Paragraph(texto(e.telefone()) + "  ·  " + texto(e.email()), fonte(9)));
        t.addCell(empresa);

        PdfPCell numero = semBorda(new PdfPCell());
        numero.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph titulo = new Paragraph("PROPOSTA COMERCIAL", fonteNegrito(13, COR_PRIMARIA));
        titulo.setAlignment(Element.ALIGN_RIGHT);
        Paragraph num = new Paragraph("Nº " + p.numero(), fonte(10));
        num.setAlignment(Element.ALIGN_RIGHT);
        Paragraph datas = new Paragraph("Emitida em " + p.dataEmissao().format(DATA)
                + "\nVálida até " + p.validaAte().format(DATA), fonte(9));
        datas.setAlignment(Element.ALIGN_RIGHT);
        numero.addElement(titulo);
        numero.addElement(num);
        numero.addElement(datas);
        t.addCell(numero);

        doc.add(t);
        doc.add(new Chunk(new LineSeparator(1.2f, 100, COR_PRIMARIA, Element.ALIGN_CENTER, -4)));
        doc.add(new Paragraph(" "));
    }

    @Override
    protected void dadosDoClienteEObra(Document doc, Proposta p) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{1f, 1f});
        t.setWidthPercentage(100);
        t.setSpacingAfter(12);

        PdfPCell cliente = caixa("CLIENTE");
        cliente.addElement(new Paragraph(p.cliente().nome(), fonteNegrito(10, Color.BLACK)));
        adicionarSeExistir(cliente, "CPF/CNPJ: ", p.cliente().documento());
        adicionarSeExistir(cliente, "Telefone: ", p.cliente().telefone());
        adicionarSeExistir(cliente, "E-mail: ", p.cliente().email());
        t.addCell(cliente);

        PdfPCell obra = caixa("OBRA");
        obra.addElement(new Paragraph(p.obra().descricao(), fonteNegrito(10, Color.BLACK)));
        adicionarSeExistir(obra, "Endereço: ", p.obra().endereco());
        adicionarSeExistir(obra, "Cidade: ", p.obra().cidade());
        if (p.obra().areaConstruidaM2() != null) {
            adicionarSeExistir(obra, "Área construída: ", numero(p.obra().areaConstruidaM2()) + " m²");
        }
        t.addCell(obra);
        doc.add(t);
    }

    @Override
    protected void tabelaDeServicos(Document doc, Proposta p) throws DocumentException {
        doc.add(new Paragraph("Serviços", fonteNegrito(12, COR_PRIMARIA)));
        PdfPTable t = new PdfPTable(new float[]{0.6f, 5f, 1.4f, 0.8f, 2f});
        t.setWidthPercentage(100);
        t.setSpacingBefore(6);
        t.setHeaderRows(1);
        for (String h : new String[]{"#", "Descrição", "Quantidade", "Un.", "Valor (R$)"}) {
            PdfPCell c = new PdfPCell(new Phrase(h, fonteNegrito(9, Color.WHITE)));
            c.setBackgroundColor(COR_PRIMARIA);
            c.setPadding(6);
            c.setBorderColor(COR_PRIMARIA);
            c.setHorizontalAlignment(h.equals("Descrição") ? Element.ALIGN_LEFT : Element.ALIGN_CENTER);
            t.addCell(c);
        }
        boolean zebra = false;
        for (Proposta.Item item : p.itens()) {
            Color fundo = zebra ? COR_CLARA : Color.WHITE;
            t.addCell(celula(String.valueOf(item.ordem()), Element.ALIGN_CENTER, fundo));
            t.addCell(celula(item.descricao(), Element.ALIGN_LEFT, fundo));
            t.addCell(celula(numero(item.quantidade()), Element.ALIGN_RIGHT, fundo));
            t.addCell(celula(item.unidade().simbolo(), Element.ALIGN_CENTER, fundo));
            t.addCell(celula(moeda(item.valor().valor()), Element.ALIGN_RIGHT, fundo));
            zebra = !zebra;
        }
        doc.add(t);
    }

    @Override
    protected void resumoDeValores(Document doc, Proposta p) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{3f, 2f});
        t.setWidthPercentage(50);
        t.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.setSpacingBefore(10);
        t.setSpacingAfter(14);

        PdfPCell rotulo = new PdfPCell(new Phrase("VALOR TOTAL", fonteNegrito(11, Color.WHITE)));
        rotulo.setBackgroundColor(COR_PRIMARIA);
        rotulo.setPadding(8);
        rotulo.setBorderColor(COR_PRIMARIA);
        t.addCell(rotulo);
        PdfPCell valor = new PdfPCell(new Phrase(p.valorTotal().formatado(), fonteNegrito(12, Color.WHITE)));
        valor.setBackgroundColor(COR_PRIMARIA);
        valor.setPadding(8);
        valor.setBorderColor(COR_PRIMARIA);
        valor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(valor);

        if (p.valorPorM2() != null) {
            t.addCell(celula("Equivale a (por m² construído)", Element.ALIGN_LEFT, COR_CLARA));
            t.addCell(celula(p.valorPorM2().formatado(), Element.ALIGN_RIGHT, COR_CLARA));
        }
        doc.add(t);
    }

    @Override
    protected void condicoesComerciais(Document doc, Proposta p) throws DocumentException {
        doc.add(new Paragraph("Condições comerciais", fonteNegrito(12, COR_PRIMARIA)));
        Paragraph c = new Paragraph();
        c.setSpacingBefore(4);
        c.setLeading(14);
        c.add(new Chunk("Forma de pagamento: ", fonteNegrito(10, Color.BLACK)));
        c.add(new Chunk(p.condicoesPagamento() + "\n", fonte(10)));
        c.add(new Chunk("Prazo de execução: ", fonteNegrito(10, Color.BLACK)));
        c.add(new Chunk(p.prazoExecucao() + "\n", fonte(10)));
        c.add(new Chunk("Validade da proposta: ", fonteNegrito(10, Color.BLACK)));
        c.add(new Chunk("até " + p.validaAte().format(DATA) + "\n", fonte(10)));
        c.add(new Chunk("Inclui: ", fonteNegrito(10, Color.BLACK)));
        c.add(new Chunk("materiais e mão de obra dos serviços listados, conforme quantitativos acima.\n", fonte(10)));
        if (p.observacoes() != null && !p.observacoes().isBlank()) {
            c.add(new Chunk("Observações: ", fonteNegrito(10, Color.BLACK)));
            c.add(new Chunk(p.observacoes() + "\n", fonte(10)));
        }
        doc.add(c);
    }

    @Override
    protected void assinatura(Document doc, Proposta p) throws DocumentException {
        Paragraph local = new Paragraph("\n" + texto(p.empresa().cidade()) + ", " + p.dataEmissao().format(DATA)
                + ".\n\n\n", fonte(10));
        doc.add(local);

        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.addCell(linhaAssinatura(p.empresa().responsavelTecnico(), texto(p.empresa().registroProfissional())));
        t.addCell(linhaAssinatura(p.cliente().nome(), "De acordo (cliente)"));
        doc.add(t);
    }

    // ------------------------------------------------------------------ auxiliares

    private static PdfPCell semBorda(PdfPCell c) {
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private static PdfPCell caixa(String titulo) {
        PdfPCell c = new PdfPCell();
        c.setBorderColor(COR_CLARA);
        c.setBackgroundColor(COR_CLARA);
        c.setPadding(8);
        c.addElement(new Paragraph(titulo, fonteNegrito(8, COR_TEXTO_SUAVE)));
        return c;
    }

    private static void adicionarSeExistir(PdfPCell c, String rotulo, String valor) {
        if (valor != null && !valor.isBlank()) {
            c.addElement(new Paragraph(rotulo + valor, fonte(9)));
        }
    }

    private static PdfPCell celula(String texto, int alinhamento, Color fundo) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fonte(9)));
        c.setHorizontalAlignment(alinhamento);
        c.setBackgroundColor(fundo);
        c.setBorderColor(new Color(0xD9, 0xDF, 0xE7));
        c.setPadding(5);
        return c;
    }

    private static PdfPCell linhaAssinatura(String nome, String detalhe) {
        PdfPCell c = semBorda(new PdfPCell());
        Paragraph linha = new Paragraph("_________________________________", fonte(10));
        linha.setAlignment(Element.ALIGN_CENTER);
        Paragraph n = new Paragraph(texto(nome), fonteNegrito(9, Color.BLACK));
        n.setAlignment(Element.ALIGN_CENTER);
        Paragraph d = new Paragraph(detalhe, fonte(8));
        d.setAlignment(Element.ALIGN_CENTER);
        c.addElement(linha);
        c.addElement(n);
        c.addElement(d);
        return c;
    }

    private static String numero(BigDecimal v) {
        NumberFormat f = NumberFormat.getNumberInstance(PT_BR);
        f.setMinimumFractionDigits(2);
        f.setMaximumFractionDigits(2);
        return f.format(v.setScale(2, RoundingMode.HALF_UP));
    }

    private static String moeda(BigDecimal v) {
        return numero(v);
    }

    private static String texto(String s) {
        return s == null ? "" : s;
    }
}
