package com.karhades.tag_it.utils.stepper;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.karhades.tag_it.utils.stepper.interfaces.Nextable;
import com.karhades.tag_it.utils.stepper.style.BaseStyle;

/**
 * Created by Francesco Cannizzaro on 23/12/2015.
 */
public abstract class AbstractStep extends Fragment implements Nextable {

    protected BaseStyle mStepper;

    public AbstractStep stepper(BaseStyle mStepper) {
        this.mStepper = mStepper;
        return this;
    }

    protected Bundle getStepData() {
        return mStepper.getStepData();
    }

    protected Bundle getStepDataFor(int step) {
        return mStepper.getStepDataFor(step);
    }

    protected Bundle getLastStepData(){
        return mStepper.getStepDataFor(mStepper.steps()-1);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public String optional() {
        return isAdded() ? getString(com.github.fcannizzaro.materialstepper.R.string.ms_optional) : "";
    }

    public abstract String name();

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public void onStepVisible() {
    }

    @Override
    public void onNext() {

    }

    @Override
    public void onPrevious() {

    }

    @Override
    public boolean nextIf() {
        return true;
    }

    @Override
    public String error() {
        return "No error";
    }
}
