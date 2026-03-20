package yuxuan.travelassisant.service;

public interface ReservationService {
    Long insert(String scenicName, String date, String timeSlot, int personCount, String contactName, String phone);

    int queryRemainingSlots(String scenicName, String date, String timeSlot);
}
