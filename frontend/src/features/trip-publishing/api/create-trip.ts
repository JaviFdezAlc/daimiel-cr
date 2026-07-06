import type { TripLocation } from '../../../shared/model/trip-location'

export type CreateTripRequest = {
  origin: TripLocation
  destination: TripLocation
  departureAt: string
  totalSeats: number
  contributionAmount: number
  departurePoint: string
  arrivalPoint: string
  comment: string | null
}

type CreateTripResponse = {
  tripId: string
}

type ApiErrorResponse = {
  code: string
  message: string
  fieldErrors?: Record<string, string>
}

export type CreateTripResult = {
  tripId: string
}

export type CreateTripParams = {
  driverId: string
  request: CreateTripRequest
  signal?: AbortSignal
}

export class CreateTripError extends Error {
  readonly status: number
  readonly code: string
  readonly fieldErrors: Record<string, string>

  constructor({
    status,
    code,
    message,
    fieldErrors = {},
  }: {
    status: number
    code: string
    message: string
    fieldErrors?: Record<string, string>
  }) {
    super(message)

    this.name = 'CreateTripError'
    this.status = status
    this.code = code
    this.fieldErrors = fieldErrors
  }
}

function getApiUrl(path: string) {
  const baseUrl = (
    import.meta.env.VITE_API_BASE_URL ?? ''
  ).replace(/\/$/, '')

  return `${baseUrl}${path}`
}

function isApiErrorResponse(
  value: unknown,
): value is ApiErrorResponse {
  if (typeof value !== 'object' || value === null) {
    return false
  }

  const response = value as Record<string, unknown>

  return (
    typeof response.code === 'string' &&
    typeof response.message === 'string'
  )
}

function isCreateTripResponse(
  value: unknown,
): value is CreateTripResponse {
  if (typeof value !== 'object' || value === null) {
    return false
  }

  const response = value as Record<string, unknown>

  return typeof response.tripId === 'string'
}

async function toCreateTripError(
  response: Response,
): Promise<CreateTripError> {
  const body: unknown = await response.json().catch(() => null)

  if (isApiErrorResponse(body)) {
    return new CreateTripError({
      status: response.status,
      code: body.code,
      message: body.message,
      fieldErrors: body.fieldErrors,
    })
  }

  return new CreateTripError({
    status: response.status,
    code: 'CREATE_TRIP_REQUEST_FAILED',
    message: `No se pudo publicar el viaje (${response.status}).`,
  })
}

export async function createTrip({
  driverId,
  request,
  signal,
}: CreateTripParams): Promise<CreateTripResult> {
  const requestInit: RequestInit = {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      'X-User-Id': driverId,
    },
    body: JSON.stringify(request),
  }

  if (signal) {
    requestInit.signal = signal
  }

  let response: Response

  try {
    response = await fetch(
      getApiUrl('/api/v1/trips'),
      requestInit,
    )
  } catch (error) {
    if (
      error instanceof Error &&
      error.name === 'AbortError'
    ) {
      throw error
    }

    throw new CreateTripError({
      status: 0,
      code: 'NETWORK_ERROR',
      message:
        'No se ha podido conectar con el servicio de viajes.',
    })
  }

  if (!response.ok) {
    throw await toCreateTripError(response)
  }

  const body: unknown = await response.json().catch(() => null)

  if (!isCreateTripResponse(body)) {
    throw new CreateTripError({
      status: response.status,
      code: 'INVALID_CREATE_TRIP_RESPONSE',
      message:
        'El servicio ha devuelto una respuesta no valida.',
    })
  }

  return {
    tripId: body.tripId,
  }
}
