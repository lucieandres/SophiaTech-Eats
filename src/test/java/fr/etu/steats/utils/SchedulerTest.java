package fr.etu.steats.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SchedulerTest {

    @Test
    void testWaitTenMinutes() throws InterruptedException {
        Scheduler scheduler = Mockito.spy(new Scheduler());
        Mockito.doNothing().when(scheduler).waitTenMinutes();
        scheduler.waitTenMinutes();
        Assertions.assertNotNull(scheduler);
        verify(scheduler, times(1)).waitTenMinutes();
    }

    @Test
    void testWaitTenMinutesWithException() throws InterruptedException {
        Scheduler scheduler = Mockito.spy(new Scheduler());
        Mockito.doThrow(new InterruptedException()).when(scheduler).waitTenMinutes();
        assertThrows(InterruptedException.class, scheduler::waitTenMinutes);
    }
}
