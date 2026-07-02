import { ArrowIcon } from '../../../shared/icons/ArrowIcon'
import type { TripSearchSort } from '../model/trip-search'

type SearchFiltersProps = {
  isOpen: boolean
  origin: string
  destination: string
  availableTrips: number
  searchDateLabel: string
  minSeats: number
  sort: TripSearchSort
  verifiedOnly: boolean
  onSortChange: (sort: TripSearchSort) => void
  onVerifiedOnlyChange: (verifiedOnly: boolean) => void
  onMinSeatsChange: (minSeats: number) => void
}

const seatOptions = [1, 2, 3]

export function SearchFilters({
  isOpen,
  origin,
  destination,
  availableTrips,
  searchDateLabel,
  minSeats,
  sort,
  verifiedOnly,
  onSortChange,
  onVerifiedOnlyChange,
  onMinSeatsChange,
}: SearchFiltersProps) {
  return (
    <aside
      className={`filters-panel ${isOpen ? 'is-open' : ''}`}
      id="mobile-filters-panel"
      aria-label="Filtros de busqueda"
    >
      <div className="route-card">
        <strong className="route-title">
          <span>{origin}</span>
          <ArrowIcon />
          <span>{destination}</span>
        </strong>

        <small>
          {availableTrips} viajes disponibles · {searchDateLabel},{' '}
          {minSeats} plaza{minSeats > 1 ? 's' : ''}
        </small>
      </div>

      <div className="filter-group">
        <div className="filter-heading">
          <h2>Ordenar por</h2>
        </div>

        <button
          className={`filter-option ${
            sort === 'earliest' ? 'is-selected' : ''
          }`}
          type="button"
          onClick={() => onSortChange('earliest')}
          aria-pressed={sort === 'earliest'}
        >
          <span />
          Salida mas temprana
        </button>

        <button
          className={`filter-option ${
            sort === 'price' ? 'is-selected' : ''
          }`}
          type="button"
          onClick={() => onSortChange('price')}
          aria-pressed={sort === 'price'}
        >
          <span />
          Precio mas bajo
        </button>

        <button
          className={`filter-option ${
            sort === 'duration' ? 'is-selected' : ''
          }`}
          type="button"
          onClick={() => onSortChange('duration')}
          aria-pressed={sort === 'duration'}
        >
          <span />
          Viaje mas corto
        </button>
      </div>

      <div className="filter-group">
        <div className="filter-heading">
          <h2>Confianza</h2>
        </div>

        <label className="toggle-option">
          <input
            type="checkbox"
            checked={verifiedOnly}
            onChange={(event) =>
              onVerifiedOnlyChange(event.target.checked)
            }
          />
          <span>Solo conductores verificados</span>
        </label>
      </div>

      <div className="filter-group">
        <div className="filter-heading">
          <h2>Plazas</h2>
        </div>

        <div
          className="seat-controls"
          aria-label="Plazas minimas"
        >
          {seatOptions.map((seatCount) => (
            <button
              className={
                minSeats === seatCount
                  ? 'is-selected'
                  : undefined
              }
              type="button"
              onClick={() => onMinSeatsChange(seatCount)}
              aria-pressed={minSeats === seatCount}
              key={seatCount}
            >
              {seatCount}
            </button>
          ))}
        </div>
      </div>
    </aside>
  )
}