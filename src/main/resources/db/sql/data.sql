INSERT INTO categories (id, name) VALUES
    (1, 'Entrada'),
    (2, 'Plato fuerte'),
    (3, 'Postre'),
    (4, 'Bebida')
ON CONFLICT DO NOTHING;
