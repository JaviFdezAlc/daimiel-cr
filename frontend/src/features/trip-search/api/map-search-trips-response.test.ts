import { describe, expect, it } from 'vitest'

import { mapSearchTripsResponse } from './map-search-trips-response'
import type { SearchTripsResponse } from './search-trips-response'

describe('mapSearchTripsResponse', () => {
  it('traduce y formatea una pagina de viajes del backend', () => {
    const response: SearchTripsResponse = {
      trips: [
        {
          id: 'a2c2bd57-4a60-4b9d-b9de-9e76f909b676',
          origin: 'DAIMIEL',
          destination: 'CIUDAD_REAL',
          departureAt: '2026-06-25T05:30:00Z',
          departurePoint: 'Plaza de Espana',
          arrivalPoint: 'Campus Universitario',
          availableSeats: 3,
          contributionAmount: 4.8,
        },
      ],
      page: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1,
    }

    expect(mapSearchTripsResponse(response)).toEqual({
      trips: [
        {
          id: 'a2c2bd57-4a60-4b9d-b9de-9e76f909b676',
          origin: 'Daimiel',
          destination: 'Ciudad Real',
          departureTime: '07:30',
          departurePoint: 'Plaza de Espana',
          arrivalPoint: 'Campus Universitario',
          availableSeats: 3,
          contributionLabel: '4,80 €',
        },
      ],
      page: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1,
    })
  })

  it('conserva los metadatos de paginacion', () => {
    const response: SearchTripsResponse = {
      trips: [],
      page: 2,
      size: 10,
      totalElements: 27,
      totalPages: 3,
    }

    expect(mapSearchTripsResponse(response)).toEqual({
      trips: [],
      page: 2,
      size: 10,
      totalElements: 27,
      totalPages: 3,
    })
  })
})
