package com.restaurant.usertype;

import com.restaurant.usertype.dto.UserTypeRequest;
import com.restaurant.usertype.dto.UserTypeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserTypeService {

    private final UserTypeRepository userTypeRepository;

    @Transactional
    public UserTypeResponse create(UserTypeRequest request) {
        if (userTypeRepository.existsByNameIgnoreCase(request.name())) {
            throw new UserTypeAlreadyExistsException("Tipo de usuário já existe: " + request.name());
        }
        UserType type = UserType.builder().name(request.name()).build();
        return UserTypeResponse.from(userTypeRepository.save(type));
    }

    @Transactional(readOnly = true)
    public List<UserTypeResponse> findAll() {
        return userTypeRepository.findAll().stream().map(UserTypeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserTypeResponse findById(Long id) {
        return UserTypeResponse.from(getById(id));
    }

    @Transactional
    public UserTypeResponse update(Long id, UserTypeRequest request) {
        UserType type = getById(id);
        if (userTypeRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new UserTypeAlreadyExistsException("Tipo de usuário já existe: " + request.name());
        }
        type.setName(request.name());
        return UserTypeResponse.from(userTypeRepository.save(type));
    }

    @Transactional
    public void delete(Long id) {
        userTypeRepository.delete(getById(id));
    }

    public UserType getById(Long id) {
        return userTypeRepository.findById(id)
                .orElseThrow(() -> new UserTypeNotFoundException("Tipo de usuário não encontrado: " + id));
    }
}
