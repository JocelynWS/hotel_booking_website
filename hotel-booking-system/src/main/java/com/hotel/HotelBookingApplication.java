package com.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.hotel.repository.HotelRepository;
import com.hotel.service.BusinessLogicService;
import com.hotel.service.AutoCancelThread;
import com.hotel.service.DatabaseManager;

@SpringBootApplication
public class HotelBookingApplication {

    public static void main(String[] args) {
        HotelRepository repository = new HotelRepository();
        BusinessLogicService logicService = new BusinessLogicService(repository);
        DatabaseManager dbManager = new DatabaseManager(repository);
        dbManager.loadAll();

        AutoCancelThread autoCancelThread = new AutoCancelThread(repository, logicService, dbManager);
        autoCancelThread.start();

        SpringApplication.run(HotelBookingApplication.class, args);
    }
}