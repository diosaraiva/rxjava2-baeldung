package com.diosaraiva.rxjava2;

import org.junit.jupiter.api.Test;
import rx.Observable;
import rx.observables.ConnectableObservable;

import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConnectableObservableIntegrationTest {

    @Test
    public void givenConnectableObservable_whenConnect_thenGetMessage() throws InterruptedException {
        String[] result = {""};
        ConnectableObservable<Long> connectable
          = Observable.interval(500, TimeUnit.MILLISECONDS).publish();
        connectable.subscribe(i -> result[0] += i);
        assertFalse(result[0].equals("01"));

        connectable.connect();
        await()
          .until(() -> assertTrue(result[0].equals("01")));
    }
}
