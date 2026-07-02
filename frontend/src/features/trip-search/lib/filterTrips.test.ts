import { describe, expect, it } from 'vitest'

import type { TripSearchItem } from '../model/trip-search'
import { filterTrips } from './filterTrips'

function createTrip(
  overrides: Partial<TripSearchItem> = {},
): TripSearchItem {
  return {
    id: 1,
    departureTime: '08:00',
    arrivalTime: '08:30',
    duration: '30 min',
    durationMinutes: 30,
    from: 'Daimiel',
    to: 'Ciudad Real',
    driver: 'Lucia',
    driverAvatarUrl: '',
    rating: '5,0',
    price: 4,
    priceLabel: '4,00 EUR',
    seats: 2,
    verified: true,
    dayOffsets: [0],
    tags: [],
    ...overrides,
  }
}

describe('filterTrips', () => {
  it('filtra por fecha y numero minimo de plazas', () => {
    const availableTrip = createTrip({
      id: 1,
      seats: 2,
      dayOffsets: [0],
    })

    const insufficientSeatsTrip = createTrip({
      id: 2,
      seats: 1,
      dayOffsets: [0],
    })

    const differentDayTrip = createTrip({
      id: 3,
      seats: 4,
      dayOffsets: [1],
    })

    const result = filterTrips({
      trips: [
        availableTrip,
        insufficientSeatsTrip,
        differentDayTrip,
      ],
      dayOffset: 0,
      minSeats: 2,
      verifiedOnly: false,
      sort: 'earliest',
    })

    expect(result).toEqual([availableTrip])
  })

  it('muestra solo conductores verificados cuando se solicita', () => {
    const verifiedTrip = createTrip({
      id: 1,
      verified: true,
    })

    const unverifiedTrip = createTrip({
      id: 2,
      verified: false,
    })

    const result = filterTrips({
      trips: [verifiedTrip, unverifiedTrip],
      dayOffset: 0,
      minSeats: 1,
      verifiedOnly: true,
      sort: 'earliest',
    })

    expect(result).toEqual([verifiedTrip])
  })

  it('ordena los viajes por salida mas temprana', () => {
    const result = filterTrips({
      trips: [
        createTrip({ id: 1, departureTime: '09:00' }),
        createTrip({ id: 2, departureTime: '07:00' }),
        createTrip({ id: 3, departureTime: '08:00' }),
      ],
      dayOffset: 0,
      minSeats: 1,
      verifiedOnly: false,
      sort: 'earliest',
    })

    expect(result.map((trip) => trip.id)).toEqual([2, 3, 1])
  })

  it('ordena los viajes por precio mas bajo', () => {
    const result = filterTrips({
      trips: [
        createTrip({ id: 1, price: 4.5 }),
        createTrip({ id: 2, price: 5 }),
        createTrip({ id: 3, price: 3.5 }),
      ],
      dayOffset: 0,
      minSeats: 1,
      verifiedOnly: false,
      sort: 'price',
    })

    expect(result.map((trip) => trip.id)).toEqual([3, 1, 2])
  })

  it('ordena los viajes por menor duracion sin mutar la lista original', () => {
    const trips = [
      createTrip({ id: 1, durationMinutes: 30 }),
      createTrip({ id: 2, durationMinutes: 45 }),
      createTrip({ id: 3, durationMinutes: 35 }),
    ]

    const result = filterTrips({
      trips,
      dayOffset: 0,
      minSeats: 1,
      verifiedOnly: false,
      sort: 'duration',
    })

    expect(result.map((trip) => trip.id)).toEqual([1, 3, 2])
    expect(trips.map((trip) => trip.id)).toEqual([1, 2, 3])
  })
})