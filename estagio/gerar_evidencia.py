#!/usr/bin/env python3
"""
Gera a "foto" de registro de um dia de estágio a partir do histórico do Git:
commits do dia, arquivos alterados e, opcionalmente, o resultado dos testes.

Uso:
    pip install pillow
    python estagio/gerar_evidencia.py 2026-11-25
    python estagio/gerar_evidencia.py 2026-11-25 --extra "mvn verify: 35 testes, 0 falhas"

Saída: estagio/fotos/AAAA-MM-DD-commits.png
"""
from __future__ import annotations

import argparse
import subprocess
import sys
from datetime import date
from pathlib import Path

try:
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    sys.exit("Instale o Pillow: pip install pillow")

PASTA = Path(__file__).resolve().parent
RAIZ = PASTA.parent

FUNDO = (24, 30, 39)
BARRA = (37, 46, 59)
TEXTO = (222, 228, 236)
SUAVE = (139, 152, 168)
VERDE = (120, 200, 140)
AMARELO = (240, 190, 90)
AZUL = (120, 170, 230)
LARANJA = (224, 122, 31)


def fonte(tamanho: int, negrito: bool = False):
    candidatas = (["DejaVuSansMono-Bold.ttf", "consolab.ttf", "Menlo-Bold.ttf"] if negrito
                  else ["DejaVuSansMono.ttf", "consola.ttf", "Menlo.ttc"])
    pastas = ["", "/usr/share/fonts/truetype/dejavu/", "C:/Windows/Fonts/", "/System/Library/Fonts/"]
    for nome in candidatas:
        for pasta in pastas:
            try:
                return ImageFont.truetype(pasta + nome, tamanho)
            except OSError:
                continue
    return ImageFont.load_default(size=tamanho)


def git(*args: str) -> str:
    return subprocess.run(["git", *args], cwd=RAIZ, capture_output=True, text=True, encoding="utf-8").stdout.strip()


def coletar(dia: date):
    desde, ate = f"{dia} 00:00:00", f"{dia} 23:59:59"
    commits = [linha.split("|", 2) for linha in
               git("log", f"--since={desde}", f"--until={ate}", "--reverse", "--pretty=format:%h|%ad|%s",
                   "--date=format:%H:%M").splitlines() if linha]
    numstat = git("log", f"--since={desde}", f"--until={ate}", "--pretty=format:", "--numstat")
    arquivos, add, rem = set(), 0, 0
    for linha in numstat.splitlines():
        partes = linha.split("\t")
        if len(partes) == 3:
            arquivos.add(partes[2])
            add += int(partes[0]) if partes[0].isdigit() else 0
            rem += int(partes[1]) if partes[1].isdigit() else 0
    return commits, arquivos, add, rem


def desenhar(dia: date, commits, arquivos, add, rem, extras: list[str], destino: Path) -> None:
    f, fb, ft = fonte(17), fonte(17, True), fonte(22, True)
    largura, margem, altura_linha = 1280, 36, 30
    linhas: list[tuple[str, tuple, object]] = []

    linhas.append((f"$ git log --since=\"{dia}\" --oneline", SUAVE, f))
    for hash_, hora, assunto in commits:
        tipo, _, resto = assunto.partition(":")
        cor = VERDE if tipo.startswith("feat") else AZUL if tipo.startswith("docs") else AMARELO
        linhas.append((f"{hash_}  {hora}  {tipo}:{resto}"[:110], cor, f))
    if not commits:
        linhas.append(("(nenhum commit neste dia)", SUAVE, f))
    linhas.append(("", TEXTO, f))
    linhas.append((f"$ git diff --shortstat   # resumo do dia", SUAVE, f))
    linhas.append((f"{len(arquivos)} arquivos alterados, {add} inserções(+), {rem} remoções(-)", TEXTO, f))
    if extras:
        linhas.append(("", TEXTO, f))
        for e in extras:
            linhas.append((e, VERDE if "✔" in e or "sucesso" in e.lower() else TEXTO, fb))

    altura = 70 + margem + len(linhas) * altura_linha + margem
    img = Image.new("RGB", (largura, altura), FUNDO)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, largura, 56], fill=BARRA)
    for i, cor in enumerate([(237, 106, 94), (245, 191, 79), (98, 197, 84)]):
        d.ellipse([20 + i * 26, 20, 36 + i * 26, 36], fill=cor)
    d.text((110, 15), "OrçaObra", font=ft, fill=LARANJA)
    d.text((232, 18), f"—  registro de atividades de {dia:%d/%m/%Y}", font=f, fill=TEXTO)

    y = 70 + margem // 2
    for texto, cor, fnt in linhas:
        d.text((margem, y), texto, font=fnt, fill=cor)
        y += altura_linha

    destino.parent.mkdir(parents=True, exist_ok=True)
    img.save(destino)
    print(f"Gerado: {destino}")


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("data", help="dia no formato AAAA-MM-DD")
    ap.add_argument("--extra", action="append", default=[], help="linha extra (ex.: resultado dos testes)")
    ap.add_argument("--saida", help="arquivo PNG de saída")
    a = ap.parse_args()
    dia = date.fromisoformat(a.data)
    destino = Path(a.saida) if a.saida else PASTA / "fotos" / f"{dia}-commits.png"
    desenhar(dia, *coletar(dia), a.extra, destino)


if __name__ == "__main__":
    main()
