type Sponsor = {
  name: string
}

const sponsors: Sponsor[] = [
  { name: 'Plaza Mayor' },
  { name: 'Taller El Carmen' },
  { name: 'Cervantes' },
  { name: 'La Vega' },
  { name: 'Autoescuela Daimiel' },
  { name: 'Casa Azuer' },
  { name: 'Tablas Cafe' },
  { name: 'Ruta 430' },
]

export const sponsorLoop = [...sponsors, ...sponsors]