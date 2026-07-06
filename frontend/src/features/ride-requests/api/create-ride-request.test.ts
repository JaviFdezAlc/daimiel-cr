import {
  afterEach,
  describe,
  expect,
  it,
  vi,
} from 'vitest'

import { createRideRequest } from './create-ride-request'

const tripId =
  'a2c2bd57-4a60-4b9d-b9de-9e76f909b676'

const passengerId =
  '22222222-2222-4222-8222-222222222222'

const request = {
  requestedSeats: 2,
  message: 'Hola, me gustaria reservar dos plazas.',
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('createRideRequest', () => {
  it('envia una solicitud de plaza y devuelve su identificador', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(
        JSON.stringify({
          rideRequestId:
            '7b04c5c4-6b0d-48cf-bbdd-e56ee2d2600d',
        }),
        { status: 201 },
      ),
    )

    vi.stubGlobal('fetch', fetchMock)

    await expect(
      createRideRequest({
        tripId,
        passengerId,
        request,
      }),
    ).resolves.toEqual({
      rideRequestId:
        '7b04c5c4-6b0d-48cf-bbdd-e56ee2d2600d',
    })

    expect(fetchMock).toHaveBeenCalledWith(
      `/api/v1/trips/${tripId}/ride-requests`,
      {
        method: 'POST',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
          'X-User-Id': passengerId,
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
            status: 409,
            code: 'RIDE_REQUEST_CONFLICT',
            message:
              'No hay plazas suficientes para esta solicitud.',
            fieldErrors: {},
          }),
          { status: 409 },
        ),
      ),
    )

    await expect(
      createRideRequest({
        tripId,
        passengerId,
        request,
      }),
    ).rejects.toMatchObject({
      name: 'CreateRideRequestError',
      status: 409,
      code: 'RIDE_REQUEST_CONFLICT',
      message:
        'No hay plazas suficientes para esta solicitud.',
    })
  })

  it('convierte un fallo de red en un error controlado', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockRejectedValue(
        new TypeError('Failed to fetch'),
      ),
    )

    await expect(
      createRideRequest({
        tripId,
        passengerId,
        request,
      }),
    ).rejects.toMatchObject({
      name: 'CreateRideRequestError',
      status: 0,
      code: 'NETWORK_ERROR',
    })
  })
})
