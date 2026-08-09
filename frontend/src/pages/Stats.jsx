import { useEffect, useState } from 'react'
import { apiFetch } from '../api.js'
import Navbar from '../components/Navbar.jsx'
import HomeLocationModal from '../components/HomeLocationModal.jsx'

function PinIcon() {
  return (
    <svg viewBox="0 0 20 20" fill="currentColor" className="h-3.5 w-3.5 shrink-0">
      <path
        fillRule="evenodd"
        d="M10 18s6-5.686 6-10a6 6 0 10-12 0c0 4.314 6 10 6 10zm0-7a3 3 0 100-6 3 3 0 000 6z"
        clipRule="evenodd"
      />
    </svg>
  )
}

function LocationRow({ label, location }) {
  return (
    <div className="flex items-center justify-between border-b border-slate-100 py-2.5 last:border-b-0">
      <span className="text-sm text-slate-600">{label}</span>
      <span className="flex items-center gap-1.5 text-sm font-medium text-slate-900">
        {location && <PinIcon />}
        {location
          ? `${location.name}${location.address ? ` (${location.address})` : ''}`
          : 'N/A'}
      </span>
    </div>
  )
}

function Stats() {
  const [year, setYear] = useState(new Date().getFullYear())
  const [stats, setStats] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const [homeLocation, setHomeLocation] = useState(null)
  const [showHomeModal, setShowHomeModal] = useState(false)

  async function loadHomeLocation() {
    const response = await apiFetch('/locations/home')
    if (response.ok) {
      setHomeLocation(await response.json())
    }
  }

  function handleHomeSaved(saved) {
    setHomeLocation(saved)
    setShowHomeModal(false)
  }

  async function loadStats(y) {
    setLoading(true)
    setError('')
    const response = await apiFetch(`/stats/${y}`)

    if (!response.ok) {
      setError('Failed to load stats.')
      setStats(null)
      setLoading(false)
      return
    }

    const data = await response.json()
    setStats(data)
    setLoading(false)
  }

  useEffect(() => {
    loadStats(year)
  }, [year])

  useEffect(() => {
    loadHomeLocation()
  }, [])

  const moodEntries = Object.entries(stats?.moodBreakdown ?? {})
  const maxCount = Math.max(1, ...moodEntries.map(([, count]) => count))

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <div className="mx-auto max-w-2xl px-6 py-8">
        <div className="mb-6 flex items-center justify-between rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
          <span className="text-sm font-medium text-slate-700">Year</span>
          <input
            type="number"
            value={year}
            onChange={(e) => setYear(Number(e.target.value))}
            className="w-28 rounded-lg border border-slate-300 px-3 py-1.5 text-right text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/30"
          />
        </div>

        <div className="mb-6 flex items-center justify-between rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
          <div>
            <p className="text-sm font-medium text-slate-700">Home address</p>
            <p className="text-sm text-slate-500">
              {homeLocation
                ? `${homeLocation.name}${homeLocation.address ? ` (${homeLocation.address})` : ''}`
                : 'Not set'}
            </p>
          </div>
          <button
            type="button"
            onClick={() => setShowHomeModal(true)}
            className="text-sm font-medium text-indigo-600 hover:underline"
          >
            {homeLocation ? 'Change' : 'Add'}
          </button>
        </div>

        {loading && <p className="text-center text-sm text-slate-500">Loading...</p>}
        {error && <p className="text-center text-sm text-red-600">{error}</p>}

        {!loading && !error && stats && (
          <div className="flex flex-col gap-4">
            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
              <p className="text-sm text-slate-500">Dominant mood</p>
              <p className="mt-1 text-2xl font-semibold tracking-tight text-slate-900">
                {stats.dominantMood ?? 'N/A'}
              </p>
            </div>

            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
              <p className="text-sm font-semibold text-slate-900">Mood breakdown</p>
              {moodEntries.length === 0 ? (
                <p className="mt-2 text-sm text-slate-500">No moments this year.</p>
              ) : (
                <div className="mt-4 flex flex-col gap-3">
                  {moodEntries.map(([mood, count]) => (
                    <div key={mood}>
                      <div className="mb-1 flex items-center justify-between text-sm">
                        <span className="font-medium text-slate-700">{mood}</span>
                        <span className="text-slate-500">{count}</span>
                      </div>
                      <div className="h-2 rounded-full bg-slate-100">
                        <div
                          className="h-2 rounded-full bg-indigo-500"
                          style={{ width: `${(count / maxCount) * 100}%` }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
              <p className="mb-1 text-sm font-semibold text-slate-900">Locations</p>
              <LocationRow label="Happiest" location={stats.happiestLocation} />
              <LocationRow label="Saddest" location={stats.saddestLocation} />
              <LocationRow
                label="Most emotionally diverse"
                location={stats.mostEmotionallyDiverseLocation}
              />
            </div>
          </div>
        )}
      </div>
      {showHomeModal && (
        <HomeLocationModal
          existingHome={homeLocation}
          onClose={() => setShowHomeModal(false)}
          onSaved={handleHomeSaved}
        />
      )}
    </div>
  )
}

export default Stats
