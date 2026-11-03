package com.restaurant.usertype;

import com.restaurant.usertype.dto.UserTypeRequest;
import com.restaurant.usertype.dto.UserTypeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTypeServiceTest {

    @Mock UserTypeRepository repository;
    @InjectMocks UserTypeService service;

    @Test
    void create_success() {
        when(repository.existsByNameIgnoreCase("OWNER")).thenReturn(false);
        when(repository.save(any())).thenReturn(new UserType(1L, "OWNER"));

        UserTypeResponse response = service.create(new UserTypeRequest("OWNER"));

        assertThat(response.name()).isEqualTo("OWNER");
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void create_duplicateName_throws() {
        when(repository.existsByNameIgnoreCase("OWNER")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new UserTypeRequest("OWNER")))
                .isInstanceOf(UserTypeAlreadyExistsException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void findAll_returnsList() {
        when(repository.findAll()).thenReturn(List.of(
                new UserType(1L, "OWNER"),
                new UserType(2L, "CUSTOMER")
        ));

        List<UserTypeResponse> result = service.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void findById_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(new UserType(1L, "OWNER")));

        UserTypeResponse response = service.findById(1L);

        assertThat(response.name()).isEqualTo("OWNER");
    }

    @Test
    void findById_notFound_throws() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(UserTypeNotFoundException.class);
    }

    @Test
    void update_success() {
        UserType existing = new UserType(1L, "OWNER");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByNameIgnoreCaseAndIdNot("OWNER_NEW", 1L)).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserTypeResponse response = service.update(1L, new UserTypeRequest("OWNER_NEW"));

        assertThat(response.name()).isEqualTo("OWNER_NEW");
    }

    @Test
    void update_duplicateName_throws() {
        when(repository.findById(1L)).thenReturn(Optional.of(new UserType(1L, "OWNER")));
        when(repository.existsByNameIgnoreCaseAndIdNot("CUSTOMER", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new UserTypeRequest("CUSTOMER")))
                .isInstanceOf(UserTypeAlreadyExistsException.class);
    }

    @Test
    void delete_success() {
        UserType type = new UserType(1L, "OWNER");
        when(repository.findById(1L)).thenReturn(Optional.of(type));

        service.delete(1L);

        verify(repository).delete(type);
    }

    @Test
    void delete_notFound_throws() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(UserTypeNotFoundException.class);
    }
}
