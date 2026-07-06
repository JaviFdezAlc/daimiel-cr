import { ArrowIcon } from '../../../shared/icons/ArrowIcon'
import type { PublishTripDraft } from '../model/publish-trip-draft'

type PublishSummaryStepProps = {
  draft: PublishTripDraft
}

export function PublishSummaryStep({
  draft,
}: PublishSummaryStepProps) {
  return (
    <div className="publish-review-card">
      <div className="review-route">
        <span className="review-eyebrow">Trayecto</span>

        <div className="review-route-line">
          <div className="review-place">
            <small>Salida · {draft.departurePoint}</small>
            <strong>{draft.origin}</strong>
          </div>

          <span
            className="review-route-track"
            aria-hidden="true"
          >
            <i />
            <ArrowIcon />
          </span>

          <div className="review-place">
            <small>Llegada · {draft.arrivalPoint}</small>
            <strong>{draft.destination}</strong>
          </div>
        </div>
      </div>

      <div
        className="review-details"
        aria-label="Detalles del viaje"
      >
        <div>
          <span>Dia</span>
          <strong>{draft.date}</strong>
        </div>

        <div>
          <span>Hora</span>
          <strong>{draft.time}</strong>
        </div>

        <div>
          <span>Plazas</span>
          <strong>
            {draft.seats} plaza
            {draft.seats > 1 ? 's' : ''}
          </strong>
        </div>

        <div>
          <span>Precio</span>
          <strong>{draft.price} EUR</strong>
        </div>
      </div>
    </div>
  )
}
