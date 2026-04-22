package com.project.bookingya.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.entities.GuestEntity;
import com.project.bookingya.entities.ReservationEntity;
import com.project.bookingya.entities.RoomEntity;
import com.project.bookingya.exceptions.EntityNotExistsException;
import com.project.bookingya.models.Reservation;
import com.project.bookingya.repositories.IGuestRepository;
import com.project.bookingya.repositories.IReservationRepository;
import com.project.bookingya.repositories.IRoomRepository;
import com.project.bookingya.shared.Constants;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private IReservationRepository reservationRepository;

    @Mock
    private IRoomRepository roomRepository;

    @Mock
    private IGuestRepository guestRepository;

    private final ModelMapper mapper = new ModelMapper();

    @InjectMocks
    private ReservationService reservationService;

    private UUID reservationId;
    private UUID roomId;
    private UUID guestId;
    private ReservationDto reservationDto;

    @BeforeEach
    void setUp() {
        reservationId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        guestId = UUID.randomUUID();

        reservationDto = new ReservationDto();
        reservationDto.setGuestId(guestId);
        reservationDto.setRoomId(roomId);
        reservationDto.setCheckIn(LocalDateTime.now().plusDays(1));
        reservationDto.setCheckOut(LocalDateTime.now().plusDays(2));
        reservationDto.setGuestsCount(2);
        reservationDto.setNotes("Reserva de prueba");
    }

    @Test
    void shouldCreateReservationSuccessfully() {
        RoomEntity room = validRoom(roomId);
        GuestEntity guest = new GuestEntity();
        guest.setId(guestId);

        ReservationEntity savedEntity = mapper.map(reservationDto, ReservationEntity.class);
        savedEntity.setId(reservationId);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));
        when(reservationRepository.existsOverlappingReservationForRoom(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.saveAndFlush(any(ReservationEntity.class))).thenReturn(savedEntity);

        Reservation result = reservationService.create(reservationDto);

        assertNotNull(result.getId());
        assertEquals(guestId, result.getGuestId());
        verify(reservationRepository).saveAndFlush(any(ReservationEntity.class));
    }

    @Test
    void shouldReturnAllReservations() {
        ReservationEntity reservationEntity = mapper.map(reservationDto, ReservationEntity.class);
        reservationEntity.setId(reservationId);

        when(reservationRepository.findAll()).thenReturn(List.of(reservationEntity));

        List<Reservation> reservations = reservationService.getAll();

        assertEquals(1, reservations.size());
        assertEquals(reservationId, reservations.get(0).getId());
    }

    @Test
    void shouldUpdateReservationSuccessfully() {
        RoomEntity room = validRoom(roomId);
        GuestEntity guest = new GuestEntity();
        guest.setId(guestId);

        ReservationEntity existing = mapper.map(reservationDto, ReservationEntity.class);
        existing.setId(reservationId);

        ReservationDto updatedDto = new ReservationDto();
        updatedDto.setGuestId(guestId);
        updatedDto.setRoomId(roomId);
        updatedDto.setCheckIn(LocalDateTime.now().plusDays(5));
        updatedDto.setCheckOut(LocalDateTime.now().plusDays(6));
        updatedDto.setGuestsCount(1);
        updatedDto.setNotes("Actualizada");

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(existing));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));
        when(reservationRepository.existsOverlappingReservationForRoom(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.saveAndFlush(existing)).thenReturn(existing);

        Reservation updated = reservationService.update(updatedDto, reservationId);

        assertEquals("Actualizada", updated.getNotes());
        verify(reservationRepository).saveAndFlush(existing);
    }

    @Test
    void shouldDeleteReservationSuccessfully() {
        ReservationEntity existing = mapper.map(reservationDto, ReservationEntity.class);
        existing.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(existing));

        reservationService.delete(reservationId);

        verify(reservationRepository).delete(existing);
        verify(reservationRepository).flush();
    }

    @Test
    void shouldGetReservationById() {
        ReservationEntity existing = mapper.map(reservationDto, ReservationEntity.class);
        existing.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(existing));

        Reservation result = reservationService.getById(reservationId);

        assertEquals(reservationId, result.getId());
    }

    @Test
    void shouldThrowWhenReservationNotFoundById() {
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        EntityNotExistsException exception = assertThrows(EntityNotExistsException.class,
            () -> reservationService.getById(reservationId));

        assertSame(Constants.RESERVATION_NOT_FOUND, exception.getMessage());
        verify(reservationRepository, never()).saveAndFlush(any());
    }

    private RoomEntity validRoom(UUID id) {
        RoomEntity room = new RoomEntity();
        room.setId(id);
        room.setAvailable(true);
        room.setMaxGuests(4);
        return room;
    }
}
