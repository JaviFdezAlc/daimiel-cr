export const weekDays = [
  'Dom',
  'Lun',
  'Mar',
  'Mie',
  'Jue',
  'Vie',
  'Sab',
]

export const monthNames = [
  'enero',
  'febrero',
  'marzo',
  'abril',
  'mayo',
  'junio',
  'julio',
  'agosto',
  'septiembre',
  'octubre',
  'noviembre',
  'diciembre',
]

export const getDateKey = (date: Date) =>
  `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(
    date.getDate(),
  ).padStart(2, '0')}`

export const getReadableDate = (date: Date) =>
  `${date.getDate()} ${monthNames[date.getMonth()]}`

export const getMonthLabel = (date: Date) => {
  const month = monthNames[date.getMonth()]

  return `${month.charAt(0).toUpperCase()}${month.slice(1)}`
}

export const getCalendarMonth = (date: Date) => {
  const year = date.getFullYear()
  const month = date.getMonth()
  const daysInMonth = new Date(year, month + 1, 0).getDate()

  return {
    key: `${year}-${month}`,
    label: getMonthLabel(date),
    year,
    month,
    startOffset: new Date(year, month, 1).getDay(),
    days: Array.from(
      { length: daysInMonth },
      (_, index) => new Date(year, month, index + 1),
    ),
  }
}

export const addMonths = (date: Date, amount: number) =>
  new Date(date.getFullYear(), date.getMonth() + amount, 1)

export const isBeforeMonth = (date: Date, compareDate: Date) =>
  date.getFullYear() < compareDate.getFullYear() ||
  (date.getFullYear() === compareDate.getFullYear() &&
    date.getMonth() < compareDate.getMonth())

export const getDateFromKey = (dateKey: string) => {
  const [year, month, day] = dateKey.split('-').map(Number)

  return new Date(year, month - 1, day)
}

export const getDayOffset = (
  dateKey: string,
  baseDateKey: string,
) => {
  const millisecondsPerDay = 24 * 60 * 60 * 1000

  return Math.round(
    (getDateFromKey(dateKey).getTime() -
      getDateFromKey(baseDateKey).getTime()) /
      millisecondsPerDay,
  )
}