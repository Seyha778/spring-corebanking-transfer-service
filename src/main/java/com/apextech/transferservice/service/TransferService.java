package com.apextech.transferservice.service;

import com.apextech.transferservice.dto.request.TransferRequest;
import com.apextech.transferservice.dto.response.SessionResponse;
import com.apextech.transferservice.dto.response.TransferResponse;

public interface TransferService {

    SessionResponse transferValidate(TransferRequest request);
    TransferResponse transferCommit(String sessionId);
}
