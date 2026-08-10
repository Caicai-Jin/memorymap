import { useEffect, useState } from 'react'
import { apiFetch } from '../api.js'
import Navbar from '../components/Navbar.jsx'
import HomeLocationModal from '../components/HomeLocationModal.jsx'

const inputClass =
  'rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/30'
const fileInputClass =
  'text-sm text-slate-600 file:mr-3 file:cursor-pointer file:rounded-lg file:border-0 file:bg-indigo-600 file:px-3 file:py-2 file:text-sm file:font-medium file:text-white file:transition-colors hover:file:bg-indigo-700'

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

function Moments() {
  const [moments, setMoments] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  const [content, setContent] = useState('')
  const [mood, setMood] = useState('')
  const [editingId, setEditingId] = useState(null)
  const [formError, setFormError] = useState('')

  const [uploadingId, setUploadingId] = useState(null)
  const [uploadError, setUploadError] = useState('')
  const [uploadPreview, setUploadPreview] = useState(null)
  const [pendingFile, setPendingFile] = useState(null)
  const [pendingFilePreview, setPendingFilePreview] = useState(null)
  const [fileInputKey, setFileInputKey] = useState(0)
  const [existingMedia, setExistingMedia] = useState([])

  const [locationQuery, setLocationQuery] = useState('')
  const [locationResults, setLocationResults] = useState([])
  const [locationSearchError, setLocationSearchError] = useState('')
  const [selectedLocation, setSelectedLocation] = useState(null)
  const [isHomeLocation, setIsHomeLocation] = useState(false)

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
    setSelectedLocation(saved)
    setIsHomeLocation(true)
    setFormError('')
  }

  function toggleUseHome(e) {
    if (!e.target.checked) {
      clearLocation()
      return
    }
    if (homeLocation) {
      setSelectedLocation(homeLocation)
      setIsHomeLocation(true)
    } else {
      setFormError('You have not added a Home location yet — add one below.')
      setShowHomeModal(true)
    }
  }

  async function loadMoments(targetPage = page) {
    setLoading(true)
    const response = await apiFetch(`/moments?page=${targetPage}`)

    if (!response.ok) {
      setError('Failed to load moments.')
      setLoading(false)
      return
    }

    const data = await response.json()
    setMoments(data.content)
    setPage(data.page.number)
    setTotalPages(data.page.totalPages)
    setLoading(false)
  }

  useEffect(() => {
    loadMoments(0)
    loadHomeLocation()
  }, [])

  function resetForm() {
    setContent('')
    setMood('')
    setEditingId(null)
    setFormError('')
    setPendingFileChoice(null)
    setFileInputKey((key) => key + 1)
    setExistingMedia([])
    clearLocation()
  }

  function setPendingFileChoice(file) {
    setPendingFilePreview((previousUrl) => {
      if (previousUrl) URL.revokeObjectURL(previousUrl)
      return file ? URL.createObjectURL(file) : null
    })
    setPendingFile(file)
  }

  function startEdit(moment) {
    setEditingId(moment.id)
    setContent(moment.content ?? '')
    setMood((moment.mood ?? []).join(', '))
    setFormError('')
    setExistingMedia(moment.mediaResponses ?? [])
    if (moment.location) {
      setSelectedLocation(moment.location)
      setIsHomeLocation(moment.location.type === 'HOME')
    } else {
      clearLocation()
    }
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function clearLocation() {
    setSelectedLocation(null)
    setIsHomeLocation(false)
    setLocationQuery('')
    setLocationResults([])
    setLocationSearchError('')
  }

  async function searchLocations() {
    if (!locationQuery.trim()) return

    setLocationSearchError('')
    const response = await apiFetch(
      `/locations/search?query=${encodeURIComponent(locationQuery)}`,
    )

    if (!response.ok) {
      setLocationSearchError('Location search failed.')
      return
    }

    const data = await response.json()
    setLocationResults(data)
  }

  function selectLocationResult(result) {
    setSelectedLocation({
      name: result.name,
      address: result.address,
      latitude: result.latitude,
      longitude: result.longitude,
    })
    setLocationResults([])
    setLocationQuery('')
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setFormError('')

    const moodList = mood
      .split(',')
      .map((m) => m.trim())
      .filter((m) => m.length > 0)

    let locationId = selectedLocation?.id ?? null

    if (selectedLocation && !locationId) {
      const locationResponse = await apiFetch('/locations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...selectedLocation,
          type: isHomeLocation ? 'HOME' : 'PUBLIC',
        }),
      })

      if (!locationResponse.ok) {
        setFormError(
          isHomeLocation
            ? 'Failed to save location (you may already have a Home location).'
            : 'Failed to save location.',
        )
        return
      }

      const savedLocation = await locationResponse.json()
      locationId = savedLocation.id
    }

    const body = JSON.stringify({
      content,
      mood: moodList,
      location: locationId ? { id: locationId } : null,
    })
    const isEditing = editingId !== null

    const response = await apiFetch(
      isEditing ? `/moments/${editingId}` : '/moments',
      {
        method: isEditing ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body,
      },
    )

    if (!response.ok) {
      setFormError(
        isEditing ? 'Failed to update moment.' : 'Failed to create moment.',
      )
      return
    }

    const savedMoment = await response.json()
    const fileToUpload = pendingFile
    resetForm()

    // A newly created moment appears first (newest-first ordering), so jump back
    // to page 0 to show it; an edit stays on whichever page the user was already on.
    const targetPage = isEditing ? page : 0
    if (fileToUpload) {
      await handleUpload(savedMoment.id, fileToUpload, targetPage)
    } else {
      await loadMoments(targetPage)
    }
  }

  async function handleDelete(id) {
    const response = await apiFetch(`/moments/${id}`, { method: 'DELETE' })

    if (!response.ok) {
      setError('Failed to delete moment.')
      return
    }

    if (editingId === id) {
      resetForm()
    }
    await loadMoments()
  }

  async function handleUpload(id, file, targetPage = page) {
    if (!file) return

    setUploadError('')
    setUploadingId(id)
    const previewUrl = URL.createObjectURL(file)
    setUploadPreview({ id, url: previewUrl, isVideo: file.type.startsWith('video') })

    const formData = new FormData()
    formData.append('file', file)

    // Without a timeout a stalled request left "Uploading..." on screen forever;
    // the try/finally below guarantees that state clears even if the request throws.
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 90000)

    try {
      const response = await apiFetch(`/moments/${id}/media`, {
        method: 'POST',
        body: formData,
        signal: controller.signal,
      })

      if (!response.ok) {
        let message = 'Failed to upload media.'
        try {
          const data = await response.json()
          if (data?.message) message = data.message
        } catch {
          // response body wasn't JSON — keep the generic message
        }
        setUploadError(message)
        return
      }

      await loadMoments(targetPage)
    } catch (err) {
      setUploadError(
        err.name === 'AbortError'
          ? 'Upload timed out. Check your connection or try a smaller file.'
          : 'Something went wrong uploading your file. Please try again.',
      )
    } finally {
      clearTimeout(timeoutId)
      setUploadingId(null)
      setUploadPreview(null)
      URL.revokeObjectURL(previewUrl)
    }
  }

  async function handleDeleteMedia(momentId, mediaId) {
    const response = await apiFetch(`/moments/${momentId}/media/${mediaId}`, {
      method: 'DELETE',
    })

    if (!response.ok) {
      setUploadError('Failed to delete media.')
      return
    }

    setExistingMedia((prev) => prev.filter((m) => m.id !== mediaId))
    await loadMoments()
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <div className="mx-auto max-w-2xl px-6 py-8">
        <form
          onSubmit={handleSubmit}
          className="mb-8 flex flex-col gap-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
        >
          <h2 className="text-base font-semibold text-slate-900">
            {editingId !== null ? 'Edit moment' : 'New moment'}
          </h2>
          <label htmlFor="content" className="flex flex-col gap-1.5">
            <span className="text-sm font-medium text-slate-700">Content</span>
            <textarea
              id="content"
              value={content}
              onChange={(e) => setContent(e.target.value)}
              rows={3}
              className={inputClass}
            />
          </label>
          <label htmlFor="mood" className="flex flex-col gap-1.5">
            <span className="text-sm font-medium text-slate-700">
              Mood (comma-separated)
            </span>
            <input
              id="mood"
              type="text"
              value={mood}
              onChange={(e) => setMood(e.target.value)}
              className={inputClass}
            />
          </label>
          <div className="flex flex-col gap-1.5">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium text-slate-700">Location</span>
              <button
                type="button"
                onClick={() => setShowHomeModal(true)}
                className="text-xs font-medium text-indigo-600 hover:underline"
              >
                {homeLocation ? 'Change Home address' : 'Add Home address'}
              </button>
            </div>
            {!selectedLocation && (
              <label className="flex items-center gap-2 text-sm text-slate-600">
                <input
                  type="checkbox"
                  checked={false}
                  onChange={toggleUseHome}
                  className="h-4 w-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500/30"
                />
                Use my Home location
              </label>
            )}
            {selectedLocation ? (
              <div className="flex items-center justify-between rounded-lg border border-indigo-200 bg-indigo-50 px-3 py-2">
                <span className="flex items-center gap-1.5 text-sm text-indigo-900">
                  <PinIcon />
                  {selectedLocation.name}
                  {selectedLocation.address ? ` (${selectedLocation.address})` : ''}
                  {isHomeLocation ? ' — Home' : ''}
                </span>
                <button
                  type="button"
                  onClick={clearLocation}
                  className="text-sm font-medium text-red-600 hover:underline"
                >
                  Remove
                </button>
              </div>
            ) : (
              <>
                <div className="flex gap-2">
                  <input
                    type="text"
                    value={locationQuery}
                    onChange={(e) => setLocationQuery(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') {
                        e.preventDefault()
                        searchLocations()
                      }
                    }}
                    placeholder="Search a place..."
                    className={`flex-1 ${inputClass}`}
                  />
                  <button
                    type="button"
                    onClick={searchLocations}
                    className="rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-100"
                  >
                    Search
                  </button>
                </div>
                {locationSearchError && (
                  <p className="text-sm text-red-600">{locationSearchError}</p>
                )}
                {locationResults.length > 0 && (
                  <ul className="max-h-56 overflow-y-auto rounded-lg border border-slate-200">
                    {locationResults.map((result, index) => (
                      <li
                        key={index}
                        className="border-b border-slate-100 last:border-b-0"
                      >
                        <button
                          type="button"
                          onClick={() => selectLocationResult(result)}
                          className="flex w-full items-center gap-1.5 px-3 py-2 text-left text-sm text-slate-700 transition-colors hover:bg-slate-50"
                        >
                          <PinIcon />
                          {result.name}
                          {result.address ? ` (${result.address})` : ''}
                        </button>
                      </li>
                    ))}
                  </ul>
                )}
              </>
            )}
            {selectedLocation && !selectedLocation.id && (
              <label className="flex items-center gap-2 text-sm text-slate-600">
                <input
                  type="checkbox"
                  checked={isHomeLocation}
                  onChange={(e) => setIsHomeLocation(e.target.checked)}
                  className="h-4 w-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500/30"
                />
                This is my Home address (hides the real address from others)
              </label>
            )}
          </div>
          {existingMedia.length > 0 && (
            <div className="flex flex-col gap-1.5">
              <span className="text-sm font-medium text-slate-700">
                Already attached
              </span>
              <div className="flex flex-wrap gap-2">
                {existingMedia.map((media) => (
                  <div key={media.id} className="relative h-24 w-24">
                    {media.type === 'VIDEO' ? (
                      <video
                        src={media.url}
                        controls
                        className="h-24 w-24 rounded-lg object-cover shadow-sm"
                      />
                    ) : (
                      <img
                        src={media.url}
                        alt=""
                        className="h-24 w-24 rounded-lg object-cover shadow-sm"
                      />
                    )}
                    <button
                      type="button"
                      onClick={() => handleDeleteMedia(editingId, media.id)}
                      aria-label="Delete media"
                      className="absolute -right-2 -top-2 flex h-6 w-6 items-center justify-center rounded-full bg-white text-slate-500 shadow-sm transition-colors hover:text-red-600"
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}
          <label className="flex flex-col gap-1.5">
            <span className="text-sm font-medium text-slate-700">
              {existingMedia.length > 0 ? 'Add another photo/video' : 'Photo/video'}
            </span>
            <input
              key={fileInputKey}
              type="file"
              accept="image/*,video/*"
              onChange={(e) => setPendingFileChoice(e.target.files[0] ?? null)}
              className={fileInputClass}
            />
          </label>
          {pendingFilePreview && (
            <div className="relative w-32">
              {pendingFile?.type.startsWith('video') ? (
                <video
                  src={pendingFilePreview}
                  controls
                  className="h-32 w-32 rounded-lg object-cover shadow-sm"
                />
              ) : (
                <img
                  src={pendingFilePreview}
                  alt="Selected preview"
                  className="h-32 w-32 rounded-lg object-cover shadow-sm"
                />
              )}
              <button
                type="button"
                onClick={() => setPendingFileChoice(null)}
                aria-label="Remove selected file"
                className="absolute -right-2 -top-2 flex h-6 w-6 items-center justify-center rounded-full bg-white text-slate-500 shadow-sm transition-colors hover:text-red-600"
              >
                ×
              </button>
            </div>
          )}
          <div className="flex gap-2 pt-1">
            <button
              type="submit"
              className="flex-1 rounded-lg bg-indigo-600 px-3 py-2.5 text-sm font-medium text-white shadow-sm transition-colors hover:bg-indigo-700"
            >
              {editingId !== null ? 'Save changes' : 'Add moment'}
            </button>
            {editingId !== null && (
              <button
                type="button"
                onClick={resetForm}
                className="rounded-lg border border-slate-300 px-3 py-2.5 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-100"
              >
                Cancel
              </button>
            )}
          </div>
          {formError && <p className="text-sm text-red-600">{formError}</p>}
        </form>

        {loading && <p className="text-center text-sm text-slate-500">Loading...</p>}
        {error && <p className="text-center text-sm text-red-600">{error}</p>}
        {uploadError && (
          <p className="text-center text-sm text-red-600">{uploadError}</p>
        )}
        {!loading && !error && moments.length === 0 && (
          <p className="text-center text-sm text-slate-500">
            No moments yet. Create your first one above.
          </p>
        )}

        <ul className="flex flex-col gap-4">
          {moments.map((moment) => (
            <li
              key={moment.id}
              className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md"
            >
              {moment.content && (
                <p className="text-slate-900">{moment.content}</p>
              )}
              {moment.mood?.length > 0 && (
                <div className="mt-2 flex flex-wrap gap-1.5">
                  {moment.mood.map((m, index) => (
                    <span
                      key={index}
                      className="rounded-full bg-indigo-50 px-2.5 py-0.5 text-xs font-medium text-indigo-700"
                    >
                      {m}
                    </span>
                  ))}
                </div>
              )}
              {moment.location && (
                <p className="mt-2 flex items-center gap-1.5 text-sm text-slate-500">
                  <PinIcon />
                  {moment.location.name}
                </p>
              )}
              {moment.mediaResponses?.length > 0 && (
                <div className="mt-3 flex flex-wrap gap-2">
                  {moment.mediaResponses.map((media) =>
                    media.type === 'VIDEO' ? (
                      <video
                        key={media.id}
                        src={media.url}
                        controls
                        className="h-28 w-28 rounded-lg object-cover shadow-sm"
                      />
                    ) : (
                      <img
                        key={media.id}
                        src={media.url}
                        alt=""
                        className="h-28 w-28 rounded-lg object-cover shadow-sm"
                      />
                    ),
                  )}
                </div>
              )}
              <p className="mt-3 text-xs text-slate-400">
                {new Date(`${moment.createdAt}Z`).toLocaleString()}
              </p>
              {uploadingId === moment.id && uploadPreview && (
                <div className="mt-2 flex items-center gap-2">
                  {uploadPreview.isVideo ? (
                    <video
                      src={uploadPreview.url}
                      className="h-20 w-20 rounded-lg object-cover opacity-70 shadow-sm"
                    />
                  ) : (
                    <img
                      src={uploadPreview.url}
                      alt="Uploading preview"
                      className="h-20 w-20 rounded-lg object-cover opacity-70 shadow-sm"
                    />
                  )}
                  <p className="text-xs text-slate-500">Uploading...</p>
                </div>
              )}
              <div className="mt-4 flex gap-2">
                <button
                  type="button"
                  onClick={() => startEdit(moment)}
                  className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-100"
                >
                  Edit
                </button>
                <button
                  type="button"
                  onClick={() => handleDelete(moment.id)}
                  className="rounded-lg border border-red-200 px-3 py-1.5 text-sm font-medium text-red-600 transition-colors hover:bg-red-50"
                >
                  Delete
                </button>
              </div>
            </li>
          ))}
        </ul>

        {totalPages > 1 && (
          <div className="mt-6 flex items-center justify-center gap-2">
            <button
              type="button"
              onClick={() => loadMoments(page - 1)}
              disabled={page === 0}
              className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-40"
            >
              Previous
            </button>
            {Array.from({ length: totalPages }, (_, i) => i).map((pageNum) => (
              <button
                key={pageNum}
                type="button"
                onClick={() => loadMoments(pageNum)}
                aria-current={pageNum === page ? 'page' : undefined}
                className={`h-8 w-8 rounded-lg text-sm font-medium transition-colors ${
                  pageNum === page
                    ? 'bg-indigo-600 text-white'
                    : 'text-slate-700 hover:bg-slate-100'
                }`}
              >
                {pageNum + 1}
              </button>
            ))}
            <button
              type="button"
              onClick={() => loadMoments(page + 1)}
              disabled={page + 1 >= totalPages}
              className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-40"
            >
              Next
            </button>
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

export default Moments
