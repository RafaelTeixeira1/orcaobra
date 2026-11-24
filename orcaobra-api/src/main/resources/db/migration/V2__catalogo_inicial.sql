-- Preços de referência (exemplo, região de Goiás). Atualize pela tela de Catálogo.
INSERT INTO material (codigo, descricao, unidade, preco_unitario) VALUES
    ('CIMENTO_CP2_50KG',       'Cimento CP II 50 kg',              'SC', 38.00),
    ('AREIA_MEDIA',            'Areia média lavada',               'M3', 140.00),
    ('BRITA_1',                'Brita 1',                          'M3', 150.00),
    ('BLOCO_CERAMICO_9X19X19', 'Bloco cerâmico 9x19x19 cm',        'UN', 1.10),
    ('ARGAMASSA_AC1_20KG',     'Argamassa colante AC-I 20 kg',     'SC', 22.00),
    ('REJUNTE_1KG',            'Rejunte cimentício',               'KG', 8.00),
    ('PISO_CERAMICO',          'Piso cerâmico PEI-4',              'M2', 45.00),
    ('TINTA_ACRILICA',         'Tinta acrílica fosca',             'L', 22.00),
    ('SELADOR_ACRILICO',       'Selador acrílico',                 'L', 12.00);

INSERT INTO funcao_mao_de_obra (codigo, descricao, valor_hora) VALUES
    ('PEDREIRO', 'Pedreiro (com encargos)', 25.00),
    ('SERVENTE', 'Servente (com encargos)', 16.00),
    ('PINTOR',   'Pintor (com encargos)',   25.00);
