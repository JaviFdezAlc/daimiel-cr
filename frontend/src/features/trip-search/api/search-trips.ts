import { mapSearchTripsResponse } from './map-search-trips-response'
import type {
  SearchTripsResponse,
  TripLocationResponse,
} from './search-trips-response'
import type { SearchTripsPage } from '../model/search-trip-result'
import type { TripSearchSort } from '../model/trip-search'

type BackendTripSort =
  | 'DEPARTURE_ASC'
  | 'CONTRIBUTION_ASC'
  | 'AVAILABLE_SEATS_DESC'

export type SearchTripsParams = {
  origin: TripLocationResponse
  destination: TripLocationResponse
  date?: string
  requiredSeats?: number
  sort?: TripSearchSort
  page?: number
  size?: number
  signal?: AbortSignal
}

type ApiErrorResponse = {
  code: string
  message: string
  fieldErrors?: Record<string, string>
}

const backendSortBySearchSort: Record<
  TripSearchSort,
  BackendTripSort
> = {
  earliest: 'DEPARTURE_ASC',
  price: 'CONTRIBUTION_ASC',
  seats: 'AVAILABLE_SEATS_DESC',
}

export class SearchTripsError extends Error {
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

    this.name = 'SearchTripsError'
    this.status = status
    this.code = code
    this.fieldErrors = fieldErrors
  }
}

export function buildSearchTripsPath({
  origin,
  destination,
  date,
  requiredSeats = 1,
  sort = 'earliest',
  page = 0,
  size = 20,
}: SearchTripsParams) {
  const query = new URLSearchParams()

  query.set('origin', origin)
  query.set('destination', destination)

  if (date) {
    query.set('date', date)
  }

  query.set('requiredSeats', String(requiredSeats))
  query.set('sort', backendSortBySearchSort[sort])
  query.set('page', String(page))
  query.set('size', String(size))

  return `/api/v1/trips?${query.toString()}`
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

async function toSearchTripsError(
  response: Response,
): Promise<SearchTripsError> {
  const body: unknown = await response.json().catch(() => null)

  if (isApiErrorResponse(body)) {
    return new SearchTripsError({
      status: response.status,
      code: body.code,
      message: body.message,
      fieldErrors: body.fieldErrors,
    })
  }

  return new SearchTripsError({
    status: response.status,
    code: 'SEARCH_TRIPS_REQUEST_FAILED',
    message: `No se pudieron cargar los viajes (${response.status}).`,
  })
}

export async function searchTrips(
  params: SearchTripsParams,
): Promise<SearchTripsPage> {
  const requestInit: RequestInit = {
    headers: {
      Accept: 'application/json',
    },
  }

  if (params.signal) {
    requestInit.signal = params.signal
  }

  let response: Response

  try {
    response = await fetch(
      getApiUrl(buildSearchTripsPath(params)),
      requestInit,
    )
  } catch (error) {
    if (
      error instanceof Error &&
      error.name === 'AbortError'
    ) {
      throw error
    }

    throw new SearchTripsError({
      status: 0,
      code: 'NETWORK_ERROR',
      message:
        'No se ha podido conectar con el servicio de viajes.',
    })
  }

  if (!response.ok) {
    throw await toSearchTripsError(response)
  }

  try {
    const body =
      (await response.json()) as SearchTripsResponse

    return mapSearchTripsResponse(body)
  } catch {
    throw new SearchTripsError({
      status: response.status,
      code: 'INVALID_SEARCH_TRIPS_RESPONSE',
      message:
        'El servicio ha devuelto una respuesta de viajes no valida.',
    })
  }
}