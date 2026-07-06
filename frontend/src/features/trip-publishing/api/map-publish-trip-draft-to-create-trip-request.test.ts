import { describe, expect, it } from 'vitest'

import {
  CreateTripRequestMappingError,
  mapPublishTripDraftToCreateTripRequest,
} from './map-publish-trip-draft-to-create-trip-request'

describe('mapPublishTripDraftToCreateTripRequest', () => {
  it('convierte el borrador del wizard al contrato del backend', () => {
    const result = mapPublishTripDraftToCreateTripRequest({
      origin: 'Daimiel',
      destination: 'Ciudad Real',
      date: '5 julio',
      dateKey: '2026-07-05',
      time: '07:30',
      seats: 3,
      price: '4,80',
    })

    expect(result).toEqual({
      origin: 'DAIMIEL',
      destination: 'CIUDAD_REAL',
      departureAt: new Date(
        2026,
        6,
        5,
        7,
        30,
        0,
      ).toISOString(),
      totalSeats: 3,
      contributionAmount: 4.8,
      departurePoint: 'Daimiel',
      arrivalPoint: 'Ciudad Real',
      comment: null,
    })
  })

  it('rechaza rutas fuera de Daimiel y Ciudad Real', () => {
    expect(() =>
      mapPublishTripDraftToCreateTripRequest({
        origin: 'Madrid',
        destination: 'Ciudad Real',
        date: '5 julio',
        dateKey: '2026-07-05',
        time: '07:30',
        seats: 2,
        price: '4,00',
      }),
    ).toThrow(CreateTripRequestMappingError)
  })

  it('rechaza origen y destino iguales', () => {
    expect(() =>
      mapPublishTripDraftToCreateTripRequest({
        origin: 'Daimiel',
        destination: 'Daimiel',
        date: '5 julio',
        dateKey: '2026-07-05',
        time: '07:30',
        seats: 2,
        price: '4,00',
      }),
    ).toThrow(
      'El origen y el destino deben ser distintos.',
    )
  })
})
