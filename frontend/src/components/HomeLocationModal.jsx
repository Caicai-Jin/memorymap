import { useState } from 'react'
import { apiFetch } from '../api.js'

const inputClass =
  'rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/30'

function HomeLocationModal({ existingHome, onClose, onSaved }) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [searchError, setSearchError] = useState('')
  const [saveError, setSaveError] = useState('')
  const [saving, setSaving] = useState(false)

  async function search() {
    if (!query.trim()) return

    setSearchError('')
    const response = await apiFetch(`/locations/search?query=${encodeURIComponent(query)}`)

    if (!response.ok) {
      setSearchError('Search failed.')
      return
    }

    setResults(await response.json())
  }

  async function selectResult(result) {
    setSaveError('')
    setSaving(true)

    const response = await apiFetch(
      existingHome ? '/locations/home' : '/locations',
      {
        method: existingHome ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...result, type: 'HOME' }),
      },
    )

    setSaving(false)

    if (!response.ok) {
      setSaveError('Failed to save Home address.')
      return
    }

    const saved = await response.json()
    onSaved(saved)
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 px-4">
      <div className="w-full max-w-sm rounded-2xl border border-slate-200 bg-white p-6 shadow-lg">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-base font-semibold text-slate-900">
            {existingHome ? 'Change Home address' : 'Add Home address'}
          </h2>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close"
            className="text-slate-400 transition-colors hover:text-slate-600"
          >
            ×
          </button>
        </div>

        {existingHome && (
          <p className="mb-3 text-sm text-slate-500">
            Current: {existingHome.name}
            {existingHome.address ? ` (${existingHome.address})` : ''}
          </p>
        )}

        <div className="flex gap-2">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search your address..."
            className={`flex-1 ${inputClass}`}
          />
          <button
            type="button"
            onClick={search}
            className="rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-100"
          >
            Search
          </button>
        </div>

        {searchError && <p className="mt-2 text-sm text-red-600">{searchError}</p>}
        {saveError && <p className="mt-2 text-sm text-red-600">{saveError}</p>}

        {results.length > 0 && (
          <ul className="mt-3 max-h-56 overflow-y-auto rounded-lg border border-slate-200">
            {results.map((result, index) => (
              <li key={index} className="border-b border-slate-100 last:border-b-0">
                <button
                  type="button"
                  disabled={saving}
                  onClick={() => selectResult(result)}
                  className="w-full px-3 py-2 text-left text-sm text-slate-700 transition-colors hover:bg-slate-50 disabled:opacity-50"
                >
                  {result.name}
                  {result.address ? ` (${result.address})` : ''}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}

export default HomeLocationModal