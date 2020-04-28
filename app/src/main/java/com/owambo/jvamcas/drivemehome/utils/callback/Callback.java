package com.owambo.jvamcas.drivemehome.utils.callback;


import com.owambo.jvamcas.drivemehome.utils.Results;

public interface Callback<T> {

    /***
     * Called when operation complete or if interrupted by an exception
     * @param mResult the results of the operation {@link Results.Success} or {@link Results.Error}
     * @param object the object written or returned by read operations
     */
    void onTaskComplete(T object, Results mResult);
}
