package com.hotel.service;

import com.hotel.repository.HotelRepository;
import com.hotel.model.Reservation;

import java.util.List;

public class AutoCancelThread extends Thread {

    private final HotelRepository repository;
    private final BusinessLogicService logicService;
    private final DatabaseManager dbManager;
    private volatile boolean running = true;

    private static final int CHECK_INTERVAL_MS = 60 * 1000;
    private static final int EXPIRE_MINUTES    = 30;

    public AutoCancelThread(HotelRepository repository,
                            BusinessLogicService logicService,
                            DatabaseManager dbManager) {
        this.repository   = repository;
        this.logicService = logicService;
        this.dbManager    = dbManager;
        this.setDaemon(true);
        this.setName("AutoCancel-Thread");
    }

    @Override
    public void run() {
        System.out.println("🔄 AutoCancelThread đã khởi động - Kiểm tra mỗi 1 phút");
        while (running) {
            try {
                checkAndCancelExpiredBookings();
                Thread.sleep(CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                System.out.println("AutoCancelThread bị dừng.");
                break;
            }
        }
    }

    private void checkAndCancelExpiredBookings() {
        List<Reservation> expired = logicService.getExpiredPendingReservations(EXPIRE_MINUTES);
        if (expired.isEmpty()) return;

        System.out.println("⏰ Tìm thấy " + expired.size() + " đơn PENDING quá 30 phút, đang hủy...");
        for (Reservation r : expired) {
            r.cancel("Hết thời gian xác nhận cọc (quá 30 phút)", "SYSTEM");
            repository.updateReservation(r);
            System.out.println("❌ Đã hủy: " + r.getReservationId()
                    + " - Khách: " + r.getGuest().getFullName());
        }
        dbManager.saveAll();
    }

    public void stopThread() {
        this.running = false;
        this.interrupt();
    }
}