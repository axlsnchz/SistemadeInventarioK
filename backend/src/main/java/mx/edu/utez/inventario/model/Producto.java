package mx.edu.utez.inventario.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true)
    private String sku;

    @Column(nullable = false)
    private BigDecimal precio;

    private int stock;
    private int stockMinimo;
    private String categoria;
    private String fotoUrl;

    @Column(unique = true)
    private String nfcId;
}