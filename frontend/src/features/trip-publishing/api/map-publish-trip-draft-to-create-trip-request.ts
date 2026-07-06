import type { TripLocation } from '../../../shared/model/trip-location'
import type { PublishTripDraft } from '../model/publish-trip-draft'
import type { CreateTripRequest } from './create-trip'

const tripLocationByLabel: Record<
  string,
  TripLocation
> = {
  Daimiel: 'DAIMIEL',
  'Ciudad Real': 'CIUDAD_REAL',
}

export class CreateTripRequestMappingError extends Error {
  constructor(message: string) {
    super(message)

    this.name = 'CreateTripRequestMappingError'
  }
}

function mapLocation(location: string): TripLocation {
  const mappedLocation =
    tripLocationByLabel[location.trim()]

  if (!mappedLocation) {
    throw new CreateTripRequestMappingError(
      'Solo se pueden publicar viajes entre Daimiel y Ciudad Real.',
    )
  }

  return mappedLocation
}

function mapContributionAmount(price: string): number {
  const normalizedPrice = price.trim().replace(',', '.')

  if (!/^\d+(\.\d{1,2})?$/.test(normalizedPrice)) {
    throw new CreateTripRequestMappingError(
      'El precio debe tener como maximo dos decimales.',
    )
  }

  const contributionAmount = Number(normalizedPrice)

  if (!Number.isFinite(contributionAmount)) {
    throw new CreateTripRequestMappingError(
      'El precio no es valido.',
    )
  }

  return contributionAmount
}

function mapDepartureAt(
  dateKey: string,
  time: string,
): string {
  const departureAt = new Date(
    `${dateKey}T${time}:00`,
  )

  if (Number.isNaN(departureAt.getTime())) {
    throw new CreateTripRequestMappingError(
      'La fecha u hora de salida no es valida.',
    )
  }

  return departureAt.toISOString()
}

function mapTripPoint(
  point: string,
  fieldName: string,
): string {
  const normalizedPoint = point.trim()

  if (!normalizedPoint) {
    throw new CreateTripRequestMappingError(
      `El ${fieldName} es obligatorio.`,
    )
  }

  if (normalizedPoint.length > 120) {
    throw new CreateTripRequestMappingError(
      `El ${fieldName} no puede superar los 120 caracteres.`,
    )
  }

  return normalizedPoint
}

export function mapPublishTripDraftToCreateTripRequest(
  draft: PublishTripDraft,
): CreateTripRequest {
  const origin = mapLocation(draft.origin)
  const destination = mapLocation(draft.destination)

  if (origin === destination) {
    throw new CreateTripRequestMappingError(
      'El origen y el destino deben ser distintos.',
    )
  }

  if (
    !Number.isInteger(draft.seats) ||
    draft.seats < 1
  ) {
    throw new CreateTripRequestMappingError(
      'Debes ofrecer al menos una plaza.',
    )
  }

  return {
    origin,
    destination,
    departureAt: mapDepartureAt(
      draft.dateKey,
      draft.time,
    ),
    totalSeats: draft.seats,
    contributionAmount: mapContributionAmount(
      draft.price,
    ),
    departurePoint: mapTripPoint(
      draft.departurePoint,
      'punto de salida',
    ),
    arrivalPoint: mapTripPoint(
      draft.arrivalPoint,
      'punto de llegada',
    ),
    comment: null,
  }
}
