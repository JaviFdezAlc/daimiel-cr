import { TripCard } from './TripCard'
import type { TripSearchItem } from '../model/trip-search'

type SearchResultsProps = {
  trips: readonly TripSearchItem[]
  searchDateLabel: string
  minSeats: number
  isRouteReversed: boolean
}

function CarIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false">
      <path d="m5 12 1.8-4.2A3 3 0 0 1 9.6 6h4.8a3 3 0 0 1 2.8 1.8L19 12" />
      <path d="M4 12h16v5H4z" />
      <circle cx="7" cy="17" r="2" />
      <circle cx="17" cy="17" r="2" />
    </svg>
  )
}

export function SearchResults({
  trips,
  searchDateLabel,
  minSeats,
  isRouteReversed,
}: SearchResultsProps) {
  return (
    <div className="results-column">
      <div className="results-header">
        <div className="results-summary">
          <strong>{trips.length} viajes disponibles</strong>
          <span>
            {searchDateLabel}, {minSeats} plaza
            {minSeats > 1 ? 's' : ''}
          </span>
        </div>

        <div className="results-tabs" aria-label="Tipos de viaje">
          <button className="is-selected" type="button">
            Todo
          </button>

          <button type="button">
            <CarIcon />
            Coche
          </button>
        </div>
      </div>

      <div className="trip-list">
        {trips.length > 0 ? (
          trips.map((trip) => (
            <TripCard
              key={trip.id}
              trip={trip}
              isRouteReversed={isRouteReversed}
            />
          ))
        ) : (
          <div className="empty-results">
            <strong>No hay viajes con esos filtros</strong>
            <span>
              Prueba otra fecha o reduce el numero de pasajeros.
            </span>
          </div>
        )}
      </div>
    </div>
  )
}