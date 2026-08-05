package com.memorymap.memorymap.service;

import com.memorymap.memorymap.dto.LocationSearchResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

// Calls Photon (photon.komoot.io), a free public search API built on OpenStreetMap data.
// No API key, no billing, no account required.
@Service
public class LocationSearchService {

    private final RestClient restClient = RestClient.create();

    public List<LocationSearchResult> search(String query) {
        PhotonResponse response = restClient.get()
                .uri("https://photon.komoot.io/api/?q={query}&limit=5", query)
                .retrieve()
                .body(PhotonResponse.class);

        if (response == null || response.features() == null) {
            return List.of();
        }

        return response.features().stream()
                .map(this::toSearchResult)
                .toList();
    }

    private LocationSearchResult toSearchResult(PhotonFeature feature) {
        PhotonProperties props = feature.properties();
        String name = props.name() != null ? props.name() : props.street();

        // Photon coordinates are ordered [longitude, latitude] — the opposite of how we usually say it out loud.
        List<Double> coordinates = feature.geometry().coordinates();
        Double longitude = coordinates.get(0);
        Double latitude = coordinates.get(1);

        return new LocationSearchResult(name, buildAddress(props), latitude, longitude);
    }

    private String buildAddress(PhotonProperties props) {
        StringBuilder address = new StringBuilder();
        if (props.housenumber() != null) address.append(props.housenumber()).append(" ");
        if (props.street() != null) address.append(props.street()).append(", ");
        if (props.city() != null) address.append(props.city()).append(", ");
        if (props.country() != null) address.append(props.country());
        return address.toString().trim();
    }

    // These records only exist to describe the shape of Photon's JSON response so Jackson can deserialize it.
    private record PhotonResponse(List<PhotonFeature> features) {
    }

    private record PhotonFeature(PhotonProperties properties, PhotonGeometry geometry) {
    }

    private record PhotonProperties(String name, String street, String housenumber, String city, String postcode, String country) {
    }

    private record PhotonGeometry(List<Double> coordinates) {
    }
}