package com.diosaraiva.rxjava2.onerror;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OnErrorRetryIntegrationTest {

    private Error UNKNOWN_ERROR = new Error("unknown error");

    @Test
    public void givenSubscriberAndError_whenRetryOnError_thenRetryConfirmed() {
        TestObserver<String> testObserver = new TestObserver<>();
        AtomicInteger atomicCounter = new AtomicInteger(0);

        Observable
          .<String>error(() -> {
              atomicCounter.incrementAndGet();
              return UNKNOWN_ERROR;
          })
          .retry(1)
          .subscribe(testObserver);

        testObserver.assertError(UNKNOWN_ERROR);
        testObserver.assertNotComplete();
        testObserver.assertNoValues();
        assertTrue(atomicCounter.get() == 2);
    }

    @Test
    public void givenSubscriberAndError_whenRetryConditionallyOnError_thenRetryConfirmed() {
        TestObserver<String> testObserver = new TestObserver<>();

        AtomicInteger atomicCounter = new AtomicInteger(0);

        Observable
          .<String>error(() -> {
              atomicCounter.incrementAndGet();
              return UNKNOWN_ERROR;
          })
          .retry((integer, throwable) -> integer < 4)
          .subscribe(testObserver);

        testObserver.assertError(UNKNOWN_ERROR);
        testObserver.assertNotComplete();
        testObserver.assertNoValues();
        assertTrue(atomicCounter.get() == 4);
    }

    @Test
    public void givenSubscriberAndError_whenRetryUntilOnError_thenRetryConfirmed() {
        TestObserver<String> testObserver = new TestObserver<>();
        AtomicInteger atomicCounter = new AtomicInteger(0);

        Observable
          .<String>error(UNKNOWN_ERROR)
          .retryUntil(() -> atomicCounter.incrementAndGet() > 3)
          .subscribe(testObserver);

        testObserver.assertError(UNKNOWN_ERROR);
        testObserver.assertNotComplete();
        testObserver.assertNoValues();
        assertTrue(atomicCounter.get() == 4);
    }

    @Test
    public void givenSubscriberAndError_whenRetryWhenOnError_thenRetryConfirmed() {
        TestObserver<String> testObserver = new TestObserver<>();
        Exception noretryException = new Exception("don't retry");

        Observable
          .<String>error(UNKNOWN_ERROR)
          .retryWhen(throwableObservable -> Observable.<String>error(noretryException))
          .subscribe(testObserver);

        testObserver.assertError(noretryException);
        testObserver.assertNotComplete();
        testObserver.assertNoValues();
    }

    @Test
    public void givenSubscriberAndError_whenRetryWhenOnError_thenCompleted() {
        TestObserver<String> testObserver = new TestObserver<>();
        AtomicInteger atomicCounter = new AtomicInteger(0);

        Observable
          .<String>error(() -> {
              atomicCounter.incrementAndGet();
              return UNKNOWN_ERROR;
          })
          .retryWhen(throwableObservable -> Observable.empty())
          .subscribe(testObserver);

        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertNoValues();
        assertTrue(atomicCounter.get() == 0);
    }

    @Test
    public void givenSubscriberAndError_whenRetryWhenOnError_thenResubscribed() {
        TestObserver<String> testObserver = new TestObserver<>();
        AtomicInteger atomicCounter = new AtomicInteger(0);

        Observable
          .<String>error(() -> {
              atomicCounter.incrementAndGet();
              return UNKNOWN_ERROR;
          })
          .retryWhen(throwableObservable -> Observable.just("anything"))
          .subscribe(testObserver);

        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertNoValues();
        assertTrue(atomicCounter.get() == 1);
    }

    @Test
    public void givenSubscriberAndError_whenRetryWhenForMultipleTimesOnError_thenResumed() {
        TestObserver<String> testObserver = new TestObserver<>();
        long before = System.currentTimeMillis();

        Observable
          .<String>error(UNKNOWN_ERROR)
          .retryWhen(throwableObservable -> throwableObservable
            .zipWith(Observable.range(1, 3), (throwable, integer) -> integer)
            .flatMap(integer -> {
                System.out.println("retried " + integer + " times");
                return Observable.timer(integer, TimeUnit.SECONDS);
            }))
          .blockingSubscribe(testObserver);

        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertNoValues();
        long secondsElapsed = (System.currentTimeMillis() - before) / 1000;
        assertTrue(secondsElapsed == 6);
    }

}
