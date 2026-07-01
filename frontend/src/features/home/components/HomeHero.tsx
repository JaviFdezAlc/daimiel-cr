import { ArrowIcon } from '../../../shared/icons/ArrowIcon'

type HomeHeroProps = {
  isVisible: boolean
  onSearchTrips: () => void
  onPublishTrip: () => void
}

export function HomeHero({
  isVisible,
  onSearchTrips,
  onPublishTrip,
}: HomeHeroProps) {
  return (
    <div className="hero-content" aria-hidden={!isVisible}>
      <p className="eyebrow">Viajes compartidos locales</p>

      <h1 id="hero-title">
        Muevete entre Daimiel y Ciudad Real
      </h1>

      <div className="hero-actions" aria-label="Acciones principales">
        <button
          className="primary-link"
          type="button"
          onClick={onSearchTrips}
        >
          Buscar viaje
          <ArrowIcon />
        </button>

        <button
          className="secondary-link"
          type="button"
          onClick={onPublishTrip}
        >
          Publicar salida
        </button>
      </div>
    </div>
  )
}