package com.zycus.pims.common.exception;

public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }

    public InvalidStateTransitionException(String resource, String fromState, String toState) {
        super(String.format("Cannot transition %s from state '%s' to '%s'", resource, fromState, toState));
    }
}
