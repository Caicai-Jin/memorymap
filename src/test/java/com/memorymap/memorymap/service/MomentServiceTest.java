package com.memorymap.memorymap.service;

import com.memorymap.memorymap.exception.MomentAccessDeniedException;
import com.memorymap.memorymap.exception.MomentNotFoundException;
import com.memorymap.memorymap.model.Moment;
import com.memorymap.memorymap.model.User;
import com.memorymap.memorymap.repository.LocationRepository;
import com.memorymap.memorymap.repository.MomentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Unit-level counterpart to MomentOwnershipTest: same IDOR rule, but here every
// dependency is mocked so this tests MomentService's logic in isolation, with no
// database or HTTP layer involved at all.
@ExtendWith(MockitoExtension.class)
class MomentServiceTest {

    @Mock
    private MomentRepository momentRepository;
    @Mock
    private UserService userService;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private MomentService momentService;

    @Test
    void createMomentSetsOwnerToTheCurrentUser() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("owner@test.com");
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(momentRepository.save(any(Moment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Moment moment = new Moment();
        Moment saved = momentService.createMoment(moment);

        assertEquals(currentUser, saved.getOwner());
        verify(momentRepository).save(moment);
    }

    @Test
    void getMomentByIdThrowsWhenCallerIsNotTheOwner() {
        User owner = new User();
        owner.setEmail("owner@test.com");
        User intruder = new User();
        intruder.setEmail("intruder@test.com");

        Moment moment = new Moment();
        moment.setId(5L);
        moment.setOwner(owner);

        when(momentRepository.findById(5L)).thenReturn(Optional.of(moment));
        when(userService.getCurrentUser()).thenReturn(intruder);

        assertThrows(MomentAccessDeniedException.class, () -> momentService.getMomentById(5L));
    }

    @Test
    void getMomentByIdThrowsWhenMomentDoesNotExist() {
        when(momentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MomentNotFoundException.class, () -> momentService.getMomentById(99L));
    }
}
