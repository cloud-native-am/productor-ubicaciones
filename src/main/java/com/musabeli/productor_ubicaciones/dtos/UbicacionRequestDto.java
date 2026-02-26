package com.musabeli.productor_ubicaciones.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UbicacionRequestDto {
    private String patente;
    private BigDecimal latitud;
    private BigDecimal longitud;
}
