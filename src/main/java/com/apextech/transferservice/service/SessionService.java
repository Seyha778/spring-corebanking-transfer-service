package com.apextech.transferservice.service;

import com.apextech.transferservice.dto.response.SessionData;
import com.apextech.transferservice.dto.response.SessionResponse;

public interface SessionService {

    SessionResponse create(SessionData data);

    boolean verify(String sessionId);

    boolean consume(String sessionId);

    SessionData getSession(String sessionId);

}