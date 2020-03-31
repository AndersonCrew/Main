package com.crewcloud.crewmain.base;

/**
 * Created by tunglam on 12/17/16.
 */

public class BasePresenter <V extends BaseView> implements IPresenter<V> {
    private V mMvpView;

    @Override
    public void attachView(V mvpView) {
        mMvpView = mvpView;
    }

    @Override
    public void detachView() {
        mMvpView = null;
    }

    public boolean isViewAttached() {
        return mMvpView != null;
    }

    public V getView() {
        return mMvpView;
    }
}
