package com.taoqi.integration.service;

import com.taoqi.integration.api.dto.EmailSendRequest;
import com.taoqi.integration.api.dto.EmailSendResult;

public interface EmailService {

    EmailSendResult send(EmailSendRequest request);
}
