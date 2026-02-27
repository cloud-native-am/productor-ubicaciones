package com.musabeli.productor_ubicaciones.services;

import com.musabeli.productor_ubicaciones.dtos.UbicacionVehiculoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class UbicacionService {

    private static final String TOPIC = "ubicaciones_vehiculos";
    private static final String[] PATENTES = {"BBBB12", "CCDD34", "FFGG56"};

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(fixedRate = 60000)
    public void publicarUbicacionAutomatica() {
        try {
            String patente = PATENTES[(int) (Math.random() * PATENTES.length)];

            UbicacionVehiculoDto dto = UbicacionVehiculoDto.builder()
                    .patente(patente)
                    .latitud(BigDecimal.valueOf(-33.45 + (Math.random() * 0.1)))
                    .longitud(BigDecimal.valueOf(-70.65 + (Math.random() * 0.1)))
                    .fechaActualizacion(LocalDateTime.now())
                    .build();

            String mensaje = objectMapper.writeValueAsString(dto);
            kafkaTemplate.send(TOPIC, dto.getPatente(), mensaje);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publicar(UbicacionVehiculoDto dto) {
        try {
            String mensaje = objectMapper.writeValueAsString(dto);
            kafkaTemplate.send(TOPIC, dto.getPatente(), mensaje);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
