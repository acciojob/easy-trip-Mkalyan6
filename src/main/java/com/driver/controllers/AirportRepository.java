package com.driver.controllers;

import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AirportRepository {
    HashMap<String, Airport> AirportDetails = new HashMap<>();
    HashMap<City,Airport> City_Airport = new HashMap<>();
    PriorityQueue<Airport> pqForGetTerminal = new PriorityQueue<>((a, b) -> {
        return b.getNoOfTerminals() - a.getNoOfTerminals();
    });

    HashMap<Integer,Flight>FlightList=new HashMap<>();
    HashMap<City, List<Flight>>ListOfFlightFromCity=new HashMap<>();
    HashMap<Integer,Passenger>PassengerDetails=new HashMap<>();

    HashMap<Integer,List<Integer>>Flight_PassengerList=new HashMap<>();
    HashMap<Integer,Integer>Passenger_CountOfFlights=new HashMap<>();
    HashMap<Date,List<Flight>>Date_FlightList=new HashMap<>();


    public void addAirport(Airport airport) {
        String name = airport.getAirportName();
        if (!AirportDetails.containsKey(name)) {
            if (!City_Airport.containsKey(airport.getCity())) {
                // It means airport name and city name are unique
                // add to hashmap
                AirportDetails.put(name, airport);
                pqForGetTerminal.add(airport);
                City_Airport.put(airport.getCity(),airport);

            }

        }
    }

    public String getLargestAirportName() {
        if (pqForGetTerminal.size() == 0) return null;
        if (pqForGetTerminal.size() == 1) return pqForGetTerminal.remove().getAirportName();
        Airport TopTerminalObj = pqForGetTerminal.remove();
        String airportName = TopTerminalObj.getAirportName();
        int TopTerminals = TopTerminalObj.getNoOfTerminals();
        while (pqForGetTerminal.size() > 0) {
            Airport a = pqForGetTerminal.remove();
            if (a.getNoOfTerminals() != TopTerminals) return airportName;
            if (airportName.compareTo(a.getAirportName()) > 0) {
                airportName = a.getAirportName();
            }
        }
        return airportName;
//            if(a.getNoOfTerminals()!=b.getNoOfTerminals())return a.getAirportName();
//            if(a.getAirportName().compareTo(b.getAirportName())<0){
//                // a airport is smaller,then add a tothe pq
//                pqForGetTerminal.add(a);
//            }else{
//                pqForGetTerminal.add(b);
//            }
//        }
//        return pqForGetTerminal.remove().getAirportName();


    }

    public String addFlight(Flight flight) {
        int  id=flight.getFlightId();
        if(!FlightList.containsKey(id)){
            FlightList.put(id,flight);
            // Now note the flight details from a particular city
            City StartCity=flight.getFromCity();
            List<Flight>listOfDest=ListOfFlightFromCity.getOrDefault(StartCity,new ArrayList<>());
            listOfDest.add(flight);
            ListOfFlightFromCity.put(StartCity,listOfDest);

            // based on date, the flight list is being collected
            Date d=flight.getFlightDate();
            List<Flight>f=Date_FlightList.getOrDefault(d,new ArrayList<>());
            f.add(flight);
            Date_FlightList.put(d,f);

        }
        return "SUCCESS";
    }

    public double getShortestDurationOfPossibleBetweenTwoCities(City fromCity, City toCity) {
        double ShortDur=Double.MAX_VALUE;
        if(ListOfFlightFromCity.containsKey(fromCity)){
            // we got that from city we have flights to different citites list
            // In that we have to search for toCity and find the shortest duration
            List<Flight>list=ListOfFlightFromCity.get(fromCity);
            for(Flight f:list){
                if(f.getToCity().equals(toCity)){
                    if(ShortDur>f.getDuration()){
                        ShortDur=f.getDuration();
                    }
                }
            }

        }
        if(ListOfFlightFromCity.containsKey(toCity)){
            // we got that from city we have flights to different citites list
            // In that we have to search for toCity and find the shortest duration
            List<Flight>list=ListOfFlightFromCity.get(toCity);
            for(Flight f:list){
                if(f.getToCity().equals(fromCity)){
                    if(ShortDur>f.getDuration()){
                        ShortDur=f.getDuration();
                    }
                }
            }

        }
        if(ShortDur==Double.MAX_VALUE)return -1;
        return ShortDur;

    }

    public String addPassenger(Passenger passenger) {
        int PassId=passenger.getPassengerId();
        if(!PassengerDetails.containsKey(PassId)){
            PassengerDetails.put(PassId,passenger);
        }
      return "SUCCESS";
    }

    public String bookATicket(Integer flightId, Integer passengerId) {
        if(!FlightList.containsKey(flightId)||!PassengerDetails.containsKey(passengerId))return "FAILURE";
        List<Integer>list=Flight_PassengerList.getOrDefault(flightId,new ArrayList<>());
        if(list.contains(passengerId))return "FAILURE";
        int maxCap=FlightList.get(flightId).getMaxCapacity();
        if(list.size()>maxCap)return "FAILURE";
        // Add the passenger to the flight passenger list
        list.add(passengerId);
        Flight_PassengerList.put(flightId,list);
        // this passenger booked this flight, so increase his counting total done by hime for varioius flights
        int count=Passenger_CountOfFlights.getOrDefault(passengerId,0)+1;
        Passenger_CountOfFlights.put(passengerId,count);

        return "SUCCESS";
    }

    public String cancelATicket(Integer flightId, Integer passengerId) {

        if(Flight_PassengerList.containsKey(flightId)){
            List<Integer> list=Flight_PassengerList.get(flightId);
            if(!list.contains(passengerId))return "FAILURE";
            list.remove(passengerId);
            return "SUCCESS";

        }else{
            return "FAILURE";// If flight number is not found
        }
    }

    public int countOfBookingsDoneByPassengerAllCombined(Integer passengerId) {
           if(Passenger_CountOfFlights.containsKey(passengerId)){
               return Passenger_CountOfFlights.get(passengerId);
           }
           return 0;
    }

    public int calculateFlightFare(Integer flightId) {
        if(!FlightList.containsKey(flightId))return 0;
        if(Flight_PassengerList.containsKey(flightId)){
            int CountOfPassengers=Flight_PassengerList.get(flightId).size();
            return 3000+(50*CountOfPassengers);
        }
        return 3000;
    }

    public String getAirportNameFromFlightId(Integer flightId) {
        // First check if flightid is valid and if so, check the starting city from it is going
        // next this city has airport or not
        if(FlightList.containsKey(flightId)){
            Flight f=FlightList.get(flightId);
            City c=f.getFromCity();
            // Check if this city has airport or not
            if(City_Airport.containsKey(c)){
                Airport a=City_Airport.get(c);
                return a.getAirportName();

            }
        }
           return null;
    }

    public int calculateRevenueOfAFlight(Integer flightId) {
        // Check if this flight has passengers or not return 0, if not
        if(Flight_PassengerList.containsKey(flightId)){
            int TotalPass=Flight_PassengerList.get(flightId).size();
            int n=TotalPass-1;
            return (3000*TotalPass)+50*((n*(n+1))/2);
        }
        return 0;
    }

    public int getNumberOfPeopleOn(Date date, String airportName) {
        // First get the city in which this airport is present
        int totalPass=0;
        if(AirportDetails.containsKey(airportName)){
            City city=AirportDetails.get(airportName).getCity();
             if(Date_FlightList.containsKey(date)){
                 List<Flight>f=Date_FlightList.get(date);
                 // traverse through the flight list on a particular date and
                 // find the starting or ending city is city and add the passenger count
                 for(Flight flightOndate:f){
                     City Start=flightOndate.getFromCity();
                     City end=flightOndate.getToCity();
                     if(Start.equals(city)||end.equals(city)){
                         // Get the flight id numbber
                         int flightid=flightOndate.getFlightId();
                         // Now go to the flight and its passengers list and add the count
                         if(Flight_PassengerList.containsKey(flightid)){
                             List<Integer>passList=Flight_PassengerList.get(flightid);
                             totalPass+=passList.size();
                         }

                     }
                 }
             }

        }
        return totalPass;
    }
}
