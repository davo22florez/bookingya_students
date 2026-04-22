package com.project.bookingya.bdd.steps;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.bookingya.models.Reservation;
import com.project.bookingya.services.ReservationService;

import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservationStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationService reservationService;

    private String requestBody;
    private ResultActions response;

    @Before
    public void setup() {
        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setGuestId(UUID.randomUUID());
        reservation.setRoomId(UUID.randomUUID());
        reservation.setCheckIn(LocalDateTime.now().plusDays(2));
        reservation.setCheckOut(LocalDateTime.now().plusDays(5));
        reservation.setGuestsCount(2);
        reservation.setNotes("Generada desde BDD");

        when(reservationService.create(any())).thenReturn(reservation);
    }

    @Dado("existe una solicitud de reserva válida")
    public void existeUnaSolicitudDeReservaValida() throws Exception {
        UUID guestId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();

        requestBody = objectMapper.writeValueAsString(new ReservationRequest(
            guestId,
            roomId,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(3),
            2,
            "Reserva desde Cucumber"
        ));
    }

    @Cuando("el cliente envía la creación de la reserva")
    public void elClienteEnviaLaCreacionDeLaReserva() throws Exception {
        response = mockMvc.perform(post("/api/reservation")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody));
    }

    @Entonces("el sistema responde con código 200")
    public void elSistemaRespondeConCodigo() throws Exception {
        response.andExpect(status().isOk());
    }

    @Entonces("el cuerpo de respuesta contiene el identificador de la reserva")
    public void elCuerpoDeRespuestaContieneElIdentificadorDeLaReserva() throws Exception {
        response.andExpect(jsonPath("$.id", notNullValue()));
    }

    private record ReservationRequest(
        UUID guestId,
        UUID roomId,
        LocalDateTime checkIn,
        LocalDateTime checkOut,
        Integer guestsCount,
        String notes
    ) {
    }
}
