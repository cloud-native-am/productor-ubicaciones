package com.musabeli.productor_ubicaciones.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UbicacionVehiculoDto {
    private String patente;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private LocalDateTime fechaActualizacion;
}
