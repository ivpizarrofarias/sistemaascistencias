package cl.ipfsoft.backend.service;

import cl.ipfsoft.backend.dto.common.StatusResponse;
import cl.ipfsoft.backend.dto.user.SystemUserRequest;
import cl.ipfsoft.backend.dto.user.SystemUserResponse;

import java.util.List;

public interface SystemUserService {
    List<SystemUserResponse> findAll();
    SystemUserResponse findById(Long id);
    SystemUserResponse create(SystemUserRequest request);
    SystemUserResponse update(Long id, SystemUserRequest request);
    StatusResponse setEnabled(Long id, boolean enabled);
    StatusResponse delete(Long id);
}
