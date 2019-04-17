/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xanite.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskletExceptionHandler implements ExceptionHandler {

    @Override
    public void handleException(RepeatContext rc, Throwable thrwbl) throws Throwable {
        log.error("!!!! ERROR Message : " + thrwbl.getMessage());
        log.error("!!!! ERROR Cause : " + thrwbl.getCause());
        throw thrwbl;
    }
}
