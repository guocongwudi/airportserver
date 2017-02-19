package com.qantas.controller;


import com.qantas.api.AirportsApi;
import com.qantas.model.Airport;
import com.qantas.model.Error;
import com.qantas.service.AirportService;
import com.qantas.service.AirportServiceImpl;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by g on 17/02/2017.
 */
@RestController
public class AirportRestApiController implements AirportsApi {
    private static final Logger logger = LoggerFactory.getLogger(AirportRestApiController.class);

    @Autowired
    AirportService airportService;

    /*
     public interface override the generated class
     */
    @Override
    public ResponseEntity<List<Airport>> airportsGet(@ApiParam(value = "contry code to filter by.") @RequestParam(value = "countrycode", required = false) String countrycode


            , @ApiParam(value = "airport code to filter by.") @RequestParam(value = "airportcode", required = false) String airportcode


            , @ApiParam(value = "is international airport to fitler by.") @RequestParam(value = "international", required = false) Boolean international


            , @ApiParam(value = "is domestic airport to filter by.") @RequestParam(value = "domestic", required = false) Boolean domestic


            , @ApiParam(value = "sortby airport code or country code, accepted value airport  or country. default to sort by airport code") @RequestParam(value = "sortby", required = false) String sortby


            , @ApiParam(value = "page number used by pagination.") @RequestParam(value = "pagenumber", required = false) Integer pagenumber


            , @ApiParam(value = "page size used by pagination.") @RequestParam(value = "pagesize", required = false) Integer pagesize


    ) {
        List<Airport> airports = null;
        try {
            airports = airportService.filterAirports(countrycode, airportcode, international, domestic, sortby, pagenumber, pagesize);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<List<Airport>>(airports, HttpStatus.OK);
    }

    @ExceptionHandler(value = Exception.class)
    /* only provide the basic exception handing */
    public ResponseEntity<Error> nfeHandler(Exception e) {

        Error error = new Error();
        logger.error("error occurs during processing /airports request: ", e);

        if (e instanceof IOException) {
            error.setMessage("Remote data feed is down");
            error.setCode(001);
            return new ResponseEntity<Error>(error, HttpStatus.SERVICE_UNAVAILABLE);

        } else {
            error.setMessage(e.getMessage());
            error.setCode(002);
            return new ResponseEntity<Error>(error, HttpStatus.BAD_REQUEST);
        }
    }

}


