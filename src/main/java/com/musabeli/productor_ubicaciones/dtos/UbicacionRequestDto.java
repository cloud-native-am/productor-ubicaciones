package com.musabeli.productor_ubicaciones.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UbicacionRequestDto {
    private String patente;
    private Double latitud;
    private Double longitud;
}
