package com.musabeli.productor_ubicaciones.dtos;

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
    private Double latitud;
    private Double longitud;
    private LocalDateTime fechaActualizacion;
}
