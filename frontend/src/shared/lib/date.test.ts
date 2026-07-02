import { describe, expect, it } from 'vitest'

import {
  addMonths,
  getCalendarMonth,
  getDateFromKey,
  getDateKey,
  getDayOffset,
  getMonthLabel,
  getReadableDate,
  isBeforeMonth,
} from './date'

describe('date utilities', () => {
  it('convierte una fecha a clave y la recupera correctamente', () => {
    const date = new Date(2026, 5, 25)

    expect(getDateKey(date)).toBe('2026-06-25')

    const parsedDate = getDateFromKey('2026-06-25')

    expect(getDateKey(parsedDate)).toBe('2026-06-25')
  })

  it('genera etiquetas legibles en espanol', () => {
    const date = new Date(2026, 5, 25)

    expect(getReadableDate(date)).toBe('25 junio')
    expect(getMonthLabel(date)).toBe('Junio')
  })

  it('construye correctamente un mes de calendario', () => {
    const calendarMonth = getCalendarMonth(
      new Date(2026, 0, 1),
    )

    expect(calendarMonth).toMatchObject({
      key: '2026-0',
      label: 'Enero',
      year: 2026,
      month: 0,
      startOffset: 4,
    })

    expect(calendarMonth.days).toHaveLength(31)
    expect(getDateKey(calendarMonth.days[0])).toBe(
      '2026-01-01',
    )
    expect(getDateKey(calendarMonth.days[30])).toBe(
      '2026-01-31',
    )
  })

  it('avanza meses incluso al cambiar de ano', () => {
    const result = addMonths(
      new Date(2026, 10, 1),
      2,
    )

    expect(getDateKey(result)).toBe('2027-01-01')
  })

  it('compara meses correctamente', () => {
    expect(
      isBeforeMonth(
        new Date(2026, 5, 1),
        new Date(2026, 6, 1),
      ),
    ).toBe(true)

    expect(
      isBeforeMonth(
        new Date(2026, 6, 1),
        new Date(2026, 6, 1),
      ),
    ).toBe(false)

    expect(
      isBeforeMonth(
        new Date(2027, 0, 1),
        new Date(2026, 11, 1),
      ),
    ).toBe(false)
  })

  it('calcula offsets de dias hacia adelante y hacia atras', () => {
    expect(
      getDayOffset('2026-06-25', '2026-06-25'),
    ).toBe(0)

    expect(
      getDayOffset('2026-06-28', '2026-06-25'),
    ).toBe(3)

    expect(
      getDayOffset('2026-06-23', '2026-06-25'),
    ).toBe(-2)
  })
})