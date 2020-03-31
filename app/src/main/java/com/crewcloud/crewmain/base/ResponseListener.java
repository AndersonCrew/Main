package com.crewcloud.crewmain.base;


import rx.Observer;

/**
 * Created by mb on 3/18/16
 */
public abstract class ResponseListener<T> implements Observer<T> {
    private boolean fromCache;

    @Override
    public final void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {

        onError("Error,Please try again.");
    }

    @Override
    public void onNext(T s) {
        onSuccess(s);
    }

    public abstract void onSuccess(T result);

    public abstract void onError(String messageResponse);


}
