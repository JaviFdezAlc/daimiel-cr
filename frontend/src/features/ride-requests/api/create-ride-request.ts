export type CreateRideRequestRequest = {
  requestedSeats: number
  message: string | null
}

type CreateRideRequestResponse = {
  rideRequestId: string
}

type ApiErrorResponse = {
  code: string
  message: string
  fieldErrors?: Record<string, string>
}

export type CreateRideRequestParams = {
  tripId: string
  passengerId: string
  request: CreateRideRequestRequest
  signal?: AbortSignal
}

export type CreateRideRequestResult = {
  rideRequestId: string
}

export class CreateRideRequestError extends Error {
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

    this.name = 'CreateRideRequestError'
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

function isCreateRideRequestResponse(
  value: unknown,
): value is CreateRideRequestResponse {
  if (typeof value !== 'object' || value === null) {
    return false
  }

  const response = value as Record<string, unknown>

  return typeof response.rideRequestId === 'string'
}

async function toCreateRideRequestError(
  response: Response,
): Promise<CreateRideRequestError> {
  const body: unknown = await response.json().catch(() => null)

  if (isApiErrorResponse(body)) {
    return new CreateRideRequestError({
      status: response.status,
      code: body.code,
      message: body.message,
      fieldErrors: body.fieldErrors,
    })
  }

  return new CreateRideRequestError({
    status: response.status,
    code: 'CREATE_RIDE_REQUEST_FAILED',
    message:
      `No se pudo solicitar plaza (${response.status}).`,
  })
}

export async function createRideRequest({
  tripId,
  passengerId,
  request,
  signal,
}: CreateRideRequestParams): Promise<CreateRideRequestResult> {
  const requestInit: RequestInit = {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      'X-User-Id': passengerId,
    },
    body: JSON.stringify(request),
  }

  if (signal) {
    requestInit.signal = signal
  }

  let response: Response

  try {
    response = await fetch(
      getApiUrl(
        `/api/v1/trips/${tripId}/ride-requests`,
      ),
      requestInit,
    )
  } catch (error) {
    if (
      error instanceof Error &&
      error.name === 'AbortError'
    ) {
      throw error
    }

    throw new CreateRideRequestError({
      status: 0,
      code: 'NETWORK_ERROR',
      message:
        'No se ha podido conectar con el servicio de viajes.',
    })
  }

  if (!response.ok) {
    throw await toCreateRideRequestError(response)
  }

  const body: unknown = await response.json().catch(() => null)

  if (!isCreateRideRequestResponse(body)) {
    throw new CreateRideRequestError({
      status: response.status,
      code: 'INVALID_RIDE_REQUEST_RESPONSE',
      message:
        'El servicio ha devuelto una respuesta no valida.',
    })
  }

  return {
    rideRequestId: body.rideRequestId,
  }
}
