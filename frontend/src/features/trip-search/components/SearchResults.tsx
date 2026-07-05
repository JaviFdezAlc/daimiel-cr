import { TripCard } from "./TripCard";
import type { SearchTripResult } from "../model/search-trip-result";

type SearchResultsProps = {
  trips: readonly SearchTripResult[];
  totalTrips: number;
  searchDateLabel: string;
  minSeats: number;
  isLoading: boolean;
  errorMessage: string | null;
  onRetry: () => void;
};

function CarIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false">
      <path d="m5 12 1.8-4.2A3 3 0 0 1 9.6 6h4.8a3 3 0 0 1 2.8 1.8L19 12" />
      <path d="M4 12h16v5H4z" />
      <circle cx="7" cy="17" r="2" />
      <circle cx="17" cy="17" r="2" />
    </svg>
  );
}

export function SearchResults({
  trips,
  searchDateLabel,
  minSeats,
  totalTrips,
  isLoading,
  errorMessage,
  onRetry,
}: SearchResultsProps) {
  return (
    <div className="results-column">
      <div className="results-header">
        <div className="results-summary">
          <strong>
            {isLoading
              ? "Buscando viajes..."
              : `${totalTrips} viajes disponibles`}
          </strong>
          <span>
            {searchDateLabel}, {minSeats} plaza
            {minSeats > 1 ? "s" : ""}
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

      <div className="trip-list" aria-live="polite">
        {isLoading ? (
          <div className="empty-results">
            <strong>Buscando viajes</strong>
            <span>Estamos consultando las salidas disponibles.</span>
          </div>
        ) : errorMessage ? (
          <div className="empty-results" role="alert">
            <strong>No se pudieron cargar los viajes</strong>
            <span>{errorMessage}</span>

            <button className="secondary-link" type="button" onClick={onRetry}>
              Reintentar
            </button>
          </div>
        ) : trips.length > 0 ? (
          trips.map((trip) => <TripCard key={trip.id} trip={trip} />)
        ) : (
          <div className="empty-results">
            <strong>No hay viajes con esos filtros</strong>
            <span>Prueba otra fecha o reduce el numero de pasajeros.</span>
          </div>
        )}
      </div>
    </div>
  );
}
