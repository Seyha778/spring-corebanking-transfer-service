package com.apextech.transferservice.service;

import com.apextech.transferservice.dto.request.SessionRequest;
import com.apextech.transferservice.dto.request.TransferRequest;
import com.apextech.transferservice.dto.response.SessionResponse;
import com.apextech.transferservice.dto.response.TransferResponse;
import com.apextech.transferservice.entity.TransferEntity;

public interface TransferService {

    SessionResponse transferValidate(TransferRequest request);
    TransferEntity transferCommit(SessionRequest request);
}
