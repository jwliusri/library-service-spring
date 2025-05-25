package com.jwliusri.library_service.security.mfa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MfaOtpRepository extends CrudRepository<MfaOtp, String> {

}
