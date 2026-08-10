import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet'
import L from 'leaflet'
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png'
import markerIcon from 'leaflet/dist/images/marker-icon.png'
import markerShadow from 'leaflet/dist/images/marker-shadow.png'

delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
})

// Same default center MapView.jsx falls back to, so a fresh manual pin
// starts out somewhere reasonable instead of the middle of the ocean.
const DEFAULT_CENTER = [42.3149, -83.0364]

function ClickHandler({ onChange }) {
  useMapEvents({
    click(e) {
      onChange(e.latlng.lat, e.latlng.lng)
    },
  })
  return null
}

function LocationMapPicker({ latitude, longitude, onChange }) {
  const hasPosition = latitude != null && longitude != null
  const position = hasPosition ? [latitude, longitude] : DEFAULT_CENTER

  return (
    <div className="overflow-hidden rounded-lg border border-slate-200">
      <MapContainer
        center={position}
        zoom={hasPosition ? 16 : 11}
        style={{ height: '200px', width: '100%' }}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <ClickHandler onChange={onChange} />
        <Marker
          position={position}
          draggable
          eventHandlers={{
            dragend: (e) => {
              const { lat, lng } = e.target.getLatLng()
              onChange(lat, lng)
            },
          }}
        />
      </MapContainer>
    </div>
  )
}

export default LocationMapPicker
