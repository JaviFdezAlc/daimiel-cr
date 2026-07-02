import { ArrowIcon } from '../../../shared/icons/ArrowIcon'
import {
  getDateKey,
  getReadableDate,
  weekDays,
  type CalendarMonth,
} from '../../../shared/lib/date'

type PublishDateStepProps = {
  selectedDateKey: string
  selectedDateLabel: string
  visibleMonths: CalendarMonth[]
  canGoToPreviousMonth: boolean
  minimumDateKey: string
  onPreviousMonth: () => void
  onNextMonth: () => void
  onSelectDate: (
    dateKey: string,
    dateLabel: string,
  ) => void
}

export function PublishDateStep({
  selectedDateKey,
  selectedDateLabel,
  visibleMonths,
  canGoToPreviousMonth,
  minimumDateKey,
  onPreviousMonth,
  onNextMonth,
  onSelectDate,
}: PublishDateStepProps) {
  return (
    <div className="publish-calendar-step">
      <div className="calendar-toolbar">
        <button
          type="button"
          onClick={onPreviousMonth}
          disabled={!canGoToPreviousMonth}
          aria-label="Mostrar mes anterior"
        >
          <ArrowIcon />
        </button>

        <span>{selectedDateLabel}</span>

        <button
          type="button"
          onClick={onNextMonth}
          aria-label="Mostrar mes siguiente"
        >
          <ArrowIcon />
        </button>
      </div>

      <div className="calendar-months">
        {visibleMonths.map((month, monthIndex) => (
          <div
            className={`calendar-month month-${monthIndex + 1}`}
            key={month.key}
          >
            <h3>{month.label}</h3>

            <div className="calendar-grid is-weekdays">
              {weekDays.map((weekDay) => (
                <span key={weekDay}>{weekDay}</span>
              ))}
            </div>

            <div className="calendar-grid">
              {Array.from(
                { length: month.startOffset },
                (_, index) => (
                  <span
                    className="calendar-empty"
                    key={`${month.key}-empty-${index}`}
                  />
                ),
              )}

              {month.days.map((dayDate) => {
                const dateKey = getDateKey(dayDate)
                const dateLabel = getReadableDate(dayDate)
                const isPast = dateKey < minimumDateKey

                return (
                  <button
                    className={
                      selectedDateKey === dateKey
                        ? 'is-selected'
                        : undefined
                    }
                    type="button"
                    onClick={() =>
                      onSelectDate(dateKey, dateLabel)
                    }
                    aria-pressed={
                      selectedDateKey === dateKey
                    }
                    disabled={isPast}
                    key={dateKey}
                  >
                    {dayDate.getDate()}
                  </button>
                )
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}