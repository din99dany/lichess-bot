package test;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainTest {

    public static void main(String[] args) throws InterruptedException {

        ApiControl apiControl = new ApiControl();

        apiControl.streamEvents();

        Thread.sleep(10000);

    }

}