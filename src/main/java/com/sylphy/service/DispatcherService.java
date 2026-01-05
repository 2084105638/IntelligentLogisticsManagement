package com.sylphy.service;

import com.sylphy.dto.DispatcherLoginDTO;
import com.sylphy.entity.model.Dispatcher;
import com.sylphy.vo.DispatcherLoginVO;

public interface DispatcherService {

    DispatcherLoginVO login(DispatcherLoginDTO loginDTO);

    void logout(String token);

    Dispatcher getDispatcherByToken(String token);
}
