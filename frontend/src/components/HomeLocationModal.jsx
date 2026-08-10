import { useState } from 'react'
import { apiFetch } from '../api.js'
import LocationMapPicker from './LocationMapPicker.jsx'

const inputClass =
  'rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/30'

function HomeLocationModal({ existingHome, onClose, onSaved }) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [searchError, setSearchError] = useState('')
  const [saveError, setSaveError] = useState('')
  const [saving, setSaving] = useState(false)
  const [draft, setDraft] = useState(null)

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

  function pickResult(result) {
    setDraft({ name: result.name, address: result.address, latitude: result.latitude, longitude: result.longitude })
    setResults([])
    setQuery('')
  }

  // For addresses OpenStreetMap doesn't have indexed (common for exact
  // house numbers) — starts with no coordinates, requiring the map picker
  // below to place them before saving is allowed.
  function startCustomPin() {
    setDraft({ name: query.trim() || 'Home', address: '', latitude: null, longitude: null })
    setResults([])
    setQuery('')
  }

  function adjustDraft(latitude, longitude) {
    setDraft((current) => (current ? { ...current, latitude, longitude } : current))
  }

  async function saveDraft() {
    if (!draft || draft.latitude == null) return

    setSaveError('')
    setSaving(true)

    const response = await apiFetch(
      existingHome ? '/locations/home' : '/locations',
      {
        method: existingHome ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...draft, type: 'HOME' }),
      },
    )

    setSaving(false)

    if (!response.ok) {
      setSaveError('Failed to save Home address.')
      return
    }

    onSaved(await response.json())
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

        {draft ? (
          <div className="flex flex-col gap-2">
            <div className="flex items-center justify-between rounded-lg border border-indigo-200 bg-indigo-50 px-3 py-2">
              <span className="text-sm text-indigo-900">
                {draft.name}
                {draft.address ? ` (${draft.address})` : ''}
              </span>
              <button
                type="button"
                onClick={() => setDraft(null)}
                className="text-sm font-medium text-red-600 hover:underline"
              >
                Remove
              </button>
            </div>
            <p className="text-xs text-slate-500">
              {draft.latitude == null
                ? 'Click on the map to place the pin.'
                : "Drag the pin if it's not quite right."}
            </p>
            <LocationMapPicker latitude={draft.latitude} longitude={draft.longitude} onChange={adjustDraft} />
            {saveError && <p className="text-sm text-red-600">{saveError}</p>}
            <button
              type="button"
              disabled={draft.latitude == null || saving}
              onClick={saveDraft}
              className="mt-1 w-full rounded-lg bg-indigo-600 px-3 py-2.5 text-sm font-medium text-white shadow-sm transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {saving ? 'Saving...' : 'Save Home address'}
            </button>
          </div>
        ) : (
          <>
            <div className="flex gap-2">
              <input
                type="text"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault()
                    search()
                  }
                }}
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

            {results.length > 0 && (
              <ul className="mt-3 max-h-56 overflow-y-auto rounded-lg border border-slate-200">
                {results.map((result, index) => (
                  <li key={index} className="border-b border-slate-100 last:border-b-0">
                    <button
                      type="button"
                      onClick={() => pickResult(result)}
                      className="w-full px-3 py-2 text-left text-sm text-slate-700 transition-colors hover:bg-slate-50"
                    >
                      {result.name}
                      {result.address ? ` (${result.address})` : ''}
                    </button>
                  </li>
                ))}
              </ul>
            )}

            <button
              type="button"
              onClick={startCustomPin}
              className="mt-3 text-xs font-medium text-indigo-600 hover:underline"
            >
              Can't find it? Place a pin manually
            </button>
          </>
        )}
      </div>
    </div>
  )
}

export default HomeLocationModal
