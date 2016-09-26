package com.karhades.tag_it.utils.stepper.interfaces;

import com.karhades.tag_it.utils.stepper.AbstractStep;

import java.util.List;

/**
 * Created by Francesco Cannizzaro on 08/05/2016.
 */
public interface Pageable {

    void add(AbstractStep fragment);

    void set(List<AbstractStep> fragments);

}
