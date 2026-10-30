package com.apextech.transferservice.controller;

import com.apextech.transferservice.dto.request.SessionRequest;
import com.apextech.transferservice.dto.request.TransferRequest;
import com.apextech.transferservice.dto.response.ResponseMessage;
import com.apextech.transferservice.dto.response.ResponseMessageBuilder;
import com.apextech.transferservice.dto.response.SessionResponse;
import com.apextech.transferservice.dto.response.TransferResponse;
import com.apextech.transferservice.entity.TransferEntity;
import com.apextech.transferservice.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/transferValidate")
    public ResponseMessage<SessionResponse> transferValidate(@RequestBody TransferRequest request) {
        SessionResponse response = transferService.transferValidate(request);
        return new ResponseMessageBuilder<SessionResponse>()
                .success()
                .data(response)
                .build();
    }

    @PostMapping("/transferCommit")
    public ResponseMessage<TransferEntity> transferCommit(@RequestBody SessionRequest request) {
        TransferEntity response = transferService.transferCommit(request);
        return new ResponseMessageBuilder<TransferEntity>()
                .success()
                .data(response)
                .build();
    }


}