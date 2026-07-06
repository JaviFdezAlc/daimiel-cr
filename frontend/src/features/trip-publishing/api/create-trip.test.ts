import {
  afterEach,
  describe,
  expect,
  it,
  vi,
} from 'vitest'

import {
  createTrip,
} from './create-trip'

const request = {
  origin: 'DAIMIEL' as const,
  destination: 'CIUDAD_REAL' as const,
  departureAt: '2026-07-05T05:30:00.000Z',
  totalSeats: 3,
  contributionAmount: 4.8,
  departurePoint: 'Daimiel',
  arrivalPoint: 'Ciudad Real',
  comment: null,
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('createTrip', () => {
  it('envia el viaje al backend y devuelve su identificador', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(
        JSON.stringify({
          tripId:
            'a2c2bd57-4a60-4b9d-b9de-9e76f909b676',
        }),
        { status: 201 },
      ),
    )

    vi.stubGlobal('fetch', fetchMock)

    await expect(
      createTrip({
        driverId:
          '11111111-1111-4111-8111-111111111111',
        request,
      }),
    ).resolves.toEqual({
      tripId:
        'a2c2bd57-4a60-4b9d-b9de-9e76f909b676',
    })

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/v1/trips',
      {
        method: 'POST',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
          'X-User-Id':
            '11111111-1111-4111-8111-111111111111',
        },
        body: JSON.stringify(request),
      },
    )
  })

  it('conserva el error devuelto por el backend', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            status: 403,
            code: 'PHONE_NOT_VERIFIED',
            message:
              'Debes verificar tu teléfono antes de publicar un viaje.',
            fieldErrors: {},
          }),
          { status: 403 },
        ),
      ),
    )

    await expect(
      createTrip({
        driverId:
          '11111111-1111-4111-8111-111111111111',
        request,
      }),
    ).rejects.toMatchObject({
      name: 'CreateTripError',
      status: 403,
      code: 'PHONE_NOT_VERIFIED',
    })
  })
})
