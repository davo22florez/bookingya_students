Feature: Gestion de reservas
  Como usuario de la plataforma
  Quiero crear una reserva
  Para asegurar un alojamiento en una fecha específica

  Scenario: Crear una reserva válida
    Given existe una solicitud de reserva válida
    When el cliente envía la creación de la reserva
    Then el sistema responde con código 200
    And el cuerpo de respuesta contiene el identificador de la reserva
