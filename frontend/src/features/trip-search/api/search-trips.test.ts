import {
  afterEach,
  describe,
  expect,
  it,
  vi,
} from 'vitest'

import {
  buildSearchTripsPath,
  searchTrips,
} from './search-trips'
import type { SearchTripsParams } from './search-trips'

const baseParams: SearchTripsParams = {
  origin: 'DAIMIEL',
  destination: 'CIUDAD_REAL',
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('buildSearchTripsPath', () => {
  it('traduce los filtros del frontend a los query params del backend', () => {
    expect(
      buildSearchTripsPath({
        ...baseParams,
        date: '2026-07-05',
        requiredSeats: 3,
        sort: 'price',
        page: 1,
        size: 10,
      }),
    ).toBe(
      '/api/v1/trips?origin=DAIMIEL&destination=CIUDAD_REAL&date=2026-07-05&requiredSeats=3&sort=CONTRIBUTION_ASC&page=1&size=10',
    )
  })

  it('usa los valores por defecto del endpoint', () => {
    expect(buildSearchTripsPath(baseParams)).toBe(
      '/api/v1/trips?origin=DAIMIEL&destination=CIUDAD_REAL&requiredSeats=1&sort=DEPARTURE_ASC&page=0&size=20',
    )
  })
})

describe('searchTrips', () => {
  it('mapea una respuesta correcta del backend', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(
        JSON.stringify({
          trips: [],
          page: 0,
          size: 20,
          totalElements: 0,
          totalPages: 0,
        }),
        { status: 200 },
      ),
    )

    vi.stubGlobal('fetch', fetchMock)

    await expect(searchTrips(baseParams)).resolves.toEqual({
      trips: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    })

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/v1/trips?origin=DAIMIEL&destination=CIUDAD_REAL&requiredSeats=1&sort=DEPARTURE_ASC&page=0&size=20',
      {
        headers: {
          Accept: 'application/json',
        },
      },
    )
  })

  it('conserva los detalles de un error del backend', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            status: 400,
            code: 'VALIDATION_ERROR',
            message: 'Las plazas deben ser validas.',
            fieldErrors: {
              requiredSeats: 'Debe ser mayor que cero.',
            },
          }),
          { status: 400 },
        ),
      ),
    )

    await expect(searchTrips(baseParams)).rejects.toMatchObject({
      name: 'SearchTripsError',
      status: 400,
      code: 'VALIDATION_ERROR',
      message: 'Las plazas deben ser validas.',
      fieldErrors: {
        requiredSeats: 'Debe ser mayor que cero.',
      },
    })
  })

  it('convierte un fallo de red en un error controlado', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockRejectedValue(
        new TypeError('Failed to fetch'),
      ),
    )

    await expect(searchTrips(baseParams)).rejects.toMatchObject({
      name: 'SearchTripsError',
      status: 0,
      code: 'NETWORK_ERROR',
    })
  })
})