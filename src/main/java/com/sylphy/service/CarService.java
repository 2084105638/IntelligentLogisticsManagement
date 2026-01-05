package com.sylphy.service;

import com.sylphy.common.PageResult;
import com.sylphy.dto.CarCreateDTO;
import com.sylphy.dto.CarQueryDTO;
import com.sylphy.dto.CarUpdateDTO;
import com.sylphy.vo.CarVO;

public interface CarService {
    Long createCar(CarCreateDTO dto);
    void updateCar(CarUpdateDTO dto);
    void deleteCar(Long carId);
    PageResult<CarVO> queryCars(CarQueryDTO dto);
    Integer getStatus(Long carId);
    void updateStatus(Long carId, Integer status);
    String getLocation(Long carId);
    void updateLocation(Long carId, String location);
}
