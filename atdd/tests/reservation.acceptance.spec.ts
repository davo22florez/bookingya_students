import { test, expect } from '@playwright/test';

test.describe('ATDD - Gestión de reservas', () => {
  test('debe crear y consultar reservas por usuario según criterios de aceptación', async ({ request }) => {
    const guestId = '11111111-1111-1111-1111-111111111111';
    const roomId = '22222222-2222-2222-2222-222222222222';

    const creationResponse = await request.post('/reservation', {
      data: {
        guestId,
        roomId,
        checkIn: '2026-06-01T14:00:00',
        checkOut: '2026-06-05T12:00:00',
        guestsCount: 2,
        notes: 'Reserva de aceptación'
      }
    });

    expect(creationResponse.ok()).toBeTruthy();

    const reservation = await creationResponse.json();
    expect(reservation.id).toBeTruthy();

    const byIdResponse = await request.get(`/reservation/${reservation.id}`);
    expect(byIdResponse.ok()).toBeTruthy();

    const byGuestResponse = await request.get(`/reservation/guest/${guestId}`);
    expect(byGuestResponse.ok()).toBeTruthy();
  });
});
