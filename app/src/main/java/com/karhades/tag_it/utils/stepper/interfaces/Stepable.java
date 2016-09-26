package com.karhades.tag_it.utils.stepper.interfaces;

/**
 * @author Francesco Cannizzaro (fcannizzaro).
 */
public interface Stepable {

    void onPrevious();

    void onNext();

    void onError();

    void onUpdate();

}
