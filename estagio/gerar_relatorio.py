#!/usr/bin/env python3
"""
Preenche o modelo oficial "Relatório Diário de Atividades" do estágio a partir de registro.yaml.

Uso:
    pip install python-docx pyyaml
    python estagio/gerar_relatorio.py

Saída: estagio/saida/Relatorio-Diario-Estagio.docx (e .pdf, se o LibreOffice estiver instalado).
"""
from __future__ import annotations

import copy
import re
import shutil
import subprocess
import sys
from datetime import date
from pathlib import Path

try:
    import yaml
    from docx import Document
    from docx.shared import Cm, Pt
    from docx.enum.text import WD_ALIGN_PARAGRAPH
except ImportError:
    sys.exit("Instale as dependências: pip install python-docx pyyaml")

PASTA = Path(__file__).resolve().parent
MODELO = PASTA / "modelo" / "relatorio-diario-modelo.docx"
SAIDA = PASTA / "saida"

ENTRADA = re.compile(r"^\s*\d{2}\.\s*_*/")       # "01. ___/___/____"
LINHA_EM_BRANCO = re.compile(r"^_{10,}\s*$")      # "__________________"
MESES = ["janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho", "agosto",
         "setembro", "outubro", "novembro", "dezembro"]


def definir_texto(paragrafo, texto: str) -> None:
    """Troca o texto mantendo a formatação do primeiro trecho (fonte, tamanho)."""
    if not paragrafo.runs:
        paragrafo.add_run(texto)
        return
    paragrafo.runs[0].text = texto
    for run in paragrafo.runs[1:]:
        run.text = ""


def inserir_paragrafo_depois(paragrafo):
    novo = copy.deepcopy(paragrafo._p)
    for filho in list(novo):
        if filho.tag.endswith("}r"):
            novo.remove(filho)
    paragrafo._p.addnext(novo)
    from docx.text.paragraph import Paragraph
    return Paragraph(novo, paragrafo._parent)


def preencher_cabecalho(doc, cab: dict) -> None:
    campos = {
        "ESTAGIÁRIO:": cab.get("estagiario"),
        "CURSO:": cab.get("curso"),
        "CONCEDENTE:": cab.get("concedente"),
        "ENDEREÇO:": cab.get("endereco"),
        "CIDADE:": cab.get("cidade"),
        "ESTADO:": cab.get("estado"),
        "SUPERVISOR:": cab.get("supervisor"),
        "PERÍODO DO ESTÁGIO:": cab.get("periodo"),
    }
    for p in doc.paragraphs:
        for rotulo, valor in campos.items():
            if p.text.strip().startswith(rotulo) and valor:
                definir_texto(p, f"{rotulo} ")
                run = p.add_run(str(valor))
                run.bold = False
                break
        if p.text.strip() == "Escreva aqui o Nome do Supervisor" and cab.get("supervisor"):
            definir_texto(p, cab["supervisor"])
        if p.text.strip() == "Escreva aqui o Nome do estagiário" and cab.get("estagiario"):
            definir_texto(p, cab["estagiario"])


def texto_do_dia(reg: dict, opcoes: dict) -> str:
    d: date = reg["data"]
    descricao = " ".join(str(reg["descricao"]).split())
    texto = f"{d:%d/%m/%Y} – {descricao}"
    if opcoes.get("mostrar_numero_das_atividades", True) and reg.get("atividades"):
        nums = ", ".join(str(n) for n in reg["atividades"])
        texto += f" (Ativ. {nums})"
    texto += f" – {formatar_horas(reg['horas'])}."
    return texto


def formatar_horas(h) -> str:
    h = float(h)
    return f"{int(h)} h" if h.is_integer() else f"{h:.1f} h".replace(".", ",")


def preencher_registros(doc, registros: list[dict], opcoes: dict) -> float:
    paragrafos = doc.paragraphs
    indices = [i for i, p in enumerate(paragrafos) if ENTRADA.match(p.text)]
    if len(registros) > len(indices):
        sys.exit(f"O modelo tem {len(indices)} linhas e o registro tem {len(registros)} dias.")

    largura = Cm(float(opcoes.get("largura_foto_cm", 13)))
    total = 0.0
    for n, idx in enumerate(indices, start=1):
        p = paragrafos[idx]
        fim = indices[n] if n < len(indices) else len(paragrafos)
        continuacoes = [paragrafos[j] for j in range(idx + 1, fim) if LINHA_EM_BRANCO.match(paragrafos[j].text)]

        if n > len(registros):
            # linha sem registro: só corrige a numeração do modelo (que repete "01.")
            definir_texto(p, re.sub(r"^\s*\d{2}\.", f"{n:02d}.", p.text, count=1))
            continue

        reg = registros[n - 1]
        total += float(reg["horas"])
        definir_texto(p, f"{n:02d}. {texto_do_dia(reg, opcoes)}")
        p.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY

        ancora = continuacoes[0] if continuacoes else p
        for extra in continuacoes:
            definir_texto(extra, "")
        for k, foto in enumerate(reg.get("fotos") or []):
            caminho = (PASTA / foto["arquivo"]).resolve()
            if not caminho.exists():
                print(f"  aviso: foto não encontrada, ignorada: {caminho}")
                continue
            alvo = ancora if (k == 0 and continuacoes) else inserir_paragrafo_depois(ancora)
            definir_texto(alvo, "")
            alvo.alignment = WD_ALIGN_PARAGRAPH.CENTER
            alvo.paragraph_format.space_before = Pt(6)
            alvo.add_run().add_picture(str(caminho), width=largura)
            legenda = inserir_paragrafo_depois(alvo)
            legenda.alignment = WD_ALIGN_PARAGRAPH.CENTER
            r = legenda.add_run(f"Foto {n}.{k + 1} – {foto.get('legenda', '')}")
            r.italic = True
            r.font.size = Pt(9)
            ancora = legenda
    return total


def preencher_total(doc, total: float) -> None:
    for p in doc.paragraphs:
        if p.text.strip().startswith("Carga horária total"):
            definir_texto(p, f"Carga horária total: {formatar_horas(total)}")


def main() -> None:
    registro = yaml.safe_load((PASTA / "registro.yaml").read_text(encoding="utf-8"))
    registros = sorted(registro.get("registros") or [], key=lambda r: r["data"])
    opcoes = registro.get("opcoes") or {}

    doc = Document(str(MODELO))
    preencher_cabecalho(doc, registro.get("cabecalho") or {})
    total = preencher_registros(doc, registros, opcoes)
    preencher_total(doc, total)

    SAIDA.mkdir(exist_ok=True)
    destino = SAIDA / "Relatorio-Diario-Estagio.docx"
    doc.save(str(destino))
    print(f"Gerado: {destino}  ({len(registros)} dia(s), {formatar_horas(total)})")

    soffice = shutil.which("soffice") or shutil.which("libreoffice")
    if soffice:
        subprocess.run([soffice, "--headless", "--convert-to", "pdf", "--outdir", str(SAIDA), str(destino)],
                       check=False, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        pdf = destino.with_suffix(".pdf")
        if pdf.exists():
            print(f"Gerado: {pdf}")


if __name__ == "__main__":
    main()
