import type { RefObject } from 'react'
import {
  ArrowRightLeft,
  Calendar,
  UserRound,
} from 'lucide-react'

import { ArrowIcon } from '../../../shared/icons/ArrowIcon'

type SearchControlsProps = {
  origin: string
  destination: string
  searchDate: string
  searchDateLabel: string
  searchDateShortLabel: string
  minimumDateKey: string
  minSeats: number
  isMobileFiltersOpen: boolean
  isPassengerPickerOpen: boolean
  searchDateInputRef: RefObject<HTMLInputElement | null>
  onToggleRoute: () => void
  onToggleMobileFilters: () => void
  onSearchDateChange: (date: string) => void
  onPassengerCountChange: (amount: number) => void
  onOpenSearchDatePicker: () => void
  onTogglePassengerPicker: () => void
  onClosePassengerPicker: () => void
}

function SearchIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false">
      <circle cx="11" cy="11" r="7" />
      <path d="m16 16 4 4" />
    </svg>
  )
}

export function SearchControls({
  origin,
  destination,
  searchDate,
  searchDateLabel,
  searchDateShortLabel,
  minimumDateKey,
  minSeats,
  isMobileFiltersOpen,
  isPassengerPickerOpen,
  searchDateInputRef,
  onToggleRoute,
  onToggleMobileFilters,
  onSearchDateChange,
  onPassengerCountChange,
  onOpenSearchDatePicker,
  onTogglePassengerPicker,
  onClosePassengerPicker,
}: SearchControlsProps) {
  return (
    <>
      <div className="mobile-search-summary" role="search">
        <SearchIcon />

        <button
          className="mobile-route-summary"
          type="button"
          onClick={onToggleRoute}
          aria-label="Cambiar origen y destino"
        >
          <strong>
            <span>{origin}</span>
            <ArrowIcon />
            <span>{destination}</span>
          </strong>

          <span>
            {searchDateLabel}, {minSeats} plaza
            {minSeats > 1 ? 's' : ''}
          </span>
        </button>

        <button
          className="mobile-filter-toggle"
          type="button"
          onClick={onToggleMobileFilters}
          aria-expanded={isMobileFiltersOpen}
          aria-controls="mobile-filters-panel"
        >
          Filtrar
        </button>

        <div className="mobile-search-controls">
          <label className="mobile-date-control">
            <span>Fecha</span>
            <input
              type="date"
              min={minimumDateKey}
              value={searchDate}
              onChange={(event) =>
                onSearchDateChange(event.target.value)
              }
              aria-label="Fecha del viaje"
            />
          </label>

          <div
            className="mobile-passenger-control"
            aria-label="Pasajeros"
          >
            <button
              type="button"
              onClick={() => onPassengerCountChange(-1)}
              disabled={minSeats === 1}
              aria-label="Quitar un pasajero"
            >
              -
            </button>

            <span>{minSeats}</span>

            <button
              type="button"
              onClick={() => onPassengerCountChange(1)}
              disabled={minSeats === 4}
              aria-label="Anadir un pasajero"
            >
              +
            </button>
          </div>
        </div>
      </div>

      <div className="search-bar" role="search">
        <div className="search-field">
          <span>Origen</span>
          <strong>{origin}</strong>
        </div>

        <button
          className="swap-route"
          type="button"
          onClick={onToggleRoute}
          aria-label="Cambiar origen y destino"
        >
          <ArrowRightLeft aria-hidden="true" />
        </button>

        <div className="search-field">
          <span>Destino</span>
          <strong>{destination}</strong>
        </div>

        <label
          className="search-field search-date-field is-small"
          onClick={onOpenSearchDatePicker}
        >
          <span>Fecha</span>

          <strong className="search-field-value">
            {searchDateShortLabel}
            <Calendar aria-hidden="true" />
          </strong>

          <input
            ref={searchDateInputRef}
            type="date"
            min={minimumDateKey}
            value={searchDate}
            onChange={(event) =>
              onSearchDateChange(event.target.value)
            }
            aria-label="Fecha del viaje"
          />
        </label>

        <div className="search-field passenger-search-field is-small">
          <span>Pasajeros</span>

          <button
            className="passenger-display"
            type="button"
            onClick={onTogglePassengerPicker}
            aria-expanded={isPassengerPickerOpen}
            aria-controls="passenger-picker"
          >
            <UserRound aria-hidden="true" />
            <strong>{minSeats}</strong>
          </button>

          <div
            className={`passenger-popover ${
              isPassengerPickerOpen ? 'is-open' : ''
            }`}
            id="passenger-picker"
          >
            <button
              type="button"
              onClick={() => onPassengerCountChange(-1)}
              disabled={minSeats === 1}
              aria-label="Quitar un pasajero"
            >
              -
            </button>

            <strong>{minSeats}</strong>

            <button
              type="button"
              onClick={() => onPassengerCountChange(1)}
              disabled={minSeats === 4}
              aria-label="Anadir un pasajero"
            >
              +
            </button>
          </div>
        </div>

        <button
          className="search-submit"
          type="button"
          onClick={onClosePassengerPicker}
        >
          Buscar
        </button>
      </div>
    </>
  )
}