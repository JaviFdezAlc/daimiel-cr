export const publishSteps = [
  'Ruta',
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

export const initialPublishTripDraft: PublishTripDraft = {
  origin: 'Daimiel',
  destination: 'Ciudad Real',
  date: '25 junio',
  dateKey: '2026-06-25',
  time: '07:30',
  seats: 2,
  price: '4,00',
}