import { useEffect, useState } from 'react'
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet'
import L from 'leaflet'
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png'
import markerIcon from 'leaflet/dist/images/marker-icon.png'
import markerShadow from 'leaflet/dist/images/marker-shadow.png'
import { apiFetch } from '../api.js'
import Navbar from '../components/Navbar.jsx'

delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
})

const DEFAULT_CENTER = [42.3149, -83.0364]

function FitBounds({ markers }) {
  const map = useMap()

  useEffect(() => {
    if (markers.length === 0) return

    const bounds = L.latLngBounds(
      markers.map((m) => [m.location.latitude, m.location.longitude]),
    )
    map.fitBounds(bounds, { padding: [40, 40], maxZoom: 13 })
  }, [markers, map])

  return null
}

function MapView() {
  const [moments, setMoments] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function loadMoments() {
      setLoading(true)
      const response = await apiFetch('/moments')

      if (!response.ok) {
        setError('Failed to load moments.')
        setLoading(false)
        return
      }

      const data = await response.json()
      setMoments(data)
      setLoading(false)
    }

    loadMoments()
  }, [])

  const markers = moments.filter(
    (m) => m.location && m.location.latitude != null && m.location.longitude != null,
  )
  const center =
    markers.length > 0
      ? [markers[0].location.latitude, markers[0].location.longitude]
      : DEFAULT_CENTER

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <div className="mx-auto max-w-4xl px-6 py-8">
        {loading && (
          <p className="mb-4 text-center text-sm text-slate-500">Loading...</p>
        )}
        {error && <p className="mb-4 text-center text-sm text-red-600">{error}</p>}
        {!loading && !error && markers.length === 0 && (
          <p className="mb-4 text-center text-sm text-slate-500">
            No moments with a public location yet. Home locations are never shown on
            the map.
          </p>
        )}
        {!loading && !error && (
          <div className="overflow-hidden rounded-2xl border border-slate-200 shadow-sm">
            <MapContainer
              center={center}
              zoom={markers.length > 0 ? 11 : 4}
              style={{ height: '70vh', width: '100%' }}
            >
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              <FitBounds markers={markers} />
              {markers.map((moment) => (
                <Marker
                  key={moment.id}
                  position={[moment.location.latitude, moment.location.longitude]}
                >
                  <Popup>
                    <p className="font-medium">{moment.location.name}</p>
                    {moment.content && <p>{moment.content}</p>}
                    {moment.mood?.length > 0 && (
                      <p className="text-sm text-slate-600">
                        Mood: {moment.mood.join(', ')}
                      </p>
                    )}
                  </Popup>
                </Marker>
              ))}
            </MapContainer>
          </div>
        )}
      </div>
    </div>
  )
}

export default MapView
