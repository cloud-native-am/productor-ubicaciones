package com.musabeli.productor_ubicaciones.controllers;

import com.musabeli.productor_ubicaciones.dtos.UbicacionRequestDto;
import com.musabeli.productor_ubicaciones.dtos.UbicacionVehiculoDto;
import com.musabeli.productor_ubicaciones.services.UbicacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/ubicaciones")
public class UbicacionController {

    @Autowired
    private UbicacionService ubicacionService;

    @PostMapping
    public ResponseEntity<String> publicarUbicacion(@RequestBody UbicacionRequestDto request) {
        UbicacionVehiculoDto dto = UbicacionVehiculoDto.builder()
                .patente(request.getPatente())
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        ubicacionService.publicar(dto);
        return ResponseEntity.ok("Ubicacion publicada para patente: " + request.getPatente());
    }
}
