import {
  getDateKey,
  getReadableDate,
} from '../../../shared/lib/date'

export const publishSteps = [
  'Ruta',
  'Puntos',
  'Fecha',
  'Hora',
  'Plazas',
  'Precio',
  'Resumen',
] as const

export type PublishStep = (typeof publishSteps)[number]

export type PublishTripDraft = {
  origin: string
  destination: string
  departurePoint: string
  arrivalPoint: string
  date: string
  dateKey: string
  time: string
  seats: number
  price: string
}

export const publishPriceOptions = [
  '3,50',
  '4,00',
  '4,50',
  '5,00',
]

export function createInitialPublishTripDraft(
  today: Date,
): PublishTripDraft {
  return {
    origin: 'Daimiel',
    destination: 'Ciudad Real',
    departurePoint: 'Daimiel centro',
    arrivalPoint: 'Ciudad Real centro',
    date: getReadableDate(today),
    dateKey: getDateKey(today),
    time: '07:30',
    seats: 2,
    price: '4,00',
  }
}
