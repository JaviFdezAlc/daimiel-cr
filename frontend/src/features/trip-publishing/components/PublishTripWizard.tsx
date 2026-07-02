import type { CSSProperties } from 'react'

import { ArrowIcon } from '../../../shared/icons/ArrowIcon'
import { getDateKey } from '../../../shared/lib/date'
import { usePublishTripWizard } from '../hooks/usePublishTripWizard'
import { publishSteps } from '../model/publish-trip-draft'
import { PublishDateStep } from './PublishDateStep'
import { PublishPriceStep } from './PublishPriceStep'
import { PublishRouteStep } from './PublishRouteStep'
import { PublishSeatsStep } from './PublishSeatsStep'
import { PublishSummaryStep } from './PublishSummaryStep'
import { PublishTimeStep } from './PublishTimeStep'

type PublishTripWizardProps = {
  isVisible: boolean
  today: Date
  onReturnHome: () => void
}

export function PublishTripWizard({
  isVisible,
  today,
  onReturnHome,
}: PublishTripWizardProps) {
  const publishMinimumDateKey = getDateKey(today)

  const {
    publishStep,
    isPublishComplete,
    publishDraft,
    currentPublishStep,
    isPublishSummaryStep,
    visibleCalendarMonths,
    canGoToPreviousCalendarMonth,
    publishProgress,
    updatePublishDraft,
    selectPublishDate,
    swapRoute,
    goToNextPublishStep,
    goToPreviousPublishStep,
    resetPublishWizard,
    goToPreviousCalendarMonth,
    goToNextCalendarMonth,
  } = usePublishTripWizard({ today })

  return (
    <section
      className="publish-view"
      aria-label="Publicar viaje"
      aria-hidden={!isVisible}
    >
      <div className="publish-card">
        {isPublishComplete ? (
          <div className="publish-success">
            <span className="publish-kicker">
              Viaje preparado
            </span>

            <h2>Tu salida esta lista para publicarse</h2>

            <p>
              Hemos guardado el borrador de {publishDraft.origin} a{' '}
              {publishDraft.destination}. En una version real aqui se
              enviaria al backend.
            </p>

            <div className="publish-summary-card">
              <div>
                <span>Ruta</span>
                <strong>
                  {publishDraft.origin} <ArrowIcon />{' '}
                  {publishDraft.destination}
                </strong>
              </div>

              <div>
                <span>Fecha y hora</span>
                <strong>
                  {publishDraft.date}, {publishDraft.time}
                </strong>
              </div>

              <div>
                <span>Plazas</span>
                <strong>{publishDraft.seats}</strong>
              </div>

              <div>
                <span>Precio</span>
                <strong>{publishDraft.price} EUR</strong>
              </div>
            </div>

            <div className="publish-actions">
              <button
                className="secondary-link"
                type="button"
                onClick={resetPublishWizard}
              >
                Crear otro viaje
              </button>

              <button
                className="primary-link"
                type="button"
                onClick={onReturnHome}
              >
                Volver al inicio
              </button>
            </div>
          </div>
        ) : (
          <>
            <div className="publish-step-shell" key={publishStep}>
              <div
                className={`publish-copy ${
                  isPublishSummaryStep ? 'is-summary' : ''
                }`}
              >
                <h2>
                  {currentPublishStep === 'Ruta' &&
                    'Define la ruta del viaje'}
                  {currentPublishStep === 'Fecha' &&
                    'Elige el dia de salida'}
                  {currentPublishStep === 'Hora' &&
                    'A que hora sales'}
                  {currentPublishStep === 'Plazas' &&
                    'Cuantas plazas ofreces'}
                  {currentPublishStep === 'Precio' &&
                    'Pon precio por plaza'}
                  {currentPublishStep === 'Resumen' &&
                    'Viaje listo para publicar'}
                </h2>

                <p>
                  {currentPublishStep === 'Ruta' &&
                    'Usa la ruta habitual o ajustala antes de continuar.'}
                  {currentPublishStep === 'Hora' &&
                    'Selecciona la hora de salida.'}
                  {currentPublishStep === 'Plazas' &&
                    'Indica cuantas personas pueden reservar tu coche.'}
                  {currentPublishStep === 'Precio' &&
                    'El precio se mostrara a los pasajeros en la busqueda.'}
                  {currentPublishStep === 'Resumen' &&
                    'Comprueba los datos antes de hacer visible tu salida.'}
                </p>
              </div>

              <div className="publish-step-body">
                {currentPublishStep === 'Ruta' && (
                  <PublishRouteStep
                    origin={publishDraft.origin}
                    destination={publishDraft.destination}
                    onOriginChange={(origin) =>
                      updatePublishDraft('origin', origin)
                    }
                    onDestinationChange={(destination) =>
                      updatePublishDraft('destination', destination)
                    }
                    onSwapRoute={swapRoute}
                  />
                )}

                {currentPublishStep === 'Fecha' && (
                  <PublishDateStep
                    selectedDateKey={publishDraft.dateKey}
                    selectedDateLabel={publishDraft.date}
                    visibleMonths={visibleCalendarMonths}
                    canGoToPreviousMonth={
                      canGoToPreviousCalendarMonth
                    }
                    minimumDateKey={publishMinimumDateKey}
                    onPreviousMonth={goToPreviousCalendarMonth}
                    onNextMonth={goToNextCalendarMonth}
                    onSelectDate={selectPublishDate}
                  />
                )}

                {currentPublishStep === 'Hora' && (
                  <PublishTimeStep
                    time={publishDraft.time}
                    onTimeChange={(time) =>
                      updatePublishDraft('time', time)
                    }
                  />
                )}

                {currentPublishStep === 'Plazas' && (
                  <PublishSeatsStep
                    seats={publishDraft.seats}
                    onSeatsChange={(seats) =>
                      updatePublishDraft('seats', seats)
                    }
                  />
                )}

                {currentPublishStep === 'Precio' && (
                  <PublishPriceStep
                    price={publishDraft.price}
                    onPriceChange={(price) =>
                      updatePublishDraft('price', price)
                    }
                  />
                )}

                {currentPublishStep === 'Resumen' && (
                  <PublishSummaryStep draft={publishDraft} />
                )}
              </div>
            </div>

            <div className="publish-actions">
              <button
                className="secondary-link"
                type="button"
                onClick={goToPreviousPublishStep}
                disabled={publishStep === 0}
              >
                Atras
              </button>

              <button
                className="primary-link"
                type="button"
                onClick={goToNextPublishStep}
              >
                {isPublishSummaryStep
                  ? 'Publicar viaje'
                  : 'Continuar'}
                <ArrowIcon />
              </button>
            </div>

            <div
              className="publish-mobile-progress"
              style={
                {
                  '--publish-progress': publishProgress,
                } as CSSProperties
              }
              aria-label={`Paso ${publishStep + 1} de ${
                publishSteps.length
              }: ${currentPublishStep}`}
            >
              <span>
                Paso {publishStep + 1} de {publishSteps.length}
              </span>
              <i aria-hidden="true" />
            </div>

            <div
              className="publish-progress"
              aria-label="Progreso de publicacion"
            >
              {publishSteps.map((step, index) => (
                <div
                  className={`publish-progress-item ${
                    index < publishStep ? 'is-complete' : ''
                  } ${index === publishStep ? 'is-current' : ''}`}
                  key={step}
                >
                  <span aria-label={`${step}, paso ${index + 1}`}>
                    {index + 1}
                  </span>

                  {index < publishSteps.length - 1 && (
                    <i aria-hidden="true" />
                  )}
                </div>
              ))}
            </div>
          </>
        )}
      </div>
    </section>
  )
}
