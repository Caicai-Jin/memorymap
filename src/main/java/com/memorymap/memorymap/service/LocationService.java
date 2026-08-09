package com.memorymap.memorymap.service;

import com.memorymap.memorymap.exception.DuplicateHomeLocationException;
import com.memorymap.memorymap.exception.LocationNotFoundException;
import com.memorymap.memorymap.model.Location;
import com.memorymap.memorymap.model.User;
import com.memorymap.memorymap.repository.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.memorymap.memorymap.model.LocationType.HOME;

@Service
public class LocationService {
    private final LocationRepository locationRepository;
    private final UserService userService;

    public LocationService(LocationRepository locationRepository, UserService userService){
        this.locationRepository = locationRepository;
        this.userService = userService;
    }
    public Location createLocation(Location location){
        User currentUser= userService.getCurrentUser();
        location.setOwner(currentUser);
        if(location.getType()== HOME){
            if(locationRepository.findByOwnerAndType(currentUser, HOME).isPresent()){
                throw  new DuplicateHomeLocationException("Location with type HOME already exists");
            }
        }
        locationRepository.save(location);
        return location;
    }

    public Optional<Location> getHomeLocation(){
        User currentUser = userService.getCurrentUser();
        return locationRepository.findByOwnerAndType(currentUser, HOME);
    }

    public Location updateHomeLocation(Location updatedData){
        User currentUser = userService.getCurrentUser();
        Location home = locationRepository.findByOwnerAndType(currentUser, HOME)
                .orElseThrow(() -> new LocationNotFoundException("No Home location set yet"));

        home.setName(updatedData.getName());
        home.setAddress(updatedData.getAddress());
        home.setLatitude(updatedData.getLatitude());
        home.setLongitude(updatedData.getLongitude());

        locationRepository.save(home);
        return home;
    }

    public Location maskIfHome(Location location){
        if(location == null){
            return null;
        }
        if(location.getType()== HOME){
            Location maskedLocation=new Location();
            maskedLocation.setName("Home");
            return maskedLocation;
        }
        else{
            return location;
        }
    }

}
